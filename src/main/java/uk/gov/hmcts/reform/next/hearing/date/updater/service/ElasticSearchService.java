package uk.gov.hmcts.reform.next.hearing.date.updater.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.next.hearing.date.updater.clients.ElasticSearchClient;
import uk.gov.hmcts.reform.next.hearing.date.updater.repository.ElasticSearchRepository;


import java.util.Collections;
import java.util.List;

/**
 * HMAN-326 - Return a paginated result set off cases for all caseTypes that needs NextHearing Date to be set
 */
@Service
public class ElasticSearchService {

    @Autowired
    private ElasticSearchRepository elasticSearchRepository;

    /**
     * Return paginated result set of case references
     * @param caseTypes
     * @return
     */
    public List<String> findOutOfDateCaseReferencesByCaseType(List<String> caseTypes) {
        elasticSearchRepository.findOutOfDateNextHearingDate();
        return Collections.emptyList();
    }
}
