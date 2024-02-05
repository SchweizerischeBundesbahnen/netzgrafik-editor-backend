package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.PROJECT_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_B;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectCreateUpdateDto;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

public class UpdateProjectIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(List.of(ProjectTestData.PROJECT_A));
    }

    @Test
    public void updateProject_expectProjectUpdated() throws Exception {
        val updateDto =
                ProjectCreateUpdateDto.builder()
                        .name("New Name")
                        .summary("New Summary")
                        .description("New Description")
                        .writeUsers(List.of(USER_A))
                        .readUsers(List.of(USER_B))
                        .build();

        mockMvc.perform(
                        put("/v1/projects/{projectId}", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        val expectedResponse =
                ProjectDto.builder()
                        .id(ProjectId.of(PROJECT_A.getId()))
                        .name(updateDto.getName())
                        .summary(updateDto.getSummary())
                        .description(updateDto.getDescription())
                        .createdBy(UserId.of(PROJECT_A.getCreatedBy()))
                        .createdAt(PROJECT_A.getCreatedAt())
                        .isWritable(true)
                        .isDeletable(false)
                        .writeUsers(updateDto.getWriteUsers())
                        .readUsers(updateDto.getReadUsers())
                        .variants(List.of())
                        .isArchived(PROJECT_A.getIsArchived())
                        .build();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse));
    }

    @Test
    public void archiveProject_expectProjectArchived() throws Exception {
        mockMvc.perform(
                        put("/v1/projects/{projectId}/archive", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        val expectedResponse =
                ProjectDto.builder()
                        .id(ProjectId.of(PROJECT_A.getId()))
                        .name(PROJECT_A.getName())
                        .summary(PROJECT_A.getSummary())
                        .description(PROJECT_A.getDescription())
                        .createdBy(UserId.of(PROJECT_A.getCreatedBy()))
                        .createdAt(PROJECT_A.getCreatedAt())
                        .isWritable(false)
                        .isDeletable(true)
                        .writeUsers(List.of())
                        .readUsers(List.of())
                        .variants(List.of())
                        .isArchived(true)
                        .build();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse));

        mockMvc.perform(
                        put("/v1/projects/{projectId}/unarchive", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                expectedResponse
                                        .withIsArchived(false)
                                        .withIsWritable(true)
                                        .withIsDeletable(false)));
    }
}
