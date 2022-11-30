package uk.gov.hmcts.reform.next.hearing.date.updater;

import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

public enum FunctionalTestFixturesFactory {
    BEAN_FACTORY;

    private BackEndFunctionalTestScenarioContext scenarioContext;

    public BackEndFunctionalTestScenarioContext getScenarioContext() {
        return scenarioContext;
    }

    public void setScenarioContext(BackEndFunctionalTestScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }
}
