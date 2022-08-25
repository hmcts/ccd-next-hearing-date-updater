package uk.gov.hmcts.reform.next.hearing.date.updater;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.CcdCaseEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.stubReturn200SubmitCaseEvent;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.stubReturn200TriggerStartEvent;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.stubReturn404SubmitCaseEvent;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.stubReturn404TriggerStartEvent;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.EVENT_ID;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.NEXT_HEARING_DETAILS_FIELD_NAME;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.HEARING_DATE_TIME_IN_PAST;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.START_EVENT_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.SUBMIT_EVENT_ERROR;

@SpringBootTest()
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.ExcessiveImports"})
class CcdCaseEventServiceIT {

    @Autowired
    private CcdCaseEventService ccdCaseEventService;

    @Autowired
    protected SecurityUtils securityUtils;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @Autowired
    private WireMockServer wireMockServer;

    private static final String CASE_REFERENCE = "1658830998852951";

    private static final String HEARING_ID = "12345";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        Logger ccdCallbackRepositoryLogger = (Logger) LoggerFactory.getLogger(CcdCaseEventRepository.class);

        listAppender = new ListAppender<>();
        listAppender.start();

        ccdCallbackRepositoryLogger.addAppender(listAppender);
        wireMockServer.resetRequests();
    }

    @Test
    void createCaseEventsWhenStartEventResponseHasHearingDateInFuture() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId(HEARING_ID)
            .caseReference(CASE_REFERENCE)
            .hearingDateTime(LocalDateTime.now().plusDays(10))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .data(Map.of(NEXT_HEARING_DETAILS_FIELD_NAME, nextHearingDetails))
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .caseDetails(caseDetails)
            .token("tokenValue")
            .build();

        stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        stubReturn200SubmitCaseEvent(CASE_REFERENCE);

        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        verify(getRequestedFor(
            urlEqualTo(String.format("/cases/%s/event-triggers/%s", CASE_REFERENCE, EVENT_ID))));
        verify(postRequestedFor(urlEqualTo(
            String.format("/cases/%s/events", CASE_REFERENCE))));

        assertTrue(getLogs().isEmpty());
    }

    @Test
    void createCaseEventsWhenStartEventResponseHasHearingDateInPast() {

        Logger nextHearingDetailsLogger = (Logger) LoggerFactory.getLogger(NextHearingDetails.class);
        listAppender = new ListAppender<>();
        listAppender.start();

        nextHearingDetailsLogger.addAppender(listAppender);

        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId(HEARING_ID)
            .caseReference(CASE_REFERENCE)
            .hearingDateTime(LocalDateTime.now().minusDays(10))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .data(Map.of(NEXT_HEARING_DETAILS_FIELD_NAME, nextHearingDetails))
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .caseDetails(caseDetails)
            .token("tokenValue")
            .build();

        stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        verify(getRequestedFor(
            urlEqualTo(String.format("/cases/%s/event-triggers/%s", CASE_REFERENCE, EVENT_ID))));


        // this endpoint will never be called if the NextHearingDate date is not valid - i.e in the past
        verify(exactly(0), postRequestedFor(urlEqualTo(
            String.format("/cases/%s/events", CASE_REFERENCE))));

        String formattedLog = HEARING_DATE_TIME_IN_PAST.replace("{}", CASE_REFERENCE);

        assertLogMessages(List.of(formattedLog));
    }

    @Test
    void errosLoggedWhenStartEventFails() {
        stubReturn404TriggerStartEvent(CASE_REFERENCE);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        String formattedLog = String.format(START_EVENT_ERROR, CASE_REFERENCE, EVENT_ID);

        assertLogMessages(List.of(formattedLog));
    }

    @Test
    void errorsLoggedWhenCreateEventFails() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId(HEARING_ID)
            .caseReference(CASE_REFERENCE)
            .hearingDateTime(LocalDateTime.now().plusDays(10))
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .data(Map.of(NEXT_HEARING_DETAILS_FIELD_NAME, nextHearingDetails))
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(EVENT_ID)
            .caseDetails(caseDetails)
            .token("tokenValue")
            .build();

        stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        stubReturn404SubmitCaseEvent(CASE_REFERENCE);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        String formattedLog = String.format(SUBMIT_EVENT_ERROR, CASE_REFERENCE);

        assertLogMessages(List.of(formattedLog));
    }

    private List<ILoggingEvent> getLogs() {
        return listAppender.list;
    }

    private void assertLogMessages(List<String> logMessages)  {
        List<ILoggingEvent> logsList = getLogs();

        List<String> formattedMessages = logsList.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .toList();

        assertTrue(formattedMessages.containsAll(logMessages));
    }
}
