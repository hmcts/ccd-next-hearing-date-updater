package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.inject.Inject;

@Slf4j
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class ApplicationBootstrap implements ApplicationRunner {

    @Inject
    private ApplicationExecutor applicationExecutor;

    public static void main(final String[] args) {
        SpringApplication.run(ApplicationBootstrap.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting the Next-Hearing-Date-Updater job.");
        applicationExecutor.execute();
        log.info("Completed the Next-Hearing-Date-Updater job successfully.");
    }
}
