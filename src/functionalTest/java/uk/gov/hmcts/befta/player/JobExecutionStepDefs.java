package uk.gov.hmcts.befta.player;

import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.vavr.Tuple2;
import org.junit.Assert;
import uk.gov.hmcts.befta.data.ResponseData;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.befta.util.EnvironmentVariableUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.utils.StreamGobbler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.next.hearing.date.updater.FunctionalTestFixturesFactory.BEAN_FACTORY;

@SuppressWarnings("PMD")
public class JobExecutionStepDefs {

    private static final String JAVA_EXECUTABLE = "java";

    private static final int EXIT_SUCCESS = 0;

    private String filePath;

    private Scenario scenario;

    private BackEndFunctionalTestScenarioContext scenarioContext;

    private static final String LOCATION = "/tmp";
    private static final Function<BackEndFunctionalTestScenarioContext, Tuple2<Long, String>> CASE_DATA_MAP_FUNCTION =
        contextData -> {
            final Map<String, Object> body = contextData.getTestData().getActualResponse().getBody();
            return new Tuple2<>(Long.valueOf(body.get("id").toString()), body.get("case_type").toString());
        };

    @BeforeStep
    public void stepPrepare(Scenario scenario) {
        this.scenario = scenario;

        // NB: The ScenarioContext is generated inside the DefaultBackEndFunctionalTestScenarioPlayer.
        // A CustomValueHandler, triggered at the start of the test scenario (e.g. see TestHookHandler
        // referenced in `Trigger_TestHookHandler.td.json`), is used to capture and register the existing
        // ScenarioContext with local BEAN_FACTORY, so it can be shared between both BEFTA DSL players.
        scenarioContext = BEAN_FACTORY.getScenarioContext();
    }

    @Given("the test csv contains case references: {string}")
    public void csvContainsCaseReferences(final String caseReferences) {
        final String content = caseReferences.replace(',', '\n');
        filePath = createCsvFile(content);
    }

    @Given("the test csv contains case references from {string}")
    public void csvContainsCaseReferencesFromContext(final String contextName) {
        final String content = buildCsv(contextName);
        filePath = createCsvFile(content);
    }

    @Given("the test csv contains case references from {string} plus the following extra case references: {string}")
    public void csvContainsCaseReferencesPlusExtras(final String contextName, final String extraCaseReferences) {
        final String content = buildCsv(contextName) + "\n" + extraCaseReferences.replace(',', '\n');
        filePath = createCsvFile(content);
    }

    @Given("the test csv is empty")
    public void csvIsEmpty() throws FunctionalTestException {
        final String content = "";
        filePath = createCsvFile(content);
    }

    @When("the next hearing date update job executes for CSV")
    public void theNextHearingDateUpdateJobExecutesForCsv() {
        final String locationParam = String.format("-DFILE_LOCATION=%s", filePath);

        executeJob(locationParam);
    }

    @When("the next hearing date update job executes for CSV with maximum CSV limit {string}")
    public void theNextHearingDateUpdateJobExecutesForCsvWithLimit(final String maxCsvRecords) {
        final String locationParam = String.format("-DFILE_LOCATION=%s", filePath);
        final String maxCsvRecordsParam = String.format("-DMAX_CSV_RECORDS=%s", maxCsvRecords);

        executeJob(locationParam, maxCsvRecordsParam);
    }

    @When("the next hearing date update job executes for case types {string}")
    public void theNextHearingDateUpdateJobExecutesForCaseTypes(final String caseType) {
        final String caseTypesParam = String.format("-DCASE_TYPES=%s", caseType);

        executeJob(caseTypesParam);
    }

    @When("the next hearing date update job executes for case types {string} with pagination size {string}")
    public void theNextHearingDateUpdateJobExecutesForCaseTypesWithSize(final String caseType,
                                                                        final String esQuerySize) {
        final String caseTypesParam = String.format("-DCASE_TYPES=%s", caseType);
        final String esQuerySizeParam = String.format("-DES_QUERY_SIZE=%s", esQuerySize);

        executeJob(caseTypesParam, esQuerySizeParam);
    }

    @Then("a success exit value is received")
    public void verifyThatASuccessExitValueResponseWasReceived() {
        int responseCode = scenarioContext.getTheResponse().getResponseCode();
        scenario.log("Exit value: " + responseCode);
        Assert.assertEquals("Exit value '" + responseCode + "' is not a success code.", EXIT_SUCCESS, responseCode);
    }

    @Then("a non-success exit value is received")
    public void verifyThatANonSuccessExitValueResponseWasReceived() {
        int responseCode = scenarioContext.getTheResponse().getResponseCode();
        scenario.log("Exit value: " + responseCode);
        Assert.assertNotEquals("Exit value '" + responseCode + "' is a success code.", EXIT_SUCCESS, responseCode);
    }

