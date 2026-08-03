package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.dto.DoctorAssignmentDto;
import com.summerproject2026.DentalWave.entity.*;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.*;
import com.summerproject2026.DentalWave.service.DoctorRuleResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/doctor-work-rules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class DoctorWorkRuleController {
    private final DoctorWorkRuleRepository rules;
    private final SchedulingResourceRepository doctors;
    private final OfficeRepository offices;
    private final ScheduleRotationGroupRepository groups;
    private final DoctorRuleResolutionService resolver;

    @GetMapping public List<DoctorWorkRule> list() { return rules.findAll(); }
    @GetMapping("/doctor/{doctorId}")
    public List<DoctorWorkRule> byDoctor(@PathVariable Long doctorId) {
        return rules.findByDoctorIdOrderByDayOfWeekAsc(doctorId);
    }
    @PostMapping public DoctorWorkRule create(@RequestBody DoctorWorkRule rule) {
        rule.setId(null); resolveAndValidate(rule); return rules.save(rule);
    }
    @PutMapping("/doctor/{doctorId}")
    @Transactional
    public List<DoctorWorkRule> replaceDoctorRules(
            @PathVariable Long doctorId,
            @RequestBody List<DoctorWorkRule> replacements) {
        SchedulingResource selectedDoctor = doctor(doctorId);
        if (replacements == null) bad("Doctor schedule rules are required.");

        List<DoctorWorkRule> existing = rules.findByDoctorIdOrderByDayOfWeekAsc(doctorId);
        rules.deleteAllInBatch(existing);

        List<DoctorWorkRule> saved = new ArrayList<>();
        for (DoctorWorkRule replacement : replacements) {
            replacement.setId(null);
            replacement.setDoctor(selectedDoctor);
            resolveAndValidate(replacement);
            saved.add(rules.save(replacement));
        }
        return saved;
    }
    @PutMapping("/{id}") public DoctorWorkRule update(
            @PathVariable Long id, @RequestBody DoctorWorkRule rule) {
        if (!rules.existsById(id)) throw new ResourceNotFoundException("Doctor work rule not found.");
        rule.setId(id); resolveAndValidate(rule); return rules.save(rule);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        rules.delete(rules.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Doctor work rule not found.")));
    }
    @GetMapping("/doctor/{doctorId}/preview")
    public List<DoctorAssignmentDto> preview(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam(defaultValue = "28") int days) {
        SchedulingResource doctor = doctor(doctorId);
        List<DoctorAssignmentDto> preview = new ArrayList<>();
        for (int offset = 0; offset < Math.min(days, 84); offset++) {
            LocalDate date = startDate.plusDays(offset);
            if (date.getDayOfWeek().getValue() <= 4) preview.add(resolver.resolve(doctor, date));
        }
        return preview;
    }

    private void resolveAndValidate(DoctorWorkRule rule) {
        if (rule.getDoctor() == null || rule.getDoctor().getId() == null)
            bad("Doctor is required.");
        rule.setDoctor(doctor(rule.getDoctor().getId()));
        if (rule.getDayOfWeek() == null) bad("Weekday is required.");
        if (rule.getWorkStatus() == null) bad("Work status is required.");
        if (rule.getRecurrenceType() == null) bad("Recurrence type is required.");
        if (rule.getEffectiveStartDate() != null && rule.getEffectiveEndDate() != null
                && rule.getEffectiveEndDate().isBefore(rule.getEffectiveStartDate()))
            bad("Effective end date cannot be before the start date.");

        if (rule.getWorkStatus() == DoctorWorkRule.WorkStatus.WORKING) {
            if (rule.getOffice() == null || rule.getOffice().getId() == null)
                bad("A working rule must include an office.");
            rule.setOffice(offices.findById(rule.getOffice().getId()).orElseThrow(() ->
                    new ResourceNotFoundException("Office not found.")));
        } else {
            rule.setOffice(null);
        }

        if (rule.getRecurrenceType() == DoctorWorkRule.RecurrenceType.ROTATING) {
            if (rule.getRotationGroup() == null || rule.getRotationGroup().getId() == null)
                bad("A rotating rule must include a rotation group.");
            ScheduleRotationGroup group = groups.findById(rule.getRotationGroup().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rotation group not found."));
            if (rule.getRotationPosition() == null || rule.getRotationPosition() < 1
                    || rule.getRotationPosition() > group.getNumberOfWeeks())
                bad("Rotation position must be within the rotation group's week range.");
            rule.setRotationGroup(group);
        } else {
            rule.setRotationGroup(null);
            rule.setRotationPosition(null);
        }

        for (DoctorWorkRule existing : rules.findByDoctorIdOrderByDayOfWeekAsc(rule.getDoctor().getId())) {
            if (existing.getId().equals(rule.getId()) || !existing.isActive()
                    || existing.getDayOfWeek() != rule.getDayOfWeek()) continue;
            boolean samePosition = existing.getRecurrenceType() == rule.getRecurrenceType()
                    && (rule.getRecurrenceType() == DoctorWorkRule.RecurrenceType.EVERY_WEEK
                    || (existing.getRotationGroup().getId().equals(rule.getRotationGroup().getId())
                    && existing.getRotationPosition().equals(rule.getRotationPosition())));
            if (samePosition && datesOverlap(existing, rule))
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        rule.getDoctor().getDisplayName() + " has two active "
                                + friendlyDay(rule) + " assignments for the same pattern.");
        }
    }
    private SchedulingResource doctor(Long id) {
        SchedulingResource value = doctors.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Doctor not found."));
        if (value.getType() != SchedulingResource.Type.DOCTOR) bad("Selected resource is not a doctor.");
        return value;
    }
    private boolean datesOverlap(DoctorWorkRule a, DoctorWorkRule b) {
        LocalDate aStart = a.getEffectiveStartDate() == null ? LocalDate.MIN : a.getEffectiveStartDate();
        LocalDate aEnd = a.getEffectiveEndDate() == null ? LocalDate.MAX : a.getEffectiveEndDate();
        LocalDate bStart = b.getEffectiveStartDate() == null ? LocalDate.MIN : b.getEffectiveStartDate();
        LocalDate bEnd = b.getEffectiveEndDate() == null ? LocalDate.MAX : b.getEffectiveEndDate();
        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }
    private String friendlyDay(DoctorWorkRule rule) {
        return rule.getDayOfWeek().name().toLowerCase();
    }
    private void bad(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
