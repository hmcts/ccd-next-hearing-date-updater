package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorDuringExecutionException;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;

@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
public class ApplicationBootstrap implements ApplicationRunner {

    @Value("${next-hearing-date-updater.processing.enabled}")
    private boolean isProcessingEnabled;

    @Autowired
    private final TelemetryClient client;

    @Value("${telemetry.wait.period:10000}")
    private int waitPeriod;

    private final NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @Autowired
    public ApplicationBootstrap(NextHearingDateUpdaterService nextHearingDateUpdaterService, TelemetryClient client) {
        this.nextHearingDateUpdaterService = nextHearingDateUpdaterService;
        this.client = client;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isProcessingEnabled) {
            try {
                log.info("Starting the Next-Hearing-Date-Updater job triggered by cron job.");
                nextHearingDateUpdaterService.execute();
                log.info("Completed the Next-Hearing-Date-Updater job successfully triggered by cron job.");
            } catch (Exception exception) {
                throw new ErrorDuringExecutionException("Error executing Next-Hearing-Date-Updater job.", exception);
            } finally {
                client.flush();
                waitTelemetryGracefulPeriod();
            }
        }
    }

    private void waitTelemetryGracefulPeriod() throws InterruptedException {
        Thread.sleep(waitPeriod);
    }

    public static void main(final String[] args) {
        final ApplicationContext context = SpringApplication.run(ApplicationBootstrap.class, args);
        SpringApplication.exit(context);
    }

}
