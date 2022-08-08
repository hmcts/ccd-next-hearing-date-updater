package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class ElasticSearchRepositoryTest {
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ElasticSearchRepository elasticSearchRepository;

    private static final String ADMIN_TOKEN = "adminToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final String CASE_TYPE = "CaseType";
    private static final int QUERY_SIZE = 3;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(elasticSearchRepository, "querySize", QUERY_SIZE);
        when(securityUtils.getNextHearingDateAdminAccessToken()).thenReturn(ADMIN_TOKEN);
        when(securityUtils.getS2SToken()).thenReturn(S2S_TOKEN);
    }

    @Test
    void testFindCasesWithOutOfDateNextHearingDate() {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(QUERY_SIZE)
            .build();
        SearchResult searchResult = SearchResult.builder()
            .cases(createCaseDetails(1L, 3L))
            .total(3)
            .build();
        when(coreCaseDataApi.searchCases(ADMIN_TOKEN, S2S_TOKEN, CASE_TYPE, elasticSearchQuery.getQuery()))
            .thenReturn(searchResult);

        ElasticSearchQuery elasticSearchSubsequentQuery = ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(QUERY_SIZE)
            .searchAfterValue("3")
            .build();
        SearchResult subsequentSearchResult = SearchResult.builder()
            .cases(createCaseDetails(4L, 5L))
            .total(2)
            .build();
        when(coreCaseDataApi.searchCases(ADMIN_TOKEN, S2S_TOKEN, CASE_TYPE, elasticSearchSubsequentQuery.getQuery()))
            .thenReturn(subsequentSearchResult);

        ElasticSearchQuery elasticSearchSecondSubsequentQuery = ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(QUERY_SIZE)
            .searchAfterValue("5")
            .build();
        SearchResult secondSubsequentSearchResult = SearchResult.builder()
            .cases(Collections.emptyList())
            .total(0)
            .build();
        when(coreCaseDataApi.searchCases(ADMIN_TOKEN, S2S_TOKEN, CASE_TYPE,
                                         elasticSearchSecondSubsequentQuery.getQuery()))
            .thenReturn(secondSubsequentSearchResult);

        List<CaseDetails> casesWithOutOfDateNextHearingDate =
            elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(CASE_TYPE);

        assertEquals(5, casesWithOutOfDateNextHearingDate.size());
    }

    @Test
    void testFindCasesWithOutOfDateNextHearingDateNoResultsFound() {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(QUERY_SIZE)
            .build();
        SearchResult searchResult = SearchResult.builder()
            .cases(Collections.emptyList())
            .total(0)
            .build();
        when(coreCaseDataApi.searchCases(ADMIN_TOKEN, S2S_TOKEN, CASE_TYPE, elasticSearchQuery.getQuery()))
            .thenReturn(searchResult);

        List<CaseDetails> casesWithOutOfDateNextHearingDate =
            elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(CASE_TYPE);

        assertTrue(casesWithOutOfDateNextHearingDate.isEmpty());
    }

    private List<CaseDetails> createCaseDetails(long startIndex, long endIndex) {
        return LongStream.rangeClosed(startIndex, endIndex)
            .mapToObj(longValue ->
                          CaseDetails.builder()
                              .caseTypeId(CASE_TYPE)
                              .id(longValue)
                              .build()
            ).toList();
    }
}
