package uk.gov.hmcts.reform.next.hearing.date.updater.exceptions;

import org.springframework.boot.ExitCodeGenerator;

import java.io.Serial;

public class ErrorDuringExecutionException extends RuntimeException implements ExitCodeGenerator {

    public static final int EXIT_FAILURE = 1;

    @Serial
    private static final long serialVersionUID = -6388986674018133745L;

    public ErrorDuringExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getExitCode() {
        return EXIT_FAILURE;
    }

}
