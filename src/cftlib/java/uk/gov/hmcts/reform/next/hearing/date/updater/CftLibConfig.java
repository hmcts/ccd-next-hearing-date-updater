package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.io.IOException;
import java.util.Scanner;

@Component
public class CftLibConfig extends ContainersBootstrap implements CFTLibConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CftLibConfig.class);

    private static final String ROLE_CASEWORKER = "caseworker";
    private static final String ROLE_CASEWORKER_AT1 = "caseworker-autotest1";
    private static final String ROLE_CASEWORKER_BM = "caseworker-befta_master";
    private static final String ROLE_CCD_IMPORT = "ccd-import";
    private static final String ROLE_NHD_ADMIN = "next-hearing-date-admin";

    @Override
    public void configure(CFTLib lib) {
        createCcdRoles(lib);
        createIdamUsers(lib);

        primeCcdEnvironment();
    }

    private void createCcdRoles(CFTLib lib) {
        LOGGER.info("About to create roles......................");
        lib.createRoles(
            ROLE_CASEWORKER,
            ROLE_CASEWORKER_AT1,
            ROLE_CASEWORKER_BM,
            ROLE_CCD_IMPORT,
            ROLE_NHD_ADMIN
        );
        LOGGER.info("Finished creating roles......................");
    }

    private void createIdamUsers(CFTLib lib) {
        LOGGER.info("About to create Idam users......................");
        lib.createIdamUser("ccd.docker.default@hmcts.net", ROLE_CCD_IMPORT);
        lib.createIdamUser("auto.test.cnp@gmail.com", ROLE_CASEWORKER, ROLE_CASEWORKER_AT1, ROLE_CCD_IMPORT);
        lib.createIdamUser("next.hearing.date.admin@gmail.com", ROLE_NHD_ADMIN);
        lib.createIdamUser("master.caseworker@gmail.com", ROLE_CASEWORKER, ROLE_CASEWORKER_BM);
        LOGGER.info("Finished creating Idam users......................");
    }

    private void primeCcdEnvironment() {

        try {
            LOGGER.info("About to prime CCD environment......................");

            // NB: need to use gradle task to allow dynamic set of env vars that BEFTA uses when generating def files
            final Process process = Runtime.getRuntime().exec("./gradlew localDataSetup");
            process.waitFor();

            String result;
            Scanner s = new Scanner(process.getInputStream()).useDelimiter("\\A");
            result = s.hasNext() ? s.next() : null;
            LOGGER.info("localDataSetup output: \n\n{}", result);

            LOGGER.info("Finished priming CCD environment......................");

        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed priming CCD environment:", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

}
