package ch.sbb.pfi.netzgrafikeditor.api.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectControllerTest {

    @Test
    public void testPattern() {
        assertFalse(ProjectController.USER_ID_PATTERN.matcher("blabla").matches());
        assertTrue(ProjectController.USER_ID_PATTERN.matcher("franz@nix.com").matches());
        assertFalse(ProjectController.USER_ID_PATTERN.matcher("Franz@nix.com").matches());
    }

}