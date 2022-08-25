package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WireMockBootstrap.WIRE_MOCK_SERVER;

public class WiremockFixtures {

    private static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
        .modules(new Jdk8Module(), new JavaTimeModule())
        .build();
    public static final String SUBMIT_CASE_EVENT_URL = "/cases/%s/events";

    public static final String TRIGGER_START_EVENT_URL = "/cases/%s/event-triggers/%s";

    public WiremockFixtures() {
        WireMock.configureFor(WIRE_MOCK_SERVER.port());
    }

    // Same issue as here https://github.com/tomakehurst/wiremock/issues/97
    public static class ConnectionClosedTransformer extends ResponseDefinitionTransformer {

        @Override
        public String getName() {
            return "keep-alive-disabler";
        }

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition,
                                            FileSource files, Parameters parameters) {
            return ResponseDefinitionBuilder.like(responseDefinition)
                .withHeader(HttpHeaders.CONNECTION, "close")
                .build();
        }
    }

    public void stubIdam() {
        stubFor(WireMock.post(urlPathMatching("/o/token"))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .withBody("""
                                                  {
                                                        "scope": "scopeValue",
                                                        "accessToken": "accessTokenValue",
                                                        "expiresIn": "expiresInValue",
                                                        "idToken": "idTokenValue",
                                                        "refreshToken": "refreshTokenValue",
                                                        "tokenType": "tokenTypeValue "
                                                      }"""))
        );

        stubFor(WireMock.post(urlPathMatching("/s2s/lease"))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                    .withBody("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1ODI2MDAyMzN9"
                                                  + ".Lz467pTdzRF0MGQye8QDzoLLY_cxk79ZB3OOYdOR"
                                                  + "-0PGYK5sVay4lxOvhIa-1VnfizaaDDZUwmPdMwQOUBfpBQ")
                    ));
    }

    public void stubReturn200TriggerStartEvent(String caseReference,
                                                      StartEventResponse startEventResponse) {
        stubFor(WireMock.get(
            urlEqualTo(String.format(TRIGGER_START_EVENT_URL, caseReference, CaseEventConfig.EVENT_ID)))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_OK)
                                    .withBody(getJsonString(startEventResponse))
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));
    }

    public void stubReturn200SubmitCaseEvent(String caseReference) {
        stubFor(WireMock.post(
                urlEqualTo(String.format(SUBMIT_CASE_EVENT_URL, caseReference)))
                    .willReturn(aResponse().withStatus(HTTP_OK)));
    }

    public void stubReturn404TriggerStartEvent(String caseReference) {
        stubFor(WireMock.get(
                urlEqualTo(String.format(TRIGGER_START_EVENT_URL, caseReference, CaseEventConfig.EVENT_ID)))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_NOT_FOUND)));
    }

    public void stubReturn404SubmitCaseEvent(String caseReference) {
        stubFor(WireMock.post(
                urlEqualTo(String.format(SUBMIT_CASE_EVENT_URL, caseReference)))
                    .willReturn(aResponse()
                                    .withStatus(HTTP_NOT_FOUND)));
    }

    public void verifyGetRequest(String path) {
        verify(getRequestedFor(urlEqualTo(path)));
    }

    public void verifyPostRequest(String path) {
        verify(postRequestedFor(urlEqualTo(path)));
    }

    @SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "squid:S112"})
    // Required as wiremock's Json.getObjectMapper().registerModule(..); not working
    // see https://github.com/tomakehurst/wiremock/issues/1127
    private static String getJsonString(Object object) {
        try {
            OBJECT_MAPPER.configure(WRITE_DATES_AS_TIMESTAMPS, false);
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
