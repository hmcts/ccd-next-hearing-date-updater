package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.CsvFileException;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.InvalidConfigurationError;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.CSV_FILE_READ_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.INVALID_CASE_REF_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_CSV_FILE;

@Service
@Slf4j
public class CsvService {
    @Value("${next-hearing-date-updater.csv.caseReferences.maximumNumberOfCsvEntriesToProcess}")
    public int maxNumCaseReferences;

    @Value("${next-hearing-date-updater.csv.caseReferences.fileLocation}")
    private String fileLocation;

    public List<String> getCaseReferences() {
        try {
            List<String> caseReferences = getCaseReferencesFromCsvFile();
            validateCaseRefsFile(caseReferences);
            return caseReferences;
        } catch (TooManyCsvRecordsException | CsvFileException exception) {
            throw new InvalidConfigurationError(CSV_FILE_READ_ERROR, exception);
        }
    }

    private List<String> getCaseReferencesFromCsvFile() throws CsvFileException {
        List<String> caseReferences = Collections.emptyList();

        if (ObjectUtils.isEmpty(fileLocation)) {
            log.info(NO_CSV_FILE);
        } else {
            try (Stream<String> lines = Files.lines(Paths.get(fileLocation))) {
                caseReferences = lines.collect(Collectors.toList());
            } catch (IOException exception) {
                throw new CsvFileException(exception);
            }
        }

        return caseReferences;
    }

    private void validateCaseRefsFile(List<String> caseReferences) throws TooManyCsvRecordsException {
        validateCsvCaseSizeLessThanMaximum(caseReferences);
        logInvalidCaseReferences(caseReferences);
    }

    private void validateCsvCaseSizeLessThanMaximum(List<String> caseReferences) throws TooManyCsvRecordsException {
        if (caseReferences != null && caseReferences.size() > maxNumCaseReferences) {
            throw new TooManyCsvRecordsException(maxNumCaseReferences);
        }
    }

    private void logInvalidCaseReferences(List<String> caseReferences) {
        Predicate<String> isInvalidCaseReference = caseRef -> !LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(caseRef);

        caseReferences.stream()
            .filter(isInvalidCaseReference)
            .forEach(invalidCaseReference -> log.error(INVALID_CASE_REF_ERROR, invalidCaseReference));

        caseReferences.removeIf(isInvalidCaseReference);
    }
}
