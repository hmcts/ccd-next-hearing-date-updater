package uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom;

import uk.gov.hmcts.befta.exception.FunctionalTestException;
import uk.gov.hmcts.befta.player.BackEndFunctionalTestScenarioContext;
import uk.gov.hmcts.befta.util.ReflectionUtils;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom.CustomValueKey.VERIFY_HEARING_DATE_TIME_IS_FUTURE_DATE;

public class VerifyHearingDateTimeIsFutureDateHandler implements CustomValueHandler {

    private static final String HEARING_DATETIME_CONTEXT_PATH
        = "testData.actualResponse.body.data.nextHearingDetails.hearingDateTime";

    @Override
    public Boolean matches(CustomValueKey key) {
        return VERIFY_HEARING_DATE_TIME_IS_FUTURE_DATE.equals(key);
    }

    @Override
    public Object calculate(BackEndFunctionalTestScenarioContext scenarioContext, Object key) {
        try {
            final String valueFromContext =
                ReflectionUtils.deepGetFieldInObject(scenarioContext, HEARING_DATETIME_CONTEXT_PATH).toString();

            if (LocalDateTime.parse(valueFromContext).isAfter(LocalDateTime.now())) {
                // return future date to allow expected value checks to PASS
                return valueFromContext;
            } else {
                // return error string to force expected value checks to FAIL
                return "Expecting future date";
            }

        } catch (Exception e) {
            throw new FunctionalTestException("Problem checking for future date: ", e);
        }
    }
}
