package uk.gov.hmcts.reform.next.hearing.date.updater;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.next.hearing.date.updater.data.NextHearingDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.SUBMIT_CASE_EVENT_URL;
import static uk.gov.hmcts.reform.next.hearing.date.updater.WiremockFixtures.TRIGGER_START_EVENT_URL;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.EVENT_ID;
import static uk.gov.hmcts.reform.next.hearing.date.updater.config.CaseEventConfig.NEXT_HEARING_DETAILS_FIELD_NAME;

/**
 * Guards the JSON contract with CCD following the Jackson 2 to Jackson 3 upgrade.
 * Payloads are written to mirror the CCD format rather than being produced by the mapper under test.
 */
@SpringBootTest
@ActiveProfiles("itest")
@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.TooManyMethods",
    "PMD.JUnitTestContainsTooManyAsserts"})
class JacksonSerializationIT extends WireMockBootstrap {

    private static final String CASE_REFERENCE = "1658830998852951";
    private static final String CASE_TYPE = "FT_NextHearingDate";
    private static final String HEARING_ID = "HRG-12345";
    private static final String HEARING_DATE_TIME = "2030-06-15T10:30:00";
    private static final String EVENT_TOKEN = "event-token-value";
    private static final String AUTH = "Bearer user-token";
    private static final String S2S = "Bearer s2s-token";

    private static final String CASE_DETAILS_JSON = """
        {
          "id": 1658830998852951,
          "jurisdiction": "BEFTA_MASTER",
          "case_type_id": "FT_NextHearingDate",
          "created_date": "2026-01-02T03:04:05.678",
          "last_modified": "2026-02-03T04:05:06",
          "state": "CaseCreated",
          "locked_by_user_id": 42,
          "security_level": 1,
          "security_classification": "PUBLIC",
          "callback_response_status": "CALLBACK_COMPLETED",
          "version": 3,
          "an_unknown_field": "should be ignored",
          "case_data": {
            "TextField": "some text",
            "NumberField": 123,
            "nextHearingDetails": {
              "hearingID": "HRG-12345",
              "hearingDateTime": "2030-06-15T10:30:00"
            }
          }
        }""";

    private static final String START_EVENT_RESPONSE_JSON = """
        {
          "case_details": %s,
          "event_id": "UpdateNextHearingInfo",
          "token": "event-token-value",
          "an_unknown_field": "should be ignored"
        }""".formatted(CASE_DETAILS_JSON);

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    private final WiremockFixtures wiremockFixtures = new WiremockFixtures();

    @BeforeEach
    void setUp() {
        wiremockFixtures.resetRequests();
    }

    @Test
    void deserialisesCaseDetailsFromCcdFormat() {
        CaseDetails caseDetails = jsonMapper.readValue(CASE_DETAILS_JSON, CaseDetails.class);

        assertCaseDetails(caseDetails);
    }

