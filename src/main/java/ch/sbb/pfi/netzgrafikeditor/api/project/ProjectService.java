package ch.sbb.pfi.netzgrafikeditor.api.project;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Projects.PROJECTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.ProjectsUsers.PROJECTS_USERS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Variants.VARIANTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import static org.jooq.impl.DSL.selectOne;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsUsersRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.AuthenticationService;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectCreateUpdateDto;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectDto;
import ch.sbb.pfi.netzgrafikeditor.api.project.model.ProjectSummaryDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantSummaryDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.VersionService;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.NowProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final NowProvider nowProvider;
    private final AuthenticationService authenticationService;
    private final DSLContext context;
    private final VersionService versionService;

    @Transactional
    public ProjectId create(ProjectCreateUpdateDto project) {
        val record =
                this.context
                        .newRecord(PROJECTS)
                        .setName(project.getName())
                        .setSummary(project.getSummary())
                        .setDescription(project.getDescription())
                        .setIsArchived(false)
                        .setCreatedAt(nowProvider.now())
                        .setCreatedBy(this.authenticationService.getCurrentUserIdFromEmail().getValue());

        record.store();

        val projectId = ProjectId.of(record.getId());

        var writeUsersIncludingCurrentUser = new ArrayList<>(project.getWriteUsers());
        writeUsersIncludingCurrentUser.add(
                this.authenticationService.getCurrentUserIdFromEmail().getValue());

        this.updateProjectUsers(projectId, writeUsersIncludingCurrentUser, project.getReadUsers());

        return projectId;
    }

    @Transactional
    public void delete(ProjectId projectId) throws ForbiddenOperationException, NotFoundException {
        this.authenticationService.getAuthorizationInfo(projectId).assertDeletable();

        this.context
                .deleteFrom(VERSIONS)
                .whereExists(
                        selectOne()
                                .from(VARIANTS)
                                .where(
                                        VERSIONS.VARIANT_ID.eq(VARIANTS.ID),
                                        VARIANTS.PROJECT_ID.eq(projectId.getValue())))
                .execute();

        this.context
                .deleteFrom(VARIANTS)
                .where(VARIANTS.PROJECT_ID.eq(projectId.getValue()))
                .execute();

        this.context
                .deleteFrom(PROJECTS_USERS)
                .where(PROJECTS_USERS.PROJECT_ID.eq(projectId.getValue()))
                .execute();

        this.context.deleteFrom(PROJECTS).where(PROJECTS.ID.eq(projectId.getValue())).execute();
    }

    private void updateProjectUsers(
            ProjectId projectId, Collection<String> writeUsers, Collection<String> readUsers) {
        this.context
                .deleteFrom(PROJECTS_USERS)
                .where(PROJECTS_USERS.PROJECT_ID.eq(projectId.getValue()))
                .execute();

        writeUsers.stream()
                .distinct()
                .forEach(
                        userId ->
                                this.context
                                        .newRecord(PROJECTS_USERS)
                                        .setProjectId(projectId.getValue())
                                        .setUserId(userId)
                                        .setIsEditor(true)
                                        .store());

        readUsers.stream()
                .filter(userId -> !writeUsers.contains(userId))
                .distinct()
                .forEach(
                        userId ->
                                this.context
                                        .newRecord(PROJECTS_USERS)
                                        .setProjectId(projectId.getValue())
                                        .setUserId(userId)
                                        .setIsEditor(false)
                                        .store());
    }

    @Transactional
    public void update(ProjectId projectId, ProjectCreateUpdateDto project)
            throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(projectId).assertWritable();

        val record =
                this.context
                        .selectFrom(PROJECTS)
                        .where(PROJECTS.ID.eq(projectId.getValue()))
                        .fetchOptional()
                        .orElseThrow(NotFoundException.of("projects", projectId));

        record.setName(project.getName());
        record.setSummary(project.getSummary());
        record.setDescription(project.getDescription());

        record.store();

        this.updateProjectUsers(projectId, project.getWriteUsers(), project.getReadUsers());
    }

    @Transactional
    public void archive(ProjectId projectId) throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(projectId).assertWritable();

        this.context
                .update(PROJECTS)
                .set(PROJECTS.IS_ARCHIVED, true)
                .where(PROJECTS.ID.eq(projectId.getValue()))
                .execute();
    }

    @Transactional
    public void unarchive(ProjectId projectId)
            throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(projectId).assertDeletable();

        this.context
                .update(PROJECTS)
                .set(PROJECTS.IS_ARCHIVED, false)
                .where(PROJECTS.ID.eq(projectId.getValue()))
                .execute();
    }

    @Transactional(readOnly = true)
    public Collection<ProjectSummaryDto> getAll() {
        val filterCondition =
                this.authenticationService.isAdmin()
                        ? selectOne()
                        : selectOne()
                                .from(PROJECTS_USERS)
                                .where(
                                        PROJECTS_USERS.PROJECT_ID.eq(PROJECTS.ID).and(
                                        PROJECTS_USERS.USER_ID.eq(
                                                this.authenticationService
                                                        .getCurrentUserIdFromEmail()
                                                        .getValue()).or(
                                                            PROJECTS_USERS.USER_ID.eq(
                                                            this.authenticationService
                                                                .getCurrentSubjectId()
                                                                .getValue()))));

        return this.context
                .selectFrom(PROJECTS)
                .whereExists(filterCondition)
                .fetch(this::mapProjectSummary);
    }

    @Transactional(readOnly = true)
    public ProjectDto getById(ProjectId projectId)
            throws NotFoundException, ForbiddenOperationException {
        val authorizationInfo = this.authenticationService.getAuthorizationInfo(projectId);
        authorizationInfo.assertReadable();

        val projectsRecord =
                this.context
                        .fetchOptional(PROJECTS, PROJECTS.ID.eq(projectId.getValue()))
                        .orElseThrow(NotFoundException.of("projects", projectId));

        val variantRecords =
                this.context.fetch(VARIANTS, VARIANTS.PROJECT_ID.eq(projectId.getValue()));
        val variantsMap = new HashMap<VariantId, VariantSummaryDto.VariantSummaryDtoBuilder>();
        for (val variant : variantRecords) {
            variantsMap.put(
                    VariantId.of(variant.getId()),
                    VariantSummaryDto.builder()
                            .id(VariantId.of(variant.getId()))
                            .projectId(projectId)
                            .isArchived(variant.getIsArchived()));
        }

        for (val releaseVersion : this.getLatestReleaseVersions(projectId)) {
            variantsMap
                    .get(releaseVersion.getVariantId())
                    .latestReleaseVersion(Optional.of(releaseVersion));
        }

        for (val snapshotVersion :
                this.getLatestSnapshotVersions(
                        projectId, authenticationService.getCurrentUserIdFromEmail())) {
            variantsMap
                    .get(snapshotVersion.getVariantId())
                    .latestSnapshotVersion(Optional.of(snapshotVersion));
        }

        val variants =
                variantsMap.values().stream()
                        .map(VariantSummaryDto.VariantSummaryDtoBuilder::build)
                        .filter(
                                variant ->
                                        variant.getLatestReleaseVersion().isPresent()
                                                || variant.getLatestSnapshotVersion().isPresent())
                        .collect(Collectors.toList());

        val projectsUsersRecords =
                this.context.fetch(
                        PROJECTS_USERS, PROJECTS_USERS.PROJECT_ID.eq(projectId.getValue()));
        Predicate<ProjectsUsersRecord> isEditor = ProjectsUsersRecord::getIsEditor;
        val writeUsers =
                projectsUsersRecords.stream()
                        .filter(isEditor)
                        .map(ProjectsUsersRecord::getUserId)
                        .collect(Collectors.toList());
        val readUsers =
                projectsUsersRecords.stream()
                        .filter(isEditor.negate())
                        .map(ProjectsUsersRecord::getUserId)
                        .collect(Collectors.toList());

        return this.mapProject(projectsRecord, variants, authorizationInfo, writeUsers, readUsers);
    }

    private ProjectSummaryDto mapProjectSummary(ProjectsRecord projectsRecord) {
        return ProjectSummaryDto.builder()
                .id(ProjectId.of(projectsRecord.getId()))
                .name(projectsRecord.getName())
                .isArchived(projectsRecord.getIsArchived())
                .build();
    }

    private ProjectDto mapProject(
            ProjectsRecord projectsRecord,
            Collection<VariantSummaryDto> variants,
            AuthenticationService.AuthorizationInfo authorizationInfo,
            List<String> writeUsers,
            List<String> readUsers) {
        return ProjectDto.builder()
                .id(ProjectId.of(projectsRecord.getId()))
                .name(projectsRecord.getName())
                .createdAt(projectsRecord.getCreatedAt())
                .createdBy(UserId.of(projectsRecord.getCreatedBy()))
                .summary(projectsRecord.getSummary())
                .description(projectsRecord.getDescription())
                .variants(variants)
                .isWritable(authorizationInfo.getWritable())
                .isDeletable(authorizationInfo.getDeletable())
                .writeUsers(writeUsers)
                .readUsers(readUsers)
                .isArchived(projectsRecord.getIsArchived())
                .build();
    }

    private List<VersionDto> getLatestReleaseVersions(ProjectId projectId) {
        return this.context
                .select(VERSIONS.asterisk())
                .distinctOn(VERSIONS.VARIANT_ID)
                .from(VERSIONS)
                .join(VARIANTS)
                .on(VARIANTS.ID.eq(VERSIONS.VARIANT_ID))
                .where(
                        VARIANTS.PROJECT_ID.eq(projectId.getValue()),
                        VERSIONS.SNAPSHOT_VERSION.isNull())
                .orderBy(VERSIONS.VARIANT_ID, VERSIONS.RELEASE_VERSION.desc())
                .fetchStreamInto(VERSIONS)
                .map(this.versionService::mapVersion)
                .collect(Collectors.toList());
    }

    private List<VersionDto> getLatestSnapshotVersions(ProjectId projectId, UserId userId) {
        return this.context
                .select(VERSIONS.asterisk())
                .distinctOn(VERSIONS.VARIANT_ID)
                .from(VERSIONS)
                .join(VARIANTS)
                .on(VARIANTS.ID.eq(VERSIONS.VARIANT_ID))
                .where(
                        VARIANTS.PROJECT_ID.eq(projectId.getValue()),
                        VERSIONS.SNAPSHOT_VERSION.isNotNull(),
                        VERSIONS.CREATED_BY.eq(userId.getValue()))
                .orderBy(
                        VERSIONS.VARIANT_ID,
                        VERSIONS.RELEASE_VERSION.desc(),
                        VERSIONS.SNAPSHOT_VERSION.desc())
                .fetchStreamInto(VERSIONS)
                .map(this.versionService::mapVersion)
                .collect(Collectors.toList());
    }
}
