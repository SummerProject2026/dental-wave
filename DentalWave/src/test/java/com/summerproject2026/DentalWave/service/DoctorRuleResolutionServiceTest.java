package com.summerproject2026.DentalWave.service;

import com.summerproject2026.DentalWave.entity.DoctorWorkRule;
import com.summerproject2026.DentalWave.entity.ScheduleRotationGroup;
import com.summerproject2026.DentalWave.repository.DoctorWorkRuleRepository;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DoctorRuleResolutionServiceTest {
    private final DoctorRuleResolutionService service =
            new DoctorRuleResolutionService(mock(DoctorWorkRuleRepository.class));

    @Test
    void rotationContinuesAcrossMonthAndYearBoundaries() {
        ScheduleRotationGroup group = new ScheduleRotationGroup();
        group.setNumberOfWeeks(2);
        group.setAnchorDate(LocalDate.of(2026, 12, 21));
        group.setActive(true);
        DoctorWorkRule rule = new DoctorWorkRule();
        rule.setDayOfWeek(DayOfWeek.THURSDAY);
        rule.setRotationGroup(group);

        assertEquals(1, service.rotationPosition(rule, LocalDate.of(2026, 12, 24)));
        assertEquals(2, service.rotationPosition(rule, LocalDate.of(2026, 12, 31)));
        assertEquals(1, service.rotationPosition(rule, LocalDate.of(2027, 1, 7)));
    }

    @Test
    void datesBeforeAnchorUseConsistentFloorMod() {
        ScheduleRotationGroup group = new ScheduleRotationGroup();
        group.setNumberOfWeeks(2);
        group.setAnchorDate(LocalDate.of(2026, 8, 10));
        DoctorWorkRule rule = new DoctorWorkRule();
        rule.setRotationGroup(group);
        assertEquals(2, service.rotationPosition(rule, LocalDate.of(2026, 8, 6)));
    }
}
