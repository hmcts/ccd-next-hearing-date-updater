package uk.gov.hmcts.reform.next.hearing.date.updater;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "cronjob.enabled=false")
public class ApplicationBootstrapIT {

    @Test
    void contextLoads(ApplicationContext context) {
        assertNotNull(context);
    }
}
