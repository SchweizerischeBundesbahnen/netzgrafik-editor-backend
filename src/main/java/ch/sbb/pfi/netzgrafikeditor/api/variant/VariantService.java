package ch.sbb.pfi.netzgrafikeditor.api.variant;

import static ch.sbb.netzgrafikeditor.jooq.model.tables.Variants.VARIANTS;
import static ch.sbb.netzgrafikeditor.jooq.model.tables.Versions.VERSIONS;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VariantsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;
import ch.sbb.pfi.netzgrafikeditor.api.common.AuthenticationService;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.ProjectId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.UserId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VariantId;
import ch.sbb.pfi.netzgrafikeditor.api.common.model.VersionId;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantCreateFromVersionDto;
import ch.sbb.pfi.netzgrafikeditor.api.variant.model.VariantDto;
import ch.sbb.pfi.netzgrafikeditor.api.version.VersionService;
import ch.sbb.pfi.netzgrafikeditor.api.version.model.VersionDto;
import ch.sbb.pfi.netzgrafikeditor.common.ForbiddenOperationException;
import ch.sbb.pfi.netzgrafikeditor.common.NotFoundException;
import ch.sbb.pfi.netzgrafikeditor.common.NowProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.jooq.DSLContext;
import org.jooq.JSON;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class VariantService {

    private final NowProvider nowProvider;
    private final AuthenticationService authenticationService;
    private final DSLContext context;
    private final VersionService versionService;

    @Transactional
    public VariantId create(ProjectId projectId, VariantCreateDto createDto)
            throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(projectId).assertWritable();

        val now = this.nowProvider.now();
        val userId = this.authenticationService.getCurrentUserId();

        val variantRecord =
                this.context
                        .newRecord(VARIANTS)
                        .setProjectId(projectId.getValue())
                        .setIsArchived(false);

        variantRecord.store();

        val variantId = variantRecord.getId();

        this.context
                .newRecord(VERSIONS)
                .setVariantId(variantId)
                .setReleaseVersion(1)
                .setSnapshotVersion(1)
                .setName(createDto.getInitialName())
                .setCreatedAt(now)
                .setCreatedBy(userId.getValue())
                .setComment("")
                .setModel(JSON.json(createDto.getInitialModel()))
                .store();

        return VariantId.of(variantId);
    }

    @Transactional
    public void delete(VariantId variantId) throws ForbiddenOperationException, NotFoundException {
        this.authenticationService.getAuthorizationInfo(variantId).assertDeletable();

        this.context
                .deleteFrom(VERSIONS)
                .where(VERSIONS.VARIANT_ID.eq(variantId.getValue()))
                .execute();

        this.context.deleteFrom(VARIANTS).where(VARIANTS.ID.eq(variantId.getValue())).execute();
    }

    @Transactional
    public void archive(VariantId variantId) throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(variantId).assertWritable();

        this.context
                .update(VARIANTS)
                .set(VARIANTS.IS_ARCHIVED, true)
                .where(VARIANTS.ID.eq(variantId.getValue()))
                .execute();
    }

    @Transactional
    public void unarchive(VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        this.authenticationService.getAuthorizationInfo(variantId).assertDeletable();

        this.context
                .update(VARIANTS)
                .set(VARIANTS.IS_ARCHIVED, false)
                .where(VARIANTS.ID.eq(variantId.getValue()))
                .execute();
    }

    @Transactional
    public VariantId createFromVersion(VersionId versionId, VariantCreateFromVersionDto createDto)
            throws NotFoundException, ForbiddenOperationException {
        this.assertVersionExists(versionId);

        val record =
                this.context
                        .select(VERSIONS.asterisk(), VARIANTS.asterisk())
                        .from(VERSIONS)
                        .join(VARIANTS)
                        .on(VERSIONS.VARIANT_ID.eq(VARIANTS.ID))
                        .where(VERSIONS.ID.eq(versionId.getValue()))
                        .fetchAny();

        val projectId = ProjectId.of(record.get(VARIANTS.PROJECT_ID));

        this.authenticationService.getAuthorizationInfo(projectId).assertWritable();

        val now = this.nowProvider.now();
        val userId = this.authenticationService.getCurrentUserId();

        val variantRecord =
                this.context
                        .newRecord(VARIANTS)
                        .setProjectId(projectId.getValue())
                        .setIsArchived(false);

        variantRecord.store();

        this.context
                .newRecord(VERSIONS)
                .setVariantId(variantRecord.getId())
                .setReleaseVersion(1)
                .setSnapshotVersion(1)
                .setName(createDto.getName())
                .setCreatedAt(now)
                .setCreatedBy(userId.getValue())
                .setComment("Erstellt aus Variante: " + record.get(VERSIONS.NAME))
                .setModel(record.get(VERSIONS.MODEL))
                .store();

        return VariantId.of(variantRecord.getId());
    }

    private void assertVariantExists(VariantId variantId) throws NotFoundException {
        if (!this.context.fetchExists(VARIANTS, VARIANTS.ID.eq(variantId.getValue()))) {
            throw new NotFoundException("variants", variantId);
        }
    }

    private void assertVersionExists(VersionId versionId) throws NotFoundException {
        if (!this.context.fetchExists(VERSIONS, VERSIONS.ID.eq(versionId.getValue()))) {
            throw new NotFoundException("versions", versionId);
        }
    }

    @Transactional(readOnly = true)
    public VariantDto getById(VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        val authorizationInfo = this.authenticationService.getAuthorizationInfo(variantId);
        authorizationInfo.assertReadable();

        return this.context
                .fetchOptional(VARIANTS, VARIANTS.ID.eq(variantId.getValue()))
                .map(
                        r ->
                                this.map(
                                        r,
                                        this.versionService.getVersions(variantId),
                                        this.versionService.getLatestVersion(variantId),
                                        authorizationInfo))
                .orElseThrow(NotFoundException.of("variants", variantId));
    }

    private VariantDto map(
            VariantsRecord record,
            List<VersionDto> versions,
            VersionDto latestVersion,
            AuthenticationService.AuthorizationInfo authorizationInfo) {
        return VariantDto.builder()
                .id(VariantId.of(record.get(VARIANTS.ID)))
                .projectId(ProjectId.of(record.get(VARIANTS.PROJECT_ID)))
                .isArchived(record.getIsArchived())
                .versions(versions)
                .latestVersion(latestVersion)
                .isWritable(authorizationInfo.getWritable())
                .isDeletable(authorizationInfo.getDeletable())
                .build();
    }

    @Transactional
    public void dropSnapshots(VariantId variantId) throws NotFoundException {
        assertVariantExists(variantId);

        val userId = this.authenticationService.getCurrentUserId();

        if (this.hasReleases(variantId)) {
            this.deleteAllSnapshots(variantId, userId);
        } else {
            // delete all snapshots except the initial one
            this.context
                    .deleteFrom(VERSIONS)
                    .where(
                            VERSIONS.VARIANT_ID
                                    .eq(variantId.getValue())
                                    .and(VERSIONS.CREATED_BY.eq(userId.getValue()))
                                    .and(VERSIONS.SNAPSHOT_VERSION.isNotNull())
                                    .and(VERSIONS.SNAPSHOT_VERSION.notEqual(1))
                                    .and(VERSIONS.RELEASE_VERSION.eq(1)))
                    .execute();

            log.info(
                    "{} has dropped all snapshots except the initial one from unreleased variant {}",
                    userId,
                    variantId);
        }
    }

    @Transactional
    public void raiseSnapshotsToNewestReleaseVersion(VariantId variantId)
            throws NotFoundException, ForbiddenOperationException {
        assertVariantExists(variantId);
        assertHasReleases(variantId);

        val userId = this.authenticationService.getCurrentUserId();

        val highestReleasedVersion =
                this.tryFetchHighestReleaseVersion(variantId)
                        .orElseThrow(
                                () ->
                                        new ForbiddenOperationException(
                                                String.format(
                                                        "Variant %s has no releases", variantId)));

        val snapshots = this.fetchSnapshots(variantId, userId);
        val snapshotsBaseReleasedVersion =
                this.tryFetchSnapshotsBaseReleaseVersion(snapshots)
                        .orElseThrow(
                                () ->
                                        new ForbiddenOperationException(
                                                String.format(
                                                        "Variant %s has no snapshots from user %s",
                                                        variantId, userId)));

        if (highestReleasedVersion.getId().equals(snapshotsBaseReleasedVersion.getId())) {
            throw new ForbiddenOperationException("No conflicting release found");
        }

        this.deleteAllSnapshots(variantId, userId);

        val restoreSnapshot =
                this.context
                        .newRecord(VERSIONS)
                        .setName(snapshotsBaseReleasedVersion.getName())
                        .setComment(
                                String.format(
                                        "Wiederhergestellt aus Version %d",
                                        snapshotsBaseReleasedVersion.getReleaseVersion()))
                        .setModel(snapshotsBaseReleasedVersion.getModel());

        val newSnapshots =
                snapshots.stream()
                        .map(
                                snapshot ->
                                        this.context
                                                .newRecord(VERSIONS)
                                                .setName(snapshot.getName())
                                                .setComment(snapshot.getComment())
                                                .setModel(snapshot.getModel()));

        AtomicInteger count = new AtomicInteger(0);
        val allNewSnapshots =
                Stream.concat(Stream.of(restoreSnapshot), newSnapshots)
                        .map(
                                versionsRecord ->
                                        versionsRecord
                                                .setVariantId(variantId.getValue())
                                                .setReleaseVersion(
                                                        highestReleasedVersion.getReleaseVersion()
                                                                + 1)
                                                .setSnapshotVersion(count.incrementAndGet())
                                                .setCreatedAt(this.nowProvider.now())
                                                .setCreatedBy(userId.getValue()))
                        .collect(Collectors.toList());

        this.context.batchStore(allNewSnapshots).execute();
    }

    private Optional<VersionsRecord> tryFetchHighestReleaseVersion(VariantId variantId) {
        return Optional.ofNullable(
                this.context
                        .selectFrom(VERSIONS)
                        .where(
                                VERSIONS.VARIANT_ID
                                        .eq(variantId.getValue())
                                        .and(VERSIONS.SNAPSHOT_VERSION.isNull()))
                        .orderBy(VERSIONS.RELEASE_VERSION.desc())
                        .fetchAny());
    }

    private Optional<VersionsRecord> tryFetchSnapshotsBaseReleaseVersion(
            List<VersionsRecord> snapshots) {
        return snapshots.stream()
                .findAny()
                .flatMap(
                        snapshot ->
                                this.context
                                        .selectFrom(VERSIONS)
                                        .where(
                                                VERSIONS.VARIANT_ID
                                                        .eq(snapshot.getVariantId())
                                                        .and(VERSIONS.SNAPSHOT_VERSION.isNull())
                                                        .and(
                                                                VERSIONS.RELEASE_VERSION.eq(
                                                                        snapshot.getReleaseVersion()
                                                                                - 1)))
                                        .fetchOptional());
    }

    private List<VersionsRecord> fetchSnapshots(VariantId variantId, UserId userId) {
        return this.context
                .selectFrom(VERSIONS)
                .where(
                        VERSIONS.VARIANT_ID
                                .eq(variantId.getValue())
                                .and(VERSIONS.CREATED_BY.eq(userId.getValue()))
                                .and(VERSIONS.SNAPSHOT_VERSION.isNotNull()))
                .fetchInto(VersionsRecord.class);
    }

    private void deleteAllSnapshots(VariantId variantId, UserId userId) {
        this.context
                .deleteFrom(VERSIONS)
                .where(
                        VERSIONS.VARIANT_ID
                                .eq(variantId.getValue())
                                .and(VERSIONS.CREATED_BY.eq(userId.getValue()))
                                .and(VERSIONS.SNAPSHOT_VERSION.isNotNull()))
                .execute();

        log.info("{} has dropped all snapshots from variant {}", userId, variantId);
    }

    private boolean hasReleases(VariantId variantId) {
        return this.context.fetchExists(
                VERSIONS,
                VERSIONS.VARIANT_ID
                        .eq(variantId.getValue())
                        .and(VERSIONS.SNAPSHOT_VERSION.isNull()));
    }

    private void assertHasReleases(VariantId variantId) throws ForbiddenOperationException {
        if (!this.hasReleases(variantId)) {
            throw new ForbiddenOperationException(
                    String.format("Variant %s has no releases", variantId));
        }
    }
}
