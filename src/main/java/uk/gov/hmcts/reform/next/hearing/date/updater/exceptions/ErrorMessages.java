package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

public final class ErrorMessages {

    public static final String CSV_FILE_READ_ERROR = "Failed to read the CSV file";

    public static final String NO_CSV_FILE = "No CSV file specified";

    public static final String MAX_CSV_ENTRIES_EXCEEDED_ERROR = "001 More than %d references in CSV";

    public static final String INVALID_CASE_REF_ERROR = "002 Invalid Case Reference number '{}' in CSV";
    public static final String ERROR_DOWNSTREAM =
        "006 ERROR occurred downstream for endpoint %s, for case id %s, (%s of %s)";

    public static final String INVALID_DATA_SOURCE_CONFIGURATION =
        "Invalid Configuration: CSV file and Case Types are both specified";
    public static final String NO_REFERENCES_TO_PROCESS = "No Case References found to be processed";
    public static final String NO_REFERENCES_TO_VALIDATE = "No Case References found to be validated";

    public static final String HEARING_DATE_TIME_IN_PAST =
        "003 hearingDateTime set is in the past '{}'";

    public static final String NULL_HEARING_DATE_TIME_LOG_MESSAGE = "004 hearingDateTime set is null '{}'";

    public static final String NULL_HEARING_ID_MESSAGE = "005 hearingID set is null '{}'";

    private ErrorMessages() {

    }
}
