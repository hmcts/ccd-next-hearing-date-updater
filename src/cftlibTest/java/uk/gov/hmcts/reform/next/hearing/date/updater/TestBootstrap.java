package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestBootstrap extends CftlibTest {

    @Test
    void bootsWithCcd() {
        // CFTLib system will stay up UNTIL terminated with 'Control C'

        assertTrue(true);
    }

}
