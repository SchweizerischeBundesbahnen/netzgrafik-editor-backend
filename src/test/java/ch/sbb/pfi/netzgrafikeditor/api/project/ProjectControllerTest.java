package ch.sbb.pfi.netzgrafikeditor.api.project;

import org.junit.jupiter.api.Test;
import java.util.regex.Matcher;

class ProjectControllerTest {

    private boolean isUserIdAsEmailPatternValid(String input) {
        Matcher matcher = ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher(input);
        return matcher.matches();
    }

    @Test
    void assertEMailPatternTest() {
        assertTrue(isUserIdAsEmailPatternValid("adrian@example.com"),"adrian@example.com should be valid");
        assertTrue(isUserIdAsEmailPatternValid("name.vorname.vorname2@mail.domain.ch"),"name.vorname.vorname2@mail.domain.ch should be valid");
        assertTrue(isUserIdAsEmailPatternValid("fun@data.cloud"),"fun@data.cloud should be valid");
        assertTrue(isUserIdAsEmailPatternValid("adrian@ai.org"),"adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("1234@x.org"),"1234@x.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("x@1234.org"),"x@1234.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("1234@1234.org"),"1234@1234.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("123a4@1234.org"),"123a4@1234.org should be valid");

        assertFalse(isUserIdAsEmailPatternValid("19....23@a.org"),"19....23@1a.org should be invalid");
        assertFalse(isUserIdAsEmailPatternValid("u123456"),"u123456 should be invalid");
        assertFalse(isUserIdAsEmailPatternValid("name.vorname.vorname2#mail.domain.ch"),"name.vorname.vorname2#mail.domain.ch should be invalid");
        assertFalse(isUserIdAsEmailPatternValid(""),"'' should be invalid");
    }
}