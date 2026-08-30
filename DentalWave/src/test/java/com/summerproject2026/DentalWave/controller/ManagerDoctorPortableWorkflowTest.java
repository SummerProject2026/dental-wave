package com.summerproject2026.DentalWave.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.summerproject2026.DentalWave.repository.DoctorWorkRuleRepository;
import com.summerproject2026.DentalWave.repository.SchedulingResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.open-in-view=false",
        "spring.datasource.url=jdbc:h2:mem:doctor-portable-workflow;DB_CLOSE_DELAY=-1;NON_KEYWORDS=MONTH",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ManagerDoctorPortableWorkflowTest {

    private static final String DOCTOR_PAYLOAD = """
            {
              "type": "DOCTOR",
              "firstName": "Dr Portable Test",
              "lastName": "",
              "displayName": "Dr Portable Test",
              "active": true,
              "defaultOffice": null,
              "offices": [],
              "color": "#65a9b8",
              "notes": "Portable workflow"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorWorkRuleRepository doctorWorkRules;

    @Autowired
    private SchedulingResourceRepository resources;

    @BeforeEach
    void clearDoctorData() {
        doctorWorkRules.deleteAll();
        resources.deleteAll();
    }

    @Test
    @WithMockUser(username = "manager", authorities = "ROLE_MANAGER")
    void managerCanCreateReloadEditAndRemoveDoctorWithPortableSessionSettings() throws Exception {
        String createResponse = mockMvc.perform(post("/api/manager/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOCTOR_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Dr Portable Test"))
                .andReturn().getResponse().getContentAsString();
        long doctorId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/doctor-work-rules/doctor/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(defaultWeekRules(doctorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].doctor.displayName").value("Dr Portable Test"))
                .andExpect(jsonPath("$[0].doctor.offices.length()").value(0));

        mockMvc.perform(get("/api/doctor-work-rules/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        String editedPayload = DOCTOR_PAYLOAD.replace("Dr Portable Test", "Dr Portable Edited");
        mockMvc.perform(put("/api/manager/resources/{doctorId}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editedPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Dr Portable Edited"));

        mockMvc.perform(get("/api/manager/resources").param("type", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Dr Portable Edited"));

        mockMvc.perform(delete("/api/manager/resources/{doctorId}", doctorId))
                .andExpect(status().isNoContent());

        assertFalse(resources.existsById(doctorId));
        assertTrue(doctorWorkRules.findByDoctorIdOrderByDayOfWeekAsc(doctorId).isEmpty());
    }

    @Test
    void unauthenticatedUserCannotCreateDoctor() throws Exception {
        mockMvc.perform(post("/api/manager/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOCTOR_PAYLOAD))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removedCredentialRecoveryEndpointsAreNotPublic() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "assistant", authorities = "ROLE_ASSISTANT")
    void wrongRoleCannotCreateDoctor() throws Exception {
        mockMvc.perform(post("/api/manager/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DOCTOR_PAYLOAD))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", authorities = "ROLE_MANAGER")
    void malformedDoctorRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/manager/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DOCTOR\",\"displayName\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void faviconIsPublicButManagerApiRemainsProtected() throws Exception {
        mockMvc.perform(get("/favicon.svg"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/resources").param("type", "DOCTOR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void packagedApplicationShellIsServedDirectlyForBrowserRoutes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<div id=\"root\"></div>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<script type=\"module\"")));

        mockMvc.perform(get("/manager/doctors"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<div id=\"root\"></div>")));
    }

    private String defaultWeekRules(long doctorId) throws Exception {
        JsonNode rules = objectMapper.readTree("""
                [
                  {"dayOfWeek":"MONDAY","active":true,"workStatus":"NOT_WORKING","recurrenceType":"EVERY_WEEK"},
                  {"dayOfWeek":"TUESDAY","active":true,"workStatus":"NOT_WORKING","recurrenceType":"EVERY_WEEK"},
                  {"dayOfWeek":"WEDNESDAY","active":true,"workStatus":"NOT_WORKING","recurrenceType":"EVERY_WEEK"},
                  {"dayOfWeek":"THURSDAY","active":true,"workStatus":"NOT_WORKING","recurrenceType":"EVERY_WEEK"}
                ]
                """);
        rules.forEach(rule -> ((com.fasterxml.jackson.databind.node.ObjectNode) rule)
                .putObject("doctor").put("id", doctorId));
        return objectMapper.writeValueAsString(rules);
    }
}
