package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

import java.io.Serial;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.MAX_CSV_ENTRIES_EXCEEDED_ERROR;

public class TooManyCsvRecordsException extends Exception {

    @Serial
    private static final long serialVersionUID = -8592448854003292738L;

    public TooManyCsvRecordsException(int maxNumRecords) {
        super(String.format(MAX_CSV_ENTRIES_EXCEEDED_ERROR, maxNumRecords));
    }
}
