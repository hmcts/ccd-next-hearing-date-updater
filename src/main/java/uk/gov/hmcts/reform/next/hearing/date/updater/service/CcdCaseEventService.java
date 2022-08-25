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

@Slf4j
@Service
public class CcdCaseEventService {

    public CcdCaseEventRepository ccdCaseEventRepository;

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
            Map nextHearingDetailsJson = (Map) startEventResult.getCaseDetails().getData().get(
                NEXT_HEARING_DETAILS_FIELD_NAME);

            LocalDateTime hearingDate = LocalDateTime.parse(nextHearingDetailsJson.get("HearingDate").toString());

            NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                .caseReference(caseReference)
                .hearingId(nextHearingDetailsJson.get("HearingId").toString())
                .hearingDateTime(hearingDate)
                .build();

            if (nextHearingDetails.isValid()) {
                ccdCaseEventRepository.createCaseEvent(startEventResult);
            }
        }
    }
}
