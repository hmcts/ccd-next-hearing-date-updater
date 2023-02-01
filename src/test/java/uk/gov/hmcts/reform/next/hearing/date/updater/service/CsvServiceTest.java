package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.CsvFileException;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.InvalidConfigurationError;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.MAX_CSV_ENTRIES_EXCEEDED_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_CSV_FILE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_REFERENCES_TO_VALIDATE;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.LinguisticNaming"})
class CsvServiceTest {

    private CsvService csvService;

    private static final String FILE_LOCATION = "fileLocation";

    private static final String TEST_RESOURCES = "src/test/resources/";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setup() {
        csvService = new CsvService();
        ReflectionTestUtils.setField(csvService, "maxNumCaseReferences", 10_000);

        Logger csvServiceLogger = (Logger) LoggerFactory.getLogger(CsvService.class);

        // create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();
        csvServiceLogger.addAppender(listAppender);
    }

    @Test
    void getCaseReferencesWhenCsvIsPresent() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "testCsv.csv");

        // WHEN
        List<String> caseReferences = csvService.getCaseReferences();

        // THEN
        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5, caseReferences.size());
    }

    @Test
    void getCaseReferencesReturnsEmptyCollectionWhenCsvConfigIsNotPresent() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, null);

        // WHEN
        List<String> caseReferences = csvService.getCaseReferences();

        // THEN
        assertNotNull(caseReferences);
        assertTrue(caseReferences.isEmpty());

        // verify the messages
        List<String> formattedMessages = extractTheFormattedErrorMessages();
        assertEquals(1, formattedMessages.size()); // NB: should just be one message when no CSV
        assertTrue(formattedMessages.contains(NO_CSV_FILE));
    }

    @Test
    void getCaseReferencesReturnsEmptyCollectionWhenCsvIsEmpty() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "testCsvEmpty.csv");

        // WHEN
        List<String> caseReferences = csvService.getCaseReferences();

        // THEN
        assertNotNull(caseReferences);
        assertTrue(caseReferences.isEmpty());

        // verify the messages
        assertTrue(extractTheFormattedErrorMessages().contains(NO_REFERENCES_TO_VALIDATE));
    }

    @Test
    void getCaseReferencesShouldErrorIfSizeLimitExceeded() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "testCsv.csv");
        final int maxNumCaseReferences = 3;
        ReflectionTestUtils.setField(csvService, "maxNumCaseReferences", maxNumCaseReferences);

        // WHEN / THEN
        InvalidConfigurationError invalidConfigurationError = assertThrows(
            InvalidConfigurationError.class,
            () -> csvService.getCaseReferences()
        );
        assertTrue(invalidConfigurationError.getCause() instanceof TooManyCsvRecordsException);

        // verify the messages
        assertTrue(extractTheFormattedErrorMessages()
                       .contains(format(MAX_CSV_ENTRIES_EXCEEDED_ERROR, maxNumCaseReferences)));
    }

    @Test
    void getCaseReferencesShouldRemoveAndLogInvalidCaseReferences() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION,
                                     TEST_RESOURCES + "testCsvContainingInvalidCaseRefs.csv");

        // WHEN
        List<String> caseReferences = csvService.getCaseReferences();

        // THEN

        // verify positive output
        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5 + 1, caseReferences.size());

        // extract the formatted error messages
        List<String> formattedMessages = extractTheFormattedErrorMessages();

        // verify number of errors
        // 6: non-numeric tests: i.e. invalidCaseRefX
        // 2: bad number format tests: i.e. bad luhn or wrong length
        // x 2: because each error generates 2 messages (1 detailed + 1 formatted for dynatrace)
        // + 1: summary
        assertEquals((6 + 2) * 2 + 1, formattedMessages.size());

        // validate the 6 for 'invalidCaseRef%s' were found
        boolean anyMatch = IntStream.rangeClosed(1, 6)
            .allMatch(i -> formattedMessages.contains(
                format("002 Invalid Case Reference number 'invalidCaseRef%s' in CSV", i)));
        assertTrue(anyMatch);

        // validate 2 extra luhn/length tests were found:
        // :: valid length but bad luhn check digit
        assertTrue(formattedMessages.contains("002 Invalid Case Reference number '1111222233334449' in CSV"));
        // :: valid luhn number but bad length (i.e. not 16 digit)
        assertTrue(formattedMessages.contains("002 Invalid Case Reference number '3162255313' in CSV"));
    }

    @Test
    void getCaseReferencesShouldErrorIfIoExceptionThrownWhenReadingCsvFile() {

        // GIVEN
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "doesNotExist.csv");

        // WHEN / THEN
        InvalidConfigurationError invalidConfigurationError = assertThrows(
            InvalidConfigurationError.class,
            () -> csvService.getCaseReferences()
        );

        assertTrue(invalidConfigurationError.getCause() instanceof CsvFileException);
        assertTrue(invalidConfigurationError.getCause().getCause() instanceof IOException);
    }

    private List<String> extractTheFormattedErrorMessages() {
        List<ILoggingEvent> logsList = listAppender.list;
        return logsList.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
    }

}
