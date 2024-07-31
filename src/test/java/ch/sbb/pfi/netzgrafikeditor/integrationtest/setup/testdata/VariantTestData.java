package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsUsersRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VariantsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;

import org.jooq.JSON;

import java.time.LocalDateTime;

public class VariantTestData {

    public static final LocalDateTime DATE = LocalDateTime.of(2021, 9, 1, 17, 25, 38);

    public static final String USER_A = "u123456@sbb.ch";
    public static final String USER_B = "u654321@sbb.ch";

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
                    .setIsEditor(false);

    public static final VariantsRecord VARIANT =
            new VariantsRecord().setId(1l).setIsArchived(false).setProjectId(PROJECT.getId());

    public static final VersionsRecord RELEASE_VERSION_1 =
            new VersionsRecord()
                    .setId(1l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(null)
                    .setName("Release 1")
                    .setComment("First Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord RELEASE_VERSION_2 =
            new VersionsRecord()
                    .setId(2l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(null)
                    .setName("Release 2")
                    .setComment("Second Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(2))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_3_1_A =
            new VersionsRecord()
                    .setId(3l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(1)
                    .setName("Release 3")
                    .setComment("")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(3))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_3_2_A =
            new VersionsRecord()
                    .setId(4l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(2)
                    .setName("Release 3")
                    .setComment("")
                    .setModel(JSON.valueOf("{\"id\":\"snapshot 3.2 user A model\"}"))
                    .setCreatedAt(DATE.plusDays(4))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord SNAPSHOT_VERSION_3_1_B =
            new VersionsRecord()
                    .setId(5l)
                    .setVariantId(VARIANT.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(1)
                    .setName("Release 3")
                    .setComment("")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(3))
                    .setCreatedBy(USER_B);
}
