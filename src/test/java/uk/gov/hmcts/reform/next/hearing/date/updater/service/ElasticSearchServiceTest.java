package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.ElasticSearchRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class ElasticSearchServiceTest {

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @InjectMocks
    private ElasticSearchService elasticSearchService;

    private static final String CASE_TYPE_1 = "FT_TestCaseType1";
    private static final String CASE_TYPE_2 = "FT_TestCaseType2";

    @Test
    void testFindOutOfDateCaseReferencesByCaseTypeForSingleCaseType() {
        ReflectionTestUtils.setField(elasticSearchService, "caseTypes", List.of(CASE_TYPE_1));
        List<CaseDetails> caseDetails = createCaseDetails(CASE_TYPE_1, 5);
        when(elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(CASE_TYPE_1))
            .thenReturn(caseDetails);

        List<String> outOfDateCaseReferencesByCaseType =
            elasticSearchService.findOutOfDateCaseReferencesByCaseType();

        assertNotNull(outOfDateCaseReferencesByCaseType);
        assertEquals(5, outOfDateCaseReferencesByCaseType.size());
        assertEquals(
            caseDetails.stream().map(returnedCaseDetails -> returnedCaseDetails.getId().toString()).toList(),
            outOfDateCaseReferencesByCaseType);
    }

    @Test
    void testFindOutOfDateCaseReferencesByCaseTypeForMultipleCaseTypes() {
        ReflectionTestUtils.setField(elasticSearchService, "caseTypes", List.of(CASE_TYPE_1, CASE_TYPE_2));
        List<CaseDetails> caseType1CaseDetails = createCaseDetails(CASE_TYPE_1, 5);
        when(elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(CASE_TYPE_1))
            .thenReturn(caseType1CaseDetails);

        List<CaseDetails> caseType2CaseDetails = createCaseDetails(CASE_TYPE_2, 5);
        when(elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(CASE_TYPE_2))
            .thenReturn(caseType2CaseDetails);

        List<String> outOfDateCaseReferencesByCaseType =
            elasticSearchService.findOutOfDateCaseReferencesByCaseType();

        List<CaseDetails> expectedCaseDetails = new ArrayList<>();
        expectedCaseDetails.addAll(caseType1CaseDetails);
        expectedCaseDetails.addAll(caseType2CaseDetails);

        assertNotNull(outOfDateCaseReferencesByCaseType);
        assertEquals(caseType1CaseDetails.size() + caseType2CaseDetails.size(),
                     outOfDateCaseReferencesByCaseType.size());

        assertEquals(expectedCaseDetails.stream().map(caseDetails -> caseDetails.getId().toString()).toList(),
            outOfDateCaseReferencesByCaseType);
    }

    private List<CaseDetails> createCaseDetails(String caseTypeId, int requiredNumInstances) {
        return IntStream.range(0, requiredNumInstances)
            .mapToObj(x -> CaseDetails.builder()
                .caseTypeId(caseTypeId)
                .id(new Random().nextLong())
                .build())
            .toList();
    }

}
