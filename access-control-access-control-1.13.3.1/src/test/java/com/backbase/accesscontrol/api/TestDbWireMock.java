package com.backbase.accesscontrol.api;

import static com.backbase.accesscontrol.domain.enums.AssignablePermissionType.REGULAR_USER_DEFAULT;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.repository.ApplicableFunctionPrivilegeJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementRefJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.BusinessFunctionJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupItemEntityRepository;
import com.backbase.accesscontrol.repository.FunctionGroupItemEntityJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.PrivilegeJpaRepository;
import com.backbase.accesscontrol.repository.SelfApprovalPolicyJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementAdminJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Transformer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "wiremock=true",
    "backbase.audit.enabled=false",
    "backbase.data-group.validation.enabled=false"
})
@SpringBootTest(
    classes = {Application.class, TestDbWireMock.TestProcessor.class},
    webEnvironment = WebEnvironment.RANDOM_PORT
)
public abstract class TestDbWireMock extends AbstractTestWireMock {

    protected static final String TEST_SERVICE_TOKEN
        = "Bearer eyJraWQiOiJlNjJXTTRyamlOMUpcL0N3M0d3ZXBEbURNTklhWm9uOHJnW"
        + "kN0YXVLd1Y1TT0iLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteS1zZXJ2aWNlIiwi"
        + "c2NvcGUiOlsiYXBpOnNlcnZpY2UiXSwiZXhwIjoyMTQ3NDgzNjQ3LCJpYXQiOjE0"
        + "ODQ4MjAxOTZ9.dOTROxf5p7fH4d00d4Ugl9HNY2zxpWpem38bn_J-ceg";

    @Autowired
    protected PlatformTransactionManager transactionManager;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected FunctionGroupItemEntityRepository functionGroupItemEntityRepository;
    @Autowired
    protected DataGroupJpaRepository dataGroupJpaRepository;
    @Autowired
    protected DataGroupItemJpaRepository dataGroupItemJpaRepository;
    @Autowired
    protected ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    protected LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    protected ApprovalFunctionGroupRefJpaRepository approvalFunctionGroupRefJpaRepository;
    @Autowired
    protected ApprovalUserContextAssignFunctionGroupJpaRepository approvalUserContextAssignFunctionGroupJpaRepository;
    @Autowired
    protected ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Autowired
    protected BusinessFunctionJpaRepository businessFunctionJpaRepository;
    @Autowired
    protected PrivilegeJpaRepository privilegeJpaRepository;
    @Autowired
    protected ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository;
    @Autowired
    protected FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    protected UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Autowired
    protected UserAssignedCombinationRepository userAssignedCombinationRepository;
    @Autowired
    protected UserContextJpaRepository userContextJpaRepository;
    @Autowired
    protected ParticipantJpaRepository participantJpaRepository;
    @Autowired
    protected ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    @Autowired
    protected ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    @Autowired
    protected AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Autowired
    protected AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Autowired
    protected BusinessFunctionCache businessFunctionCache;
    @Autowired
    protected ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    @Autowired
    protected ServiceAgreementAdminJpaRepository serviceAgreementAdminJpaRepository;
    @Autowired
    protected ApprovalServiceAgreementJpaRepository approvalServiceAgreementJpaRepository;
    @Autowired
    protected ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;
    @Autowired
    protected SelfApprovalPolicyJpaRepository selfApprovalPolicyJpaRepository;
    @Autowired
    protected FunctionGroupItemEntityJpaRepository functionGroupItemEntityJpaRepository;
    @Autowired
    protected RepositoryCleaner repositoryCleaner;

    @Autowired
    private Processor processor;
    @Autowired
    private MessageCollector messageCollector;

    protected LegalEntity rootLegalEntity;
    protected String contextUserId = getUuid();
    protected ServiceAgreement rootMsa;
    protected AssignablePermissionSet apsDefaultRegular;

    @Before
    public void setUpRestTemplate() {

        tearDown();

        setUpInitialData();

        messageCollector.forChannel(processor.output()).clear();
    }