    @Test
    void deserialisesCaseDetailsUsingJsonAliases() {
        String json = """
            {
              "id": 1658830998852951,
              "case_type": "FT_NextHearingDate",
              "created_on": "2026-01-02T03:04:05.678",
              "last_modified_on": "2026-02-03T04:05:06",
              "data": { "TextField": "some text" }
            }""";

        CaseDetails caseDetails = jsonMapper.readValue(json, CaseDetails.class);

        assertEquals(CASE_TYPE, caseDetails.getCaseTypeId());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5, 678_000_000), caseDetails.getCreatedDate());
        assertEquals(LocalDateTime.of(2026, 2, 3, 4, 5, 6), caseDetails.getLastModified());
        assertEquals("some text", caseDetails.getData().get("TextField"));
    }

    @Test
    void serialisesCaseDetailsToCcdFormat() {
        CaseDetails caseDetails = jsonMapper.readValue(CASE_DETAILS_JSON, CaseDetails.class);

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(caseDetails));

        assertEquals(1658830998852951L, json.get("id").asLong());
        assertEquals(CASE_TYPE, json.get("case_type_id").asString());
        assertEquals("PUBLIC", json.get("security_classification").asString());
        assertEquals(42, json.get("locked_by_user_id").asInt());
        assertEquals(1, json.get("security_level").asInt());
        // Dates must be ISO-8601 strings, not numeric timestamps or arrays
        assertTrue(json.get("created_date").isString());
        assertEquals("2026-01-02T03:04:05.678", json.get("created_date").asString());
        assertEquals("2026-02-03T04:05:06", json.get("last_modified").asString());
        assertEquals(HEARING_ID,
            json.at("/case_data/nextHearingDetails/hearingID").asString());
        assertEquals(HEARING_DATE_TIME,
            json.at("/case_data/nextHearingDetails/hearingDateTime").asString());
        assertFalse(json.has("caseTypeId"));
        assertFalse(json.has("createdDate"));
        assertFalse(json.has("data"));

        CaseDetails roundTripped = jsonMapper.treeToValue(json, CaseDetails.class);
        assertEquals(caseDetails, roundTripped);
    }

    @Test
    void deserialisesStartEventResponseFromCcdFormat() {
        StartEventResponse response = jsonMapper.readValue(START_EVENT_RESPONSE_JSON, StartEventResponse.class);

        assertEquals(EVENT_ID, response.getEventId());
        assertEquals(EVENT_TOKEN, response.getToken());
        assertCaseDetails(response.getCaseDetails());
    }

    @Test
    void serialisesCaseDataContentToCcdFormat() {
        CaseDataContent content = CaseDataContent.builder()
            .caseReference(CASE_REFERENCE)
            .eventToken(EVENT_TOKEN)
            .event(Event.builder().id(EVENT_ID).summary("summary").build())
            .securityClassification(Classification.RESTRICTED)
            .data(Map.of(NEXT_HEARING_DETAILS_FIELD_NAME,
                Map.of(NextHearingDetails.HEARING_ID, HEARING_ID)))
            .build();

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(content));

        assertCaseDataContentJson(json);
        assertEquals("RESTRICTED", json.get("security_classification").asString());
        assertEquals("summary", json.at("/event/summary").asString());
    }

    @Test
    void deserialisesSearchResult() {
        String json = """
            {
              "total": 2,
              "cases": [ %s, { "id": 1111222233334444, "case_type_id": "FT_NextHearingDate" } ],
              "case_types_results": []
            }""".formatted(CASE_DETAILS_JSON);

        SearchResult searchResult = jsonMapper.readValue(json, SearchResult.class);

        assertEquals(2, searchResult.getTotal());
        assertEquals(2, searchResult.getCases().size());
        assertCaseDetails(searchResult.getCases().get(0));
        assertEquals(1111222233334444L, searchResult.getCases().get(1).getId());
    }

    @Test
    void serialisesNextHearingDetails() {
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingID(HEARING_ID)
            .hearingDateTime(LocalDateTime.parse(HEARING_DATE_TIME))
            .caseReference(CASE_REFERENCE)
            .build();

        JsonNode json = jsonMapper.readTree(jsonMapper.writeValueAsString(nextHearingDetails));

        assertEquals(HEARING_ID, json.get(NextHearingDetails.HEARING_ID).asString());
        assertTrue(json.get(NextHearingDetails.HEARING_DATE_TIME).isString());
        assertEquals(HEARING_DATE_TIME, json.get(NextHearingDetails.HEARING_DATE_TIME).asString());
        assertFalse(json.has("hearingId"));
    }

    @Test
    void feignClientDecodesStartEventResponse() {
        stubFor(WireMock.get(urlEqualTo(String.format(TRIGGER_START_EVENT_URL, CASE_REFERENCE, EVENT_ID)))
                    .willReturn(jsonResponse(START_EVENT_RESPONSE_JSON)));

        StartEventResponse response = coreCaseDataApi.startEvent(AUTH, S2S, CASE_REFERENCE, EVENT_ID);

        assertEquals(EVENT_ID, response.getEventId());
        assertEquals(EVENT_TOKEN, response.getToken());
        assertCaseDetails(response.getCaseDetails());
    }

    @Test
    void feignClientEncodesCaseDataContentAndDecodesCaseResource() {
        String submitUrl = String.format(SUBMIT_CASE_EVENT_URL, CASE_REFERENCE);
        stubFor(WireMock.post(urlEqualTo(submitUrl))
                    .willReturn(jsonResponse("""
                        {
                          "id": "1658830998852951",
                          "jurisdiction": "BEFTA_MASTER",
                          "case_type": "FT_NextHearingDate",
                          "created_on": "2026-01-02T03:04:05",
                          "last_modified_on": "2026-02-03T04:05:06.789",
                          "state": "CaseCreated",
                          "security_classification": "PUBLIC",
                          "data": {
                            "nextHearingDetails": { "hearingID": "HRG-12345" }
                          },
                          "callback_response_status_code": 200
                        }""")));

        CaseDataContent content = CaseDataContent.builder()
            .caseReference(CASE_REFERENCE)
            .eventToken(EVENT_TOKEN)
            .event(Event.builder().id(EVENT_ID).build())
            .data(Map.of(NEXT_HEARING_DETAILS_FIELD_NAME,
                Map.of(NextHearingDetails.HEARING_ID, HEARING_ID)))
            .build();

        CaseResource caseResource = coreCaseDataApi.createEvent(AUTH, S2S, CASE_REFERENCE, content);

        List<LoggedRequest> requests = WIRE_MOCK_SERVER.findAll(postRequestedFor(urlEqualTo(submitUrl)));
        assertEquals(1, requests.size());
        assertCaseDataContentJson(jsonMapper.readTree(requests.get(0).getBodyAsString()));

        assertEquals(CASE_REFERENCE, caseResource.getReference());
        assertEquals(CASE_TYPE, caseResource.getCaseType());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5), caseResource.getCreatedOn());
        assertEquals(LocalDateTime.of(2026, 2, 3, 4, 5, 6, 789_000_000), caseResource.getLastModifiedOn());
        assertEquals(Classification.PUBLIC, caseResource.getSecurityClassification());
        assertEquals(200, caseResource.getCallbackResponseStatusCode());
        assertEquals(HEARING_ID, caseResource.getData().get(NEXT_HEARING_DETAILS_FIELD_NAME)
            .get(NextHearingDetails.HEARING_ID).asString());
    }

    @Test
    void feignClientDecodesSearchResult() {
        stubFor(WireMock.post(urlPathEqualTo("/searchCases"))
                    .willReturn(jsonResponse("""
                        { "total": 1, "cases": [ %s ] }""".formatted(CASE_DETAILS_JSON))));

        SearchResult searchResult = coreCaseDataApi.searchCases(AUTH, S2S, CASE_TYPE, "{\"query\":{}}");

        assertEquals(1, searchResult.getTotal());
        assertCaseDetails(searchResult.getCases().get(0));
    }

    private static ResponseDefinitionBuilder jsonResponse(String body) {
        return aResponse()
            .withStatus(HTTP_OK)
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .withBody(body);
    }

    private static void assertCaseDataContentJson(JsonNode json) {
        assertEquals(CASE_REFERENCE, json.get("case_reference").asString());
        assertEquals(EVENT_TOKEN, json.get("event_token").asString());
        assertEquals(EVENT_ID, json.at("/event/id").asString());
        assertFalse(json.get("ignore_warning").asBoolean());
        assertEquals(HEARING_ID, json.at("/data/nextHearingDetails/hearingID").asString());
        assertFalse(json.has("caseReference"));
        assertFalse(json.has("eventToken"));
        assertFalse(json.has("ignoreWarning"));
    }

    @SuppressWarnings("unchecked")
    private static void assertCaseDetails(CaseDetails caseDetails) {
        assertEquals(1658830998852951L, caseDetails.getId());
        assertEquals("BEFTA_MASTER", caseDetails.getJurisdiction());
        assertEquals(CASE_TYPE, caseDetails.getCaseTypeId());
        assertEquals(LocalDateTime.of(2026, 1, 2, 3, 4, 5, 678_000_000), caseDetails.getCreatedDate());
        assertEquals(LocalDateTime.of(2026, 2, 3, 4, 5, 6), caseDetails.getLastModified());
        assertEquals("CaseCreated", caseDetails.getState());
        assertEquals(42, caseDetails.getLockedBy());
        assertEquals(1, caseDetails.getSecurityLevel());
        assertEquals(Classification.PUBLIC, caseDetails.getSecurityClassification());
        assertEquals("CALLBACK_COMPLETED", caseDetails.getCallbackResponseStatus());
        assertEquals(3, caseDetails.getVersion());

        Map<String, Object> data = caseDetails.getData();
        assertEquals("some text", data.get("TextField"));
        assertEquals(123, data.get("NumberField"));
        // CcdCaseEventService reads these back as raw strings from the untyped case data map
        Map<String, Object> nextHearingDetails = (Map<String, Object>) data.get(NEXT_HEARING_DETAILS_FIELD_NAME);
        assertEquals(HEARING_ID, nextHearingDetails.get(NextHearingDetails.HEARING_ID));
        assertEquals(HEARING_DATE_TIME, nextHearingDetails.get(NextHearingDetails.HEARING_DATE_TIME));
    }
}
