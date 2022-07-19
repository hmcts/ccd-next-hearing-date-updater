package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;

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

    private static final String FILE_NAME = "fileName";

    @BeforeEach
    public void setup() {
        csvService = new CsvService();
        ReflectionTestUtils.setField(csvService, "fileLocation", "src/test/resources");
        ReflectionTestUtils.setField(csvService, FILE_NAME, "testCsv.csv");
    }

    @Test
    void getCaseReferencesWhenCsvIsPresent() throws Throwable {
        List<String> caseReferences = csvService.getCaseReferences();
        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5, caseReferences.size());
    }

    @Test
    void getCaseReferencesReturnsEmptyCollectionWhenCsvIsNotPresent() throws Throwable {
        ReflectionTestUtils.setField(csvService, FILE_NAME, null);
        List<String> caseReferences = csvService.getCaseReferences();
        assertNotNull(caseReferences);
        assertTrue(caseReferences.isEmpty());
    }

    @Test
    void getCaseReferencesShouldErrorIfSizeLimitExceeded() {
        ReflectionTestUtils.setField(csvService, FILE_NAME, "tenthousandandonelines.csv");

        TooManyCsvRecordsException tooManyCsvRecordsException = assertThrows(
            TooManyCsvRecordsException.class,
            () -> csvService.getCaseReferences()
        );

        assertEquals(TooManyCsvRecordsException.ERROR_MESSAGE, tooManyCsvRecordsException.getMessage());
    }

    @Test
    void getCaseReferencesShouldRemoveAndLogInvalidCaseReferences() throws TooManyCsvRecordsException {

        Logger csvServiceLogger = (Logger) LoggerFactory.getLogger(CsvService.class);

        // create and start a ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        csvServiceLogger.addAppender(listAppender);

        ReflectionTestUtils.setField(csvService, FILE_NAME, "testCsvContainingInvalidCaseRefs.csv");

        List<String> caseReferences = csvService.getCaseReferences();

        assertNotNull(caseReferences);
        assertFalse(caseReferences.isEmpty());
        assertEquals(5, caseReferences.size());

        List<ILoggingEvent> logsList = listAppender.list;

        List<String> formattedMessages = logsList.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
        assertEquals(6, formattedMessages.size());

        boolean anyMatch = IntStream.rangeClosed(1, 6)
            .allMatch(i -> formattedMessages.contains(
                format("002 Invalid Case Reference number 'invalidCaseRef%s' in CSV", i)));

        assertTrue(anyMatch);
    }
}