    protected void verifyServiceAgreementEvents(Collection<ServiceAgreementEvent> events) throws IOException {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<ServiceAgreementEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    protected void verifyLegalEntityEvents(Collection<LegalEntityEvent> events) {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<LegalEntityEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    protected void verifyFunctionGroupEvents(Collection<FunctionGroupEvent> events) throws IOException {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<FunctionGroupEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    protected void verifyDataGroupEvents(Collection<DataGroupEvent> events) throws IOException {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<DataGroupEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    protected void verifyUserContextEvents(Collection<UserContextEvent> events) throws IOException {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<UserContextEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    protected void verifyAssignablePermissionSetEvents(Collection<AssignablePermissionSetEvent> events) throws IOException {

        assertTrue(messageCollector.forChannel(processor.output()).stream()
            .map(msg -> msg.getPayload())
            .map(payload -> convertEvent((String) payload, new TypeReference<AssignablePermissionSetEvent>() {}))
            .collect(Collectors.toList())
            .containsAll(events));
    }

    private Event convertEvent(String payload, TypeReference<? extends Event> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception err) {
            return null;
        }
    }

    protected void setUpInitialData() {
        rootDataSetup();
    }

    protected void rootDataSetup() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {

            apsDefaultRegular = assignablePermissionSetJpaRepository
                .findFirstByType(REGULAR_USER_DEFAULT.getValue()).get();

            rootLegalEntity = legalEntityJpaRepository.save(new LegalEntity()
                .withExternalId("BANK001")
                .withName("BANK")
                .withType(LegalEntityType.BANK));

            rootMsa = new ServiceAgreement()
                .withName("rootMsa")
                .withDescription("rootMsa")
                .withExternalId("externalRootMsa")
                .withCreatorLegalEntity(rootLegalEntity)
                .withMaster(true);
            rootMsa.addParticipant(new Participant()
                .withShareUsers(true)
                .withShareAccounts(true)
                .withLegalEntity(rootLegalEntity));
            rootMsa = serviceAgreementJpaRepository.save(rootMsa);

            return true;
        });
    }
    
    protected String executeClientRequest(String url, HttpMethod method, Object request, String userName,
                    Map<String, String> functionPrivileges) throws IOException, JSONException {
        return getExecuteClientRequest(url, request, userName, method, new HashMap<>(), functionPrivileges,
                        null, null, false).getBody();
    }

    protected String executeClientRequest(String url, HttpMethod method, Object request,
        String userName, String function, String privileges)
        throws IOException, JSONException {

        return executeClientRequest(url, method, request, userName,
            new HashMap<>(), function, privileges);
    }

    protected String executeClientRequest(String url, HttpMethod method, Object request,
        String userName,
        Map<String, String> additionalHeaders, String function, String privileges)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName,
            method, additionalHeaders, function, privileges, null, null).getBody();
    }

