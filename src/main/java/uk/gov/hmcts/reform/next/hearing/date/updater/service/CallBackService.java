package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.CcdCallbackRepository;

import java.util.List;

@Service
public class CallBackService {

    public CcdCallbackRepository ccdCallbackRepository;

    @Autowired
    public CallBackService(CcdCallbackRepository ccdCallbackRepository) {
        this.ccdCallbackRepository = ccdCallbackRepository;
    }

    public void performCallbacks(List<String> caseReferences) {

        caseReferences.forEach(caseReference ->
            ccdCallbackRepository.performCcdCallback(caseReference)
        );
    }
}
