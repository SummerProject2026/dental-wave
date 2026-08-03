package com.summerproject2026.DentalWave.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;
@Getter @AllArgsConstructor
public class DoctorAssignmentDto {
    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private boolean working;
    private Long officeId;
    private String officeName;
    private Long matchedRuleId;
    private String recurrenceType;
    private Integer rotationPosition;
    private boolean fallback;
    private boolean manuallyOverridden;
}
