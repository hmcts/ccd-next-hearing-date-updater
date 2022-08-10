package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;

@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam"})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class ApplicationBootstrap implements ApplicationRunner {

    @Value("${next-hearing-date-updater.processing.enabled}")
    private boolean isProcessingEnabled;

    private final NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @Autowired
    public ApplicationBootstrap(NextHearingDateUpdaterService nextHearingDateUpdaterService) {
        this.nextHearingDateUpdaterService = nextHearingDateUpdaterService;
    }

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isProcessingEnabled) {
            log.info("Starting the Next-Hearing-Date-Updater job triggered by cron job.");
            nextHearingDateUpdaterService.execute();
            log.info("Completed the Next-Hearing-Date-Updater job successfully triggered by cron job.");
        }
    }
}
