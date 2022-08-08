package uk.gov.hmcts.reform.next.hearing.date.updater.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class CaseData {
    private String reference;

    @JsonProperty("case_type_id")
    private String caseType;

    @JsonProperty("data")
    private Map<String, Object> nextHearingDetails;
}
