package uk.gov.hmcts.reform.next.hearing.date.updater.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class IdamRepositoryTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "token";

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private IdamRepository idamRepository;

    @Test
    void testGetNextHearingDateAdminAccessTokenReturnsToken() {
        ReflectionTestUtils.setField(idamRepository, USERNAME, USERNAME);
        ReflectionTestUtils.setField(idamRepository, PASSWORD, PASSWORD);

        when(idamClient.getAccessToken(USERNAME, PASSWORD)).thenReturn(TOKEN);
        assertNotNull(idamRepository.getNextHearingDateAdminAccessToken());
    }
}
