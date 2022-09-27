package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig extends ContainersBootstrap implements CFTLibConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CftLibConfig.class);

    @Override
    public void configure(CFTLib lib) throws Exception {
        createCcdRoles(lib);
        createIdamUsers(lib);
    }

    private void createCcdRoles(CFTLib lib) {
        LOGGER.info("About to create roles......................");
        lib.createRoles(
            "caseworker",
            "caseworker-autotest1",
            "caseworker-befta_master",
            "ccd-import",
            "next-hearing-date-admin"
        );
        LOGGER.info("Finished creating roles......................");
    }

    private void createIdamUsers(CFTLib lib) {
        LOGGER.info("About to create Idam users......................");
        lib.createIdamUser("ccd.docker.default@hmcts.net", "ccd-import");
        lib.createIdamUser("auto.test.cnp@gmail.com", "caseworker", "caseworker-autotest1", "ccd-import");
        lib.createIdamUser("next.hearing.date.admin@gmail.com", "next-hearing-date-admin");
        lib.createIdamUser("master.caseworker@gmail.com", "caseworker", "caseworker-befta_master");
        LOGGER.info("Finished creating Idam users......................");
    }
}
