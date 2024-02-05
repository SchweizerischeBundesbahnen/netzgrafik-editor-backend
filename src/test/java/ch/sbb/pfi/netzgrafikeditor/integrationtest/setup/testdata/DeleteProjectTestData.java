package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsUsersRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VariantsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.VersionsRecord;

import org.jooq.JSON;

import java.time.LocalDateTime;

public class DeleteProjectTestData {

    private static final LocalDateTime DATE = LocalDateTime.of(2021, 9, 1, 17, 25, 38);

    public static final String USER_A = "u123456";

    public static final ProjectsRecord PROJECT_A =
            new ProjectsRecord()
                    .setId(1l)
                    .setName("Project A")
                    .setSummary("Project Summary")
                    .setDescription("Project Descriptions")
                    .setCreatedAt(LocalDateTime.of(2021, 8, 7, 9, 53, 1))
                    .setCreatedBy(USER_A)
                    .setIsArchived(true);

    public static final ProjectsRecord PROJECT_B =
            new ProjectsRecord()
                    .setId(2l)
                    .setName("Project B")
                    .setSummary("Project Summary")
                    .setDescription("Project Descriptions")
                    .setCreatedAt(LocalDateTime.of(2021, 8, 7, 9, 53, 1))
                    .setCreatedBy(USER_A)
                    .setIsArchived(false);

    public static final ProjectsUsersRecord PROJECT_A_USERS_A =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT_A.getId())
                    .setUserId(USER_A)
                    .setIsEditor(false);

    public static final ProjectsUsersRecord PROJECT_B_USERS_A =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT_B.getId())
                    .setUserId(USER_A)
                    .setIsEditor(true);

    public static final VariantsRecord VARIANT_A =
            new VariantsRecord().setId(1l).setIsArchived(true).setProjectId(PROJECT_A.getId());

    public static final VariantsRecord VARIANT_B =
            new VariantsRecord().setId(2l).setIsArchived(false).setProjectId(PROJECT_B.getId());

    public static final VersionsRecord VERSION_A =
            new VersionsRecord()
                    .setId(1l)
                    .setVariantId(VARIANT_A.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(null)
                    .setName("Release 1")
                    .setComment("First Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);

    public static final VersionsRecord VERSION_B =
            new VersionsRecord()
                    .setId(2l)
                    .setVariantId(VARIANT_B.getId())
                    .setReleaseVersion(1)
                    .setSnapshotVersion(null)
                    .setName("Release 1")
                    .setComment("First Release")
                    .setModel(JSON.valueOf("{}"))
                    .setCreatedAt(DATE.plusDays(1))
                    .setCreatedBy(USER_A);
}
