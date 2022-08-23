package uk.gov.hmcts.reform.next.hearing.date.updater.repository;

import lombok.Builder;

@Builder
public class ElasticSearchQuery {
    private static final String START_QUERY = "{\n"
        + "    \"_source\": [\n"
        + "        \"reference\"\n"
        + "    ],\n"
        + "    \"query\": {\n"
        + "        \"range\": {\n"
        + "            \"data.NextHearingDetails.hearingDateTime\": {\n"
        + "                \"lt\": \"now\"\n"
        + "            }\n"
        + "        }\n"
        + "    },\n"
        + "    \"sort\": [\n"
        + "        {\n"
        + "            \"reference.keyword\": \"asc\"\n"
        + "        }\n"
        + "    ],\n"
        + "    \"size\": %s";

    private static final String END_QUERY  = "\n}";

    private static final String SEARCH_AFTER = "\"search_after\": [%s]";

    private String searchAfterValue;
    private int size;
    private boolean initialSearch;

    public String getQuery() {
        if (initialSearch) {
            return getInitialQuery();
        } else {
            return getSubsequentQuery();
        }
    }

    private String getInitialQuery() {
        return String.format(START_QUERY, size) + END_QUERY;
    }

    private String getSubsequentQuery() {
        return String.format(START_QUERY, size) + "," + String.format(SEARCH_AFTER, searchAfterValue) + END_QUERY;
    }
}
