package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@ExtendWith({MockitoExtension.class})
class NextHearingDateUpdaterServiceTest {

    @Mock
    private CsvService csvService;

    @Mock
    private ElasticSearchService elasticSearchService;

    @Mock
    private CallBackService callBackService;

    @InjectMocks
    private NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void executeCaseReferencesFromCsv() throws TooManyCsvRecordsException {
        List<String> caseReferences = List.of("123", "456");
        when(csvService.getCaseReferences()).thenReturn(caseReferences);

        nextHearingDateUpdaterService.execute();

        verify(csvService, never()).getCaseTypes();
        verify(elasticSearchService, never()).findOutOfDateCaseReferencesByCaseType(any());
        verify(callBackService).performCallbacks(caseReferences);
    }

    @Test
    void executeCaseReferencesFromCsvMaxCaseReferencesExceeded() throws Exception {
        Logger csvServiceLogger = (Logger) LoggerFactory.getLogger(NextHearingDateUpdaterService.class);

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        csvServiceLogger.addAppender(listAppender);

        when(csvService.getCaseReferences()).thenAnswer(invocation -> {
            throw new TooManyCsvRecordsException();
        });
        nextHearingDateUpdaterService.execute();
        verify(callBackService, never()).performCallbacks(any());

        List<ILoggingEvent> logsList = listAppender.list;

        assertTrue(logsList.get(0).getFormattedMessage().contains("Failed to get case references"));
    }

    @Test
    void executeCaseReferencesFromCaseTypeCsv() throws Exception {
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());

        nextHearingDateUpdaterService.execute();

        verify(csvService).getCaseTypes();
        verify(elasticSearchService).findOutOfDateCaseReferencesByCaseType(any());
        verify(callBackService).performCallbacks(any());
    }
}
