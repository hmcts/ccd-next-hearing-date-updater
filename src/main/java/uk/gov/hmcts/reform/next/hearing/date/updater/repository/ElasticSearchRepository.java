package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

@Repository
public class ElasticSearchRepository {

    private final CoreCaseDataApi coreCaseDataApi;

    @Autowired
    public ElasticSearchRepository(CoreCaseDataApi coreCaseDataApi) {
        this.coreCaseDataApi = coreCaseDataApi;
    }

    public void findOutOfDateNextHearingDate() {
        coreCaseDataApi.searchCases("", "", "", "");
    }
}
