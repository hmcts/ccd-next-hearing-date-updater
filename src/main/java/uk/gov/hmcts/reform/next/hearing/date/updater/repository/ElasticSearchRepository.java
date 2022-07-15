package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.next.hearing.date.updater.clients.ElasticSearchClient;

public class ElasticSearchRepository {

    @Autowired
    private ElasticSearchClient elasticSearchClient;

    public void findOutOfDateNextHearingDate() {
        elasticSearchClient.callEndpoint();
    }
}
