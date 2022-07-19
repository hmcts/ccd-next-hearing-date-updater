package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.TooManyCsvRecordsException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CsvService {
    public static final int MAX_NUM_CASE_REFERENCES = 10_000;

    @Value("${csv.caseReferences.fileLocation}")
    private String fileLocation;

    @Value("${csv.caseReferences.fileName}")
    private String fileName;

    public List<String> getCaseReferences() throws TooManyCsvRecordsException {
        List<String> caseReferences = getCaseReferencesFromCsvFile();
        validateCaseRefsFile(caseReferences);
        return caseReferences;
    }

    private List<String> getCaseReferencesFromCsvFile() {
        List<String> caseReferences = Collections.emptyList();

        final String fileToRead = fileLocation + File.separator + fileName;

        try (Stream<String> lines = Files.lines(Paths.get(fileToRead))) {
            caseReferences = lines.collect(Collectors.toList());
        } catch (IOException ioException) {
            log.error(String.format("Failed to read the CSV file at %s", fileToRead));
        }

        return caseReferences;
    }

    private void validateCaseRefsFile(List<String> caseReferences) throws TooManyCsvRecordsException {
        validateCsvCaseSizeLessThanMaximum(caseReferences);
        logInvalidCaseReferences(caseReferences);
    }

    private void validateCsvCaseSizeLessThanMaximum(List<String> caseReferences) throws TooManyCsvRecordsException {
        if (caseReferences != null && caseReferences.size() > MAX_NUM_CASE_REFERENCES) {
            throw new TooManyCsvRecordsException();
        }
    }

    private void logInvalidCaseReferences(List<String> caseReferences) {
        Predicate<String> isInvalidCaseReference = caseRef -> !LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(caseRef);

        caseReferences.stream()
            .filter(isInvalidCaseReference)
            .forEach(invalidCaseReference -> log.error("002 Invalid Case Reference number '{}' in CSV",
                                                       invalidCaseReference));

        caseReferences.removeIf(isInvalidCaseReference);
    }

    /**
     * HMAN-320 Check the properties file for a new variable that will have caseTypes as a comma separated values.
     *
     * @return List of case types
     */
    public List<String> getCaseTypes() {
        return Collections.emptyList();
    }
}
