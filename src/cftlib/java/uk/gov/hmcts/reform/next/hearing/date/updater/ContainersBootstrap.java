package uk.gov.hmcts.reform.next.hearing.date.updater;

import ch.qos.logback.classic.Level;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static uk.gov.hmcts.reform.next.hearing.date.updater.ContainersShutdownListener.EXIT_COMMAND;

@Slf4j
public class ContainersBootstrap {

    private static final String STOP_MESSAGE = String.format(
        "%n%nTo stop the primary `bootWithCcd` components plus the TEST_STUBS_CONTAINER type `%s` then enter:%n",
        EXIT_COMMAND
    );
    private static final int STOP_MESSAGE_INTERVAL = 5;

    private static final String BEFTA_EXECUTION_FILE_PATH
        = String.format(".%s%s", File.separator, "befta_recent_executions_info_LOCAL.json");
    private static final String TEST_STUB_ENV_FILE_PATH
        = String.format(".%s%s", File.separator, ".env.test.stub.service.env");

    private static final String TEST_STUBS_IMAGE = "hmctspublic.azurecr.io/ccd/test-stubs-service:latest";

    protected static final GenericContainer<?> TEST_STUBS_CONTAINER = new GenericContainer<>(TEST_STUBS_IMAGE)
        .withExposedPorts(5555)
        .withLogConsumer(new Slf4jLogConsumer(log))
        .waitingFor(Wait.forListeningPort());

    private static final ContainersShutdownListener SHUTDOWN_LISTENER = new ContainersShutdownListener();


    private static int testStubsPort = 0;
    private static LocalDateTime previousStopMessageShown = LocalDateTime.now();
    private static String previousStopMessage = "";

    @PostConstruct
    public void init() {
        log.info("==================== Starting TEST_STUBS_CONTAINER ====================");

        if (!TEST_STUBS_CONTAINER.isRunning()) {
            TEST_STUBS_CONTAINER.start();
        }

        createEnvFile();
    }

    @PreDestroy
    public void cleanUp() {
        log.info("==================== Stopping TEST_STUBS_CONTAINER ====================");

        if (TEST_STUBS_CONTAINER.isRunning()) {
            TEST_STUBS_CONTAINER.stop();
        }

        // NB: Test Stub env file not needed if bootWithCcd has ended
        deleteEnvFile();

        // delete BEFTA last execution file to force re-import of definitions files if switching back to CCD-Docker
        deleteFileIfExists(BEFTA_EXECUTION_FILE_PATH);
    }

    private static void resetPreviousStopMessageShown() {
        previousStopMessageShown = LocalDateTime.now();
    }

    private static void setPreviousStopMessage(String message) {
        previousStopMessage = message;
    }

    private static void setTestStubsPort(int port) {
        testStubsPort = port;
    }

    private void createEnvFile() {
        ContainersBootstrap.setTestStubsPort(TEST_STUBS_CONTAINER.getFirstMappedPort());

        final String content = String.format("""
                                             # This file is only required when running bootWithCcd
                                             #
                                             # WARNING: overriding test stub URL: for use ONLY with `bootWithCcd`
                                             TEST_STUB_SERVICE_BASE_URL=http://localhost:%d
                                             """,
                                             ContainersBootstrap.testStubsPort);

        final Path path = Paths.get(TEST_STUB_ENV_FILE_PATH);

        try {
            log.info("Writing environment variable 'TEST_STUB_SERVICE_BASE_URL' to ==> {}", TEST_STUB_ENV_FILE_PATH);
            Files.writeString(path, content);

        } catch (IOException e) {
            log.error("Something went wrong:::", e);
        }
    }

    private void deleteEnvFile() {
        deleteFileIfExists(TEST_STUB_ENV_FILE_PATH);
    }

    private void deleteFileIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            log.error("Something went wrong:::", e);
        }
    }

    private static boolean testStubsReady() {
        if (ContainersBootstrap.testStubsPort == 0) {
            return false; // i.e. not started
        } else if (!TEST_STUBS_CONTAINER.isRunning()) {
            log.warn("The TEST_STUBS_CONTAINER is not running.");
            return false; // if still pulling image there may still be time for it to recover naturally
        }

        try {
            // Wait for TestStubs container to come up.
            String url = String.format("http://localhost:%s/health", ContainersBootstrap.testStubsPort);
            var c = (HttpURLConnection) new URL(url)
                .openConnection();
            if (c.getResponseCode() != 200) {
                throw new IllegalStateException("Service is not initialised");
            }

        } catch (Exception e) {
            log.info("The TEST_STUBS_CONTAINER is not ready...");

            return false;
        }

        return true;
    }

    private static boolean testStubsShutdown() {
        if (!SHUTDOWN_LISTENER.isRunning()) {
            new Thread(SHUTDOWN_LISTENER).start();

        } else if (SHUTDOWN_LISTENER.shouldExit()) {
            return true;
        }

        // check state and set relevant stop message
        if (!TEST_STUBS_CONTAINER.isRunning()) {
            showStopMessage(
                "The TEST_STUBS_CONTAINER is not running.",
                Level.WARN
            );

        } else if (!testStubsReady()) {
            showStopMessage(
                "The TEST_STUBS_CONTAINER is not accessible on expected port.  Please restart `bootWithCcd`.",
                Level.WARN
            );

        } else {
            showStopMessage(
                "==================== TEST_STUBS_CONTAINER is running ====================",
                Level.INFO
            );
        }

        return false;
    }

    private static void showStopMessage(String message, Level level) {

        // only show if message has changed or if interval has passed
        if (!previousStopMessage.equals(message)
            || previousStopMessageShown.plusMinutes(STOP_MESSAGE_INTERVAL).isBefore(LocalDateTime.now())
        ) {
            if (Level.INFO.equals(level)) {
                log.info(message + STOP_MESSAGE);
            } else {
                log.warn(message + STOP_MESSAGE);
            }
            resetPreviousStopMessageShown();
            setPreviousStopMessage(message);
        }
    }

    public static void waitForStartUp() {
        Awaitility.await()
            .pollInSameThread()
            .pollDelay(Duration.ofSeconds(20)) // at least give container a chance to start
            .pollInterval(Duration.ofSeconds(5))
            .ignoreExceptions()
            .timeout(Duration.ofMinutes(10))
            .until(ContainersBootstrap::testStubsReady);

        log.info("The TEST_STUBS_CONTAINER is available.");
    }

    public static void waitForShutdown() {
        Awaitility.await()
            .pollInSameThread()
            .pollInterval(Duration.ofSeconds(5))
            .ignoreExceptions()
            .forever()
            .until(ContainersBootstrap::testStubsShutdown);
    }

}
