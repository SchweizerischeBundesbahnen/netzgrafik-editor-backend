package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup.testdata;

import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsRecord;
import ch.sbb.netzgrafikeditor.jooq.model.tables.records.ProjectsUsersRecord;

import java.time.LocalDateTime;

public class ProjectTestData {
    public static final LocalDateTime DATE_A = LocalDateTime.of(2021, 9, 1, 17, 25, 38);

    public static final String USER_A = "u123456@sbb.ch";
    public static final String USER_B = "u654321@sbb.ch";
    public static final String USER_C = "u999999@sbb.ch";

    public static final ProjectsRecord PROJECT_A =
            new ProjectsRecord()
                    .setId(1l)
                    .setName("Project A")
                    .setSummary("Project A Summary")
                    .setDescription("Project A Description")
                    .setCreatedAt(LocalDateTime.of(2021, 8, 9, 12, 23, 18))
                    .setCreatedBy(USER_A)
                    .setIsArchived(false);

    public static final ProjectsRecord PROJECT_B =
            new ProjectsRecord()
                    .setId(2l)
                    .setName("Project B")
                    .setSummary("Project B Summary")
                    .setDescription("Project B Description")
                    .setCreatedAt(LocalDateTime.of(2021, 8, 7, 9, 53, 1))
                    .setCreatedBy(USER_A)
                    .setIsArchived(true);

    public static final ProjectsUsersRecord PROJECT_A_USERS_A =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT_A.getId())
                    .setUserId(USER_A)
                    .setIsEditor(true);

    public static final ProjectsUsersRecord PROJECT_A_USERS_B =
            new ProjectsUsersRecord()
                    .setProjectId(PROJECT_A.getId())
                    .setUserId(USER_B)
                    .setIsEditor(false);
}
