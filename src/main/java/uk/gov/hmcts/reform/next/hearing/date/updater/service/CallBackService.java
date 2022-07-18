package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCallbackRepository;

import java.util.List;

public class CallBackService {

    @Autowired
    public CcdCallbackRepository callbackRepository;

    public void performCallbacks(List<String> caseReferences) {

        caseReferences.forEach(caseReference ->
            callbackRepository.performCcdCallback(caseReference)
        );
    }
}
