package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.next.hearing.date.updater.service.NextHearingDateUpdaterService;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestTask extends CftlibTest {

    @Autowired
    private NextHearingDateUpdaterService nextHearingDateUpdaterService;

    @Test
    public void testTheThing() {
        nextHearingDateUpdaterService.execute();
    }
}
