package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * HMAN-321
 */
@Service
public class NextHearingDateUpdaterService {

    @Autowired
    private CsvService csvService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private CallBackService callBackService;

    public void execute() {
        List<String> caseReferences = csvService.getCaseReferences();
        if (caseReferences.isEmpty()) {
            List<String> caseTypes = csvService.getCaseTypes();

            //run elastic search/call service
            List<String> outOfDateCaseReferencesByCaseType = elasticSearchService.findOutOfDateCaseReferencesByCaseType(
                caseTypes);
        } else {
            callBackService.performCallbacks(caseReferences);
        }
    }
}
