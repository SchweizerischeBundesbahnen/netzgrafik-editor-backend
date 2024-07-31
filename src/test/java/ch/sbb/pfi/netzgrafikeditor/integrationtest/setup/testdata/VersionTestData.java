package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsUsersRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VariantsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;

import org.jooq.JSON;

import java.time.LocalDateTime;

public class VersionTestData {

    public static final LocalDateTime DATE = LocalDateTime.of(2021, 9, 1, 17, 25, 38);

    public static final String USER_A = "u123456@sbb.ch";
    public static final String USER_B = "u654321@sbb.ch";
    public static final String USER_C = "u999999@sbb.ch";

    public static final ProjectsRecord PROJECT =
            new ProjectsRecord()
                    .setId(1l)
                    .setName("Variant Project")
                    .setSummary("Project Summary")
                    .setDescription("Project Descriptions")
                    .setCreatedAt(LocalDateTime.of(2021, 8, 7, 9, 53, 1))
                    .setCreatedBy(USER_A)
                    .setIsArchived(false);

    public static final ProjectsUsersRecord PROJECTS_USERS_A =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT.getId())
                    .setUserId(USER_A)
                    .setIsEditor(true);

    public static final ProjectsUsersRecord PROJECTS_USERS_B =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT.getId())
                    .setUserId(USER_B)
                    .setIsEditor(true);

    public static final ProjectsUsersRecord PROJECTS_USERS_C =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT.getId())
                    .setUserId(USER_C)
                    .setIsEditor(false);

    public static final VariantsRecord VARIANT =
            new VariantsRecord().setId(1l).setIsArchived(false).setProjectId(PROJECT.getId());

    public static final String VERSIONS_NAME = "Version XY";

    public static final VersionsRecord SNAPSHOT_VERSION_1_1 =
            new VersionsRecord()
                    .setId(10l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(1)
                    .setName(VERSIONS_NAME)
                    .setComment("Base Version")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_1_2 =
            new VersionsRecord()
                    .setId(11l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(2)
                    .setName(VERSIONS_NAME)
                    .setComment("Base Version")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord RELEASE_VERSION_1 =
            new VersionsRecord()
                    .setId(20l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(null)
                    .setName(VERSIONS_NAME)
                    .setComment("Release Version 1")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_2_1 =
            new VersionsRecord()
                    .setId(21l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(1)
                    .setName(VERSIONS_NAME)
                    .setComment("Snapshot 1 Version 2")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_2_2 =
            new VersionsRecord()
                    .setId(22l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(2)
                    .setName(VERSIONS_NAME)
                    .setComment("Snapshot 2 Version 2")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_2_1_USER_B =
            new VersionsRecord()
                    .setId(23l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(1)
                    .setName(VERSIONS_NAME)
                    .setComment("Snapshot 1 Version 2 User B")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_B);

    public static final VersionsRecord RELEASE_VERSION_2 =
            new VersionsRecord()
                    .setId(24l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(null)
                    .setName(VERSIONS_NAME)
                    .setComment("Release Version 2")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord RELEASE_VERSION_3 =
            new VersionsRecord()
                    .setId(25l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(null)
                    .setName(VERSIONS_NAME)
                    .setComment("Release Version 2")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord RELEASE_VERSION_4 =
            new VersionsRecord()
                    .setId(26l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(4)
                    .setSnapshotVersion(null)
                    .setName(VERSIONS_NAME)
                    .setComment("Release Version 2")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);
}
