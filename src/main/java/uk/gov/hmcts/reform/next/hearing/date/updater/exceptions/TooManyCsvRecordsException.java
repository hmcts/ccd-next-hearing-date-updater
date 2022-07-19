package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

import java.io.Serial;

public class TooManyCsvRecordsException extends Exception {

    @Serial
    private static final long serialVersionUID = -8592448854003292738L;

    public static final String ERROR_MESSAGE = "001 More than 10,000 references in CSV";

    public TooManyCsvRecordsException() {
        super(ERROR_MESSAGE);
    }
}