    @Then("the following response is logged as output: {string}")
    public void verifyThatJobOutputContained(String lookup) {
        String jobOutput = getJobOutput();
        boolean found = findInJobOutput(jobOutput, lookup);

        Assert.assertTrue("Message '" + lookup + "' not found in job output: \n" + jobOutput, found);
    }

    @Then("no WARN or ERROR logged in output")
    public void verifyThatJobOutputContainedNoWarningOrError() {
        String jobOutput = getJobOutput();
        boolean found = findInJobOutput(jobOutput, "\\s(?i)(warn|error)\\s");

        if (!found) {
            // log success state
            scenario.log("No message of type WARN or ERROR found in job output.");
        }

        Assert.assertFalse("Unexpected WARN or ERROR message found in job output: \n" + jobOutput, found);
    }

    private boolean findInJobOutput(String jobOutput, String lookup) {
        Pattern pattern = Pattern.compile("^.*" + lookup + ".*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(jobOutput);
        boolean found = false;

        while (matcher.find()) {
            scenario.log("Found the following in the job output:\n " + matcher.group(0));
            found = true;
        }

        return found;
    }

    private String getJobOutput() {
        // NB: output to console from job should be in main context response: see `executeJob`
        return scenarioContext.getTheResponse().getResponseMessage();
    }

    private ResponseData executeCommand(final String... command) {

        try {

            final StringBuilder textBuilder = new StringBuilder();

            final Consumer<String> logger = logString -> textBuilder.append(logString).append("\n");

            final Process process = new ProcessBuilder()
                .command(command)
                .start();

            // consume output streams from process
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), logger);
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), logger);
            errorGobbler.start();
            outputGobbler.start();

            int exitVal = process.waitFor();
            String output = textBuilder.toString();

            // record output response to scenarioContext for verification later
            ResponseData response = new ResponseData();
            response.setResponseCode(exitVal);
            response.setResponseMessage(output);

            return response;

        } catch (IOException | InterruptedException e) {
            throw new FunctionalTestException(e.getMessage(), e.getCause());
        }
    }

    private void executeJob(final String... params) {
        BeftaUtils.defaultLog("===================== About to execute "
                                  + "ccd-next-hearing-date-updater =====================");

        final String executableJar = String.format("%s/build/libs/ccd-next-hearing-date-updater.jar",
                                                   System.getProperty("user.dir"));

        // log java version information
        ResponseData versionResponse = executeCommand(JAVA_EXECUTABLE, "-version");
        BeftaUtils.defaultLog("java -version\n" + versionResponse.getResponseMessage());
        Assert.assertEquals("Java version check failed with exit value '" + versionResponse.getResponseCode() + "'.",
                            EXIT_SUCCESS, versionResponse.getResponseCode());

        BeftaUtils.defaultLog(scenario,"Executing job with params: \n\t" + String.join("\n\t", params));

        // run job direct from jar
        ArrayList<String> command = new ArrayList<>();
        command.add(JAVA_EXECUTABLE);
        command.add("-jar");
        command.addAll(Arrays.stream(params).toList());
        command.add(executableJar);
        ResponseData jobResponse = executeCommand(command.toArray(new String[0]));

        // record output response to scenarioContext for verification later
        this.scenarioContext.setTheResponse(jobResponse);

        // log output to both console and scenario (i.e. report output)
        BeftaUtils.defaultLog(scenario, "ccd-next-hearing-date-update output:\n" + jobResponse.getResponseMessage()
            + "\nExit value: " + jobResponse.getResponseCode());

        BeftaUtils.defaultLog("===================== Finished executing "
                                  + "ccd-next-hearing-date-updater =====================\n");
    }

    private String createCsvFile(final String content) throws FunctionalTestException {
        final String filePath = LOCATION + "/" + getFilename();
        final Path path = Paths.get(filePath);

        scenario.log("CSV content:\n\n" + content);

        try {
            BeftaUtils.defaultLog("Writing Case References CSV to ==> " + filePath);
            Files.writeString(path, content);
            return filePath;
        } catch (IOException e) {
            throw new FunctionalTestException(e.getMessage(), e.getCause());
        }
    }

    private List<Tuple2<Long, String>> createCases(final String contextName) {
        final List<Tuple2<Long, String>> result = scenarioContext.getSiblingContexts()
            .get(contextName).getChildContexts()
            .values().stream()
            .map(CASE_DATA_MAP_FUNCTION)
            .toList();
        BeftaUtils.defaultLog("Case Data: \n" + result);
        return result;
    }

    private String buildCsv(final String contextName) {
        final List<Tuple2<Long, String>> caseData = createCases(contextName);
        return caseData.stream()
            .map(data -> data._1.toString())
            .collect(Collectors.joining("\n"));
    }

    private String getFilename() {
        return scenarioContext.getParentContext().getCurrentScenarioTag() + ".csv";
    }
}
