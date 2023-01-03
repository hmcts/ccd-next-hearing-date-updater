package uk.gov.hmcts.reform.next.hearing.date.updater.befta.custom;

import java.util.Arrays;

public enum CustomValueKey {
    TEST_HOOK("testHook"),
    VERIFY_HEARING_DATE_TIME_IS_FUTURE_DATE("VerifyHearingDateTimeIsFutureDate"),
    DEFAULT_KEY("DefaultKey");

    private final String value;

    CustomValueKey(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static CustomValueKey getEnum(final String value) {
        return Arrays.stream(values())
            .filter(key -> value.startsWith(key.getValue()))
            .findFirst()
            .orElse(DEFAULT_KEY);
    }
}
