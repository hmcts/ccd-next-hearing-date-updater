package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class ApplicationBootstrap implements ApplicationRunner, CommandLineRunner {

    @Value("${cronjob.enabled}")
    private boolean cronJobEnabled;

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
        if (cronJobEnabled) {
            log.info("Starting the Next-Hearing-Date-Updater job triggered by cron job.");
            //nextHearingDateUpdaterService.execute();
        }
        log.info("Completed the Next-Hearing-Date-Updater job successfully triggered by cron job.");
    }

    @Override
    public void run(String... args) throws Exception {
        if (cronJobEnabled) {
            log.info("Starting the Next-Hearing-Date-Updater job from command line.");
            //nextHearingDateUpdaterService.execute();
        }
        log.info("Completed the Next-Hearing-Date-Updater job from command line successfully.");
    }
}
