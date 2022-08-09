package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.InvalidConfigurationError;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.fail;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@ExtendWith({MockitoExtension.class})
class NextHearingDateUpdaterServiceTest {

    @Mock
    private CsvService csvService;

    @Mock
    private ElasticSearchService elasticSearchService;

    @Mock
    private CcdCaseEventService ccdCaseEventService;

    @Mock
    private CcdCaseEventRepository ccdCaseEventRepository;

    @InjectMocks
    private NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @Test
    void executeCaseReferencesFromCsv() {
        List<String> caseReferences = List.of("123", "456");
        when(csvService.getCaseReferences()).thenReturn(caseReferences);
        when(elasticSearchService.findOutOfDateCaseReferences()).thenReturn(Collections.emptyList());
        nextHearingDateUpdaterService.execute();

        verify(ccdCaseEventService).createCaseEvents(caseReferences);
    }

    @Test
    void executeCaseReferencesFromCsvMaxCaseReferencesExceeded() {
        when(csvService.getCaseReferences())
            .thenThrow(new InvalidConfigurationError(ErrorMessages.CSV_FILE_READ_ERROR,
                                                     new TooManyCsvRecordsException(2)));

        try {
            nextHearingDateUpdaterService.execute();
            fail("Exception not thrown");
        } catch (Exception exception) {
            assertNotNull(exception);
        }
        verify(ccdCaseEventService, never()).createCaseEvents(any());
    }

    @Test
    void executeCaseReferencesFromCaseType() {
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());
        List<String> caseRefs = List.of("123");
        when(elasticSearchService.findOutOfDateCaseReferences()).thenReturn(caseRefs);
        nextHearingDateUpdaterService.execute();

        verify(elasticSearchService).findOutOfDateCaseReferences();
        verify(ccdCaseEventService).createCaseEvents(caseRefs);
    }

    @Test
    void executeThrowsErrorIfCaseRefsFoundFromCsvAndCaseTypes() {
        List<String> caseRefs = List.of("123");
        when(csvService.getCaseReferences()).thenReturn(caseRefs);
        when(elasticSearchService.findOutOfDateCaseReferences()).thenReturn(caseRefs);

        assertThrows(InvalidConfigurationError.class, () -> nextHearingDateUpdaterService.execute());

        verify(ccdCaseEventRepository, never()).createCaseEvent(any());
    }

    @Test
    void executeDoesNotProcessIfNoCaseReferencesExist() {
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());
        when(elasticSearchService.findOutOfDateCaseReferences()).thenReturn(Collections.emptyList());

        nextHearingDateUpdaterService.execute();

        verify(ccdCaseEventRepository, never()).createCaseEvent(any());
    }
}
