package uk.gov.hmcts.reform.next.hearing.date.updater;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.EVENT_ID;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.NEXT_HEARING_DETAILS_FIELD_NAME;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.HEARING_DATE_TIME_IN_PAST;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.START_EVENT_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.SUBMIT_EVENT_ERROR;

@SpringBootTest()
@ActiveProfiles("itest")
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class CcdCaseEventServiceIT extends WireMockBootstrap {

    @Autowired
    private CcdCaseEventService ccdCaseEventService;

    @Autowired
    protected SecurityUtils securityUtils;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String CASE_REFERENCE = "1658830998852951";

    private static final String HEARING_ID = "12345";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        wiremockFixtures.stubIdam();

        Logger ccdCallbackRepositoryLogger = (Logger) LoggerFactory.getLogger(CcdCaseEventRepository.class);

        listAppender = new ListAppender<>();
        listAppender.start();

        ccdCallbackRepositoryLogger.addAppender(listAppender);
    }

    @Test
    void createCaseEvents() {
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

        wiremockFixtures.stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        wiremockFixtures.stubReturn200SubmitCaseEvent(CASE_REFERENCE);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        assertTrue(getLogs().isEmpty());
    }

    @Test
    void performCcdCallbackHearingDateIsInPast() {

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

        wiremockFixtures.stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        String formattedLog = HEARING_DATE_TIME_IN_PAST.replace("{}", CASE_REFERENCE);

        assertLogMessages(List.of(formattedLog));
    }

    @Test
    void performCcdAboutToStartCallbackStartErrors() {
        wiremockFixtures.stubReturn404TriggerStartEvent(CASE_REFERENCE);
        ccdCaseEventService.createCaseEvents(List.of(CASE_REFERENCE));

        String formattedLog = String.format(START_EVENT_ERROR, CASE_REFERENCE, EVENT_ID);

        assertLogMessages(List.of(formattedLog));
    }

    @Test
    void performCcdSubmitEventErrors() {
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

        wiremockFixtures.stubReturn200TriggerStartEvent(CASE_REFERENCE, startEventResponse);
        wiremockFixtures.stubReturn404SubmitCaseEvent(CASE_REFERENCE);
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
