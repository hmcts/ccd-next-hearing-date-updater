package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.ElasticSearchRepository;

import java.util.List;
import java.util.Objects;

/**
 * HMAN-326 - Return a paginated result set off cases for all caseTypes that needs NextHearing Date to be set.
 *
 */
@Service
public class ElasticSearchService {

    @Value("#{'${next-hearing-date-updater.elasticsearch.caseTypes}'.split(',')}")
    private List<String> caseTypes;

    private final ElasticSearchRepository elasticSearchRepository;

    @Autowired
    public ElasticSearchService(ElasticSearchRepository elasticSearchRepository) {
        this.elasticSearchRepository = elasticSearchRepository;
    }

    /**
     * Return paginated result set of case references.
     *
     * @return paginated result set of case references
     */
    public List<String> findOutOfDateCaseReferencesByCaseType() {
        return caseTypes.stream()
            .filter(this::isNotEmpty)
            .flatMap(caseType -> elasticSearchRepository.findCasesWithOutOfDateNextHearingDate(caseType).stream())
            .map(caseDetails -> caseDetails.getId().toString())
            .toList();
    }

    private boolean isNotEmpty(final String subject) {
        return !"".equals(subject);
    }
}
