package uk.gov.hmcts.reform.next.hearing.date.updater.clients;

import org.springframework.cloud.openfeign.FeignClient;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.ElasticSearchClientConfig;

@FeignClient(
    name = "elastic-search",
    url = "${elasticsearch.host}",
    configuration = ElasticSearchClientConfig.class)
public interface ElasticSearchClient {
    void callEndpoint();
}
