package uk.gov.hmcts.reform.next.hearing.date.updater.befta;

import uk.gov.hmcts.befta.BeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultBeftaTestDataLoader;
import uk.gov.hmcts.befta.DefaultTestAutomationAdapter;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.BeftaUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueHandler;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueKey;
import uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.TestHookHandler;

import java.util.List;

public class ServiceTestAutomationAdapter extends DefaultTestAutomationAdapter {
    private final List<CustomValueHandler> customValueHandlers = List.of(
        new TestHookHandler()
    );

    @Override
    public synchronized Object calculateCustomValue(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        return customValueHandlers.stream()
            .filter(candidate -> candidate.matches(CustomValueKey.getEnum(key.toString())))
            .findFirst()
            .map(evaluator -> evaluator.calculate(scenarioContext, key))
            .orElseGet(() -> super.calculateCustomValue(scenarioContext, key));
    }

    @Override
    public BeftaTestDataLoader getDataLoader() {
/*
        return new DataLoaderToDefinitionStore(this,
                                               DataLoaderToDefinitionStore.VALID_CCD_TEST_DEFINITIONS_PATH) {

            @Override
            protected void createRoleAssignment(String resource, String filename) {
                // Do not create role assignments.
                BeftaUtils.defaultLog("Will NOT create role assignments!");
            }
        };
*/
        return new DefaultBeftaTestDataLoader() {
            @Override
            public void doLoadTestData() {

            }

            @Override
            public boolean isTestDataLoadedForCurrentRound() {
                return false;
            }

            @Override
            public void loadDataIfNotLoadedVeryRecently() {

            }
        };
    }
}
