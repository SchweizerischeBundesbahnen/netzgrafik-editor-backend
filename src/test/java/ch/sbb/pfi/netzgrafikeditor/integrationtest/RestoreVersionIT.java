package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.VersionRecordToDtoMapper;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

public class RestoreVersionIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        VersionTestData.PROJECT,
                        VersionTestData.PROJECTS_USERS_A,
                        VersionTestData.VARIANT,
                        VersionTestData.RELEASE_VERSION_1,
                        VersionTestData.SNAPSHOT_VERSION_2_1,
                        VersionTestData.SNAPSHOT_VERSION_2_2));
        controllableNowProvider.setNow(VersionTestData.DATE);
    }

    @Test
    public void restoreRelease_expectRestoredSnapshot() throws Exception {
        // given

        // when
        val restoreVersionResult =
                mockMvc.perform(
                                MockMvcRequestBuilders.post(
                                                "/v1/versions/{versionId}/restore",
                                                VersionTestData.RELEASE_VERSION_1.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        // then
        val restoringSnapshotId =
                VersionId.of(
                        Long.parseLong(restoreVersionResult.getResponse().getContentAsString()));

        val expectedVersion =
                VersionDto.builder()
                        .id(restoringSnapshotId)
                        .variantId(VariantId.of(VersionTestData.RELEASE_VERSION_1.getVariantId()))
                        .releaseVersion(VersionTestData.SNAPSHOT_VERSION_2_2.getReleaseVersion())
                        .snapshotVersion(
                                Optional.of(
                                        VersionTestData.SNAPSHOT_VERSION_2_2.getSnapshotVersion()
                                                + 1))
                        .name(VersionTestData.RELEASE_VERSION_1.getName())
                        .comment(
                                "Wiederhergestellt aus Version "
                                        + VersionTestData.RELEASE_VERSION_1.getReleaseVersion())
                        .createdAt(ProjectTestData.DATE_A)
                        .createdBy(UserId.of(ProjectTestData.USER_A))
                        .build();

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VersionTestData.RELEASE_VERSION_1.getVariantId()))
                        .projectId(ProjectId.of(VersionTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.RELEASE_VERSION_1),
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.SNAPSHOT_VERSION_2_1),
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.SNAPSHOT_VERSION_2_2),
                                        expectedVersion))
                        .latestVersion(expectedVersion)
                        .isWritable(true)
                        .isArchived(false)
                        .isDeletable(false)
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", expectedVariant.getId().getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }

    @Test
    public void restoreSnapshot_expectRestoredSnapshot() throws Exception {
        // given

        // when
        val restoreVersionResult =
                mockMvc.perform(
                                MockMvcRequestBuilders.post(
                                                "/v1/versions/{versionId}/restore",
                                                VersionTestData.SNAPSHOT_VERSION_2_1.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        // then
        val restoringSnapshotId =
                VersionId.of(
                        Long.parseLong(restoreVersionResult.getResponse().getContentAsString()));

        val expectedVersion =
                VersionDto.builder()
                        .id(restoringSnapshotId)
                        .variantId(
                                VariantId.of(VersionTestData.SNAPSHOT_VERSION_2_1.getVariantId()))
                        .releaseVersion(VersionTestData.SNAPSHOT_VERSION_2_2.getReleaseVersion())
                        .snapshotVersion(
                                Optional.of(
                                        VersionTestData.SNAPSHOT_VERSION_2_2.getSnapshotVersion()
                                                + 1))
                        .name(VersionTestData.SNAPSHOT_VERSION_2_1.getName())
                        .comment(
                                "Wiederhergestellt aus Änderung "
                                        + VersionTestData.SNAPSHOT_VERSION_2_1.getSnapshotVersion())
                        .createdAt(ProjectTestData.DATE_A)
                        .createdBy(UserId.of(ProjectTestData.USER_A))
                        .build();

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VersionTestData.RELEASE_VERSION_1.getVariantId()))
                        .projectId(ProjectId.of(VersionTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.RELEASE_VERSION_1),
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.SNAPSHOT_VERSION_2_1),
                                        VersionRecordToDtoMapper.map(
                                                VersionTestData.SNAPSHOT_VERSION_2_2),
                                        expectedVersion))
                        .latestVersion(expectedVersion)
                        .isWritable(true)
                        .isArchived(false)
                        .isDeletable(false)
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", expectedVariant.getId().getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }
}
