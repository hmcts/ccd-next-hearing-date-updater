package uk.gov.hmcts.reform.next.hearing.date.updater.befta;

import uk.gov.hmcts.befta.BeftaMain;

public final class ServiceBeftaMain {

    private ServiceBeftaMain() {
        // Hide Utility Class Constructor :
        // Utility classes should not have a public or default constructor (squid:S1118)
    }

    public static void main(String[] args) {
        BeftaMain.main(args, new ServiceTestAutomationAdapter());
    }

}
