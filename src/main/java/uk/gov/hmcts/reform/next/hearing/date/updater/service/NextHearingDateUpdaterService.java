package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.InvalidConfigurationError;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.INVALID_DATA_SOURCE_CONFIGURATION;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_REFERENCES_TO_PROCESS;

/**
 * HMAN-321.
 *
 * <p>STEP 1 : Check if provided CSV has any case reference number provided, this is optional the file will not be
 * present every time. The path to the CSV is in the properties file HMAN-319.
 *
 * <p>STEP 2 : IF Case Reference Numbers are present and valid,
 *                   For each case reference number new Next Hearing Date needs to be set. HMAN-322.
 *
 * <p>STEP 3 : ELSE
 *          For each caseType present in the properties file, elastic search query will invoked. This will return
 *          paginated result set off case reference numbers. HMAN-320.
 *          For each case reference number present in the result set, a new Next Hearing Date needs to be set. HMAN-322.
 */
@Slf4j
@Service
public class NextHearingDateUpdaterService {

    private final CsvService csvService;

    private final ElasticSearchService elasticSearchService;

    private final CcdCaseEventService ccdCaseEventService;

    @Autowired
    public NextHearingDateUpdaterService(CsvService csvService,
                                         ElasticSearchService elasticSearchService,
                                         CcdCaseEventService ccdCaseEventService) {
        this.csvService = csvService;
        this.elasticSearchService = elasticSearchService;
        this.ccdCaseEventService = ccdCaseEventService;
    }

    public void execute() {
        List<String> caseReferencesFromCsv = csvService.getCaseReferences();
        List<String> caseReferencesFromCaseTypes = elasticSearchService.findOutOfDateCaseReferencesByCaseType();

        if (!caseReferencesFromCsv.isEmpty() && !caseReferencesFromCaseTypes.isEmpty()) {
            throw new InvalidConfigurationError(INVALID_DATA_SOURCE_CONFIGURATION);
        }

        if (caseReferencesFromCsv.isEmpty() && caseReferencesFromCaseTypes.isEmpty()) {
            log.info(NO_REFERENCES_TO_PROCESS);
        } else {
            List<String> caseReferences = new ArrayList<>();

            caseReferences.addAll(caseReferencesFromCsv);
            caseReferences.addAll(caseReferencesFromCaseTypes);

            ccdCaseEventService.createCaseEvents(caseReferences);
        }
    }
}
