package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.DATE_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_B;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_C;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public class CreateProjectIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(ProjectTestData.PROJECT_A, ProjectTestData.PROJECT_B));
        controllableNowProvider.setNow(DATE_A);
    }

    @Test
    public void createAndFetchProjects__expectCreatedProject() throws Exception {
        val projectDraft =
                ProjectCreateUpdateDto.builder()
                        .name("New fancy project")
                        .summary("Short summary")
                        .description("Detailed description")
                        .writeUsers(List.of())
                        .readUsers(List.of(USER_B))
                        .build();

        val postResult =
                mockMvc.perform(
                                post("/v1/projects")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(projectDraft))
                                        .with(user(USER_A, SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val location = postResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val locationId = Long.parseLong(postResult.getResponse().getContentAsString());

        val expectedResponse =
                ProjectDto.builder()
                        .id(ProjectId.of(locationId))
                        .name(projectDraft.getName())
                        .summary(projectDraft.getSummary())
                        .description(projectDraft.getDescription())
                        .createdBy(UserId.of(USER_A))
                        .createdAt(DATE_A)
                        .isWritable(true)
                        .isDeletable(false)
                        .writeUsers(List.of(USER_A))
                        .readUsers(List.of(USER_B))
                        .variants(List.of())
                        .isArchived(false)
                        .build();

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse));

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse));

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse.withIsWritable(false)));

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_C, SecurityConfig.USER_ROLE)))
                .andExpect(status().isNotFound());

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_C, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResponse));

        mockMvc.perform(get(location).contentType(MediaType.APPLICATION_JSON).with(user(USER_A)))
                .andExpect(status().isForbidden());
    }
}
