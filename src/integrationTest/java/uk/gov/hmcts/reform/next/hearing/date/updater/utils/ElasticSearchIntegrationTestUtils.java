package uk.gov.hmcts.reform.next.hearing.date.updater.utils;

import com.pivovarit.function.ThrowingConsumer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.es.ElasticSearchIndexCreator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.client.RequestOptions.DEFAULT;

@Component
public class ElasticSearchIntegrationTestUtils {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    private ElasticSearchIndexCreator elasticSearchIndexCreator;


    public List<String> getAllDocuments(final String indexName) throws IOException {
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(QueryBuilders.matchAllQuery());
        final SearchRequest searchRequest = new SearchRequest(indexName)
            .source(searchSourceBuilder);

        final SearchResponse searchResponse = elasticsearchClient.search(searchRequest, DEFAULT);

        return Arrays.stream(searchResponse.getHits().getHits())
            .filter(hit -> indexName.startsWith(hit.getIndex()))
            .map(SearchHit::getId).toList();
    }

    public void resetIndices(final Set<String> caseTypes) throws IOException {
        final BulkRequest bulkRequest = new BulkRequest()
            .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

        caseTypes.forEach(ThrowingConsumer.unchecked(caseType -> {

            final String indexName = getIndexName(caseType);
            if (elasticsearchClient.indices().exists(new GetIndexRequest(indexName), DEFAULT)) {
                final List<String> documents = getAllDocuments(indexName);
                documents.forEach(documentId -> {
                    final DeleteRequest deleteRequest = new DeleteRequest(indexName)
                        .id(documentId)
                        .type("_doc");
                    bulkRequest.add(deleteRequest);
                });
            }
        }));

        if (bulkRequest.numberOfActions() > 0) {
            final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, DEFAULT);

            if (bulkResponse.hasFailures()) {
                throw new IllegalStateException("Errors resetting indices::: " + bulkResponse.buildFailureMessage());
            }
        }
    }

    public void createElasticSearchIndex(final Map<String, List<CaseData>> indexedData) {
        indexedData.entrySet()
            .forEach(ThrowingConsumer.unchecked(entry -> {
                elasticSearchIndexCreator.insertDataIntoElasticsearch(entry.getKey(), entry.getValue());
            }));
    }

    public String getIndexName(String caseType) {
        return caseType.toLowerCase(Locale.ENGLISH);
    }

}
