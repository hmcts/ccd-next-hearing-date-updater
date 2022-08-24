package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.EVENT_ID;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class CcdCaseEventRepositoryTest {
    @Mock
    private CoreCaseDataApi datastoreClient;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CcdCaseEventRepository ccdCaseEventRepository;

    private static final String CASE_REFERENCE = "1111222233334444";
    private static final String TOKEN = "token";
    private static final String S2S_TOKEN = "S2SToken";
    private static final String ADMIN_TOKEN = "AdminToken";

    private static final Map<String, Object> DATA = Map.of("TestData", List.of("value", "value2"));

    @Captor
    private ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor;

    private static final StartEventResponse START_EVENT_RESPONSE = StartEventResponse.builder()
        .token(TOKEN)
        .eventId(EVENT_ID)
        .caseDetails(CaseDetails.builder().id(Long.parseLong(CASE_REFERENCE)).data(DATA).build())
        .build();

    @Test
    void testTriggerAboutToStartEvent() {
        when(securityUtils.getS2SToken()).thenReturn(S2S_TOKEN);
        when(securityUtils.getNextHearingDateAdminAccessToken()).thenReturn(ADMIN_TOKEN);

        when(datastoreClient.startEvent(ADMIN_TOKEN, S2S_TOKEN, CASE_REFERENCE, EVENT_ID))
            .thenReturn(START_EVENT_RESPONSE);

        StartEventResponse returnedStartEventResponse = ccdCaseEventRepository.triggerAboutToStartEvent(CASE_REFERENCE);

        assertNotNull(returnedStartEventResponse);
        verify(securityUtils).getNextHearingDateAdminAccessToken();
        verify(securityUtils).getS2SToken();
    }

    @Test
    void testTriggerAboutToStartEventThrowsFeignException() {
        when(datastoreClient.startEvent(any(), any(), any(), any()))
            .thenThrow(FeignException.class);

        StartEventResponse returnedStartEventResponse = ccdCaseEventRepository.triggerAboutToStartEvent(CASE_REFERENCE);

        assertNull(returnedStartEventResponse);
        verify(securityUtils).getNextHearingDateAdminAccessToken();
        verify(securityUtils).getS2SToken();
    }

    @Test
    void testCreateCaseEventPopulatesCaseDataContent() {
        when(securityUtils.getS2SToken()).thenReturn(S2S_TOKEN);
        when(securityUtils.getNextHearingDateAdminAccessToken()).thenReturn(ADMIN_TOKEN);


        ccdCaseEventRepository.createCaseEvent(START_EVENT_RESPONSE);

        verify(datastoreClient).createEvent(any(), any(), any(), caseDataContentArgumentCaptor.capture());

        CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();

        assertAll(
            () -> assertNotNull(caseDataContent),
            () -> assertEquals(CASE_REFERENCE, caseDataContent.getCaseReference()),
            () -> assertEquals(EVENT_ID, caseDataContent.getEvent().getId()),
            () -> assertEquals(TOKEN, caseDataContent.getEventToken()),
            () -> assertEquals(DATA, caseDataContent.getData())
        );
    }

    @Test
    void testCreateCaseEvent() {
        when(securityUtils.getS2SToken()).thenReturn(S2S_TOKEN);
        when(securityUtils.getNextHearingDateAdminAccessToken()).thenReturn(ADMIN_TOKEN);
        when(datastoreClient.createEvent(any(), any(), any(), any())).thenReturn(new CaseResource());

        CaseResource caseEvent = ccdCaseEventRepository.createCaseEvent(START_EVENT_RESPONSE);

        assertNotNull(caseEvent);
        verify(securityUtils).getNextHearingDateAdminAccessToken();
        verify(securityUtils).getS2SToken();
    }

    @Test
    void testCreateCaseEventThrowsFeignException() {
        when(datastoreClient.createEvent(any(), any(), any(), any()))
            .thenThrow(FeignException.class);

        CaseResource caseEvent = ccdCaseEventRepository.createCaseEvent(START_EVENT_RESPONSE);

        assertNull(caseEvent);
        verify(securityUtils).getNextHearingDateAdminAccessToken();
        verify(securityUtils).getS2SToken();

    }
}
