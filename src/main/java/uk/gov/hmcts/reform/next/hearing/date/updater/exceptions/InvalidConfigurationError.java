package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

import java.io.Serial;

public class InvalidConfigurationError extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4856517311744893428L;

    public InvalidConfigurationError(String message) {
        super(message);
    }

    public InvalidConfigurationError(String message, Throwable exception) {
        super(message, exception);
    }
}
