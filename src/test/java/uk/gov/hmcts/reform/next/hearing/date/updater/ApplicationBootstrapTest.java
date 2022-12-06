package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage",
    "PMD.JUnitTestsShouldIncludeAssert"})
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

    @Test
    void testShouldRunExecutor() throws Exception {
        ReflectionTestUtils.setField(underTest, "isProcessingEnabled", true);
        doNothing().when(nextHearingDateUpdaterService).execute();
        doNothing().when(client).flush();
        underTest.run(applicationArguments);
        verify(nextHearingDateUpdaterService).execute();
        verify(client).flush();
    }

    @Test
    void testShouldRunExecutorWithException() throws Exception {
        ReflectionTestUtils.setField(underTest, "isProcessingEnabled", true);
        doThrow(new RuntimeException("test")).when(nextHearingDateUpdaterService).execute();
        doNothing().when(client).flush();
        underTest.run(applicationArguments);
        verify(nextHearingDateUpdaterService).execute();
        verify(client).flush();
    }


    @Test
    void testMain() {
        ApplicationBootstrap.main(new String[]{
            "--spring.main.web-environment=false",
            "--spring.autoconfigure.exclude=blahblahblah",
            // Override any other environment properties according to your needs
        });
    }
}
