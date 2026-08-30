package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.entity.SchedulingResource;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.OfficeRepository;
import com.summerproject2026.DentalWave.repository.DoctorWorkRuleRepository;
import com.summerproject2026.DentalWave.repository.ReusableTeamRepository;
import com.summerproject2026.DentalWave.repository.SchedulingResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/manager/resources")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ManagerResourceController {
    private final SchedulingResourceRepository resources;
    private final OfficeRepository offices;
    private final ReusableTeamRepository teams;
    private final DoctorWorkRuleRepository doctorWorkRules;

    @GetMapping
    public List<SchedulingResource> list(@RequestParam SchedulingResource.Type type) {
        return resources.findByTypeOrderByLastNameAsc(type);
    }

    @PostMapping
    @Transactional
    public SchedulingResource create(@RequestBody SchedulingResource value) {
        validate(value);
        normalizeName(value);
        value.setId(null);
        resolveOffices(value);
        return resources.save(value);
    }

    @PutMapping("/{id}")
    @Transactional
    public SchedulingResource update(@PathVariable Long id, @RequestBody SchedulingResource value) {
        if (!resources.existsById(id)) {
            throw new ResourceNotFoundException("Scheduling resource not found.");
        }
        validate(value);
        normalizeName(value);
        value.setId(id);
        resolveOffices(value);
        return resources.save(value);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        SchedulingResource value = resources.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduling resource not found."));
        if (value.getType() == SchedulingResource.Type.DOCTOR && teams.existsByDoctorId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This doctor is used by a reusable team. Remove the doctor from that team first.");
        }
        if (value.getType() == SchedulingResource.Type.DOCTOR) {
            doctorWorkRules.deleteByDoctorId(id);
        }
        resources.delete(value);
    }

    private void validate(SchedulingResource value) {
        if (value.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resource type is required.");
        }
        if (blank(value.getDisplayName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required.");
        }
    }

    private void normalizeName(SchedulingResource value) {
        value.setDisplayName(value.getDisplayName().trim());
        value.setFirstName(value.getDisplayName());
        value.setLastName("");
    }

    private void resolveOffices(SchedulingResource value) {
        if (value.getDefaultOffice() != null) {
            Long officeId = value.getDefaultOffice().getId();
            if (officeId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default office id is required.");
            }
            value.setDefaultOffice(offices.findById(officeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Office not found.")));
        }
        value.setOffices(value.getOffices() == null ? List.of() : value.getOffices().stream()
                .map(office -> {
                    if (office == null || office.getId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Office id is required.");
                    }
                    return offices.findById(office.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Office not found."));
                })
                .distinct()
                .toList());
    }

    private boolean blank(String text) {
        return text == null || text.isBlank();
    }
}
