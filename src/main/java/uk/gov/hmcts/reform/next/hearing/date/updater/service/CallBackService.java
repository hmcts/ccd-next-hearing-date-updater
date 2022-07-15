package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CallbackRepository;


import java.util.List;

public class CallBackService {

    @Autowired
    public CallbackRepository callbackRepository;

    public void performCallbacks(List<String> caseReferences) {

        caseReferences.forEach(caseReference ->
            callbackRepository.performCallback(caseReference)
        );
    }
}
