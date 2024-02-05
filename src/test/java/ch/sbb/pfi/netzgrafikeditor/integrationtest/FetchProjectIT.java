package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantSummaryDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.VersionRecordToDtoMapper;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.FetchProjectTestData;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class FetchProjectIT extends IntegrationTest {

    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        FetchProjectTestData.PROJECT,
                        FetchProjectTestData.VARIANT_1,
                        FetchProjectTestData.VARIANT_2,
                        FetchProjectTestData.VARIANT_1_RELEASE_1,
                        FetchProjectTestData.VARIANT_1_RELEASE_2,
                        FetchProjectTestData.VARIANT_1_SNAPSHOT_1A,
                        FetchProjectTestData.VARIANT_1_SNAPSHOT_1B,
                        FetchProjectTestData.VARIANT_2_SNAPSHOT_1B));
    }

    @Test
    public void fetchVariants__userA() throws Exception {
        ProjectDto expectedResult =
                ProjectDto.builder()
                        .id(ProjectId.of(FetchProjectTestData.PROJECT.getId()))
                        .name(FetchProjectTestData.PROJECT.getName())
                        .description(FetchProjectTestData.PROJECT.getDescription())
                        .summary(FetchProjectTestData.PROJECT.getSummary())
                        .variants(
                                List.of(
                                        VariantSummaryDto.builder()
                                                .id(
                                                        VariantId.of(
                                                                FetchProjectTestData.VARIANT_1
                                                                        .getId()))
                                                .projectId(
                                                        ProjectId.of(
                                                                FetchProjectTestData.PROJECT
                                                                        .getId()))
                                                .isArchived(
                                                        FetchProjectTestData.VARIANT_1
                                                                .getIsArchived())
                                                .latestReleaseVersion(
                                                        Optional.of(
                                                                VersionRecordToDtoMapper.map(
                                                                        FetchProjectTestData
                                                                                .VARIANT_1_RELEASE_2)))
                                                .latestSnapshotVersion(
                                                        Optional.of(
                                                                VersionRecordToDtoMapper.map(
                                                                        FetchProjectTestData
                                                                                .VARIANT_1_SNAPSHOT_1A)))
                                                .build()))
                        .createdAt(FetchProjectTestData.PROJECT.getCreatedAt())
                        .createdBy(UserId.of(FetchProjectTestData.PROJECT.getCreatedBy()))
                        .isWritable(true)
                        .isDeletable(false)
                        .writeUsers(List.of())
                        .readUsers(List.of())
                        .isArchived(FetchProjectTestData.PROJECT.getIsArchived())
                        .build();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", FetchProjectTestData.PROJECT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(ProjectTestData.USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResult));
    }

    @Test
    public void fetchVariants__userB() throws Exception {
        ProjectDto expectedResult =
                ProjectDto.builder()
                        .id(ProjectId.of(FetchProjectTestData.PROJECT.getId()))
                        .name(FetchProjectTestData.PROJECT.getName())
                        .description(FetchProjectTestData.PROJECT.getDescription())
                        .summary(FetchProjectTestData.PROJECT.getSummary())
                        .variants(
                                List.of(
                                        VariantSummaryDto.builder()
                                                .id(
                                                        VariantId.of(
                                                                FetchProjectTestData.VARIANT_1
                                                                        .getId()))
                                                .projectId(
                                                        ProjectId.of(
                                                                FetchProjectTestData.PROJECT
                                                                        .getId()))
                                                .isArchived(
                                                        FetchProjectTestData.VARIANT_1
                                                                .getIsArchived())
                                                .latestReleaseVersion(
                                                        Optional.of(
                                                                VersionRecordToDtoMapper.map(
                                                                        FetchProjectTestData
                                                                                .VARIANT_1_RELEASE_2)))
                                                .latestSnapshotVersion(
                                                        Optional.of(
                                                                VersionRecordToDtoMapper.map(
                                                                        FetchProjectTestData
                                                                                .VARIANT_1_SNAPSHOT_1B)))
                                                .build(),
                                        VariantSummaryDto.builder()
                                                .id(
                                                        VariantId.of(
                                                                FetchProjectTestData.VARIANT_2
                                                                        .getId()))
                                                .projectId(
                                                        ProjectId.of(
                                                                FetchProjectTestData.PROJECT
                                                                        .getId()))
                                                .isArchived(
                                                        FetchProjectTestData.VARIANT_2
                                                                .getIsArchived())
                                                .latestSnapshotVersion(
                                                        Optional.of(
                                                                VersionRecordToDtoMapper.map(
                                                                        FetchProjectTestData
                                                                                .VARIANT_2_SNAPSHOT_1B)))
                                                .build()))
                        .createdAt(FetchProjectTestData.PROJECT.getCreatedAt())
                        .createdBy(UserId.of(FetchProjectTestData.PROJECT.getCreatedBy()))
                        .isWritable(true)
                        .isDeletable(false)
                        .writeUsers(List.of())
                        .readUsers(List.of())
                        .isArchived(FetchProjectTestData.PROJECT.getIsArchived())
                        .build();

        mockMvc.perform(
                        get("/v1/projects/{projectId}", FetchProjectTestData.PROJECT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(FetchProjectTestData.USER_B, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedResult));
    }
}
