package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class FetchVersionIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        VersionTestData.PROJECT,
                        VersionTestData.VARIANT,
                        VersionTestData.SNAPSHOT_VERSION_1_1));
        controllableNowProvider.setNow(VersionTestData.DATE);
    }

    @Test
    public void fetchVersion__expectVersion() throws Exception {
        mockMvc.perform(
                        get(
                                        "/v1/versions/{versionId}",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.ADMIN_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                VersionDto.builder()
                                        .id(
                                                VersionId.of(
                                                        VersionTestData.SNAPSHOT_VERSION_1_1
                                                                .getId()))
                                        .variantId(VariantId.of(VersionTestData.VARIANT.getId()))
                                        .releaseVersion(
                                                VersionTestData.SNAPSHOT_VERSION_1_1
                                                        .getReleaseVersion())
                                        .snapshotVersion(
                                                Optional.ofNullable(
                                                        VersionTestData.SNAPSHOT_VERSION_1_1
                                                                .getSnapshotVersion()))
                                        .name(VersionTestData.SNAPSHOT_VERSION_1_1.getName())
                                        .comment(VersionTestData.SNAPSHOT_VERSION_1_1.getComment())
                                        .createdBy(
                                                UserId.of(
                                                        VersionTestData.SNAPSHOT_VERSION_1_1
                                                                .getCreatedBy()))
                                        .createdAt(
                                                VersionTestData.SNAPSHOT_VERSION_1_1.getCreatedAt())
                                        .build()));
    }

    @Test
    public void fetchVersionModel__expectJson() throws Exception {
        mockMvc.perform(
                        get(
                                        "/v1/versions/{versionId}/model",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType("application/json;charset=UTF-8")
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("{}"));
    }
}
