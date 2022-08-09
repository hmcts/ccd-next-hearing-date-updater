package uk.gov.hmcts.reform.next.hearing.date.updater.data;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.HEARING_DATE_TIME_IN_PAST;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_DATE_TIME_LOG_MESSAGE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_ID_MESSAGE;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class NextHearingDetailsTest {

    private static final String CASE_REFERENCE = "1111222233334444";
    private static final String HEARING_ID = "hearingId";

    private static final String LOG_FORMAT_DELIMITER = "{}";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        Logger nextHearingDetailsLogger = (Logger) LoggerFactory.getLogger(NextHearingDetails.class);

        listAppender = new ListAppender<>();
        listAppender.start();

        nextHearingDetailsLogger.addAppender(listAppender);
    }

    @Test
    void testValidateValuesNullHearingIdAndNullHearingDate() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder().caseReference(CASE_REFERENCE).build();
        assertTrue(nextHearingDetails.isValid());

        assertTrue(getLogs().isEmpty());
    }

    @Test
    void testValidateValuesNullHearingDateAndNonNullHearingId() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .caseReference(CASE_REFERENCE)
            .hearingId(HEARING_ID)
            .build();
        assertFalse(nextHearingDetails.isValid());

        String formattedLog = getFormattedLogMessage(NULL_HEARING_DATE_TIME_LOG_MESSAGE);

        assertLogMessages(List.of(formattedLog));
    }

    private String getFormattedLogMessage(String message) {
        return message.replace(LOG_FORMAT_DELIMITER, CASE_REFERENCE);
    }

    @Test
    void testValidateValuesNonNullHearingDateNullHearingId() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .caseReference(CASE_REFERENCE)
            .hearingDateTime(LocalDateTime.now().plusHours(1))
            .build();
        assertFalse(nextHearingDetails.isValid());

        String formattedLog = getFormattedLogMessage(NULL_HEARING_ID_MESSAGE);

        assertLogMessages(List.of(formattedLog));
    }

    @Test
    void testValidateValuesHearingDateInPast() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .caseReference(CASE_REFERENCE)
            .hearingId(HEARING_ID)
            .hearingDateTime(LocalDateTime.now().minusDays(1))
            .build();
        assertFalse(nextHearingDetails.isValid());

        String hearingDateInPast = getFormattedLogMessage(HEARING_DATE_TIME_IN_PAST);

        assertLogMessages(List.of(hearingDateInPast));
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
