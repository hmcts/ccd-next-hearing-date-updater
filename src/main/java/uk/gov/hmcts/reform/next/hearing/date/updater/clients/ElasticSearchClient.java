package uk.gov.hmcts.reform.next.hearing.date.updater.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.ElasticSearchClientConfig;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.DatastoreClientConfig.GET_EVENT_TRIGGER_BY_EVENT_ID;

@FeignClient(
    name = "elastic-search",
    url = "${next-hearing-date-updater.elasticsearch.host}",
    configuration = ElasticSearchClientConfig.class)
public interface ElasticSearchClient {
    @GetMapping(value = GET_EVENT_TRIGGER_BY_EVENT_ID, consumes = APPLICATION_JSON_VALUE)
    void callEndpoint();
}
