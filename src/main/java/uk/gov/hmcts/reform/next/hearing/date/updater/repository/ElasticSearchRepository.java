package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.next.hearing.date.updater.clients.ElasticSearchClient;

@Repository
public class ElasticSearchRepository {

    private final ElasticSearchClient elasticSearchClient;

    @Autowired
    public ElasticSearchRepository(ElasticSearchClient elasticSearchClient) {
        this.elasticSearchClient = elasticSearchClient;
    }

    public void findOutOfDateNextHearingDate() {
        elasticSearchClient.callEndpoint();
    }
}
