package uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom;

import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import static uk.gov.hmcts.reform.next.hearing.date.updater.FunctionalTestFixturesFactory.BEAN_FACTORY;
import static uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueKey.TEST_HOOK;

public class TestHookHandler implements CustomValueHandler {
    @Override
    public Boolean matches(CustomValueKey key) {
        return TEST_HOOK.equals(key);
    }

    @Override
    public Object calculate(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        BEAN_FACTORY.setScenarioContext(scenarioContext);
        return "";
    }
}
