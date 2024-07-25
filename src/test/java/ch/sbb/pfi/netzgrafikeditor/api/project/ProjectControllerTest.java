package ch.sbb.pfi.netzgrafikeditor.api.project;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProjectControllerTest {

    @Test
    void assertEMailPattern() {
        assertTrue("adrian@example.com".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertFalse("u123456".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("name.vorname.vorname2@mail.domain.ch".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertFalse("name.vorname.vorname2#mail.domain.ch".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertFalse("".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("fun@data.cloud".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("adrian@ai.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("1978@x.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("x@1978.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("1978@1978.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("1978@1978.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertTrue("19_78@19_78.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
        assertFalse("19....78@19_78.org".matches(ProjectController.USER_ID_AS_EMAIL_PATTERN));
    }
}
