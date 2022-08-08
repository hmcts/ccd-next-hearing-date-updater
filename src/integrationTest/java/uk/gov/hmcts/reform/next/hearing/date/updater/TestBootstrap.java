package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SuppressWarnings({"checkstyle:hideutilityclassconstructor"})
@ContextConfiguration(initializers = {TestBootstrap.WireMockServerInitializer.class})
public class TestBootstrap {

    protected static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(
        options().dynamicPort().withRootDirectory("classpath:/wiremock-stubs")
    );

    static {
        if (!WIRE_MOCK_SERVER.isRunning()) {
            WIRE_MOCK_SERVER.start();
        }
    }

    public static class WireMockServerInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "wiremock.server.port=" + WIRE_MOCK_SERVER.port()
            );

            applicationContext.addApplicationListener((ApplicationListener<ContextClosedEvent>) event -> {
                if (WIRE_MOCK_SERVER.isRunning()) {
                    WIRE_MOCK_SERVER.shutdown();
                }
            });
        }
    }
}
