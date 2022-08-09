package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCaseEventRepository;

import java.util.List;

@Slf4j
@Service
public class CcdCaseEventService {

    public CcdCaseEventRepository ccdCaseEventRepository;

    @Autowired
    public CcdCaseEventService(CcdCaseEventRepository ccdCaseEventRepository) {
        this.ccdCaseEventRepository = ccdCaseEventRepository;
    }

    public void createCaseEvents(List<String> caseReferences) {
        caseReferences.forEach(caseReference ->
            ccdCaseEventRepository.createCaseEvents(caseReference)
        );
    }
}
