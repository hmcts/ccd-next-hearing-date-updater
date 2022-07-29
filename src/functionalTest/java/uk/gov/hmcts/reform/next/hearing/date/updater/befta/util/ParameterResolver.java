package uk.gov.hmcts.reform.next.hearing.date.updater.befta.util;

import java.util.Optional;

public class ParameterResolver {
    private ParameterResolver() {
    }

    public static String getElasticsearchHost() {
        return System.getenv("ELASTIC_SEARCH_DATA_NODES_HOSTS");
    }

    public static Integer getElasticsearchPort() {
        return Optional.ofNullable(System.getenv("ELASTIC_SEARCH_PORT"))
            .map(Integer::valueOf)
            .orElse(null);
    }
}
