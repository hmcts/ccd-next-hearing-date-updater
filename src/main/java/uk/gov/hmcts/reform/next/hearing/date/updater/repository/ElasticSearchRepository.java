package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.next.hearing.date.updater.security.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class ElasticSearchRepository {

    private final CoreCaseDataApi coreCaseDataApi;

    private final SecurityUtils securityUtils;

    @Value("${next-hearing-date-updater.elasticsearch.querySize}")
    private int querySize;

    @Autowired
    public ElasticSearchRepository(CoreCaseDataApi coreCaseDataApi, SecurityUtils securityUtils) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.securityUtils = securityUtils;
    }

    public List<CaseDetails> findCasesWithOutOfDateNextHearingDate(String caseType) {
        ElasticSearchQuery elasticSearchQuery = ElasticSearchQuery.builder()
            .initialSearch(true)
            .size(querySize)
            .build();

        log.info("Processing the Next-Hearing-Date-Updater search for case type {}.",caseType);

        SearchResult searchResult = coreCaseDataApi.searchCases(securityUtils.getNextHearingDateAdminAccessToken(),
                                                                securityUtils.getS2SToken(),
                                                                caseType, elasticSearchQuery.getQuery());

        List<CaseDetails> caseDetails = new ArrayList<>();
        if (searchResult.getTotal() > 0) {
            List<CaseDetails> searchResultCases = searchResult.getCases();
            caseDetails.addAll(searchResultCases);

            String searchAfterValue = searchResultCases.get(searchResultCases.size() - 1).getId().toString();

            boolean keepSearching;
            do {

                ElasticSearchQuery subsequentElasticSearchQuery = ElasticSearchQuery.builder()
                    .initialSearch(false)
                    .size(querySize)
                    .searchAfterValue(searchAfterValue)
                    .build();

                SearchResult subsequentSearchResult =
                    coreCaseDataApi.searchCases(securityUtils.getNextHearingDateAdminAccessToken(),
                                                securityUtils.getS2SToken(),
                                                caseType, subsequentElasticSearchQuery.getQuery()
                    );

                caseDetails.addAll(subsequentSearchResult.getCases());
                keepSearching = !subsequentSearchResult.getCases().isEmpty();
                if (keepSearching) {
                    searchAfterValue = subsequentSearchResult.getCases()
                        .get(subsequentSearchResult.getCases().size() - 1)
                        .getId().toString();
                }

            } while (keepSearching);
        }
        log.info("The Next-Hearing-Date-Updater has processed caseDetails {}.",caseDetails.size());
        return caseDetails;
    }
}
