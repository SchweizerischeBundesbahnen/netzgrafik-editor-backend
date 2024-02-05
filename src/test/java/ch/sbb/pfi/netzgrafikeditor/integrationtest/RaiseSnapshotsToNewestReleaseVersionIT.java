package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.DATE;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.PROJECT;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.PROJECTS_USERS_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.PROJECTS_USERS_B;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.RELEASE_VERSION_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.RELEASE_VERSION_2;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.RELEASE_VERSION_3;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.RELEASE_VERSION_4;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_1_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_1_2;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.SNAPSHOT_VERSION_2_1_USER_B;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.USER_B;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData.VARIANT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.VersionRecordToDtoMapper;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Collectors;

public class RaiseSnapshotsToNewestReleaseVersionIT extends IntegrationTest {

    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(PROJECT, PROJECTS_USERS_A, PROJECTS_USERS_B, VARIANT));
        controllableNowProvider.setNow(DATE);
    }

    @Test
    public void
            riseSnapshotReleaseVersionsToNewestWithSingleConflictingRelease__expectEditedSnapshots()
                    throws Exception {
        // given
        testDataService.insertTestData(
                List.of(RELEASE_VERSION_1, SNAPSHOT_VERSION_2_1_USER_B, RELEASE_VERSION_2));

        // when
        mockMvc.perform(
                        put("/v1/variants/{variantId}/snapshots/asNewest", VARIANT.getId())
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk());

        // then
        val expectedRestoreSnapshot =
                RELEASE_VERSION_1
                        .copy()
                        .setId(0l) // ignored
                        .setReleaseVersion(3)
                        .setComment("Wiederhergestellt aus Version 1")
                        .setSnapshotVersion(1)
                        .setCreatedAt(controllableNowProvider.now())
                        .setCreatedBy(USER_B);

        val expectedLatestVersion =
                SNAPSHOT_VERSION_2_1_USER_B
                        .copy()
                        .setId(0l) // ignored
                        .setReleaseVersion(3)
                        .setSnapshotVersion(2)
                        .setCreatedAt(controllableNowProvider.now());

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VARIANT.getId()))
                        .projectId(ProjectId.of(VariantTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                                RELEASE_VERSION_1,
                                                RELEASE_VERSION_2,
                                                expectedRestoreSnapshot,
                                                expectedLatestVersion)
                                        .stream()
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(true)
                        .isDeletable(false)
                        .latestVersion(VersionRecordToDtoMapper.map(expectedLatestVersion))
                        .isArchived(VARIANT.getIsArchived())
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                jsonMatcher ->
                                        jsonMatcher
                                                .ignorePath("/versions/2/id")
                                                .ignorePath("/versions/3/id")
                                                .ignorePath("/latestVersion/id")
                                                .assertEquals(expectedVariant)));
    }

    @Test
    public void
            riseSnapshotReleaseVersionsToNewestWithMultipleConflictingReleases__expectEditedSnapshots()
                    throws Exception {
        // given
        testDataService.insertTestData(
                List.of(
                        RELEASE_VERSION_1,
                        SNAPSHOT_VERSION_2_1_USER_B,
                        RELEASE_VERSION_2,
                        RELEASE_VERSION_3,
                        RELEASE_VERSION_4));

        // when
        mockMvc.perform(
                        put("/v1/variants/{variantId}/snapshots/asNewest", VARIANT.getId())
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk());

        // then
        val expectedRestoreSnapshot =
                RELEASE_VERSION_1
                        .copy()
                        .setId(0l) // ignored
                        .setReleaseVersion(5)
                        .setComment("Wiederhergestellt aus Version 1")
                        .setSnapshotVersion(1)
                        .setCreatedAt(controllableNowProvider.now())
                        .setCreatedBy(USER_B);

        val expectedLatestVersion =
                SNAPSHOT_VERSION_2_1_USER_B
                        .copy()
                        .setId(0l) // ignored
                        .setReleaseVersion(5)
                        .setSnapshotVersion(2)
                        .setCreatedAt(controllableNowProvider.now());

        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VARIANT.getId()))
                        .projectId(ProjectId.of(VariantTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                                RELEASE_VERSION_1,
                                                RELEASE_VERSION_2,
                                                RELEASE_VERSION_3,
                                                RELEASE_VERSION_4,
                                                expectedRestoreSnapshot,
                                                expectedLatestVersion)
                                        .stream()
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(true)
                        .isDeletable(false)
                        .latestVersion(VersionRecordToDtoMapper.map(expectedLatestVersion))
                        .isArchived(VariantTestData.VARIANT.getIsArchived())
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                jsonMatcher ->
                                        jsonMatcher
                                                .ignorePath("/versions/4/id")
                                                .ignorePath("/versions/5/id")
                                                .ignorePath("/latestVersion/id")
                                                .assertEquals(expectedVariant)));
    }

    @Test
    public void riseSnapshotReleaseVersionsToNewestWhenNoConflictingRelease__expectForbidden()
            throws Exception {
        // given
        testDataService.insertTestData(List.of(RELEASE_VERSION_1, SNAPSHOT_VERSION_2_1_USER_B));

        // when & then
        mockMvc.perform(
                        put("/v1/variants/{variantId}/snapshots/asNewest", VARIANT.getId())
                                .with(user(USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void riseSnapshotReleaseVersionsToNewestWhenRelease__expectForbidden() throws Exception {
        // given
        testDataService.insertTestData(List.of(SNAPSHOT_VERSION_1_1, SNAPSHOT_VERSION_1_2));

        // when & then
        mockMvc.perform(
                        put("/v1/variants/{variantId}/snapshots/asNewest", VARIANT.getId())
                                .with(user(USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden());
    }
}
