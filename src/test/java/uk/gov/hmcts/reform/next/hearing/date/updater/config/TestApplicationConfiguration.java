package uk.gov.hmcts.reform.next.hearing.date.updater.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class TestApplicationConfiguration {

    @Bean
    @Primary
    public TelemetryClient provideTestTelemetryClient() {
        return new TelemetryClient();
    }
}
