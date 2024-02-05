package ch.sbb.pfi.netzgrafikeditor.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {
    @Bean
    public FlywayMigrationStrategy migrationStrategy() {
        // disable flyway migration on startup and only validate the existing migration against the
        // ones
        // on the classpath
        return Flyway::validate;
    }
}
