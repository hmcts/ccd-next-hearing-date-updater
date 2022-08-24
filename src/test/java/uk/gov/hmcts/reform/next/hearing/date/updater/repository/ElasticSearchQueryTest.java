package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class ElasticSearchQueryTest {

    @Test
    void testGetInitialQuery() {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(50)
            .build();

        String query = elasticSearchQuery.getQuery();

        assertTrue(query.contains("\"size\": 50"));
        assertFalse(query.contains("\"search_after\""));
    }

    @Test
    void testGetSubsequentQuery() {
        final String caseReference = "1111222233334444";

        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(50)
            .searchAfterValue(caseReference)
            .build();

        String query = elasticSearchQuery.getQuery();

        assertTrue(query.contains("\"size\": 50"));
        assertTrue(query.contains("\"search_after\": [" + caseReference + "]"));
    }
}
