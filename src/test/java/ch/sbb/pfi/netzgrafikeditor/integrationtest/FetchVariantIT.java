package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.stream.Collectors;

public class FetchVariantIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        VariantTestData.PROJECT,
                        VariantTestData.PROJECTS_USERS_A,
                        VariantTestData.PROJECTS_USERS_B,
                        VariantTestData.VARIANT,
                        VariantTestData.RELEASE_VERSION_1,
                        VariantTestData.RELEASE_VERSION_2,
                        VariantTestData.SNAPSHOT_VERSION_3_1_A,
                        VariantTestData.SNAPSHOT_VERSION_3_2_A,
                        VariantTestData.SNAPSHOT_VERSION_3_1_B));
    }

    @Test
    public void fetchVariant__expectExistingVersionForUserA() throws Exception {
        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VariantTestData.VARIANT.getId()))
                        .projectId(ProjectId.of(VariantTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                                VariantTestData.RELEASE_VERSION_1,
                                                VariantTestData.RELEASE_VERSION_2,
                                                VariantTestData.SNAPSHOT_VERSION_3_1_A,
                                                VariantTestData.SNAPSHOT_VERSION_3_2_A)
                                        .stream()
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(true)
                        .isDeletable(false)
                        .isArchived(VariantTestData.VARIANT.getIsArchived())
                        .latestVersion(
                                VersionRecordToDtoMapper.map(
                                        VariantTestData.SNAPSHOT_VERSION_3_2_A))
                        .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        "/v1/variants/{variantId}", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VariantTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }

    @Test
    public void fetchVariant__expectExistingVersionForUserB() throws Exception {
        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VariantTestData.VARIANT.getId()))
                        .projectId(ProjectId.of(VariantTestData.PROJECT.getId()))
                        .versions(
                                List.of(
                                                VariantTestData.RELEASE_VERSION_1,
                                                VariantTestData.RELEASE_VERSION_2,
                                                VariantTestData.SNAPSHOT_VERSION_3_1_B)
                                        .stream()
                                        .map(VersionRecordToDtoMapper::map)
                                        .collect(Collectors.toList()))
                        .isWritable(false)
                        .isDeletable(false)
                        .isArchived(VariantTestData.VARIANT.getIsArchived())
                        .latestVersion(
                                VersionRecordToDtoMapper.map(
                                        VariantTestData.SNAPSHOT_VERSION_3_1_B))
                        .build();

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        "/v1/variants/{variantId}", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VariantTestData.USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));
    }
}
