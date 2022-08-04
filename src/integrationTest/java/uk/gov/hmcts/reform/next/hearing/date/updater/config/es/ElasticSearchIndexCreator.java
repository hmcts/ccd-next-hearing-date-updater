package uk.gov.hmcts.reform.next.hearing.date.updater.config.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingConsumer;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.next.hearing.date.updater.utils.CaseData;
import uk.gov.hmcts.reform.next.hearing.date.updater.utils.ElasticSearchIntegrationTestUtils;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.client.RequestOptions.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Component
@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
public class ElasticSearchIndexCreator {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    private ElasticSearchIntegrationTestUtils elasticSearchIntegrationTestUtils;

    @Autowired
    private ObjectMapper objectMapper;

    public void insertDataIntoElasticsearch(final String indexName, final List<CaseData> caseDataEntities)
        throws IOException {
        final String caseIndex = elasticSearchIntegrationTestUtils.getIndexName(indexName);

        final BulkRequest bulkRequest = buildBulkRequest(caseIndex, caseDataEntities);

        final BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest, DEFAULT);

        assertFalse(bulkResponse.hasFailures());

        refreshIndex(caseIndex);
    }

    private BulkRequest buildBulkRequest(final String caseIndex, final List<CaseData> caseDataEntities) {
        final BulkRequest bulkRequest = new BulkRequest();
        caseDataEntities.forEach(ThrowingConsumer.unchecked(data -> {
            final String value = objectMapper.writeValueAsString(data);
            final IndexRequest indexRequest = new IndexRequest(caseIndex)
                .source(value, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }));

        return bulkRequest;
    }

    private void refreshIndex(final String caseIndex) throws IOException {
        final RefreshRequest refreshRequest = new RefreshRequest(caseIndex);
        elasticsearchClient.indices().refresh(refreshRequest, DEFAULT);
    }
}
