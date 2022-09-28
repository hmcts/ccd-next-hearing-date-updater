package uk.gov.hmcts.reform.next.hearing.date.updater.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class SecurityUtilsTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamRepository idamRepository;

    @InjectMocks
    private SecurityUtils securityUtils;

    private static final String S2S_TOKEN_VALUE = "s2sToken";

    private static final String NEXT_HEARING_ADMIN_ACCESS_TOKEN_VALUE = "nextHearingAdminAccessTokenValue";

    @Test
    void testGetS2STokenReturnsValidValue() {
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN_VALUE);

        assertEquals(S2S_TOKEN_VALUE, securityUtils.getS2SToken());
    }

    @Test
    void testGetS2STokenReturnsNull() {
        when(authTokenGenerator.generate()).thenReturn(null);

        assertNull(securityUtils.getS2SToken());
    }

    @Test
    void testGetNextHearingDateAdminAccessTokenReturnsValidValue() {
        when(idamRepository.getNextHearingDateAdminAccessToken()).thenReturn(NEXT_HEARING_ADMIN_ACCESS_TOKEN_VALUE);

        assertEquals(NEXT_HEARING_ADMIN_ACCESS_TOKEN_VALUE, securityUtils.getNextHearingDateAdminAccessToken());
    }

    @Test
    void testGetNextHearingDateAdminAccessTokenReturnsNull() {
        when(idamRepository.getNextHearingDateAdminAccessToken()).thenReturn(null);

        assertNull(securityUtils.getNextHearingDateAdminAccessToken());
    }
}
