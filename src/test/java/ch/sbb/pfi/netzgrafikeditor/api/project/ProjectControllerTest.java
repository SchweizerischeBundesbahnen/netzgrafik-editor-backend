package ch.sbb.pfi.netzgrafikeditor.api.project;

import org.junit.jupiter.api.Test;

class ProjectControllerTest {

    @Test
    void assertEMailPatternTest() {
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("adrian@example.com").matches());
        assertFalse(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("u123456").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("name.vorname.vorname2@mail.domain.ch").matches());
        assertFalse(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("name.vorname.vorname2#mail.domain.ch").matches());
        assertFalse(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("fun@data.cloud").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("adrian@ai.org").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("1978@x.org").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("x@1978.org").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("1978@1978.org").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("1978@1978.org").matches());
        assertTrue(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("19_78@19_78.org").matches());
        assertFalse(ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher("19....78@19_78.org").matches());
    }
}
