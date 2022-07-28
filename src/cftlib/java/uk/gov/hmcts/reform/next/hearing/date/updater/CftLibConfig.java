package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

@Component
public class CftLibConfig implements CFTLibConfigurer {

    @Override
    public void configure(CFTLib lib) throws Exception {
        createCcdRoles(lib);
        createIdamUsers(lib);
    }

    private void createCcdRoles(CFTLib lib) {
        lib.createRoles(
            "next-hearing-date-admin"
        );
    }

    private void createIdamUsers(CFTLib lib) {
        lib.createIdamUser("next.hearing.date.admin@gmail.com", "next-hearing-date-admin");
    }
}
