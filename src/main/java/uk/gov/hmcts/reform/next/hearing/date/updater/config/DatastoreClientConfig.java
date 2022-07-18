package uk.gov.hmcts.reform.next.hearing.date.updater.config;

public class DatastoreClientConfig {

    public static final String GET_EVENT_TRIGGER_BY_EVENT_ID
        = "/cases/{caseId}/event-triggers/{eventId}";

    public static final String POST_EVENT_TRIGGER_BY_CASE_ID
        = "/cases/{caseId}";

//    @Bean
//    public SystemUserAuthHeadersInterceptor systemUserAuthHeadersInterceptor(SecurityUtils securityUtils) {
//        return new SystemUserAuthHeadersInterceptor(securityUtils);
//    }
//
//    @Bean
//    public Retryer retryer() {
//        return Retryer.NEVER_RETRY;
//    }
}
