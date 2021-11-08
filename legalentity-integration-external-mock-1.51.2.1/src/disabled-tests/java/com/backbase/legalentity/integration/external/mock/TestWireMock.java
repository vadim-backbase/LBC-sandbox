package com.backbase.legalentity.integration.external.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;

import com.backbase.buildingblocks.common.HttpCommunicationConstants;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("it")
@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "wiremock=true",
    "backbase.audit.enabled=false",
    "backbase.communication.services.dbs.approval.presentation.serviceId=localhost",
    "backbase.communication.services.dbs.approval.persistence.serviceId=localhost",
    "backbase.communication.services.dbs.approval.persistence.baseUri=/service-api/v2"
})

@SpringBootTest(
    classes = Application.class,
    webEnvironment = WebEnvironment.RANDOM_PORT
)
public abstract class TestWireMock {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestWireMock.class);

    protected static String baseServiceUrl = "/service-api/v2";
    protected static String baseClientUrl = "/client-api/v2";

    public static final String TEST_SERVICE_TOKEN
        = "Bearer eyJraWQiOiI1eFRhc09Hbko4TWVqMGxOQ2p2WmpEcEFoaFAyN0xqTGVFWlBZZlZKbEVBPSIsImFsZyI6I"
        + "khTMjU2In0.eyJzdWIiOiJteS1zZXJ2aWNlIiwic2NvcGUiOlsiYXBpOnNlcnZpY2UiXSwiZXhwIjoyMTQ3NDgzN"
        + "jQ3LCJpYXQiOjE0ODQ4MjAxOTZ9.CoAVba-NyHZ4NNn6-aw0GUQhZptmDNxBbQ2N7HpgSxQ";

    public static final String TOKEN_RESPONSE =
        "{\n"
            + "    \"access_token\": \"" + TEST_SERVICE_TOKEN.substring(7, TEST_SERVICE_TOKEN.length())
            + "\",\n"
            + "    \"token_type\": \"bearer\",\n"
            + "    \"expires_in\": 299,\n"
            + "    \"scope\": \"api:service\",\n"
            + "    \"sub\": \"bb-client\",\n"
            + "    \"iss\": \"token-converter\",\n"
            + "    \"jti\": \"611dded8-54eb-4dc2-abf0-60b26cf2eddc\"\n"
            + "}\n";

    @LocalServerPort
    protected int serverPort;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private InternalContextService internalContextService;
    @Autowired
    private WireMockServer wireMockServer;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private List<StubMapping> stubMappings = new ArrayList<>();

    private String baseUrlClient(String path) {
        return "http://localhost:" + serverPort + baseClientUrl + path;
    }

    private String baseUrlService(String path) {
        return "http://localhost:" + serverPort + baseServiceUrl + path;
    }

    @Before
    public void initialSetup() {
        stubMappings.clear();
        wireMockServer.resetRequests();
        createTokenSub();
    }

    private void createTokenSub() {
        wireMockServer.addStubMapping(WireMock.post(urlPathMatching("/token*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                .withBody(TOKEN_RESPONSE)).build());
    }

    protected void addStubPost(String url, Object response, int status) {
        addStubPost(url, response, status, new HashMap<>());
    }

    protected void addStubPost(String url, Object response, int status, Map<String, String> headers) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern);

        addStub(method, response, status, headers);
    }

    protected void addStubGet(String url, Object response, int status) {
        addStubGet(url, response, status, new HashMap<>());
    }

    protected void addStubGet(String url, Object response, int status, Map<String, String> headers) {

        UrlPattern urlPathPattern = urlEqualTo(url);
        MappingBuilder method = WireMock.get(urlPathPattern);

        addStub(method, response, status, headers);
    }


    protected void addStubPut(String url, Object response, int status) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern);

        addStub(method, response, status, new HashMap<>());
    }

    protected void addStubDelete(String url, Object response, int status) {
        addStubDelete(url, response, status, new HashMap<>());
    }

    protected void addStubDelete(String url, Object response, int status,
        Map<String, String> headers) {

        UrlPattern urlPathPattern = urlEqualTo(url);
        MappingBuilder method = WireMock.delete(urlPathPattern);

        addStub(method, response, status, new HashMap<>());
    }

    protected void addStubPostContains(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern)
            .withRequestBody(containing(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>());
    }

    protected void addStubPostEqualToJson(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern)
            .withRequestBody(equalToJson(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>());
    }

    protected void addStubPutContains(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern)
            .withRequestBody(containing(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>());
    }

    protected void addStubPutEqualToJson(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern)
            .withRequestBody(equalToJson(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>());
    }

    private void addStub(MappingBuilder method, Object response, int status,
        Map<String, String> headers) {

        if (!headers.containsKey("X-TID")) {
            headers.put("X-TID", "T1");
        }

        headers.forEach((key, value) -> method.withHeader(key, containing(value)));
        StubMapping stubMapping = method
            .willReturn(aResponse()
                .withStatus(status)
                .withHeader("Content-Type", "application/json")
                .withHeader(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
                .withBody(getStringFromObject(response))).build();
        stubMappings.add(stubMapping);
        wireMockServer.addStubMapping(stubMapping);
    }

    protected String getStringFromObject(Object object) {
        String responseString = null;
        if (Objects.isNull(object)) {
            return "";
        }

        if (object instanceof String) {
            responseString = (String) object;
        } else {
            try {
                responseString = objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return responseString;
    }

    private void stubGetUserDetails(String userName, String legalEntityId, String userId, String tId) {
        String userResponse = "{\n"
            + "  \"externalId\": \"" + userName + "\",\n"
            + "  \"legalEntityId\": \"" + legalEntityId + "\",\n"
            + "  \"id\": \"" + userId + "\"\n"
            + "}";

        HashMap<String, String> headers = new HashMap<>();
        if (Objects.nonNull(tId)) {
            headers.put("X-TID", tId);
        }
        addStubGet("/service-api/v2/users/externalId/" + userName, userResponse, 200, headers);
    }

    private void stubCheckPermissions(String serviceAgreementId, boolean hasPermissions, String userId) {
        StubMapping stubMapping = WireMock.get(WireMock.urlPathMatching(
            "^/service-api/v2/accessgroups/users/permission.*"))
            .withQueryParam("userId", new EqualToPattern(userId))
            .withQueryParam("serviceAgreementId", new EqualToPattern(serviceAgreementId))
            .withQueryParam("resourceName", new RegexPattern(".*"))
            .withQueryParam("functionName", new RegexPattern(".*"))
            .withQueryParam("privileges", new RegexPattern(".*"))
            .willReturn(aResponse()
                .withStatus(hasPermissions ? 200 : 403)).build();

        wireMockServer.addStubMapping(stubMapping);
    }

    protected String executeClientRequest(String url, Object request,
        String userName, String serviceAgreementId, boolean hasPermissions, HttpMethod method)
        throws IOException, JSONException {

        String legalEntityId = getUuid().replace("-", "");
        String userId = getUuid().replace("-", "");

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, new HashMap<>()).getBody();
    }

    protected String executeClientRequest(String url, Object request,
        String userName, String serviceAgreementId, boolean hasPermissions, HttpMethod method,
        Map<String, String> additionalHeaders)
        throws IOException, JSONException {

        String legalEntityId = getUuid().replace("-", "");
        String userId = getUuid().replace("-", "");

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, additionalHeaders).getBody();
    }

    protected ResponseEntity<String> executeClientRequestReturnFullResponse(String url, Object request,
        String userName, String serviceAgreementId, boolean hasPermissions, HttpMethod method,
        Map<String, String> additionalHeaders)
        throws IOException, JSONException {

        String legalEntityId = getUuid().replace("-", "");
        String userId = getUuid().replace("-", "");

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, additionalHeaders);
    }

    protected String executeClientRequest(String url, Object request,
        String userName, String userId, String serviceAgreementId, boolean hasPermissions, HttpMethod method)
        throws IOException, JSONException {
        String legalEntityId = getUuid().replace("-", "");

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, new HashMap<>()).getBody();
    }

    protected String executeClientRequest(String url, Object request,
        String userName, String userId, String serviceAgreementId, String legalEntityId, boolean hasPermissions,
        HttpMethod method)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, new HashMap<>()).getBody();
    }

    protected ResponseEntity<String> executeClientRequestEntity(String url, Object request,
        String userName, String userId, String serviceAgreementId, boolean hasPermissions, HttpMethod method)
        throws IOException, JSONException {

        String legalEntityId = getUuid().replace("-", "");

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, new HashMap<>());
    }

    protected ResponseEntity<String> executeClientRequestEntity(String url, Object request,
        String userName, String userId, String serviceAgreementId, String legalEntityId, boolean hasPermissions,
        HttpMethod method)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName, userId, serviceAgreementId, legalEntityId,
            hasPermissions, method, new HashMap<>());
    }

    private ResponseEntity<String> getExecuteClientRequest(String url, Object request,
        String userName, String userId, String serviceAgreementId, String legalEntityId, boolean hasPermissions,
        HttpMethod method,
        Map<String, String> additionalHeaders)
        throws IOException, JSONException {

        String tId = additionalHeaders.containsKey("X-TID") ? additionalHeaders.get("X-TID") : null;

        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach(headers::set);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer "
            + internalContextService
            .createInternalContextToken(userName, serviceAgreementId, legalEntityId, userId, tId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        stubCheckPermissions(serviceAgreementId, hasPermissions, userId);

        return getExecuteRequestEntity(baseUrlClient(url), request, method, headers);
    }

    protected String executeServiceRequest(String url, Object request, String userName, String serviceAgreementId,
        HttpMethod method) throws IOException, JSONException {

        return executeServiceRequest(url, request, userName, serviceAgreementId, "", "", method, new HashMap<>());
    }

    protected String executeServiceRequest(String url, Object request, String userName, String serviceAgreementId,
        String userId, String leId,
        HttpMethod method, Map<String, String> additionalHeaders) throws IOException, JSONException {

        String tId = additionalHeaders.containsKey("X-TID") ? additionalHeaders.get("X-TID") : null;

        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach(headers::set);
        headers.set(HttpCommunicationConstants.X_CXT_USER_TOKEN,
            internalContextService.createInternalContextToken(userName, serviceAgreementId, leId, userId, tId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return getExecuteServiceRequest(url, request, method, headers).getBody();
    }

    protected String executeRequest(String url, Object request, HttpMethod method) {
        return getExecuteServiceRequest(url, request, method, new HttpHeaders()).getBody();
    }

    protected ResponseEntity<String> executeRequestEntity(String url, Object request, HttpMethod method) {
        return getExecuteServiceRequest(url, request, method, new HttpHeaders());
    }

    private ResponseEntity<String> getExecuteServiceRequest(String url, Object request, HttpMethod method,
        HttpHeaders headers) {
        headers.set(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return getExecuteRequestEntity(baseUrlService(url), request, method, headers);
    }

    private ResponseEntity<String> getExecuteRequestEntity(String url, Object request, HttpMethod method,
        HttpHeaders headers) {
        if (!headers.containsKey("X-TID")) {
            headers.set("X-TID", "T1");
        }
        HttpEntity<String> entity = new HttpEntity<>(getStringFromObject(request), headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new HttpErrorHandler());

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            method,
            entity,
            new ParameterizedTypeReference<String>() {
            });
        return response;
    }

    @After
    public void checkAndCleanStubs() {

        List<LoggedRequest> unmatched = wireMockServer.findAllUnmatchedRequests();

        for (LoggedRequest log : unmatched) {
            System.err.println("Unmatched :" + log);
        }

        Assert.assertEquals(0, unmatched.size());

        List<StubMapping> eventList = wireMockServer.getAllServeEvents().stream().map(s -> s.getStubMapping())
            .collect(Collectors.toList());

        stubMappings.forEach(s -> {
            if (!eventList.contains(s)) {
                System.err.println("NOT USED STUB: " + s);
            }
        });

        Assert.assertTrue(eventList.containsAll(stubMappings));

        stubMappings.forEach(stub -> wireMockServer.removeStub(stub));
        wireMockServer.resetMappings();
    }

    private class HttpErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
            return httpResponse.getStatusCode().series() == CLIENT_ERROR;
        }

        @Override
        public void handleError(ClientHttpResponse httpResponse) throws IOException {
            switch (httpResponse.getStatusCode().value()) {
                case 400:
                    throw (BadRequestException) objectMapper
                        .readValue(httpResponse.getBody(), new TypeReference<BadRequestException>() {
                        });
                case 404:
                    throw (NotFoundException) objectMapper
                        .readValue(httpResponse.getBody(), new TypeReference<NotFoundException>() {
                        });
                case 403:
                    throw (ForbiddenException) objectMapper
                        .readValue(httpResponse.getBody(), new TypeReference<ForbiddenException>() {
                        });
                default:
                    throw new RuntimeException();
            }
        }
    }

    public <T> T readValue(String content, TypeReference<T> valueTypeRef)
        throws IOException, JsonParseException, JsonMappingException {
        return objectMapper.readValue(content, valueTypeRef);
    }

    public <T> T readValue(String content, Class<T> valueType)
        throws IOException, JsonParseException, JsonMappingException {
        return objectMapper.readValue(content, valueType);
    }

    public <T> T convertValue(Object fromValue, Class<T> toValueType) throws IllegalArgumentException {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    public static String getUuid() {
        return UUID.randomUUID().toString();
    }
}