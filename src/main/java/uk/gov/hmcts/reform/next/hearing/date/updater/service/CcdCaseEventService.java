package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.NEXT_HEARING_DETAILS_FIELD_NAME;
import static uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails.HEARING_DATE_TIME;
import static uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails.HEARING_ID;

@Slf4j
@Service
public class CcdCaseEventService {

    private final CcdCaseEventRepository ccdCaseEventRepository;

    @Autowired
    public CcdCaseEventService(CcdCaseEventRepository ccdCaseEventRepository) {
        this.ccdCaseEventRepository = ccdCaseEventRepository;
    }

    public void createCaseEvents(List<String> caseReferences) {
        caseReferences.forEach(this::createCaseEvent);
    }

    private void createCaseEvent(String caseReference) {
        StartEventResponse startEventResult = ccdCaseEventRepository.triggerAboutToStartEvent(caseReference);

        if (startEventResult != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nextHearingDetailsJson = (Map<String, Object>) startEventResult.getCaseDetails()
                .getData()
                .get(NEXT_HEARING_DETAILS_FIELD_NAME);

            String hearingId = getStringFromJsonMap(nextHearingDetailsJson, HEARING_ID);
            LocalDateTime hearingDate = getLocalDateTimeFromJsonMap(nextHearingDetailsJson, HEARING_DATE_TIME);

            NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                .caseReference(caseReference)
                .hearingID(hearingId)
                .hearingDateTime(hearingDate)
                .build();

            if (nextHearingDetails.isValid()) {
                ccdCaseEventRepository.createCaseEvent(startEventResult);
            }
        }
    }

    private String getStringFromJsonMap(Map<String, Object> jsonMap, String key) {
        if (jsonMap.containsKey(key)) {
            Object value = jsonMap.get(key);

            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }

        return null; // use null if not found or if null value returned from map
    }

    @SuppressWarnings("SameParameterValue")
    private LocalDateTime getLocalDateTimeFromJsonMap(Map<String, Object> jsonMap, String key) {
        String value = getStringFromJsonMap(jsonMap, key);
        return value == null ? null : LocalDateTime.parse(value);
    }

}
