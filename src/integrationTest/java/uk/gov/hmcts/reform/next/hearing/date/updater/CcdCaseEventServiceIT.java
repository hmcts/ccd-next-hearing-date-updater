package uk.gov.hmcts.reform.next.hearing.date.updater;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.CcdCaseEventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.SUBMIT_CASE_EVENT_URL;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.TRIGGER_START_EVENT_URL;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.EVENT_ID;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.NEXT_HEARING_DETAILS_FIELD_NAME;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.HEARING_DATE_TIME_IN_PAST;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_DATE_TIME_LOG_MESSAGE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_ID_MESSAGE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.START_EVENT_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository.SUBMIT_EVENT_ERROR;

@SpringBootTest()
@ActiveProfiles("itest")
@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.TooManyMethods"})
class CcdCaseEventServiceIT extends WireMockBootstrap {

    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(10);
    private static final LocalDateTime PAST_DATE = LocalDateTime.now().minusDays(10);

    @Autowired
    private CcdCaseEventService underTest;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    private static final String CASE_REFERENCE = "1658830998852951";

    private static final String HEARING_ID = "12345";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        wiremockFixtures.resetRequests();
        wiremockFixtures.stubIdam();

        listAppender = new ListAppender<>();
        listAppender.start();
        appendLoggerFor(CcdCaseEventRepository.class);
    }

    private void appendLoggerFor(Class<?> clazz) {
        Logger logger = (Logger) LoggerFactory.getLogger(clazz);
        logger.addAppender(listAppender);
    }

    @Test // HMAN-322 AC01
    void createCaseEventsWhenStartEventResponseHasHearingDateInFuture() {

        // GIVEN
        NextHearingDetails nextHearingDetails = createNextHearingDetails(HEARING_ID, FUTURE_DATE);
        stubReturn200TriggerStartEvent(nextHearingDetails);
        wiremockFixtures.stubReturn200SubmitCaseEvent(CASE_REFERENCE);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        wiremockFixtures.verifyGetRequest(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID));
        wiremockFixtures.verifyPostRequest(String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE));

        assertTrue(getLogs().isEmpty());
    }

    @Test // HMAN-322 AC03
    void createCaseEventsWhenStartEventResponseHasHearingDateInPast() {

        // GIVEN
        appendLoggerFor(NextHearingDetails.class);

        NextHearingDetails nextHearingDetails = createNextHearingDetails(HEARING_ID, PAST_DATE);
        stubReturn200TriggerStartEvent(nextHearingDetails);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        wiremockFixtures.verifyGetRequest(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID));

        // this endpoint will never be called if the NextHearingDate date is not valid - i.e. in the past
        wiremockFixtures.verifyNoPostRequest(String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE));

        String formattedLog = HEARING_DATE_TIME_IN_PAST.replace("{}", CASE_REFERENCE);
        assertLogMessages(List.of(formattedLog));
    }

    // HMAN-322 AC04
    @ParameterizedTest(
        name = "createCaseEventsWhenStartEventResponseHasHearingDateIsNullEmptyOrMissing: {0}"
    )
    @MethodSource("nextHearingDateObjectWithHearingDateNullEmptyOrMissingParams")
    void createCaseEventsWhenStartEventResponseHasHearingDateIsNullEmptyOrMissing(Object nextHearingDetails) {

        // GIVEN
        appendLoggerFor(NextHearingDetails.class);
        stubReturn200TriggerStartEvent(nextHearingDetails);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        wiremockFixtures.verifyGetRequest(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID));

        // this endpoint will never be called if the NextHearingDate date is not valid - i.e. null
        wiremockFixtures.verifyNoPostRequest(String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE));

        String formattedLog = NULL_HEARING_DATE_TIME_LOG_MESSAGE.replace("{}", CASE_REFERENCE);
        assertLogMessages(List.of(formattedLog));
    }

    // HMAN-322 AC05
    @ParameterizedTest(
        name = "createCaseEventsWhenStartEventResponseHasHearingIdIsNullEmptyOrMissing: {0}"
    )
    @MethodSource("nextHearingDateObjectWithHearingIdNullEmptyOrMissingParams")
    void createCaseEventsWhenStartEventResponseHasHearingIdIsNullEmptyOrMissing(Object nextHearingDetails) {

        // GIVEN
        appendLoggerFor(NextHearingDetails.class);
        stubReturn200TriggerStartEvent(nextHearingDetails);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        wiremockFixtures.verifyGetRequest(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID));

        // this endpoint will never be called if the NextHearingDate ID is not valid i.e. null
        wiremockFixtures.verifyNoPostRequest(String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE));

        String formattedLog = NULL_HEARING_ID_MESSAGE.replace("{}", CASE_REFERENCE);
        assertLogMessages(List.of(formattedLog));
    }

    // HMAN-322 AC06
    @ParameterizedTest(
        name = "createCaseEventsWhenStartEventResponseHasBothHearingDateAndIdNullEmptyOrMissing: {0}"
    )
    @MethodSource("nextHearingDateObjectWithBothHearingDateAndIdNullEmptyOrMissingParams")
    void createCaseEventsWhenStartEventResponseHasBothHearingDateAndIdNullEmptyOrMissing(Object nextHearingDetails) {

        // GIVEN
        stubReturn200TriggerStartEvent(nextHearingDetails);
        wiremockFixtures.stubReturn200SubmitCaseEvent(CASE_REFERENCE);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        wiremockFixtures.verifyGetRequest(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID));
        // NB: when both date and ID are null proceed with case event submission
        wiremockFixtures.verifyPostRequest(String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE));

        assertTrue(getLogs().isEmpty());
    }

    @Test // HMAN-322 AC02
    void errorsLoggedWhenStartEventFails() {

        // GIVEN
        wiremockFixtures.stubReturn404TriggerStartEvent(CASE_REFERENCE);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        String formattedLog = String.format(START_EVENT_ERROR, CASE_REFERENCE, EVENT_ID);
        assertLogMessages(List.of(formattedLog));
    }

    @Test // HMAN-322 AC07
    void errorsLoggedWhenCreateEventFails() {

        // GIVEN
        NextHearingDetails nextHearingDetails = createNextHearingDetails(HEARING_ID, FUTURE_DATE);
        stubReturn200TriggerStartEvent(nextHearingDetails);
        wiremockFixtures.stubReturn404SubmitCaseEvent(CASE_REFERENCE);

        // WHEN
        underTest.createCaseEvents(List.of(CASE_REFERENCE));

        // THEN
        String formattedLog = String.format(SUBMIT_EVENT_ERROR, CASE_REFERENCE);
        assertLogMessages(List.of(formattedLog));
    }

    private static NextHearingDetails createNextHearingDetails(String hearingID, LocalDateTime hearingDateTime) {
        return NextHearingDetails.builder()
            .hearingID(hearingID)
            .caseReference(CASE_REFERENCE)
            .hearingDateTime(hearingDateTime)
            .build();
    }

    private void stubReturn200TriggerStartEvent(Object nextHearingDetails) {

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

    @SuppressWarnings("unused")
    private static Stream<Arguments> nextHearingDateObjectWithBothHearingDateAndIdNullEmptyOrMissingParams() {
        return Stream.of(
            // NB: params correspond to:
            // * nextHearingDetails :: mock response for wiremockFixtures.stubReturn200TriggerStartEvent(...)
            Arguments.of(createNextHearingDetails(null, null)),
            Arguments.of(createNextHearingDetails("", null)),
            Arguments.of(Map.of(NextHearingDetails.HEARING_ID, "")),
            Arguments.of(Map.of(NextHearingDetails.HEARING_DATE_TIME, "")),
            Arguments.of(Map.of()) // i.e. both HEARING_ID & HEARING_DATE_TIME are missing
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> nextHearingDateObjectWithHearingDateNullEmptyOrMissingParams() {
        return Stream.of(
            // NB: params correspond to:
            // * nextHearingDetails :: mock response for wiremockFixtures.stubReturn200TriggerStartEvent(...)
            Arguments.of(createNextHearingDetails(HEARING_ID, null)),
            Arguments.of(Map.of(NextHearingDetails.HEARING_ID, HEARING_ID,
                                NextHearingDetails.HEARING_DATE_TIME, "")),
            Arguments.of(Map.of(NextHearingDetails.HEARING_ID, HEARING_ID)) // i.e. HEARING_DATE_TIME is missing
        );
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> nextHearingDateObjectWithHearingIdNullEmptyOrMissingParams() {
        return Stream.of(
            // NB: params correspond to:
            // * nextHearingDetails :: mock response for wiremockFixtures.stubReturn200TriggerStartEvent(...)
            Arguments.of(createNextHearingDetails(null, FUTURE_DATE)),
            Arguments.of(createNextHearingDetails("", FUTURE_DATE)),
            Arguments.of(Map.of(NextHearingDetails.HEARING_DATE_TIME, FUTURE_DATE)) // i.e. HEARING_ID is missing
        );
    }
}
