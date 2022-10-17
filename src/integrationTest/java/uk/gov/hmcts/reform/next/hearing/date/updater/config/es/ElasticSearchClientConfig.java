package uk.gov.hmcts.reform.next.hearing.date.updater.config.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class ElasticSearchClientConfig {
    private RestHighLevelClient restHighLevelClient;

    @Value("#{'${elasticsearch.hosts}'.split(',')}")
    private List<String> elasticsearchHosts;

    @PostConstruct
    public void init() {
        final HttpHost[] httpHosts = getElasticsearchHosts().stream()
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);
        final RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        restHighLevelClient = new RestHighLevelClient(restClientBuilder);
    }

    private List<String> getElasticsearchHosts() {
        return elasticsearchHosts.stream()
            .map(quotedHost -> quotedHost.replace("\"", "").strip()).toList();
    }

    @PreDestroy
    public void cleanUp() throws IOException {
        restHighLevelClient.close();
    }

    @Bean
    public RestHighLevelClient provideRestHighLevelClient() {
        return restHighLevelClient;
    }

}
