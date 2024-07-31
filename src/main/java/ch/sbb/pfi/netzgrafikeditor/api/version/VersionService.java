package ch.sbb.pfi.netzgrafikeditor.api.version;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import static org.jooq.impl.DSL.or;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.AuthenticationService;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateReleaseDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionCreateSnapshotDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.common.ConflictException;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.NowProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VersionService {

    private final DSLContext context;
    private final NowProvider nowProvider;
    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public VersionId createSnapshotVersion(
            VersionId baseVersionId, VersionCreateSnapshotDto createSnapshotDto)
            throws NotFoundException, ConflictException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(baseVersionId).assertWritable();

        val baseVersion =
                context.fetchOptional(VERSIONS, VERSIONS.ID.eq(baseVersionId.getValue()))
                        .orElseThrow(NotFoundException.of("versions", baseVersionId));

        int newReleaseVersion;
        int newSnapshotVersion;

        if (baseVersion.getSnapshotVersion() == null) {
            // baseVersion is a release
            newReleaseVersion = baseVersion.getReleaseVersion() + 1;
            newSnapshotVersion = 1;
        } else {
            // baseVersion is a snapshot
            newReleaseVersion = baseVersion.getReleaseVersion();
            newSnapshotVersion = baseVersion.getSnapshotVersion() + 1;
        }

        val userId = this.authenticationService.getCurrentUserIdFromEmail();

        this.assertNewSnapshotVersionAvailable(
                VariantId.of(baseVersion.getVariantId()),
                newReleaseVersion,
                newSnapshotVersion,
                userId);

        val newVersion =
                this.context
                        .newRecord(VERSIONS)
                        .setVariantId(baseVersion.getVariantId())
                        .setReleaseVersion(newReleaseVersion)
                        .setSnapshotVersion(newSnapshotVersion)
                        .setName(createSnapshotDto.getName())
                        .setComment(createSnapshotDto.getComment())
                        .setModel(JSON.json(createSnapshotDto.getModel()))
                        .setCreatedAt(this.nowProvider.now())
                        .setCreatedBy(userId.getValue());

        newVersion.store();

        return VersionId.of(newVersion.getId());
    }

    private void assertNewSnapshotVersionAvailable(
            VariantId variantId, int newReleaseVersion, int newSnapshotVersion, UserId newUserId)
            throws ConflictException {
        val hasConflictingVersion =
                this.context.fetchExists(
                        VERSIONS,
                        VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                        VERSIONS.RELEASE_VERSION.eq(newReleaseVersion),
                        VERSIONS.SNAPSHOT_VERSION.eq(newSnapshotVersion),
                        VERSIONS.CREATED_BY.eq(newUserId.getValue()));

        if (hasConflictingVersion) {
            throw new ConflictException(
                    "Version(release_version="
                            + newReleaseVersion
                            + ", snapshot_version="
                            + newSnapshotVersion
                            + ", created_by="
                            + newUserId
                            + ") already exists");
        }
    }

    @Transactional
    public VersionId createReleaseVersion(
            VersionId snapshotVersionId, VersionCreateReleaseDto createReleaseDto)
            throws ConflictException, NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(snapshotVersionId).assertWritable();

        val baseSnapshotVersion =
                context.fetchOptional(VERSIONS, VERSIONS.ID.eq(snapshotVersionId.getValue()))
                        .orElseThrow(NotFoundException.of("versions", snapshotVersionId));

        this.assertNewReleaseVersionAvailable(
                VariantId.of(baseSnapshotVersion.getVariantId()),
                baseSnapshotVersion.getReleaseVersion());

        val userId = this.authenticationService.getCurrentUserIdFromEmail();

        val newVersion =
                this.context
                        .newRecord(VERSIONS)
                        .setVariantId(baseSnapshotVersion.getVariantId())
                        .setReleaseVersion(baseSnapshotVersion.getReleaseVersion())
                        .setSnapshotVersion(null)
                        .setName(baseSnapshotVersion.getName())
                        .setComment(createReleaseDto.getComment())
                        .setModel(baseSnapshotVersion.getModel())
                        .setCreatedAt(this.nowProvider.now())
                        .setCreatedBy(userId.getValue());

        newVersion.store();

        this.deleteOldSnapshotsOfUser(VariantId.of(baseSnapshotVersion.getVariantId()), userId);

        return VersionId.of(newVersion.getId());
    }

    @Transactional
    public VersionId restore(VersionId versionToRestoreId)
            throws ConflictException, NotFoundException, ForbiddenOperationException {
        val versionToRestore =
                this.context
                        .fetchOptional(VERSIONS, VERSIONS.ID.eq(versionToRestoreId.getValue()))
                        .orElseThrow(NotFoundException.of("versions", versionToRestoreId));

        val latestVersion = this.getLatestVersion(VariantId.of(versionToRestore.getVariantId()));

        val comment =
                Optional.ofNullable(versionToRestore.getSnapshotVersion())
                        .map(
                                snapshotVersion ->
                                        String.format(
                                                "Wiederhergestellt aus Änderung %d",
                                                snapshotVersion))
                        .orElseGet(
                                () ->
                                        String.format(
                                                "Wiederhergestellt aus Version %d",
                                                versionToRestore.getReleaseVersion()));

        val restoredVersion =
                this.createSnapshotVersion(
                        latestVersion.getId(),
                        VersionCreateSnapshotDto.builder()
                                .name(versionToRestore.getName())
                                .model(versionToRestore.getModel().data())
                                .comment(comment)
                                .build());

        this.authenticationService.getCurrentUserIdFromEmail();
        log.debug(
                "User {} has restored version {} in snapshot {}",
                this.authenticationService.getCurrentUserIdFromEmail(),
                versionToRestoreId,
                restoredVersion);

        return restoredVersion;
    }

    private void assertNewReleaseVersionAvailable(VariantId variantId, int newReleaseVersion)
            throws ConflictException {
        val hasConflictingVersion =
                this.context.fetchExists(
                        VERSIONS,
                        VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                        VERSIONS.RELEASE_VERSION.eq(newReleaseVersion),
                        VERSIONS.SNAPSHOT_VERSION.isNull());

        if (hasConflictingVersion) {
            throw new ConflictException(
                    "Version(release_version=" + newReleaseVersion + ") already exists");
        }
    }

    private void deleteOldSnapshotsOfUser(VariantId variantId, UserId userId) {
        this.context
                .deleteFrom(VERSIONS)
                .where(
                        VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                        VERSIONS.SNAPSHOT_VERSION.isNotNull(),
                        VERSIONS.CREATED_BY.eq(userId.getValue()))
                .execute();
    }

    @Transactional(readOnly = true)
    public VersionDto getVersion(VersionId versionId)
            throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(versionId).assertReadable();

        return this.context
                .fetchOptional(VERSIONS, VERSIONS.ID.eq(versionId.getValue()))
                .map(this::mapVersion)
                .orElseThrow(NotFoundException.of("versions", versionId));
    }

    @Transactional(readOnly = true)
    public JsonNode getVersionModel(VersionId versionId)
            throws NotFoundException, JsonProcessingException {
        val jsonModel =
                this.context
                        .select(VERSIONS.MODEL)
                        .from(VERSIONS)
                        .where(VERSIONS.ID.eq(versionId.getValue()))
                        .fetchOptionalInto(JSON.class)
                        .orElseThrow(NotFoundException.of("versions", versionId));
        return this.objectMapper.readTree(jsonModel.data());
    }

    @Transactional(readOnly = true)
    public List<VersionDto> getVersions(VariantId variantId) {
        return this.context
                .selectFrom(VERSIONS)
                .where(
                        VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                        or(
                                VERSIONS.SNAPSHOT_VERSION.isNull(),
                                VERSIONS.CREATED_BY.eq(
                                    authenticationService.getCurrentUserIdFromEmail().getValue()),
                                VERSIONS.CREATED_BY.eq(
                                        authenticationService.getCurrentSubjectId().getValue())))
                .orderBy(
                        VERSIONS.RELEASE_VERSION.asc(), VERSIONS.SNAPSHOT_VERSION.asc().nullsLast())
                .fetch(this::mapVersion);
    }

    @SneakyThrows // TODO FIXME: Remove
    @Transactional(readOnly = true)
    public VersionDto getLatestVersion(VariantId variantId) {
        var latestRelease =
                this.context
                        .selectFrom(VERSIONS)
                        .where(
                                VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                                VERSIONS.SNAPSHOT_VERSION.isNull())
                        .orderBy(VERSIONS.RELEASE_VERSION.desc())
                        .limit(1)
                        .fetchOptional(this::mapVersion);

        var latestSnapshot =
                this.context
                        .selectFrom(VERSIONS)
                        .where(
                                VERSIONS.VARIANT_ID.eq(variantId.getValue()),
                                VERSIONS.SNAPSHOT_VERSION.isNotNull(),
                                VERSIONS.CREATED_BY.eq(
                                        this.authenticationService.getCurrentUserIdFromEmail().getValue()).or(
                                    VERSIONS.CREATED_BY.eq(
                                        authenticationService.getCurrentSubjectId().getValue())
                                ))
                        .orderBy(VERSIONS.RELEASE_VERSION.desc(), VERSIONS.SNAPSHOT_VERSION.desc())
                        .limit(1)
                        .fetchOptional(this::mapVersion);

        if (latestSnapshot.isPresent()) {
            return latestSnapshot.get();
        } else {
            return latestRelease.orElseThrow(NotFoundException.of("variants", variantId));
        }
    }

    public VersionDto mapVersion(VersionsRecord record) {
        return VersionDto.builder()
                .id(VersionId.of(record.getId()))
                .variantId(VariantId.of(record.getVariantId()))
                .name(record.getName())
                .comment(record.getComment())
                .releaseVersion(record.getReleaseVersion())
                .snapshotVersion(Optional.ofNullable(record.getSnapshotVersion()))
                .createdAt(record.getCreatedAt())
                .createdBy(UserId.of(record.getCreatedBy()))
                .build();
    }
}
