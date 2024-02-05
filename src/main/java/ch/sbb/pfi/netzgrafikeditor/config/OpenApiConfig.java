package ch.sbb.pfi.netzgrafikeditor.config;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static {
        // generate named enums in open-api spec
        ModelResolver.enumsAsRef = true;
    }

    @Value("${auth.audience.service-name}")
    private String serviceName;

    @Value("${springdoc.oAuthFlow.authorizationUrl:}")
    private String authorizationUrl;

    @Value("${springdoc.oAuthFlow.tokenUrl:}")
    private String tokenUrl;

    @Bean
    public OpenAPI customOpenAPIConfig() {
        OAuthFlow oAuthFlow =
                new OAuthFlow()
                        .authorizationUrl(authorizationUrl)
                        .tokenUrl(tokenUrl)
                        .scopes(
                                new Scopes()
                                        .addString(
                                                serviceName + "/.default",
                                                serviceName + "/.default"));

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "OAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.OAUTH2)
                                                .scheme("bearer")
                                                .bearerFormat("jwt")
                                                .in(SecurityScheme.In.HEADER)
                                                .name("Authorization")
                                                .flows(
                                                        new OAuthFlows()
                                                                .authorizationCode(oAuthFlow))))
                .addSecurityItem(new SecurityRequirement().addList("OAuth"));
    }
}
