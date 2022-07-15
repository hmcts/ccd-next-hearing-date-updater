package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;


import javax.inject.Inject;

@Slf4j
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class ApplicationBootstrap implements ApplicationRunner, CommandLineRunner {

    @Inject
    private NextHearingDateUpdaterService nextHearingDateUpdaterService;

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting the Next-Hearing-Date-Updater job triggered by cron job.");
        nextHearingDateUpdaterService.execute();
        log.info("Completed the Next-Hearing-Date-Updater job successfully triggered by cron job.");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting the Next-Hearing-Date-Updater job from command line.");
        nextHearingDateUpdaterService.execute();
        log.info("Completed the Next-Hearing-Date-Updater job from command line successfully.");
    }
}
