package uk.gov.hmcts.reform.next.hearing.date.updater.befta.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.pivovarit.function.ThrowingConsumer;
import uk.gov.hmcts.befta.exception.FunctionalTestException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ElasticSearchOperations {

    private static final String INDEX_TYPE = "_doc";
    private static final String CASE_REFERENCE_FIELD = "reference";

    private final ElasticsearchClient elasticsearchClient;

    public ElasticSearchOperations(final ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public void aVoid(final Long caseReference, final String caseType) {
        try {
        final SearchResponse<Long> searchResponse = elasticsearchClient.search(s -> s.index(getIndexName(caseType))
                                                                        .query(q -> q.term(t -> t
                                                                            .field(CASE_REFERENCE_FIELD)
                                                                            .value(v -> v.longValue(caseReference)))),
                                                                 Long.class);

        final Optional<String> first = searchResponse.hits().hits().stream()
            .map(Hit::id)
            .findFirst();

        System.out.println("=======================");
        System.out.println(first);
        System.out.println("=======================");
        } catch (IOException e) {
            throw new FunctionalTestException(e.getMessage(), e.getCause());
        }
    }



    public void verifyCaseDataAreInElasticsearch(final Map<String, List<Long>> indexedData) {
        indexedData.forEach((key, value) -> {
            final String indexName = getIndexName(key);

            value.forEach(ThrowingConsumer.unchecked(caseReference -> {
                refreshIndex(indexName);
                final Optional<Long> actualCaseReference = findCaseByReference(indexName, caseReference);

                assertThat(actualCaseReference)
                        .isPresent()
                        .hasValue(caseReference);
            }));
        });
    }

    private Optional<Long> findCaseByReference(final String caseIndex, final Long caseReference) throws IOException {
//        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//                .query(QueryBuilders.termQuery(CASE_REFERENCE_FIELD, caseReference))
//                .from(0);
//
//        final SearchRequest searchRequest = new SearchRequest(caseIndex)
//                .types(INDEX_TYPE)
//                .source(searchSourceBuilder);
//
//        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, DEFAULT);
//        final Optional<String> first = Arrays.stream(searchResponse.getHits().getHits())
//                .map(SearchHit::getId)
//                .findFirst();
//
//        return first.map(ThrowingFunction.unchecked(id -> {
//            final GetRequest getRequest = new GetRequest(caseIndex, INDEX_TYPE, id);
//            final GetResponse getResponse = elasticsearchClient.get(getRequest, DEFAULT);
//
//            if (!getResponse.isExists()) {
//                return null;
//            }
//            final Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
//            return (Long) sourceAsMap.get(CASE_REFERENCE_FIELD);
//        }));
        return Optional.empty();
    }

    private void refreshIndex(final String index) throws IOException {
//        final RefreshRequest request = new RefreshRequest(index);
//        elasticsearchClient.indices().refresh(request, DEFAULT);
    }

    public String getIndexName(String caseType) {
        return String.format("%s_cases", caseType.toLowerCase());
    }

}
