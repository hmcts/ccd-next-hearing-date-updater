package uk.gov.hmcts.reform.next.hearing.date.updater.befta;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.befta.BeftaMain;
import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.auth.UserTokenProviderConfig;
import uk.gov.hmcts.befta.data.UserData;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueHandler;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueKey;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.TestHookHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH;

public class ServiceTestAutomationAdapter extends DefaultTestAutomationAdapter {
    private final List<CustomValueHandler> customValueHandlers = List.of(
        new TestHookHandler()
    );

    @PostConstruct
    public void init() {
        addRole();
    }

    @Override
    @SuppressWarnings("PMD")
    public synchronized Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        return customValueHandlers.stream()
            .filter(candidate -> candidate.matches(CustomValueKey.getEnum(key.toString())))
            .findFirst()
            .map(evaluator -> evaluator.calculate(scenarioContext, key))
            .orElseGet(() -> super.calculateCustomValue(scenarioContext, key));
    }

    @Override
    public BeftaTestDataLoader getDataLoader() {
        return new DataLoaderToDefinitionStore(this, VALID_CCD_TEST_DEFINITIONS_PATH) {
            @Override
            protected void createRoleAssignment(String resource, String filename) {
                // Do not create role assignments.
                BeftaUtils.defaultLog("Will NOT create role assignments!");
            }
        };
    }

    private RequestSpecification asAutoTestImporter() {
        final String definitionStoreUrl = BeftaMain.getConfig().getDefinitionStoreUrl();
        final UserData importingUser = new UserData(
            BeftaMain.getConfig().getImporterAutoTestEmail(),
            BeftaMain.getConfig().getImporterAutoTestPassword());
        try {
            authenticate(importingUser, UserTokenProviderConfig.DEFAULT_INSTANCE.getClientId());
            final String s2sToken = getNewS2STokenWithEnvVars("CCD_API_GATEWAY_S2S_ID",
                                                              "CCD_API_GATEWAY_S2S_KEY");
            return RestAssured.given(new RequestSpecBuilder().setBaseUri(definitionStoreUrl).build())
                .header("Authorization", "Bearer " + importingUser.getAccessToken())
                .header("ServiceAuthorization", s2sToken);
        } catch (ExecutionException e) {
            String message = String.format("authenticating as %s failed ", importingUser.getUsername());
            throw new RuntimeException(message, e);
        }
    }

    private void addRole() {
        final Map<String, String> ccdRoleInfo = Map.of(
            "role", "next-hearing-date-admin",
            "security_classification", "PUBLIC"
        );
        final Response response = asAutoTestImporter().given()
            .header("Content-type", "application/json")
            .body(ccdRoleInfo)
            .when().put("/api/user-role");
        if (response.getStatusCode() / 100 != 2) {
            String message = "Import failed with response body: %s\nand http code: %d";
            throw new RuntimeException(
                String.format(message, response.body().prettyPrint(), response.statusCode())
            );
        }
    }

}
