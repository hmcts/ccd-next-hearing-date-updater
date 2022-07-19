package uk.gov.hmcts.reform.next.hearing.date.updater.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

public final class DatastoreClientConfig {
    public static final String GET_EVENT_TRIGGER_BY_EVENT_ID
        = "cases/{caseId}/event-triggers/{eventId}";

    public static final String POST_EVENT_TRIGGER_BY_CASE_ID
        = "cases/{caseId}";

    private DatastoreClientConfig() {
    }
}
