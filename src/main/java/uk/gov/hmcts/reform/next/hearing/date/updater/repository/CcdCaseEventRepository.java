package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.next.hearing.date.updater.clients.DatastoreClient;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.CaseDataContent;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.StartEventResult;

/**
 * HMAN-322.
 */
@Repository
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class CcdCaseEventRepository {

    private final DatastoreClient datastoreClient;
    private static final String EVENT_ID = "UpdateNextHearingInfo";

    @Autowired
    public CcdCaseEventRepository(DatastoreClient datastoreClient) {
        this.datastoreClient = datastoreClient;
    }

    private StartEventResult triggerAboutToStartCallback(String caseReference) {
        return datastoreClient.getEventTriggerByEventId(caseReference, EVENT_ID);
    }

    private void createEvent(String caseReference) {
        datastoreClient.createEvent(caseReference, new CaseDataContent());
    }

    public void createCaseEvents(String caseReference) {
        // StartEventResult startEventResult = triggerAboutToStartCallback(caseReference);

        // String token = startEventResult.getToken();
        // createEvent(caseReference);
    }
}
