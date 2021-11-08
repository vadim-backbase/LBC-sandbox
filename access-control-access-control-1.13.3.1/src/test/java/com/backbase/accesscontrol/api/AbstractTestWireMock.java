package com.backbase.accesscontrol.api;

import static com.backbase.accesscontrol.api.TestConstants.TEST_SERVICE_TOKEN;
import static com.backbase.accesscontrol.api.TestConstants.TOKEN_RESPONSE;
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
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractTestWireMock {

    protected static String baseServiceUrl = "/service-api/v2";
    protected static String baseClientUrl = "/client-api/v2";

    @LocalServerPort
    protected int serverPort;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected InternalContextService internalContextService;
    @Autowired
    private WireMockServer wireMockServer;

    private List<StubMapping> stubMappings = new ArrayList<>();

    protected String baseUrlClient(String path) {
        return "http://localhost:" + serverPort + baseClientUrl + path;
    }

    protected String baseUrlService(String path) {
        return "http://localhost:" + serverPort + baseServiceUrl + path;
    }

    @Before
    public void initialSetup() {
        stubMappings.clear();
        wireMockServer.resetRequests();
        createTokenSub();
    }

    public String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void createTokenSub() {
        wireMockServer.addStubMapping(WireMock.post(urlPathMatching("/token*"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                .withBody(TOKEN_RESPONSE)).build());
    }

    protected void addStubPost(String url, Object response, int status) {
        addStubPost(url, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubPost(String url, Object response, int status, Map<String, String> headers,
        Map<String, String> responseHeaders) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern);

        addStub(method, response, status, headers, responseHeaders);
    }

    protected void addStubGet(String url, Object response, int status) {
        addStubGet(url, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubGet(String url, Object response, int status, Map<String, String> headers,
        Map<String, String> responseHeaders) {

        UrlPattern urlPathPattern = urlEqualTo(url);
        MappingBuilder method = WireMock.get(urlPathPattern);

        addStub(method, response, status, headers, responseHeaders);
    }

    protected void addStubPut(String url, Object response, int status) {
        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern);

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubDelete(String url, Object response, int status) {
        addStubDelete(url, response, status, new HashMap<>());
    }

    protected void addStubDelete(String url, Object response, int status,
        Map<String, String> headers) {

        UrlPattern urlPathPattern = urlEqualTo(url);
        MappingBuilder method = WireMock.delete(urlPathPattern);

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubPostContains(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern)
            .withRequestBody(containing(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubPostEqualToJson(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.post(urlPathPattern)
            .withRequestBody(equalToJson(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubPutContains(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern)
            .withRequestBody(containing(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    protected void addStubPutEqualToJson(String url, Object response, int status, Object substringMatching) {

        UrlPathPattern urlPathPattern = urlPathEqualTo(url);
        MappingBuilder method = WireMock.put(urlPathPattern)
            .withRequestBody(equalToJson(getStringFromObject(substringMatching)));

        addStub(method, response, status, new HashMap<>(), new HashMap<>());
    }

    private void addStub(MappingBuilder method, Object response, int status,
        Map<String, String> headers, Map<String, String> responseHeaders) {

        headers.forEach((key, value) -> method.withHeader(key, containing(value)));

        responseHeaders.put("Content-Type", "application/json");
        responseHeaders.put(HttpHeaders.AUTHORIZATION, TestConstants.TEST_SERVICE_TOKEN);
        com.github.tomakehurst.wiremock.http
            .HttpHeaders httpHeaders = new com.github.tomakehurst.wiremock.http
            .HttpHeaders(responseHeaders.entrySet().stream()
            .map(header ->
                new HttpHeader(header.getKey(), header.getValue()))
            .collect(Collectors.toSet()));

        StubMapping stubMapping = method
            .willReturn(aResponse()
                .withStatus(status)
                .withHeaders(httpHeaders)
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

    protected String executeServiceRequest(String url, Object request, String userName, String serviceAgreementId,
        HttpMethod method) throws IOException, JSONException {

        return executeServiceRequest(url, request, userName, serviceAgreementId, "", "", method, new HashMap<>());
    }

    protected String executeServiceRequest(String url, Object request, String userName, String serviceAgreementId,
        String userId, String leId,
        HttpMethod method, Map<String, String> additionalHeaders) throws IOException, JSONException {

        String tId = additionalHeaders.getOrDefault("X-TID", null);

        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach(headers::set);
        headers.set(HttpCommunicationConstants.X_CXT_USER_TOKEN,
            internalContextService.createInternalContextToken(userName, serviceAgreementId, leId, userId, tId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return getExecuteServiceRequest(url, request, method, headers).getBody();
    }

    protected ResponseEntity<String> executeServiceRequestReturnResponseEntity(String url, Object request, String userName, String serviceAgreementId,
        String userId, String leId,
        HttpMethod method, Map<String, String> additionalHeaders) throws IOException, JSONException {

        String tId = additionalHeaders.getOrDefault("X-TID", null);

        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach(headers::set);
        headers.set(HttpCommunicationConstants.X_CXT_USER_TOKEN,
            internalContextService.createInternalContextToken(userName, serviceAgreementId, leId, userId, tId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return getExecuteServiceRequest(url, request, method, headers);
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

    protected ResponseEntity<String> getExecuteRequestEntity(String url, Object request, HttpMethod method,
        HttpHeaders headers) {
        if (!headers.containsKey("X-TID")) {
            headers.set("X-TID", "T1");
        }
        HttpEntity<String> entity = new HttpEntity<>(getStringFromObject(request), headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new HttpErrorHandler());

        return restTemplate.exchange(
            url,
            method,
            entity,
            new ParameterizedTypeReference<String>() {
            });
    }

    @After
    public void checkAndCleanStubs() {

        List<LoggedRequest> unmatched = wireMockServer.findAllUnmatchedRequests();

        for (LoggedRequest log : unmatched) {
            System.err.println("Unmatched :" + log);
        }

        Assert.assertEquals(0, unmatched.size());

        List<StubMapping> eventList = wireMockServer.getAllServeEvents().stream().map(ServeEvent::getStubMapping)
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
                    String response = IOUtils.toString(httpResponse.getBody(), StandardCharsets.UTF_8);
                    if (isaXmlContent(response)) {
                        throw new BadRequestException(getErrorMessage(response));
                    }
                    throw (BadRequestException) objectMapper
                        .readValue(response, new TypeReference<BadRequestException>() {
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

        private String getErrorMessage(String response) {
            response = response.replaceAll("<ErrorResponse>", "")
                .replaceAll("<message>", "")
                .replaceAll("</ErrorResponse>", "")
                .replaceAll("</message>", "");
            return response;
        }

        private boolean isaXmlContent(String response) {
            return response.startsWith("<");
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
}