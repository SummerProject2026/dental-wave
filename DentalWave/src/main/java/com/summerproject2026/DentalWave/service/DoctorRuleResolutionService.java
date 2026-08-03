package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.dto.DoctorAssignmentDto;
import com.summerproject2026.DentalWave.entity.DoctorWorkRule;
import com.summerproject2026.DentalWave.entity.SchedulingResource;
import com.summerproject2026.DentalWave.repository.DoctorWorkRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorRuleResolutionService {
    private final DoctorWorkRuleRepository rules;

    public DoctorAssignmentDto resolve(SchedulingResource doctor, LocalDate date) {
        List<DoctorWorkRule> applicable = rules
                .findByDoctorIdAndDayOfWeekAndActiveTrue(doctor.getId(), date.getDayOfWeek())
                .stream().filter(rule -> appliesOn(rule, date)).toList();
        if (applicable.size() > 1) {
            throw new IllegalStateException(doctor.getDisplayName()
                    + " has multiple active assignments for " + date + ".");
        }
        if (applicable.isEmpty()) {
            boolean doctorHasConfiguredPattern =
                    !rules.findByDoctorIdOrderByDayOfWeekAsc(doctor.getId()).isEmpty();
            return new DoctorAssignmentDto(doctor.getId(), doctor.getDisplayName(), date,
                    false, null, null, null, null, null,
                    !doctorHasConfiguredPattern, false);
        }
        DoctorWorkRule rule = applicable.get(0);
        boolean working = rule.getWorkStatus() == DoctorWorkRule.WorkStatus.WORKING;
        return new DoctorAssignmentDto(doctor.getId(), doctor.getDisplayName(), date,
                working,
                working ? rule.getOffice().getId() : null,
                working ? rule.getOffice().getName() : null,
                rule.getId(), rule.getRecurrenceType().name(),
                rule.getRotationPosition(), false, false);
    }

    public int rotationPosition(DoctorWorkRule rule, LocalDate date) {
        LocalDate anchor = rule.getRotationGroup().getAnchorDate();
        long weeks = Math.floorDiv(ChronoUnit.DAYS.between(anchor, date), 7);
        return Math.floorMod((int) weeks, rule.getRotationGroup().getNumberOfWeeks()) + 1;
    }

    private boolean appliesOn(DoctorWorkRule rule, LocalDate date) {
        if (rule.getEffectiveStartDate() != null && date.isBefore(rule.getEffectiveStartDate())) return false;
        if (rule.getEffectiveEndDate() != null && date.isAfter(rule.getEffectiveEndDate())) return false;
        if (rule.getRecurrenceType() == DoctorWorkRule.RecurrenceType.EVERY_WEEK) return true;
        return rule.getRotationGroup() != null
                && rule.getRotationGroup().isActive()
                && rule.getRotationPosition() != null
                && rotationPosition(rule, date) == rule.getRotationPosition();
    }
}
