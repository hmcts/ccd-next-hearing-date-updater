package uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom;

import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueKey.FUTURE_DATE;

public class FutureDateHandler implements CustomValueHandler {
    @Override
    public Boolean matches(CustomValueKey key) {
        return FUTURE_DATE.equals(key);
    }

    @Override
    public Object calculate(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        return LocalDate.now().plusDays(30).toString();
    }
}
