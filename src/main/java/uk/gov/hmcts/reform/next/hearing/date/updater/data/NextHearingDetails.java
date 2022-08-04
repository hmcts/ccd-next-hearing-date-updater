package uk.gov.hmcts.reform.next.hearing.date.updater.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Builder
@Data
public class NextHearingDetails {
    private String hearingId;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime hearingDateTime;
    private String caseReference;
    public static final String HEARING_DATE_TIME_IN_PAST =
        "003 hearingDateTime set is in the past '{}'";
    public static final String NULL_HEARING_DATE_TIME_LOG_MESSAGE = "004 hearingDateTime set is null '{}'";
    public static final String NULL_HEARING_ID_MESSAGE = "005 hearingID set is null '{}'";

    public boolean validateValues() {
        if (hearingDateTime != null && hearingDateTime.isBefore(LocalDateTime.now())) {
            log.error(HEARING_DATE_TIME_IN_PAST, caseReference);
            return false;
        }

        if (hearingId != null && hearingDateTime == null) {
            log.error(NULL_HEARING_DATE_TIME_LOG_MESSAGE, caseReference);
            return false;
        }

        if (hearingId == null && hearingDateTime != null) {
            log.error(NULL_HEARING_ID_MESSAGE, caseReference);
            return false;
        }
        return true;
    }
}
