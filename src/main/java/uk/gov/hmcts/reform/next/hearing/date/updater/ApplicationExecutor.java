package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {

    public void execute() {
        log.info("Next-Hearing-Date-Updater started...");
        log.info("Next-Hearing-Date-Updater finished...");
    }

}
