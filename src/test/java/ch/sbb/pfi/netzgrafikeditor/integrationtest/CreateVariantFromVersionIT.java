package ch.sbb.pfi.netzgrafikeditor.integrationtest;

import static ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.UserHelper.user;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.DATE;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.PROJECT;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.PROJECTS_USERS_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.RELEASE_VERSION_1;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.RELEASE_VERSION_2;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.SNAPSHOT_VERSION_3_1_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.SNAPSHOT_VERSION_3_2_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.USER_A;
import static ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VariantTestData.VARIANT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateFromVersionDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.config.SecurityConfig;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.IntegrationTest;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata.VersionTestData;

import lombok.val;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

public class CreateVariantFromVersionIT extends IntegrationTest {
    @BeforeEach
    protected void insertTestData() {
        testDataService.insertTestData(
                List.of(
                        PROJECT,
                        PROJECTS_USERS_A,
                        VARIANT,
                        RELEASE_VERSION_1,
                        RELEASE_VERSION_2,
                        SNAPSHOT_VERSION_3_1_A,
                        SNAPSHOT_VERSION_3_2_A));

        controllableNowProvider.setNow(DATE);
    }

    @Test
    public void createVariantFromSnapshot__expectCreatedVariant() throws Exception {
        // given
        val variantCreateDto = VariantCreateFromVersionDto.builder().name("my variant").build();

        // when
        val postResult =
                mockMvc.perform(
                                post(
                                                "/v1/versions/{versionId}/variant/new",
                                                SNAPSHOT_VERSION_3_2_A.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(variantCreateDto))
                                        .with(user(USER_A, SecurityConfig.USER_ROLE)))
                        .andExpect(status().isCreated())
                        .andReturn();

        // then
        val getNewVariantUrl = postResult.getResponse().getHeader(HttpHeaders.LOCATION);
        val variantId = VariantId.of(Long.parseLong(postResult.getResponse().getContentAsString()));

        val expectedVersion =
                VersionDto.builder()
                        .id(VersionId.of(0)) // ignored
                        .variantId(variantId)
                        .releaseVersion(1)
                        .snapshotVersion(Optional.of(1))
                        .name(variantCreateDto.getName())
                        .comment("Erstellt aus Variante: " + SNAPSHOT_VERSION_3_2_A.getName())
                        .createdAt(controllableNowProvider.now())
                        .createdBy(UserId.of(USER_A))
                        .build();

        val expectedVariant =
                VariantDto.builder()
                        .id(variantId)
                        .projectId(ProjectId.of(VARIANT.getProjectId()))
                        .versions(List.of(expectedVersion))
                        .latestVersion(expectedVersion)
                        .isWritable(true)
                        .isDeletable(false)
                        .isArchived(false)
                        .build();

        val getNewVariantResult =
                mockMvc.perform(
                                get(getNewVariantUrl)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .with(user(USER_A, SecurityConfig.USER_ROLE)))
                        .andExpect(status().isOk())
                        .andExpect(
                                jsonResponse(
                                        jsonMatcher ->
                                                jsonMatcher
                                                        .ignorePath("/versions/0/id")
                                                        .ignorePath("/latestVersion/id")
                                                        .assertEquals(expectedVariant)))
                        .andReturn();

        val latestVersionId =
                jsonHandler
                        .createAccessor(getNewVariantResult.getResponse().getContentAsString())
                        .getIntegerByPath("/latestVersion/id")
                        .orElseThrow();

        val getModelResult =
                mockMvc.perform(
                                get("/v1/versions/{versionId}/model", latestVersionId)
                                        .contentType("application/json;charset=UTF-8")
                                        .with(
                                                user(
                                                        VersionTestData.USER_A,
                                                        SecurityConfig.USER_ROLE)))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType("application/json"))
                        .andReturn();

        Assertions.assertThat(getModelResult.getResponse().getContentAsString())
                .isEqualTo(SNAPSHOT_VERSION_3_2_A.getModel().data());
    }
}
