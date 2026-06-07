package com.summerproject2026.DentalWave.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for TimeOffRequest.
 * Used to transfer time-off request data between layers
 * without exposing the full entity or its relationships.
 */
public class TimeOffRequestDto {

    /** Unique identifier of the time-off request. */
    private Long id;

    /** ID of the employee who submitted the request. */
    private Long employeeId;

    /** Full name of the employee who submitted the request. */
    private String employeeName;

    /** Start date of the requested time off. */
    private LocalDate startDate;

    /** End date of the requested time off. */
    private LocalDate endDate;

    /**
     * Start time of the time-off period.
     * Used for partial-day requests.
     */
    private LocalTime startTime;

    /**
     * End time of the time-off period.
     * Used for partial-day requests.
     */
    private LocalTime endTime;

    /** Reason provided by the employee for the time-off request. */
    private String reason;

    /**
     * Current status of the request.
     * Can be PENDING, APPROVED, or DENIED.
     */
    private RequestStatus status;

    /** ID of the user (manager/admin) who reviewed the request. */
    private Long reviewedById;

    /** Full name of the user who reviewed the request. */
    private String reviewedByName;

    /** Timestamp of when the request was reviewed. */
    private LocalDateTime reviewedAt;

    /** Optional comment left by the reviewer upon approval or denial. */
    private String reviewComment;

    /**
     * Indicates whether the request is an emergency.
     * Emergency requests may bypass standard approval workflows.
     */
    private Boolean emergency;

    /** Timestamp of when the request was submitted by the employee. */
    private LocalDateTime submittedAt;

    // -------------------------
    // Constructors
    // -------------------------

    /** Default no-args constructor required for serialization/deserialization. */
    public TimeOffRequestDto() {}

    /**
     * Full constructor for creating a populated TimeOffRequestDto.
     *
     * @param id             the unique ID of the request
     * @param employeeId     the ID of the requesting employee
     * @param employeeName   the full name of the requesting employee
     * @param startDate      the start date of the time off
     * @param endDate        the end date of the time off
     * @param startTime      the start time (for partial-day requests)
     * @param endTime        the end time (for partial-day requests)
     * @param reason         the reason for the request
     * @param status         the current status of the request
     * @param reviewedById   the ID of the reviewer
     * @param reviewedByName the full name of the reviewer
     * @param reviewedAt     the timestamp of the review
     * @param reviewComment  the comment from the reviewer
     * @param emergency      whether this is an emergency request
     * @param submittedAt    the timestamp when the request was submitted
     */
    public TimeOffRequestDto(Long id,
                             Long employeeId,
                             String employeeName,
                             LocalDate startDate,
                             LocalDate endDate,
                             LocalTime startTime,
                             LocalTime endTime,
                             String reason,
                             RequestStatus status,
                             Long reviewedById,
                             String reviewedByName,
                             LocalDateTime reviewedAt,
                             String reviewComment,
                             Boolean emergency,
                             LocalDateTime submittedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = status;
        this.reviewedById = reviewedById;
        this.reviewedByName = reviewedByName;
        this.reviewedAt = reviewedAt;
        this.reviewComment = reviewComment;
        this.emergency = emergency;
        this.submittedAt = submittedAt;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public Long getReviewedById() { return reviewedById; }
    public void setReviewedById(Long reviewedById) { this.reviewedById = reviewedById; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public Boolean getEmergency() { return emergency; }
    public void setEmergency(Boolean emergency) { this.emergency = emergency; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}