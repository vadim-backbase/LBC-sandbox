package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_064;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.service.rest.spec.model.UserAssignedFunctionGroupResponse;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementServiceApiController#getUsers(String, String, Integer, Integer)}
 */
public class GetServiceAgreementUsersWithAssignedFunctionGroupIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements/{serviceAgreementId}/function-groups/{functionGroupId}/users";

    private LegalEntity rootLegalEntity;
    private LegalEntity childLegalEntity;
    private LegalEntity grandChildLegalEntity;
    private ServiceAgreement rootLegalEntityMSA;
    private FunctionGroup defaultFGForRootLegalEntityMSA;
    private FunctionGroup templateFGForRootLegalEntityMSA;

    @Before
    public void setUp() throws Exception {
        initLegalEntities();

        initServiceAgreements();

        initFunctionGroups();
    }

    private void initLegalEntities() {
        rootLegalEntity = new LegalEntity();
        rootLegalEntity.setName("root-le-name");
        rootLegalEntity.setExternalId("root-le-ext-id");
        rootLegalEntity.setType(LegalEntityType.BANK);
        rootLegalEntity = legalEntityJpaRepository.save(rootLegalEntity);

        childLegalEntity = new LegalEntity();
        childLegalEntity.setName("le-child");
        childLegalEntity.setExternalId("le-child-ext-id");
        childLegalEntity.setType(LegalEntityType.CUSTOMER);
        childLegalEntity.setParent(rootLegalEntity);
        childLegalEntity = legalEntityJpaRepository.save(childLegalEntity);

        grandChildLegalEntity = new LegalEntity();
        grandChildLegalEntity.setName("le-grand-child");
        grandChildLegalEntity.setExternalId("le-grand-child-ext-id");
        grandChildLegalEntity.setType(LegalEntityType.CUSTOMER);
        grandChildLegalEntity.setParent(childLegalEntity);
        grandChildLegalEntity = legalEntityJpaRepository.save(grandChildLegalEntity);
    }

    private void initServiceAgreements() {
        rootLegalEntityMSA = new ServiceAgreement();
        rootLegalEntityMSA.setName("ROOT-LE-MSA");
        rootLegalEntityMSA.setDescription("root-le-msa");
        rootLegalEntityMSA.setCreatorLegalEntity(rootLegalEntity);
        rootLegalEntityMSA.setMaster(true);
        rootLegalEntityMSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        rootLegalEntityMSA = serviceAgreementJpaRepository.save(rootLegalEntityMSA);
    }

    private void initFunctionGroups() {
        defaultFGForRootLegalEntityMSA = new FunctionGroup();
        defaultFGForRootLegalEntityMSA.setName("default-fg");
        defaultFGForRootLegalEntityMSA.setDescription("default");
        defaultFGForRootLegalEntityMSA.setType(FunctionGroupType.DEFAULT);
        defaultFGForRootLegalEntityMSA.setServiceAgreement(rootLegalEntityMSA);
        defaultFGForRootLegalEntityMSA = functionGroupJpaRepository.save(defaultFGForRootLegalEntityMSA);

        templateFGForRootLegalEntityMSA = new FunctionGroup();
        templateFGForRootLegalEntityMSA.setName("template-fg");
        templateFGForRootLegalEntityMSA.setDescription("template");
        templateFGForRootLegalEntityMSA.setType(FunctionGroupType.TEMPLATE);
        templateFGForRootLegalEntityMSA.setServiceAgreement(rootLegalEntityMSA);
        templateFGForRootLegalEntityMSA.setAssignablePermissionSet(apsDefaultRegular);
        templateFGForRootLegalEntityMSA = functionGroupJpaRepository.save(templateFGForRootLegalEntityMSA);
    }

    @Test
    public void shouldGetFirstUserIdWithAssignedFunctionGroup() throws IOException {
        UserContext userContext1 = new UserContext("userId1", rootLegalEntityMSA.getId());
        userContext1 = userContextJpaRepository.save(userContext1);
        UserContext userContext2 = new UserContext("userId2", rootLegalEntityMSA.getId());
        userContext2 = userContextJpaRepository.save(userContext2);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(defaultFGForRootLegalEntityMSA, userContext1));
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(defaultFGForRootLegalEntityMSA, userContext2));

        String url = buildURL(rootLegalEntityMSA.getId(), defaultFGForRootLegalEntityMSA.getId(), "0", "1");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId1"), actualResponse);
    }

    @Test
    public void shouldGetAllUserIdsWithAssignedFunctionGroupWhenSizeGreaterAmountOfAssignedUsers() throws IOException {
        UserContext userContext1 = new UserContext("userId1", rootLegalEntityMSA.getId());
        userContext1 = userContextJpaRepository.save(userContext1);
        UserContext userContext2 = new UserContext("userId2", rootLegalEntityMSA.getId());
        userContext2 = userContextJpaRepository.save(userContext2);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(defaultFGForRootLegalEntityMSA, userContext1));
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(defaultFGForRootLegalEntityMSA, userContext2));

        String url = buildURL(rootLegalEntityMSA.getId(), defaultFGForRootLegalEntityMSA.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(2, actualResponse.size());
        assertEquals(createResponse("userId1", "userId2"), actualResponse);
    }

    @Test
    public void shouldGetUserIdsWithAssignedFunctionGroupWhenQueryParametersAreMissed() throws IOException {
        UserContext userContext1 = new UserContext("userId1", rootLegalEntityMSA.getId());
        userContext1 = userContextJpaRepository.save(userContext1);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(defaultFGForRootLegalEntityMSA, userContext1));

        String url = new UrlBuilder(URL)
            .addPathParameter(rootLegalEntityMSA.getId())
            .addPathParameter(defaultFGForRootLegalEntityMSA.getId())
            .build();

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId1"), actualResponse);
    }

    @Test
    public void shouldGetEmptyListWhenFunctionGroupIsNotAssignedToAnyOfUsers() throws IOException {
        String url = buildURL(rootLegalEntityMSA.getId(), defaultFGForRootLegalEntityMSA.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertTrue(actualResponse.isEmpty());
    }

    @Test
    public void shouldGetUserIdsWhenFunctionGroupIsTemplateFromMasterServiceAgreementOfChildLegalEntity()
        throws IOException {
        ServiceAgreement childLegalEntityMSA = new ServiceAgreement();
        childLegalEntityMSA.setName("CHILD-LE-MSA");
        childLegalEntityMSA.setDescription("child-le-msa");
        childLegalEntityMSA.setCreatorLegalEntity(childLegalEntity);
        childLegalEntityMSA.setMaster(true);
        childLegalEntityMSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        childLegalEntityMSA = serviceAgreementJpaRepository.save(childLegalEntityMSA);

        FunctionGroup templateFGForChildLegalEntityMSA = new FunctionGroup();
        templateFGForChildLegalEntityMSA.setName("template-fg");
        templateFGForChildLegalEntityMSA.setDescription("template");
        templateFGForChildLegalEntityMSA.setType(FunctionGroupType.TEMPLATE);
        templateFGForChildLegalEntityMSA.setServiceAgreement(childLegalEntityMSA);
        templateFGForChildLegalEntityMSA.setAssignablePermissionSet(apsDefaultRegular);
        templateFGForChildLegalEntityMSA = functionGroupJpaRepository.save(templateFGForChildLegalEntityMSA);

        UserContext userContext = new UserContext("userId", childLegalEntityMSA.getId());
        userContext = userContextJpaRepository.save(userContext);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(templateFGForChildLegalEntityMSA, userContext));

        String url = buildURL(childLegalEntityMSA.getId(), templateFGForChildLegalEntityMSA.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId"), actualResponse);
    }

    @Test
    public void shouldGetUserIdsInCustomServiceAgreementForChildLegalEntityWhenFunctionGroupIsTemplateFromMasterServiceAgreementOfRootLegalEntity()
        throws IOException {
        ServiceAgreement childLegalEntityMSA = new ServiceAgreement();
        childLegalEntityMSA.setName("CHILD-LE-MSA");
        childLegalEntityMSA.setDescription("child-le-msa");
        childLegalEntityMSA.setCreatorLegalEntity(childLegalEntity);
        childLegalEntityMSA.setMaster(true);
        childLegalEntityMSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        serviceAgreementJpaRepository.save(childLegalEntityMSA);

        ServiceAgreement childLegalEntityCSA = new ServiceAgreement();
        childLegalEntityCSA.setName("CHILD-LE-CSA");
        childLegalEntityCSA.setDescription("child-le-csa");
        childLegalEntityCSA.setCreatorLegalEntity(childLegalEntity);
        childLegalEntityCSA.setMaster(false);
        childLegalEntityCSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        childLegalEntityCSA = serviceAgreementJpaRepository.save(childLegalEntityCSA);

        UserContext userContext = new UserContext("userId", childLegalEntityCSA.getId());
        userContext = userContextJpaRepository.save(userContext);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(templateFGForRootLegalEntityMSA, userContext));

        String url = buildURL(childLegalEntityCSA.getId(), templateFGForRootLegalEntityMSA.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId"), actualResponse);
    }

    @Test
    public void shouldGetUserIdsInCustomServiceAgreementForGrandChildLegalEntityWhenFunctionGroupIsTemplateFromMasterServiceAgreementOfRootLegalEntity()
        throws IOException {
        ServiceAgreement childLegalEntityMSA = new ServiceAgreement();
        childLegalEntityMSA.setName("CHILD-LE-MSA");
        childLegalEntityMSA.setDescription("child-le-msa");
        childLegalEntityMSA.setCreatorLegalEntity(childLegalEntity);
        childLegalEntityMSA.setMaster(true);
        childLegalEntityMSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        serviceAgreementJpaRepository.save(childLegalEntityMSA);

        ServiceAgreement grandChildLegalEntityMSA = new ServiceAgreement();
        grandChildLegalEntityMSA.setName("GRAND-CHILD-LE-MSA");
        grandChildLegalEntityMSA.setDescription("grand-child-le-msa");
        grandChildLegalEntityMSA.setCreatorLegalEntity(grandChildLegalEntity);
        grandChildLegalEntityMSA.setMaster(true);
        grandChildLegalEntityMSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        serviceAgreementJpaRepository.save(grandChildLegalEntityMSA);

        ServiceAgreement grandChildLegalEntityCSA = new ServiceAgreement();
        grandChildLegalEntityCSA.setName("GRAND-CHILD-LE-CSA");
        grandChildLegalEntityCSA.setDescription("grand-child-le-csa");
        grandChildLegalEntityCSA.setCreatorLegalEntity(grandChildLegalEntity);
        grandChildLegalEntityCSA.setMaster(false);
        grandChildLegalEntityCSA.setPermissionSetsRegular(Sets.newHashSet(apsDefaultRegular));
        grandChildLegalEntityCSA = serviceAgreementJpaRepository.save(grandChildLegalEntityCSA);

        UserContext userContext = new UserContext("userId", grandChildLegalEntityCSA.getId());
        userContext = userContextJpaRepository.save(userContext);
        userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(templateFGForRootLegalEntityMSA, userContext));

        String url = buildURL(grandChildLegalEntityCSA.getId(), templateFGForRootLegalEntityMSA.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId"), actualResponse);
    }

    @Test
    public void shouldGetUserIdsWhenRequestedSystemFunctionGroup() throws IOException {
        FunctionGroup systemFunctionGroup = new FunctionGroup();
        systemFunctionGroup.setName("system-fg");
        systemFunctionGroup.setDescription("system");
        systemFunctionGroup.setType(FunctionGroupType.SYSTEM);
        systemFunctionGroup.setServiceAgreement(rootLegalEntityMSA);
        systemFunctionGroup = functionGroupJpaRepository.save(systemFunctionGroup);

        UserContext userContext = new UserContext("userId", rootLegalEntityMSA.getId());
        userContextJpaRepository.save(userContext);
        userAssignedFunctionGroupJpaRepository.save(new UserAssignedFunctionGroup(systemFunctionGroup, userContext));

        String url = buildURL(rootLegalEntityMSA.getId(), systemFunctionGroup.getId(), "0", "10");

        String responseAsString = executeRequest(url, null, HttpMethod.GET);

        List<UserAssignedFunctionGroupResponse> actualResponse = readValue(responseAsString, new TypeReference<>() {});

        assertEquals(1, actualResponse.size());
        assertEquals(createResponse("userId"), actualResponse);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenRequestedServiceAgreementNotExists() {
        String url = buildURL("not-existing-sa-id", defaultFGForRootLegalEntityMSA.getId(), "0", "10");

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeRequest(url, null, HttpMethod.GET));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenRequestedFunctionGroupNotFoundInCurrentServiceAgreement() {
        String url = buildURL(rootLegalEntityMSA.getId(), "fg-id", "0", "10");

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeRequest(url, null, HttpMethod.GET));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenFromParameterIsNegative() {
        String url = buildURL(rootLegalEntityMSA.getId(), "fg_id", "-1", "10");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(url, null, HttpMethod.GET));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenSizeParameterIsNegative() {
        String url = buildURL(rootLegalEntityMSA.getId(), "fg_id", "0", "-10");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(url, null, HttpMethod.GET));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_064.getErrorMessage(), ERR_AG_064.getErrorCode()));
    }

    private String buildURL(String serviceAgreementId, String functionGroupId, String from, String size) {
        return new UrlBuilder(URL)
            .addPathParameter(serviceAgreementId)
            .addPathParameter(functionGroupId)
            .addQueryParameter("from", from)
            .addQueryParameter("size", size)
            .build();
    }

    private List<UserAssignedFunctionGroupResponse> createResponse(String... userIds) {
        return Arrays.stream(userIds)
            .map(id -> new UserAssignedFunctionGroupResponse().id(id).additions(Collections.emptyMap()))
            .collect(Collectors.toList());
    }
}