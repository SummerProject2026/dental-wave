package com.summerproject2026.DentalWave.dto;

/**
 * Data Transfer Object for Office.
 * Used to transfer office data between layers
 * without exposing the full entity.
 */
public class OfficeDto {

    /** Unique identifier of the office. */
    private Long id;

    /** Name of the office location (e.g., "Downtown Branch"). */
    private String name;

    /** Physical street address of the office. */
    private String address;

    /** Contact phone number for the office. */
    private String phoneNumber;

    // -------------------------
    // Constructors
    // -------------------------

    /** Default no-args constructor required for serialization/deserialization. */
    public OfficeDto() {}

    /**
     * Full constructor for creating a populated OfficeDto.
     *
     * @param id          the unique ID of the office
     * @param name        the name of the office
     * @param address     the physical address of the office
     * @param phoneNumber the contact phone number of the office
     */
    public OfficeDto(Long id, String name, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}