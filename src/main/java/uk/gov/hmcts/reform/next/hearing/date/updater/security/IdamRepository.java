package uk.gov.hmcts.reform.next.hearing.date.updater.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Repository
@Slf4j
public class IdamRepository {

    @Value("${idam.nextHearingDateSystemUser.username}")
    private String username;

    @Value("${idam.nextHearingDateSystemUser.password}")
    private String password;

    private final IdamClient idamClient;

    @Autowired
    public IdamRepository(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Cacheable("nextHearingDateAdminAccessTokenCache")
    public String getNextHearingDateAdminAccessToken() {
        return idamClient.getAccessToken(username, password);
    }
}
