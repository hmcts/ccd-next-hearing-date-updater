package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

import java.io.Serial;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.CSV_FILE_READ_ERROR;

public class CsvFileException extends Exception {

    @Serial
    private static final long serialVersionUID = -6603201301165524130L;

    public CsvFileException(Throwable throwable) {
        super(CSV_FILE_READ_ERROR, throwable);
    }
}
