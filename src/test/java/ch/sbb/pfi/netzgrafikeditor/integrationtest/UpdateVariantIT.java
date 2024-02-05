package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.ProjectTestData.USER_A;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class UpdateVariantIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        VariantTestData.PROJECT,
                        VariantTestData.VARIANT,
                        VariantTestData.RELEASE_VERSION_1));
    }

    @Test
    public void updateVariant__expectVariantUpdated() throws Exception {
        mockMvc.perform(
                        put("/v1/variants/{variantId}/archive", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        val expectedVersion = this.mapVersion(VariantTestData.RELEASE_VERSION_1);
        val expectedVariant =
                VariantDto.builder()
                        .id(VariantId.of(VariantTestData.VARIANT.getId()))
                        .projectId(ProjectId.of(VariantTestData.VARIANT.getProjectId()))
                        .versions(List.of(expectedVersion))
                        .latestVersion(expectedVersion)
                        .isArchived(true)
                        .isWritable(false)
                        .isDeletable(true)
                        .build();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(jsonResponse(expectedVariant));

        mockMvc.perform(
                        put("/v1/variants/{variantId}/unarchive", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isNoContent())
                .andReturn();

        mockMvc.perform(
                        get("/v1/variants/{variantId}", VariantTestData.VARIANT.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                expectedVariant
                                        .withIsArchived(false)
                                        .withIsWritable(true)
                                        .withIsDeletable(false)));
    }

    private VersionDto mapVersion(VersionsRecord record) {
        return VersionDto.builder()
                .id(VersionId.of(record.getId()))
                .variantId(VariantId.of(record.getVariantId()))
                .releaseVersion(record.getReleaseVersion())
                .snapshotVersion(Optional.ofNullable(record.getSnapshotVersion()))
                .name(record.getName())
                .comment(record.getComment())
                .createdBy(UserId.of(record.getCreatedBy()))
                .createdAt(record.getCreatedAt())
                .build();
    }
}
