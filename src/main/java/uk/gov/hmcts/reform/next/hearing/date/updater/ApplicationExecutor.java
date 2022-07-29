package uk.gov.hmcts.reform.next.hearing.date.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {
    @Value("${csv.caseReferences.fileLocation}")
    private String fileLocation;

    @Value("${csv.caseReferences.fileName}")
    private String fileName;

    public void execute() {
        log.info("Next-Hearing-Date-Updater started...");
        final String csvFile = fileLocation + File.separator + fileName;
        log.info(csvFile);
        log.info("Next-Hearing-Date-Updater finished...");
    }

}
