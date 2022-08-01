package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.stereotype.Service;

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
@Service
public class NextHearingDateUpdaterService {

    public void execute() {

    }
}
