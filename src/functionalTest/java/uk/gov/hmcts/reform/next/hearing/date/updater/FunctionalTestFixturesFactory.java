package uk.gov.hmcts.reform.next.hearing.date.updater;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.util.ElasticSearchOperations;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.util.ParameterResolver;

public enum FunctionalTestFixturesFactory {
    BEAN_FACTORY;

    private final ElasticSearchOperations elasticSearchOperations;

    private BackEndFunctionalTestScenarioContext scenarioContext;

    FunctionalTestFixturesFactory() {
        final HttpHost httpHost = new HttpHost(ParameterResolver.getElasticsearchHost(),
                                               ParameterResolver.getElasticsearchPort());
        System.out.println("-----------------------------------------------------------------");
        System.out.println(httpHost.toURI());
        System.out.println("-----------------------------------------------------------------");
        final RestClient restClient = RestClient
            .builder(httpHost)
            .build();
        final ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        final ElasticsearchClient elasticsearchClient = new ElasticsearchClient(transport);

        elasticSearchOperations = new ElasticSearchOperations(elasticsearchClient);
    }

    public ElasticSearchOperations getElasticSearchOperations() {
        return elasticSearchOperations;
    }

    public BackEndFunctionalTestScenarioContext getScenarioContext() {
        return scenarioContext;
    }

    public void setScenarioContext(BackEndFunctionalTestScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }
}
