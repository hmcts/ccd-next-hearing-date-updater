package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
        when(elasticSearchService.findOutOfDateCaseReferencesByCaseType()).thenReturn(Collections.emptyList());

        nextHearingDateUpdaterService.execute();

        verify(ccdCaseEventService).createCaseEvents(caseReferences);
    }

    @Test
    void executeCaseReferencesFromCsvMaxCaseReferencesExceeded() {
        // GIVEN
        when(csvService.getCaseReferences())
            .thenThrow(new InvalidConfigurationError(ErrorMessages.CSV_FILE_READ_ERROR,
                                                     new TooManyCsvRecordsException(2)));

        // WHEN
        try {
            nextHearingDateUpdaterService.execute();
            fail("Exception not thrown");
        } catch (Exception exception) {
            assertNotNull(exception);
        }

        // THEN
        verify(ccdCaseEventService, never()).createCaseEvents(any());
    }

    @Test
    void executeCaseReferencesFromCaseType() {
        // GIVEN
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());
        List<String> caseRefs = List.of("123");
        when(elasticSearchService.findOutOfDateCaseReferencesByCaseType()).thenReturn(caseRefs);

        // WHEN
        nextHearingDateUpdaterService.execute();

        // THEN
        verify(elasticSearchService).findOutOfDateCaseReferencesByCaseType();
        verify(ccdCaseEventService).createCaseEvents(caseRefs);
    }

    @Test
    void executeThrowsErrorIfCaseRefsFoundFromCsvAndCaseTypes() {
        // GIVEN
        List<String> caseRefs = List.of("123");
        when(csvService.getCaseReferences()).thenReturn(caseRefs);
        when(elasticSearchService.findOutOfDateCaseReferencesByCaseType()).thenReturn(caseRefs);

        // WHEN
        assertThrows(InvalidConfigurationError.class, () -> nextHearingDateUpdaterService.execute());

        // THEN
        verify(ccdCaseEventRepository, never()).createCaseEvent(any());
    }

    @Test
    void executeDoesNotProcessIfNoCaseReferencesExist() {
        // GIVEN
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());
        when(elasticSearchService.findOutOfDateCaseReferencesByCaseType()).thenReturn(Collections.emptyList());

        // WHEN
        nextHearingDateUpdaterService.execute();

        // THEN
        verify(ccdCaseEventRepository, never()).createCaseEvent(any());
    }

    @Test
    void executeCaseReferencesFromElasticSearchQueryNoCaseTypesPresent() {
        // GIVEN
        ReflectionTestUtils.setField(elasticSearchService, "caseTypes", Collections.emptyList());
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());

        // WHEN
        nextHearingDateUpdaterService.execute();

        // THEN
        verify(ccdCaseEventService, never()).createCaseEvents(any());
    }

    @Test
    void executeCaseReferencesFromElasticSearchQueryCaseTypesPresent() {
        // GIVEN
        when(csvService.getCaseReferences()).thenReturn(Collections.emptyList());

        List<String> caseReferences = List.of("caseRef1", "caseRef2", "caseRef3", "caseRef4", "caseRef5", "caseRef6");
        when(elasticSearchService.findOutOfDateCaseReferencesByCaseType()).thenReturn(caseReferences);

        // WHEN
        nextHearingDateUpdaterService.execute();

        // THEN
        verify(elasticSearchService).findOutOfDateCaseReferencesByCaseType();
        verify(ccdCaseEventService).createCaseEvents(caseReferences);
    }
}
