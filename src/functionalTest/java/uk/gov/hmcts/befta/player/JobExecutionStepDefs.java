package uk.gov.hmcts.befta.player;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.vavr.Tuple2;
import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.util.BeftaUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.next.hearing.date.updater.FunctionalTestFixturesFactory.BEAN_FACTORY;

@SuppressWarnings("PMD")
public class JobExecutionStepDefs {
    private String filePath;

    private final BackEndFunctionalTestScenarioContext scenarioContext = BEAN_FACTORY.getScenarioContext();

    private static final String LOCATION = "/tmp";
    private static final Function<BackEndFunctionalTestScenarioContext, Tuple2<Long, String>> CASE_DATA_MAP_FUNCTION =
        contextData -> {
            final Map<String, Object> body = contextData.getTestData().getActualResponse().getBody();
            return new Tuple2<>(Long.valueOf(body.get("id").toString()), body.get("case_type").toString());
        };

    @Given("the test csv contains case references from {string}")
    public void csvContainsCaseReferences(final String contextName) throws FunctionalTestException {
        filePath = createCsvFile(contextName);
    }

    @When("the next hearing date update job executes")
    public void executeJobStep() throws FunctionalTestException {
        final String executableJar = String.format("%s/build/libs/ccd-next-hearing-date-updater.jar",
                                                   System.getProperty("user.dir"));

        final String locationParam = String.format("-DFILE_LOCATION=%s", filePath);

        try {
            final Process process = new ProcessBuilder().inheritIO()
                .command("java", "-jar", locationParam, executableJar)
                .start();
            process.waitFor(1, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            throw new FunctionalTestException(e.getMessage(), e.getCause());
        }
    }

    private String createCsvFile(final String contextName) throws FunctionalTestException {
        final String csv = buildCsv(contextName);
        final String filePath = LOCATION + "/" + getFilename();
        final Path path = Paths.get(filePath);

        try {
            BeftaUtils.defaultLog("Writing Case References CSV to ==> " + filePath);
            Files.writeString(path, csv);
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
