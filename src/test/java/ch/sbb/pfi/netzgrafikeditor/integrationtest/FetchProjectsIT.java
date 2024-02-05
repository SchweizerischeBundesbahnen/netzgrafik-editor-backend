package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_B;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectSummaryDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FetchProjectsIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        ProjectTestData.PROJECT_A,
                        ProjectTestData.PROJECT_B,
                        ProjectTestData.PROJECT_A_USERS_A));
    }

    private ProjectSummaryDto mapProjectSummary(ProjectsRecord project) {
        return ProjectSummaryDto.builder()
                .id(ProjectId.of(project.getId()))
                .name(project.getName())
                .isArchived(project.getIsArchived())
                .build();
    }

    @Test
    void fetchAllProjects__expectExistingProjectsAsAdmin() throws Exception {
        val expectedProjects =
                Stream.of(ProjectTestData.PROJECT_A, ProjectTestData.PROJECT_B)
                        .map(this::mapProjectSummary)
                        .collect(Collectors.toList());

        mockMvc.perform(
                        get("/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedProjects));
    }

    @Test
    void fetchAllProjects__expectExistingProjectsAsUserA() throws Exception {
        val expectedProjects = List.of(this.mapProjectSummary(ProjectTestData.PROJECT_A));
        mockMvc.perform(
                        get("/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedProjects));
    }

    @Test
    void fetchAllProjects__expectExistingProjectsAsUserB() throws Exception {
        val expectedProjects = List.of();
        mockMvc.perform(
                        get("/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedProjects));
    }

    @Test
    void fetchAllProjects__expectForbiddenWithoutRole() throws Exception {
        mockMvc.perform(
                        get("/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fetchAllProjects__expectUnauthorizedWithoutUser() throws Exception {
        mockMvc.perform(get("/v1/projects").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
