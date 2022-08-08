package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
/**
 * HMAN-319
 *
 * read the CSV file
 *  - check if csv has more than 10,000 Case Reference numbers
 *  - check if the case reference numbers are valid.
 */
public class CsvService {

    public List<String> getCaseReferences() {

        List<String> caseReferences = getCaseReferencesFromCsvFile();

        if (validateCaseRefsFile(caseReferences)) {
            return caseReferences;
        }

        return Collections.emptyList();
    }

    public List<String> getCaseTypes() {
        return Collections.emptyList();
    }

    private List<String> getCaseReferencesFromCsvFile() {
        return Collections.emptyList();
    }

    private boolean validateCaseRefsFile(List<String> caseReferences) {
        return isCsvCaseSizeValid(caseReferences) && areCaseReferencesValid(caseReferences);
    }

    private boolean isCsvCaseSizeValid(List<String> caseReferences) {
        log.info(String.valueOf(caseReferences));
        return true;
    }

    private boolean areCaseReferencesValid(List<String> caseReferences) {
        log.info(String.valueOf(caseReferences));
        return true;
    }
}
