package uk.gov.hmcts.reform.next.hearing.date.updater.data;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// partial javadoc attributes added prior to checkstyle implementation in module
@SuppressWarnings("checkstyle:SummaryJavadoc")
public class CaseDetails implements Cloneable {

    private static final Logger LOG = LoggerFactory.getLogger(CaseDetails.class);

    private String id;

    @JsonIgnore
    private Long reference;

    @JsonProperty("version")
    private Integer version;

    private String jurisdiction;

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("created_date")
    private LocalDateTime createdDate;

    @JsonProperty("last_modified")
    private LocalDateTime lastModified;

    @JsonProperty("last_state_modified_date")
    private LocalDateTime lastStateModifiedDate;

    private String state;

    @JsonProperty("case_data")
    private Map<String, JsonNode> data;

    @JsonIgnore
    private final Map<String, Object> metadata = new HashMap<>();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter("id")
    public Long getReference() {
        return reference;
    }

    @JsonIgnore
    public String getReferenceAsString() {
        return reference != null ? reference.toString() : null;
    }

    @JsonSetter("id")
    public void setReference(Long reference) {
        this.reference = reference;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public Map<String, JsonNode> getData() {
        return data;
    }

    public void setData(Map<String, JsonNode> data) {
        this.data = data;
    }

    public LocalDateTime getLastStateModifiedDate() {
        return lastStateModifiedDate;
    }

    public void setLastStateModifiedDate(LocalDateTime lastStateModifiedDate) {
        this.lastStateModifiedDate = lastStateModifiedDate;
    }


    @JsonIgnore
    public CaseDetails shallowClone() throws CloneNotSupportedException {
        return (CaseDetails) super.clone();
    }


    @JsonIgnore
    public boolean hasCaseReference() {
        return getReference() != null;
    }

    @JsonIgnore
    public Map<String, JsonNode> getCaseEventData(Set<String> caseFieldIds) {
        Map<String, JsonNode> caseEventData = new HashMap<>();
        if (this.data != null) {
            for (String caseFieldId : caseFieldIds) {
                JsonNode value = this.data.get(caseFieldId);
                if (value != null) {
                    caseEventData.put(caseFieldId, value);
                }
            }
        }
        return caseEventData;
    }
}
