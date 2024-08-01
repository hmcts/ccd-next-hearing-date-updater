package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorDuringExecutionException;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorDuringExecutionException.EXIT_FAILURE;

@ExtendWith(MockitoExtension.class)
class ApplicationBootstrapTest {

    @Mock
    private ApplicationArguments applicationArguments;

    @Mock
    private NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @Mock
    private TelemetryClient client;

    @InjectMocks
    private ApplicationBootstrap underTest;

    @BeforeEach
    void setUp() {
        openMocks(this);
        underTest = new ApplicationBootstrap(nextHearingDateUpdaterService, client);
        ReflectionTestUtils.setField(underTest, "isProcessingEnabled", true);
    }

    @Test
    void testShouldRunExecutor() throws Exception {
        doNothing().when(nextHearingDateUpdaterService).execute();
        doNothing().when(client).flush();

        underTest.run(applicationArguments);

        verify(nextHearingDateUpdaterService).execute();
        verify(client).flush();
    }

    @Test
    void testShouldRunExecutorWithException() throws Exception {
        doThrow(new RuntimeException("test")).when(nextHearingDateUpdaterService).execute();
        doNothing().when(client).flush();

        ErrorDuringExecutionException exception = assertThrows(
            ErrorDuringExecutionException.class, () -> underTest.run(applicationArguments)
        );

        // NB: must still verify execute and `client.flush` have still been actioned
        verify(nextHearingDateUpdaterService).execute();
        verify(client).flush();
        assertEquals(EXIT_FAILURE, exception.getExitCode(), "Expecting the failure exit code.");
    }

    @SuppressWarnings({"java:S2699", "PMD.JUnitTestsShouldIncludeAssert"})
    @Test
    void testMain() {
        ApplicationBootstrap.main(new String[]{
            "--spring.main.web-environment=false",
            "--spring.autoconfigure.exclude=blahblahblah",
        });
    }

}
