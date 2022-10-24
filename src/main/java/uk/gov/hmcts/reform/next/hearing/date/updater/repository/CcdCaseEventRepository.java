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
import uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;

/**
 * HMAN-322.
 */
@Repository
@Slf4j
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class CcdCaseEventRepository {

    public static final String SUBMIT_EVENT_FEIGN_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/events";
    public static final String START_EVENT_FEIGN_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/event-triggers/%s";
    public static final String SUBMIT_EVENT_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/events because of the following: ";
    public static final String START_EVENT_ERROR =
        "Call to following downstream CCD endpoint failed: /cases/%s/event-triggers/%s because of the following: ";

    private final CoreCaseDataApi datastoreClient;
    private final SecurityUtils securityUtils;

    @Autowired
    public CcdCaseEventRepository(CoreCaseDataApi datastoreClient, SecurityUtils securityUtils) {
        this.datastoreClient = datastoreClient;
        this.securityUtils = securityUtils;
    }

    public StartEventResponse triggerAboutToStartEvent(String caseReference) {
        StartEventResponse startEventResponse = null;
        try {
            String nextHearingDateAdminAccessToken = securityUtils.getNextHearingDateAdminAccessToken();
            String s2SToken = securityUtils.getS2SToken();
            startEventResponse = datastoreClient.startEvent(nextHearingDateAdminAccessToken, s2SToken,
                                                            caseReference, CaseEventConfig.EVENT_ID);
        } catch (FeignException feignException) {
            log.error(String.format(START_EVENT_FEIGN_ERROR, caseReference, CaseEventConfig.EVENT_ID), feignException);
        }  catch (Exception exception) {
            log.error(String.format(START_EVENT_ERROR, caseReference, CaseEventConfig.EVENT_ID), exception);
        }
        return startEventResponse;
    }

    public CaseResource createCaseEvent(StartEventResponse startEventResponse)  {
        String caseReference = startEventResponse.getCaseDetails().getId().toString();

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .caseReference(caseReference)
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(CaseEventConfig.EVENT_ID).build())
            .data(startEventResponse.getCaseDetails().getData())
            .build();

        try {
            return datastoreClient.createEvent(
                securityUtils.getNextHearingDateAdminAccessToken(),
                securityUtils.getS2SToken(),
                caseReference,
                caseDataContent
            );
        } catch (FeignException feignException) {
            log.error(String.format(SUBMIT_EVENT_FEIGN_ERROR, caseReference), feignException);
            return null;
        } catch (Exception exception) {
            log.error(String.format(SUBMIT_EVENT_ERROR, caseReference), exception);
            return null;
        }
    }
}
