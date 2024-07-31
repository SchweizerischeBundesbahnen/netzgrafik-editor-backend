package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VariantsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;

import org.jooq.JSON;

import java.time.LocalDateTime;

public class FetchProjectTestData {

    private static final LocalDateTime DATE = LocalDateTime.of(2021, 9, 1, 17, 25, 38);

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

    public static final VariantsRecord VARIANT_1 =
            new VariantsRecord().setId(1l).setIsArchived(false).setProjectId(PROJECT.getId());

    public static final VariantsRecord VARIANT_2 =
            new VariantsRecord().setId(2l).setIsArchived(false).setProjectId(PROJECT.getId());

    public static final VersionsRecord VARIANT_1_RELEASE_1 =
            new VersionsRecord()
                    .setId(1l)
                    .setVariantId(VARIANT_1.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(null)
                    .setName("Release 1")
                    .setComment("First Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord VARIANT_1_RELEASE_2 =
            new VersionsRecord()
                    .setId(2l)
                    .setVariantId(VARIANT_1.getId())
                    .setReleaseVersion(2)
                    .setSnapshotVersion(null)
                    .setName("Release 2")
                    .setComment("Second Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(2))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord VARIANT_1_SNAPSHOT_1A =
            new VersionsRecord()
                    .setId(3l)
                    .setVariantId(VARIANT_1.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(1)
                    .setName("Snapshot 3.1")
                    .setComment("")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(3))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord VARIANT_1_SNAPSHOT_1B =
            new VersionsRecord()
                    .setId(4l)
                    .setVariantId(VARIANT_1.getId())
                    .setReleaseVersion(3)
                    .setSnapshotVersion(1)
                    .setName("Snapshot 3.1")
                    .setComment("")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(3))
                    .setCreatedBy(USER_B);

    public static final VersionsRecord VARIANT_2_SNAPSHOT_1B =
            new VersionsRecord()
                    .setId(5l)
                    .setVariantId(VARIANT_2.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(1)
                    .setName("Snapshot 1.1")
                    .setComment("")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(3))
                    .setCreatedBy(USER_B);
}
