package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.DeleteProjectTestData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

public class DeleteProjectIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        DeleteProjectTestData.PROJECT_A,
                        DeleteProjectTestData.PROJECT_B,
                        DeleteProjectTestData.PROJECT_A_USERS_A,
                        DeleteProjectTestData.PROJECT_B_USERS_A,
                        DeleteProjectTestData.VARIANT_A,
                        DeleteProjectTestData.VARIANT_B,
                        DeleteProjectTestData.VERSION_A,
                        DeleteProjectTestData.VERSION_B));
    }

    @Test
    public void deleteProject_expectNotFound() throws Exception {
        mockMvc.perform(
                        delete("/v1/projects/{projectId}", 1234567)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void deleteProject_expectForbiddenWhenNotWritable() throws Exception {
        mockMvc.perform(
                        delete("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(DeleteProjectTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void deleteProject_expectForbiddenWhenNotArchived() throws Exception {
        mockMvc.perform(
                        delete("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_B.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void deleteProject_expectDataRemoved() throws Exception {
        mockMvc.perform(
                        get("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(
                        delete("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_A.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNotFound())
                .andReturn();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", DeleteProjectTestData.PROJECT_B.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        user(
                                                DeleteProjectTestData.USER_A,
                                                SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andReturn();
    }
}
