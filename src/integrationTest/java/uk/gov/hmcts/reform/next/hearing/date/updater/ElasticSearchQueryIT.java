package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.es.TestContainers;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.ElasticSearchQuery;
import uk.gov.hmcts.reform.next.hearing.date.updater.utils.CaseData;
import uk.gov.hmcts.reform.next.hearing.date.updater.utils.ElasticSearchIntegrationTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
@ComponentScan({"uk.gov.hmcts.reform.next.hearing.date.updater"})
@Slf4j
@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage",
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.AvoidDuplicateLiterals"})
@Disabled
class ElasticSearchQueryIT extends TestContainers {
    private static final String FT_NEXT_HEARING_DATE = "FT_NextHearingDate";

    @Autowired
    private ElasticSearchIntegrationTestUtils elasticSearchIntegrationTestUtils;

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    Map<String, List<CaseData>> caseTypesToCaseReferencesMap;

    @BeforeEach
    void setup() throws Exception {
        caseTypesToCaseReferencesMap =
            Map.of(FT_NEXT_HEARING_DATE, List.of(
                createCaseData("1658755384933059", null),
                createCaseData("1658830998852951", null),
                createCaseData("1658829366592785", LocalDateTime.now().plusDays(1)),
                createCaseData("8810901722389022", LocalDateTime.now().plusDays(1)),
                createCaseData("1658755615615681", LocalDateTime.now()),
                createCaseData("2092542372492013", LocalDateTime.now().minusDays(1)),
                createCaseData("9454757880038200", LocalDateTime.now().minusDays(1)),
                createCaseData("3716171233970241", LocalDateTime.now())
            ));

        elasticSearchIntegrationTestUtils.resetIndices(caseTypesToCaseReferencesMap.keySet());
        elasticSearchIntegrationTestUtils.createElasticSearchIndex(caseTypesToCaseReferencesMap);
    }

    @Test
    void testQueryReturnsHearingDatesInPast() throws Exception {
        ElasticSearchQuery query = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(10)
            .build();

        sendRequestAndAssertResponseContainsReference(query.getQuery(),
                                                      List.of("2092542372492013", "9454757880038200"));
    }

    @Test
    void testQueryReturnsSingleHearingDateInPastUsingSearchAfter() throws Exception {
        ElasticSearchQuery querySizeOfOne = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(1)
            .build();

        sendRequestAndAssertResponseContainsReference(querySizeOfOne.getQuery(), List.of("2092542372492013"));

        ElasticSearchQuery subsequentQuery = ElasticSearchQuery.builder()
            .initialSearch(false)
            .size(1)
            .searchAfterValue("2092542372492013")
            .build();

        sendRequestAndAssertResponseContainsReference(subsequentQuery.getQuery(), List.of("9454757880038200"));
    }

    private void sendRequestAndAssertResponseContainsReference(String query, List<String> caseReferences)
        throws Exception {
        Request request = new Request("GET", "_search");

        request.setJsonEntity(query);
        Response response = elasticsearchClient.getLowLevelClient().performRequest(request);

        String responseEntity = EntityUtils.toString(response.getEntity());

        List<String> referencesFromResponse = JsonPath.read(responseEntity, "$.hits.hits..reference");

        log.info("Expected {}, received{}", caseReferences, referencesFromResponse);

        assertTrue(referencesFromResponse.containsAll(caseReferences));
    }

    private CaseData createCaseData(String caseReference, LocalDateTime hearingDateTime) {

        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId(hearingDateTime == null ? null : UUID.randomUUID().toString())
            .caseReference(caseReference)
            .hearingDateTime(hearingDateTime)
            .build();

        return CaseData.builder()
            .reference(caseReference)
            .caseType(FT_NEXT_HEARING_DATE)
            .nextHearingDetails(Map.of("NextHearingDetails", nextHearingDetails))
            .build();
    }
}
