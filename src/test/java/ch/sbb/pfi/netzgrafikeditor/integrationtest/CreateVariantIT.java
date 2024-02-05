package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData;

import lombok.val;

import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class CreateVariantIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        ProjectTestData.PROJECT_A,
                        ProjectTestData.PROJECT_A_USERS_A,
                        ProjectTestData.PROJECT_A_USERS_B));
        controllableNowProvider.setNow(ProjectTestData.DATE_A);
    }

    @Test
    public void createAndFetchVariants__expectCreatedVariant() throws Exception {
        val variantCreateDto =
                VariantCreateDto.builder().initialModel("{}").initialName("my variant").build();

        val projectId = ProjectId.of(ProjectTestData.PROJECT_A.getId());

        val postResult =
                mockMvc.perform(
                                post("/v1/projects/{projectId}/variants", projectId.getValue())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(variantCreateDto))
                                        .with(
                                                UserHelper.user(
                                                        ProjectTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val location = postResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val locationId = Long.parseLong(postResult.getResponse().getContentAsString());

        val latestVersionId =
                this.context.select(DSL.max(VERSIONS.ID)).from(VERSIONS).fetchOneInto(Long.class);

        val expectedVersion =
                VersionDto.builder()
                        .id(VersionId.of(latestVersionId))
                        .variantId(VariantId.of(locationId))
                        .releaseVersion(1)
                        .snapshotVersion(Optional.of(1))
                        .name(variantCreateDto.getInitialName())
                        .comment("")
                        .createdAt(ProjectTestData.DATE_A)
                        .createdBy(UserId.of(ProjectTestData.USER_A))
                        .build();

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(locationId))
                        .projectId(projectId)
                        .versions(List.of(expectedVersion))
                        .latestVersion(expectedVersion)
                        .isWritable(true)
                        .isDeletable(false)
                        .isArchived(false)
                        .build();

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        UserHelper.user(
                                                ProjectTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));

        mockMvc.perform(
                        get(location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(
                                        UserHelper.user(
                                                ProjectTestData.USER_C, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void createVariants__expectForbiddenWhenNotWritable() throws Exception {
        val variantCreateDto =
                VariantCreateDto.builder().initialModel("{}").initialName("variant name").build();

        val projectId = ProjectId.of(ProjectTestData.PROJECT_A.getId());

        mockMvc.perform(
                        post("/v1/projects/{projectId}/variants", projectId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(variantCreateDto))
                                .with(
                                        UserHelper.user(
                                                ProjectTestData.USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
