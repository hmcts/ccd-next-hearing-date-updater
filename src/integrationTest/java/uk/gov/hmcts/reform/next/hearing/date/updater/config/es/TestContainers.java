package uk.gov.hmcts.reform.next.hearing.date.updater.config.es;


import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Testcontainers
@ContextConfiguration(initializers = {TestContainers.ElasticsearchInitializer.class})
public class TestContainers {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainers.class);
    private static final String IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:%s";
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(getImageName())
        .withLogConsumer(new Slf4jLogConsumer(LOGGER))
        .waitingFor(Wait.forListeningPort());

    static {
        if (!ELASTICSEARCH_CONTAINER.isRunning()) {
            ELASTICSEARCH_CONTAINER.start();
        }
    }

    @SuppressWarnings("RegExpUnnecessaryNonCapturingGroup")
    private static String getImageName() {
        // regex to search only JAR paths ...
        // ... and extract the jar version (see output of first match group)
        final String regex = "^(?:jar:.*-)(.*)(?:\\.jar!.*)$";

        // use ES client to find compatible image version
        Class<?> clazz =  org.elasticsearch.client.RestHighLevelClient.class;

        // load class path including jar information when packaged
        final String className = clazz.getSimpleName() + ".class";
        final String classPath = Objects.requireNonNull(clazz.getResource(className)).toString();

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(classPath);

        if (matcher.find()) {
            return String.format(IMAGE, matcher.group(1));
        } else {
            // NB: this will generate a descriptive ExceptionInInitializerError if version number not found
            return "Error: ES image version number not found";
        }
    }

    public static class ElasticsearchInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "elasticsearch.hosts=" + ELASTICSEARCH_CONTAINER.getHttpHostAddress()
            );
        }
    }
}
