package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.entity.ScheduleRotationGroup;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.DoctorWorkRuleRepository;
import com.summerproject2026.DentalWave.repository.ScheduleRotationGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/schedule-rotation-groups")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ScheduleRotationGroupController {
    private final ScheduleRotationGroupRepository groups;
    private final DoctorWorkRuleRepository rules;

    @GetMapping public List<ScheduleRotationGroup> list() { return groups.findAll(); }
    @GetMapping("/{id}") public ScheduleRotationGroup get(@PathVariable Long id) {
        return find(id);
    }
    @PostMapping public ScheduleRotationGroup create(@RequestBody ScheduleRotationGroup group) {
        group.setId(null); validate(group); return groups.save(group);
    }
    @PutMapping("/{id}") public ScheduleRotationGroup update(
            @PathVariable Long id, @RequestBody ScheduleRotationGroup group) {
        find(id); group.setId(id); validate(group); return groups.save(group);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ScheduleRotationGroup group = find(id);
        if (rules.existsByRotationGroupId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remove this rotation's doctor rules before deleting the rotation.");
        }
        groups.delete(group);
    }
    private ScheduleRotationGroup find(Long id) {
        return groups.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Rotation group not found."));
    }
    private void validate(ScheduleRotationGroup group) {
        if (group.getName() == null || group.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rotation name is required.");
        if (group.getNumberOfWeeks() < 2 || group.getNumberOfWeeks() > 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rotation length must be between 2 and 12 weeks.");
        if (group.getAnchorDate() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rotation anchor date is required.");
        group.setName(group.getName().trim());
    }
}
