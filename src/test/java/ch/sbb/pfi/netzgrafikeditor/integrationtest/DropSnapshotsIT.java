package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.PROJECT;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.PROJECTS_USERS_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.RELEASE_VERSION_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_1_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_1_2;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_2_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_2_2;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.VARIANT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.VersionRecordToDtoMapper;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropSnapshotsIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(List.of(PROJECT, PROJECTS_USERS_A, VARIANT));
        controllableNowProvider.setNow(VersionTestData.DATE);
    }

    @Test
    public void dropSnapshotsFromReleasedVersion__expectLatestReleasedAsLatestVersion()
            throws Exception {
        testDataService.insertTestData(
                List.of(RELEASE_VERSION_1, SNAPSHOT_VERSION_2_1, SNAPSHOT_VERSION_2_2));

        mockMvc.perform(
                        delete("/v1/variants/{variantId}/snapshots", VARIANT.getId())
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VariantTestData.VARIANT.getId()))
                        .projectId(ProjectId.of(PROJECT.getId()))
                        .versions(
                                Stream.of(RELEASE_VERSION_1)
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(true)
                        .isDeletable(false)
                        .isArchived(VariantTestData.VARIANT.getIsArchived())
                        .latestVersion(VersionRecordToDtoMapper.map(RELEASE_VERSION_1))
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VARIANT.getId())
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }

    @Test
    public void dropSnapshotsFromNonReleasedVersion__expectInitialSnapshotAsLatestVersion()
            throws Exception {
        testDataService.insertTestData(List.of(SNAPSHOT_VERSION_1_1, SNAPSHOT_VERSION_1_2));

        mockMvc.perform(
                        delete("/v1/variants/{variantId}/snapshots", VARIANT.getId())
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VariantTestData.VARIANT.getId()))
                        .projectId(ProjectId.of(PROJECT.getId()))
                        .versions(
                                Stream.of(SNAPSHOT_VERSION_1_1)
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(true)
                        .isDeletable(false)
                        .isArchived(VariantTestData.VARIANT.getIsArchived())
                        .latestVersion(VersionRecordToDtoMapper.map(SNAPSHOT_VERSION_1_1))
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VARIANT.getId())
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }
}
