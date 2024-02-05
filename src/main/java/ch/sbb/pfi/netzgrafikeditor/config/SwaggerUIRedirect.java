/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2019.
 */

package ch.sbb.pfi.netzgrafikeditor.config;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Profile({"local", "localdb"})
public class SwaggerUIRedirect {

    @Hidden
    @GetMapping("/")
    public RedirectView redirectToSwaggerUI() {
        return new RedirectView("/swagger-ui.html");
    }
}
