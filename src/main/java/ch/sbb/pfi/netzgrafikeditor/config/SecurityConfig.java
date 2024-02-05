package ch.sbb.pfi.netzgrafikeditor.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private static final String ROLES_KEY = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    public static final String USER_ROLE = "ROLE_User";
    public static final String ADMIN_ROLE = "ROLE_Admin";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is enabled by default. It can be disabled using
                // <code>.csrf().disable()</code>.
                // @see <a
                // href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#csrf-when">When to use CSRF protection</a>

                // CORS: by default Spring uses a bean with the name of corsConfigurationSource:
                // @see
                // ch.sbb.esta.config.CorsConfig
                .cors(withDefaults())

                // for details about stateless authentication see e.g.
                // https://golb.hplar.ch/2019/05/stateless.html
                .sessionManagement(
                        sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // @see <a
                // href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#jc-authorize-requests">Authorize Requests</a>
                .authorizeHttpRequests(
                        authorizeRequests ->
                                authorizeRequests
                                        // Method security may also be configured using the
                                        // annotations
                                        // <code>@PreAuthorize</code> and
                                        // <code>@PostAuthorize</code>
                                        // that permit to set fine grained control using the Spring
                                        // Expression Language:
                                        // @see <a
                                        // href="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#method-security-expressions">Method Security Expressions</a>
                                        // In order to use these annotations, you have to enable
                                        // global-method-security
                                        // using <code>@EnableGlobalMethodSecurity(prePostEnabled =
                                        // true)</code>.

                                        // allow unauthenticated access to actuator for OpenShift
                                        // health monitoring
                                        .requestMatchers(HttpMethod.GET, "/actuator/health")
                                        .permitAll()

                                        // allow unauthenticated access to OpenAPI / Swagger
                                        .requestMatchers(
                                                "/",
                                                "/v3/api-docs/**",
                                                "/swagger-ui/**",
                                                "/swagger-ui.html")
                                        .permitAll()

                                        // all other request need either the User or Admin role
                                        .anyRequest()
                                        .hasAnyAuthority(USER_ROLE, ADMIN_ROLE))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter roleConverter = new JwtGrantedAuthoritiesConverter();
        roleConverter.setAuthorityPrefix(ROLE_PREFIX);
        roleConverter.setAuthoritiesClaimName(ROLES_KEY);
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(roleConverter);
        return jwtAuthenticationConverter;
    }
}