    protected String executeClientRequest(String url, HttpMethod method,
        String userName)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, null, userName,
            method, new HashMap<>(), "", "", null, null).getBody();
    }

    protected String executeClientRequestWithContext(String url, HttpMethod method,
        String userName, UserContext userContext, String contextLegalEntityId)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, null, userName,
            method, new HashMap<>(), "", "", userContext, contextLegalEntityId).getBody();
    }

    protected String executeClientRequestWithContext(String url, HttpMethod method, String request,
        String userName, String function, String privileges, UserContext userContext, String contextLegalEntityId)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName, method, new HashMap<>(), function, privileges,
            userContext, contextLegalEntityId).getBody();
    }

    protected String executeClientRequest(String url, HttpMethod method,
        String userName, String function, String privileges)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, null, userName,
            method, new HashMap<>(), function, privileges, null, null).getBody();
    }


    protected ResponseEntity<String> executeClientRequestEntity(String url, HttpMethod method, Object request,
        String userName)
        throws IOException, JSONException {

        return executeClientRequestEntity(url, method, request, userName, "", "");
    }

    protected ResponseEntity<String> executeClientRequestEntity(String url, HttpMethod method, Object request,
        String userName, String function, String privileges)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName,
            method, new HashMap<>(), function, privileges, null, null);
    }

    protected ResponseEntity<String> executeClientRequestEntityWithUserContext(String url, HttpMethod method,
        Object request, String userName, String function, String privileges, UserContext withUserContext,
        String contextLegalEntityId)
        throws IOException, JSONException {

        return getExecuteClientRequest(url, request, userName,
            method, new HashMap<>(), function, privileges, withUserContext, contextLegalEntityId);
    }

    private ResponseEntity<String> getExecuteClientRequest(String url, Object request, String userName,
        HttpMethod method, Map<String, String> additionalHeaders, String function, String privileges,
        UserContext withUserContext, String contextLegalEntityId)
        throws IOException, JSONException {
        return getExecuteClientRequest(url, request, userName, method, additionalHeaders, Map.of(function, privileges),
            withUserContext, contextLegalEntityId, false);
    }

    private ResponseEntity<String> getExecuteClientRequest(String url, Object request, String userName,
        HttpMethod method, Map<String, String> additionalHeaders, Map<String, String> functionPrivileges,
        UserContext withUserContext, String contextLegalEntityId, boolean skipErrorHandling)
        throws IOException, JSONException {

        String serviceAgreementId =
            nonNull(withUserContext) ? withUserContext.getServiceAgreementId() : rootMsa.getId();
        String legalEntityId = nonNull(contextLegalEntityId) ? contextLegalEntityId : rootLegalEntity.getId();
        String userId = nonNull(withUserContext) ? withUserContext.getUserId() : contextUserId;

        String tId = additionalHeaders.getOrDefault("X-TID", null);

        HttpHeaders headers = new HttpHeaders();
        additionalHeaders.forEach(headers::set);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer "
            + internalContextService
            .createInternalContextToken(userName, serviceAgreementId, legalEntityId, userId, tId));
        headers.setContentType(MediaType.APPLICATION_JSON);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        if (nonNull(tId)) {
            setTenant(tId);
        }
        
        Set<String> applicableFunctionPrivileges = new HashSet<>();
        for (Entry<String, String> entry : functionPrivileges.entrySet()) {
            applicableFunctionPrivileges.addAll(businessFunctionCache.getByFunctionNameOrResourceNameOrPrivilegesOptional(entry.getKey(),
                                            null, Lists.newArrayList(entry.getValue().split(","))));
        }
        
        transactionTemplate.execute(transactionStatus -> {

            FunctionGroup functionGroup = createFunctionGroup(getUuid(), getUuid(),
                serviceAgreementJpaRepository.findById(rootMsa.getId()).get(),
                applicableFunctionPrivileges, FunctionGroupType.DEFAULT);

            UserContext userContext = userContextJpaRepository
                .findByUserIdAndServiceAgreementId(userId, serviceAgreementId)
                .orElseGet(() -> userContextJpaRepository.save(new UserContext(userId, serviceAgreementId)));

            userAssignedFunctionGroupJpaRepository.save(new UserAssignedFunctionGroup()
                .withFunctionGroup(functionGroup)
                .withUserContext(userContext));
            return true;
        });

        if (nonNull(tId)) {
            setTenant(null);
        }
        return getExecuteRequestEntity(baseUrlClient(url), request, method, headers);
    }

    protected void setTenant(String tenantId) {

        throw new UnsupportedOperationException("Not multi tenancy test");
    }

    protected AssignablePermissionSet createAssignablePermissionSet(
        String name,
        AssignablePermissionType assignablePermissionType,
        String description,
        String... afpIds) {

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName(name);
        assignablePermissionSet.setType(assignablePermissionType);
        assignablePermissionSet.setDescription(description);

        assignablePermissionSet.setPermissions(new HashSet<>(Arrays.asList(afpIds)));

        return assignablePermissionSet;
    }


    public void tearDown() {
        repositoryCleaner.clean();
    }

    public FunctionGroup createFunctionGroup(String name, String description,
        ServiceAgreement serviceAgreement, Collection<String> applicableFunctionPrivilegeIds,
        FunctionGroupType functionGroupType) {
        return functionGroupJpaRepository.save(
            new FunctionGroup()
                .withServiceAgreement(serviceAgreement)
                .withName(name)
                .withDescription(description)
                .withType(functionGroupType)
                .withPermissions(applicableFunctionPrivilegeIds
                    .stream()
                    .map(afpId -> new GroupedFunctionPrivilege(null, afpId))
                    .collect(Collectors.toSet())));
    }

    @EnableBinding(Processor.class)
    public static class TestProcessor {

        @Autowired
        protected ObjectMapper objectMapper;

        private static final Logger LOGGER = LoggerFactory.getLogger(TestProcessor.class);

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent",
                outputChannel = Processor.OUTPUT)
        public ServiceAgreementEvent transformServiceAgreementEvent(ServiceAgreementEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent",
                outputChannel = Processor.OUTPUT)
        public FunctionGroupEvent transformFunctionGroupEvent(FunctionGroupEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent",
                outputChannel = Processor.OUTPUT)
        public DataGroupEvent transformDataGroupEvent(DataGroupEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent",
                outputChannel = Processor.OUTPUT)
        public AssignablePermissionSetEvent transformAssignablePermissionSetEvent(AssignablePermissionSetEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent",
                outputChannel = Processor.OUTPUT)
        public UserContextEvent transformUserContextEvent(UserContextEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }

        @Transformer(inputChannel = "com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent",
                outputChannel = Processor.OUTPUT)
        public LegalEntityEvent transformLegalEntityEvent(LegalEntityEvent value) {
            LOGGER.info("processing: {}", value);
            return value;
        }
    }

}