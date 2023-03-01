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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.CSV_FILE_READ_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.INVALID_CASE_REF_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.MAX_CSV_ENTRIES_EXCEEDED_ERROR;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_CSV_FILE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NO_REFERENCES_TO_VALIDATE;

@Service
@Slf4j
public class CsvService {
    @Value("${next-hearing-date-updater.csv.caseReferences.maximumNumberOfCsvEntriesToProcess}")
    public int maxNumCaseReferences;

    @Value("${next-hearing-date-updater.csv.caseReferences.fileLocation}")
    private String fileLocation;

    public List<String> getCaseReferences() {
        try {
            if (ObjectUtils.isEmpty(fileLocation)) {
                log.info(NO_CSV_FILE);
                return Collections.emptyList();
            } else {
                List<String> caseReferences = getCaseReferencesFromCsvFile();
                validateCaseRefsFile(caseReferences);
                log.info("The Next-Hearing-Date-Updater has processed csv and found the following "
                             + "number of case references {}.", caseReferences.size());
                return caseReferences;
            }
        } catch (TooManyCsvRecordsException | CsvFileException exception) {
            throw new InvalidConfigurationError(CSV_FILE_READ_ERROR, exception);
        }
    }

    @SuppressWarnings("java:S6204")
    private List<String> getCaseReferencesFromCsvFile() throws CsvFileException {
        try (Stream<String> lines = Files.lines(Paths.get(fileLocation))) {
            return lines.collect(Collectors.toList()); // Compliant, the list needs to be mutable
        } catch (IOException exception) {
            throw new CsvFileException(exception);
        }
    }

    private void validateCaseRefsFile(List<String> caseReferences) throws TooManyCsvRecordsException {
        if (caseReferences.isEmpty()) {
            log.info(NO_REFERENCES_TO_VALIDATE);
        } else {
            validateCsvCaseSizeLessThanMaximum(caseReferences);
            logInvalidCaseReferences(caseReferences);
        }
    }

    private void validateCsvCaseSizeLessThanMaximum(List<String> caseReferences) throws TooManyCsvRecordsException {
        if (caseReferences.size() > maxNumCaseReferences) {
            log.error(String.format(MAX_CSV_ENTRIES_EXCEEDED_ERROR, maxNumCaseReferences));
            throw new TooManyCsvRecordsException(maxNumCaseReferences);
        }
    }

    private void logInvalidCaseReferences(List<String> caseReferences) {
        Predicate<String> isInvalidCaseReference =
            caseRef -> !LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(caseRef) || caseRef.length() != 16;

        caseReferences.stream()
            .filter(isInvalidCaseReference)
            .forEach(invalidCaseReference -> {
                log.error(INVALID_CASE_REF_ERROR, invalidCaseReference);
                log.error(String.format("Error, failed to set next hearing date for %s at %s", invalidCaseReference,
                    LocalDateTime.now()));
            });

        caseReferences.removeIf(isInvalidCaseReference);
    }
}
