package ch.sbb.pfi.netzgrafikeditor.api.project;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;

class ProjectControllerTest {

    private boolean isUserIdAsEmailPatternValid(String input) {
        Matcher matcher = ProjectController.USER_ID_AS_EMAIL_PATTERN.matcher(input);
        return matcher.matches();
    }

    @Test
    void assertEMailPatternTest() {
        assertTrue(isUserIdAsEmailPatternValid("franz@nix.com"), "franz@nix.com should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("adrian@example.com"),
                "adrian@example.com should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("muster.hans@example.test.zurich"),
                "muster.hans@example.test.zurich should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("name.vorname.vorname2@mail.domain.ch"),
                "name.vorname.vorname2@mail.domain.ch should be valid");
        assertTrue(isUserIdAsEmailPatternValid("fun@data.cloud"), "fun@data.cloud should be valid");
        assertTrue(isUserIdAsEmailPatternValid("adrian@ai.org"), "adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("1234@x.org"), "1234@x.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("x@1234.org"), "x@1234.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("1234@1234.org"), "1234@1234.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("123a4@1234.org"), "123a4@1234.org should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("123a4@1234abc123zsd.a123sb.org"),
                "123a4@1234.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("Franz@nix.com"), "Franz@nix.com should be valid");
        assertTrue(isUserIdAsEmailPatternValid("Adrian@ai.org"), "adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("aDrian@ai.org"), "adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("ADRIAN@AI.ORG"), "adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("adrian@AI.ORG"), "adrian@ai.org should be valid");
        assertTrue(isUserIdAsEmailPatternValid("adri+an@ai.ORG"), "adri+an@ai.org should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("ad+ri-an@AI.org"), "ad+ri-an@ai.org should be valid");
        assertTrue(
                isUserIdAsEmailPatternValid("ad+ri-an@A-I.org"),
                "ad+ri-an@a-i.org should be valid");

        assertTrue(isUserIdAsEmailPatternValid("u123456"), "u123456 should be valid");
        assertTrue(isUserIdAsEmailPatternValid("u000000"), "u000000 should be valid");
        assertTrue(isUserIdAsEmailPatternValid("ue122322"), "ue122322 should be valid");
        assertTrue(isUserIdAsEmailPatternValid("e123211"), "e123211 should be valid");
        assertTrue(isUserIdAsEmailPatternValid("u123456"), "u123456 should be invalid");

        assertFalse(isUserIdAsEmailPatternValid("blabla"), "blabla should be invalid");
        assertFalse(
                isUserIdAsEmailPatternValid("name.vorname.vorname2#mail.domain.ch"),
                "name.vorname.vorname2#mail.domain.ch should be invalid");
        assertFalse(isUserIdAsEmailPatternValid(""), "'' should be invalid");
        assertFalse(isUserIdAsEmailPatternValid("u12345z"), "u12345z should be invalid");
        assertFalse(isUserIdAsEmailPatternValid("ua122322"), "ua122322 should be invalid");
        assertFalse(isUserIdAsEmailPatternValid("U123211"), "U123211 should be invalid");
    }
}
