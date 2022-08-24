package uk.gov.hmcts.reform.next.hearing.date.updater.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamRepository idamRepository;

    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator, IdamRepository idamRepository) {
        this.authTokenGenerator = authTokenGenerator;
        this.idamRepository = idamRepository;
    }

    public String getS2SToken() {
        return authTokenGenerator.generate();
    }

    public String getNextHearingDateAdminAccessToken() {
        return idamRepository.getNextHearingDateAdminAccessToken();
    }
}
