package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.List;

@Service
/**
 * HMAN-319 - read the CSV file and check if the case reference numbers are valid.
 */
public class CsvService {

    public List<String> getCaseReferences() {
        return Collections.emptyList();
    }

    public List<String> getCaseTypes() {
        return Collections.emptyList();
    }

    private boolean validateCaseRefsFile() {
        return true;
    }

    private boolean isCsvCaseSizeValid() {
        return true;
    }

    private boolean isCaseReferenceValid() {
        return true;
    }
}
