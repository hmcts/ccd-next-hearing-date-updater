package uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom;

import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

public interface CustomValueHandler {
    Boolean matches(CustomValueKey key);

    Object calculate(BackEndFunctionalTestScenarioContext scenarioContext, Object key);
}
