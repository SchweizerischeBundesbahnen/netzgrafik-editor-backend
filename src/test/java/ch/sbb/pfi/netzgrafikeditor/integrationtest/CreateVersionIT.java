package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateReleaseDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateSnapshotDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class CreateVersionIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        VersionTestData.PROJECT,
                        VersionTestData.PROJECTS_USERS_A,
                        VersionTestData.PROJECTS_USERS_B,
                        VersionTestData.PROJECTS_USERS_C,
                        VersionTestData.VARIANT,
                        VersionTestData.SNAPSHOT_VERSION_1_1));
        controllableNowProvider.setNow(VersionTestData.DATE);
    }

    @Test
    public void createAndFetchVersions__expectCreatedVersions() throws Exception {
        // create release
        val createReleaseDto = VersionCreateReleaseDto.builder().comment("First Release").build();

        val releasePostResult =
                mockMvc.perform(
                                post(
                                                "/v1/versions/{baseVersionId}/release",
                                                VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createReleaseDto))
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val releaseLocation = releasePostResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val releaseLocationId =
                Long.parseLong(releasePostResult.getResponse().getContentAsString());

        mockMvc.perform(
                        get(releaseLocation)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                VersionDto.builder()
                                        .id(VersionId.of(releaseLocationId))
                                        .variantId(VariantId.of(VersionTestData.VARIANT.getId()))
                                        .releaseVersion(
                                                VersionTestData.SNAPSHOT_VERSION_1_1
                                                        .getReleaseVersion())
                                        .snapshotVersion(Optional.empty())
                                        .name(VersionTestData.SNAPSHOT_VERSION_1_1.getName())
                                        .comment(createReleaseDto.getComment())
                                        .createdBy(UserId.of(VersionTestData.USER_A))
                                        .createdAt(VersionTestData.DATE)
                                        .build()));

        // snapshot gets deleted after releasing
        mockMvc.perform(
                        get(
                                        "/v1/versions/{versionId}",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isNotFound());

        // create snapshot 1
        val createSnapshot1Dto =
                VersionCreateSnapshotDto.builder()
                        .name("Snapshot 1")
                        .model("{\"x\":1234}")
                        .comment("snapshot 1 comment")
                        .build();

        val snapshotPostResult =
                mockMvc.perform(
                                post("/v1/versions/{baseVersionId}/snapshot", releaseLocationId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(createSnapshot1Dto))
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val snapshot1Location = snapshotPostResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val snapshot1LocationId =
                Long.parseLong(snapshotPostResult.getResponse().getContentAsString());

        mockMvc.perform(
                        get(snapshot1Location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                VersionDto.builder()
                                        .id(VersionId.of(snapshot1LocationId))
                                        .variantId(VariantId.of(VersionTestData.VARIANT.getId()))
                                        .releaseVersion(
                                                VersionTestData.SNAPSHOT_VERSION_1_1
                                                                .getReleaseVersion()
                                                        + 1)
                                        .snapshotVersion(Optional.of(1))
                                        .name(createSnapshot1Dto.getName())
                                        .comment(createSnapshot1Dto.getComment())
                                        .createdBy(UserId.of(VersionTestData.USER_A))
                                        .createdAt(VersionTestData.DATE)
                                        .build()));

        // create snapshot 2
        val createSnapshot2Dto =
                VersionCreateSnapshotDto.builder()
                        .name("Snapshot 2")
                        .model("{\"x\":4567}")
                        .comment("snapshot 2")
                        .build();

        val snapshot2PostResult =
                mockMvc.perform(
                                post("/v1/versions/{baseVersionId}/snapshot", snapshot1LocationId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(createSnapshot2Dto))
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val snapshot2Location = snapshot2PostResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val snapshot2LocationId =
                Long.parseLong(snapshot2PostResult.getResponse().getContentAsString());

        mockMvc.perform(
                        get(snapshot2Location)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isOk())
                .andExpect(
                        jsonResponse(
                                VersionDto.builder()
                                        .id(VersionId.of(snapshot2LocationId))
                                        .variantId(VariantId.of(VersionTestData.VARIANT.getId()))
                                        .releaseVersion(
                                                VersionTestData.SNAPSHOT_VERSION_1_1
                                                                .getReleaseVersion()
                                                        + 1)
                                        .snapshotVersion(Optional.of(2))
                                        .name(createSnapshot2Dto.getName())
                                        .comment(createSnapshot2Dto.getComment())
                                        .createdBy(UserId.of(VersionTestData.USER_A))
                                        .createdAt(VersionTestData.DATE)
                                        .build()));
    }

    @Test
    public void createExistingRelease__expectConflict() throws Exception {
        // create snapshot as second user
        val createSnapshotDto =
                VersionCreateSnapshotDto.builder()
                        .model("{}")
                        .name("Base Snapshot")
                        .comment("")
                        .build();

        val postResult =
                mockMvc.perform(
                                post(
                                                "/v1/versions/{baseVersionId}/snapshot",
                                                VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(createSnapshotDto))
                                        .with(
                                                user(
                                                        VersionTestData.USER_B,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        val snapshotLocationId = Long.parseLong(postResult.getResponse().getContentAsString());

        // create first release
        val createReleaseDto = VersionCreateReleaseDto.builder().comment("First Release").build();

        mockMvc.perform(
                        post(
                                        "/v1/versions/{baseVersionId}/release",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReleaseDto))
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isCreated())
                .andReturn();

        // conflict on second release attempt
        mockMvc.perform(
                        post("/v1/versions/{baseVersionId}/release", snapshotLocationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReleaseDto))
                                .with(user(VersionTestData.USER_B, SecurityConfig.USER_ROLE)))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    public void createExistingSnapshot__expectConflict() throws Exception {
        // create snapshot
        val createSnapshotDto =
                VersionCreateSnapshotDto.builder()
                        .model("{}")
                        .name("Base Snapshot")
                        .comment("")
                        .build();

        mockMvc.perform(
                        post(
                                        "/v1/versions/{baseVersionId}/snapshot",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createSnapshotDto))
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isCreated())
                .andReturn();

        // conflict if snapshot is created a second time
        mockMvc.perform(
                        post(
                                        "/v1/versions/{baseVersionId}/snapshot",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createSnapshotDto))
                                .with(user(VersionTestData.USER_A, SecurityConfig.USER_ROLE)))
                .andExpect(status().isConflict())
                .andReturn();
    }

    @Test
    public void createReleaseVersion__expectForbiddenWhenNotWritable() throws Exception {
        val createReleaseDto = VersionCreateReleaseDto.builder().comment("First Release").build();

        mockMvc.perform(
                        post(
                                        "/v1/versions/{baseVersionId}/release",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReleaseDto))
                                .with(user(VersionTestData.USER_C, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void createSnapshotVersion__expectForbiddenWhenNotWritable() throws Exception {
        // create snapshot
        val createSnapshotDto =
                VersionCreateSnapshotDto.builder()
                        .model("{}")
                        .name("Base Snapshot")
                        .comment("")
                        .build();

        mockMvc.perform(
                        post(
                                        "/v1/versions/{baseVersionId}/snapshot",
                                        VersionTestData.SNAPSHOT_VERSION_1_1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createSnapshotDto))
                                .with(user(VersionTestData.USER_C, SecurityConfig.USER_ROLE)))
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
