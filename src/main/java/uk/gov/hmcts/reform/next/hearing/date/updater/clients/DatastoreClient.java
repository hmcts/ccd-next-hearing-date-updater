package uk.gov.hmcts.reform.next.hearing.date.updater.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.DatastoreClientConfig;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.CaseDataContent;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.StartEventResult;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.DatastoreClientConfig.GET_EVENT_TRIGGER_BY_EVENT_ID;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.DatastoreClientConfig.POST_EVENT_TRIGGER_BY_CASE_ID;

@FeignClient(
    name = "datastore",
    url = "${ccd.datastore.host}",
    configuration = DatastoreClientConfig.class)
public interface DatastoreClient {

    @GetMapping(value = GET_EVENT_TRIGGER_BY_EVENT_ID, consumes = APPLICATION_JSON_VALUE)
    StartEventResult getEventTriggerByEventId(@PathVariable("caseId") String caseId,
                                              @PathVariable("eventId") String eventId);

    @PostMapping(value = POST_EVENT_TRIGGER_BY_CASE_ID, consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<String> createEvent(@PathVariable("caseId") String caseId,
                                       @RequestBody CaseDataContent caseDataContent);
}
