package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup;

import ch.sbb.pfi.netzgrafikeditor.common.NowProvider;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.ControllableNowProvider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class IntegrationTestConfiguration {

    @Bean
    public ControllableNowProvider controllableNowProvider() {
        return new ControllableNowProvider();
    }

    @Bean
    public NowProvider nowProvider(ControllableNowProvider controllableNowProvider) {
        return controllableNowProvider;
    }
}
