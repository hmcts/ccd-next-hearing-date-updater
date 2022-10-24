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

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.LinguisticNaming"})
class CsvServiceTest {

    private CsvService csvService;

    private static final String FILE_LOCATION = "fileLocation";

    private static final String TEST_RESOURCES = "src/test/resources/";

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setup() {
        csvService = new CsvService();
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "testCsv.csv");
        ReflectionTestUtils.setField(csvService, "maxNumCaseReferences", 10_000);

        Logger csvServiceLogger = (Logger) LoggerFactory.getLogger(CsvService.class);

        // create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();
        csvServiceLogger.addAppender(listAppender);
    }

    @Test
    void getCaseReferencesWhenCsvIsPresent() {
        List<String> caseReferences = csvService.getCaseReferences();
        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5, caseReferences.size());
    }

    @Test
    void getCaseReferencesReturnsEmptyCollectionWhenCsvIsNotPresent() {
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, null);
        List<String> caseReferences = csvService.getCaseReferences();
        assertNotNull(caseReferences);
        assertTrue(caseReferences.isEmpty());
    }

    @Test
    void getCaseReferencesShouldErrorIfSizeLimitExceeded() {
        final int maxNumCaseReferences = 3;
        ReflectionTestUtils.setField(csvService, "maxNumCaseReferences", maxNumCaseReferences);

        assertThrows(InvalidConfigurationError.class, () -> csvService.getCaseReferences());
    }

    @Test
    void getCaseReferencesShouldRemoveAndLogInvalidCaseReferences() {

        ReflectionTestUtils.setField(csvService, FILE_LOCATION,
                                     TEST_RESOURCES + "testCsvContainingInvalidCaseRefs.csv");

        List<String> caseReferences = csvService.getCaseReferences();

        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5, caseReferences.size());

        List<ILoggingEvent> logsList = listAppender.list;

        List<String> formattedMessages = logsList.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
        assertEquals(7, formattedMessages.size());

        boolean anyMatch = IntStream.rangeClosed(1, 6)
            .allMatch(i -> formattedMessages.contains(
                format("002 Invalid Case Reference number 'invalidCaseRef%s' in CSV", i)));

        assertTrue(anyMatch);
    }

    @Test
    void getCaseReferencesShouldErrorIfIoExceptionThrownWhenReadingCsvFile() {
        ReflectionTestUtils.setField(csvService, FILE_LOCATION, TEST_RESOURCES + "doesnotexist.csv");

        InvalidConfigurationError invalidConfigurationError = assertThrows(
            InvalidConfigurationError.class,
            () -> csvService.getCaseReferences()
        );

        assertTrue(invalidConfigurationError.getCause() instanceof CsvFileException);
        assertTrue(invalidConfigurationError.getCause().getCause() instanceof IOException);
    }
}
