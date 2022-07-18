package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.next.hearing.date.updater.clients.DatastoreClient;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.CaseDataContent;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.StartEventResult;

/**
 * HMAN-322
 */
@Repository
public class CallbackRepository {

    private final DatastoreClient datastoreClient;
    private final String EVENT_ID = "UpdateNextHearingInfo";

    @Autowired
    public CallbackRepository(DatastoreClient datastoreClient) {
        this.datastoreClient = datastoreClient;
    }

    private StartEventResult triggerAboutToStartCallback(String caseReference) {
        return datastoreClient.getEventTriggerByEventId(caseReference, EVENT_ID);
    }

    private void createEvent(String caseReference) {
        datastoreClient.createEvent(caseReference, new CaseDataContent());
    }

    public void performCallback(String caseReference) {
        StartEventResult startEventResult = triggerAboutToStartCallback(caseReference);

        String token = startEventResult.getToken();
        createEvent(caseReference);
    }
}
