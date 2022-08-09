package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HMAN-322.
 */
@Repository
@Slf4j
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class CcdCaseEventRepository {

    public static final String SUBMIT_EVENT_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/events";
    public static final String START_EVENT_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/event-triggers/%s";

    private final CoreCaseDataApi datastoreClient;
    private final SecurityUtils securityUtils;

    public static final String EVENT_ID = "UpdateNextHearingInfo";


    @Autowired
    public CcdCaseEventRepository(CoreCaseDataApi datastoreClient, SecurityUtils securityUtils) {
        this.datastoreClient = datastoreClient;
        this.securityUtils = securityUtils;
    }

    private StartEventResponse triggerAboutToStartCallback(String caseReference) {
        StartEventResponse startEventResponse = null;
        try {
            String nextHearingDateAdminAccessToken = securityUtils.getNextHearingDateAdminAccessToken();
            String s2SToken = securityUtils.getS2SToken();
            startEventResponse = datastoreClient.startEvent(nextHearingDateAdminAccessToken, s2SToken,
                                                                               caseReference, EVENT_ID);
        } catch (FeignException feignException) {
            log.error(String.format(START_EVENT_ERROR, caseReference, EVENT_ID), feignException);
        }

        return startEventResponse;
    }

    private CaseResource createCaseEvent(StartEventResponse startEventResponse)  {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(EVENT_ID).build())
            .data(startEventResponse.getCaseDetails().getData())
            .build();

        String caseReference = startEventResponse.getCaseDetails().getId().toString();

        try {
            return datastoreClient.createEvent(
                securityUtils.getNextHearingDateAdminAccessToken(),
                securityUtils.getS2SToken(),
                startEventResponse.getCaseDetails().getId().toString(),
                caseDataContent
            );
        } catch (FeignException feignException) {
            log.error(String.format(SUBMIT_EVENT_ERROR, caseReference), feignException);
            return null;
        }
    }

    public void performCcdCallback(String caseReference) {
        StartEventResponse startEventResult = triggerAboutToStartCallback(caseReference);

        if (startEventResult != null) {
            Map nextHearingDetailsJson = (Map) startEventResult.getCaseDetails().getData().get("NextHearingDetails");

            LocalDateTime hearingDate = LocalDateTime.parse(nextHearingDetailsJson.get("hearingDateTime").toString());

            NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                .caseReference(caseReference)
                .hearingId(nextHearingDetailsJson.get("hearingId").toString())
                .hearingDateTime(hearingDate)
                .build();

            if (nextHearingDetails.validateValues()) {
                createCaseEvent(startEventResult);
            }
        }
    }
}
