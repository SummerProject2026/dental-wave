package com.summerproject2026.DentalWave.controller;

import com.summerproject2026.DentalWave.entity.ReusableTeam;
import com.summerproject2026.DentalWave.entity.SchedulingResource;
import com.summerproject2026.DentalWave.exception.ResourceNotFoundException;
import com.summerproject2026.DentalWave.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/manager/teams")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ReusableTeamController {
    private final ReusableTeamRepository teams;
    private final SchedulingResourceRepository resources;
    private final OfficeRepository offices;

    @GetMapping
    public List<ReusableTeam> list() {
        return teams.findAll();
    }

    @PostMapping
    @Transactional
    public ReusableTeam create(@RequestBody ReusableTeam team) {
        team.setId(null);
        resolve(team);
        rejectDuplicateName(team.getName(), null);
        return save(team);
    }

    @PutMapping("/{id}")
    @Transactional
    public ReusableTeam update(@PathVariable Long id, @RequestBody ReusableTeam team) {
        if (!teams.existsById(id)) {
            throw new ResourceNotFoundException("Team not found.");
        }
        team.setId(id);
        resolve(team);
        rejectDuplicateName(team.getName(), id);
        return save(team);
    }

    @PostMapping("/{id}/duplicate")
    @Transactional
    public ReusableTeam duplicate(@PathVariable Long id) {
        ReusableTeam source = teams.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        ReusableTeam copy = new ReusableTeam();
        copy.setName(uniqueName(source.getName() + " Copy"));
        copy.setDoctor(source.getDoctor());
        copy.setAssistants(new ArrayList<>(source.getAssistants()));
        copy.setDefaultOffice(source.getDefaultOffice());
        copy.setColor(source.getColor());
        copy.setNotes(source.getNotes());
        copy.setActive(source.isActive());
        return save(copy);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void delete(@PathVariable Long id) {
        ReusableTeam team = teams.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found."));
        teams.delete(team);
    }

    private void resolve(ReusableTeam team) {
        if (team.getName() == null || team.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Team name is required.");
        }
        team.setName(team.getName().trim());
        if (team.getDoctor() == null || team.getDoctor().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is required.");
        }
        team.setDoctor(resource(team.getDoctor().getId(), SchedulingResource.Type.DOCTOR));
        Set<Long> ids = new HashSet<>();
        team.setAssistants(team.getAssistants() == null ? List.of() : team.getAssistants().stream()
                .map(assistant -> {
                    if (assistant == null || assistant.getId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assistant id is required.");
                    }
                    return resource(assistant.getId(), SchedulingResource.Type.ASSISTANT);
                })
                .filter(assistant -> ids.add(assistant.getId()))
                .toList());
        if (team.getDefaultOffice() != null) {
            Long officeId = team.getDefaultOffice().getId();
            if (officeId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default office id is required.");
            }
            team.setDefaultOffice(offices.findById(officeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Office not found.")));
        }
    }

    private SchedulingResource resource(Long id, SchedulingResource.Type type) {
        SchedulingResource value = resources.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduling resource not found."));
        if (!value.isActive() || value.getType() != type) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected " + type.name().toLowerCase() + " is inactive or invalid.");
        }
        return value;
    }

    private void rejectDuplicateName(String name, Long currentId) {
        teams.findByNameIgnoreCase(name)
                .filter(existing -> !Objects.equals(existing.getId(), currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A team with this name already exists.");
                });
    }

    private ReusableTeam save(ReusableTeam team) {
        try {
            return teams.save(team);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A team with this name already exists.");
        }
    }

    private String uniqueName(String base) {
        String name = base;
        int suffix = 2;
        while (teams.existsByNameIgnoreCase(name)) {
            name = base + " " + suffix++;
        }
        return name;
    }
}
