package uk.gov.hmcts.reform.next.hearing.date.updater.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StartEventResult {
    @JsonProperty("case_details")
    private CaseDetails caseDetails;
    @JsonProperty("event_id")
    private String eventId;
    private String token;

    public CaseDetails getCaseDetails() {
        return caseDetails;
    }

    public void setCaseDetails(CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonIgnore
    public String getCaseTypeId() {
        return caseDetails.getCaseTypeId();
    }

    @JsonIgnore
    public String getCaseReference() {
        return caseDetails.getReferenceAsString();
    }
}
