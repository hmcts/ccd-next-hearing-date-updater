package uk.gov.hmcts.reform.next.hearing.date.updater.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.HEARING_DATE_TIME_IN_PAST;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_DATE_TIME_LOG_MESSAGE;
import static uk.gov.hmcts.reform.next.hearing.date.updater.exceptions.ErrorMessages.NULL_HEARING_ID_MESSAGE;

@Slf4j
@Builder
@Data
public class NextHearingDetails {
    private String hearingID;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime hearingDateTime;
    private String caseReference;

    public boolean isValid() {
        if (hearingDateTime != null && hearingDateTime.isBefore(LocalDateTime.now())) {
            log.error(HEARING_DATE_TIME_IN_PAST, caseReference);
            return false;
        }

        if (hearingID != null && hearingDateTime == null) {
            log.error(NULL_HEARING_DATE_TIME_LOG_MESSAGE, caseReference);
            return false;
        }

        if (hearingID == null && hearingDateTime != null) {
            log.error(NULL_HEARING_ID_MESSAGE, caseReference);
            return false;
        }
        return true;
    }
}
