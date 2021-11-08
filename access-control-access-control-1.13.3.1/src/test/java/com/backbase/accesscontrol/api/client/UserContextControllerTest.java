package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.business.service.UserContextEncryptionServiceTest.SERVICE_AGREEMENT_ID;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_072;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.client.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.client.rest.spec.model.Serviceagreementpartialitem;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.client.UserContextServiceAgreementsGetResponseBodyToServiceagreementpartialitemConverter;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.UserContextFlowService;
import com.backbase.accesscontrol.service.facades.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserContextControllerTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private UserContextUtil userContextUtil;

    @Mock
    private ServiceAgreementIdProvider serviceAgreementIdProvider;

    @Mock
    private ParameterValidationService validationUtil;

    @Mock
    private UserContextFlowService userContextFlowService;

    @Mock
    private ValidationConfig validationConfig;

    @InjectMocks
    private UserContextController userContextController;

    @Spy
    private final PayloadConverter payloadConverter =
        new PayloadConverter(singletonList(
            spy(Mappers
                .getMapper(UserContextServiceAgreementsGetResponseBodyToServiceagreementpartialitemConverter.class))
        ));

    @Test
    void testValidateUserContext() {
        final String SERVICE_AGREEMENT_ID = "serviceAgreementId";
        final String ENCRYPTED_TOKEN = "zaq1xsw2";
        final String COOKIE_NAME = "USER_CONTEXT";

        mockUserSetup();
        when(userContextService.validate(anyString(), anyString())).thenReturn(ENCRYPTED_TOKEN);

        when(userContextUtil.getCookie(COOKIE_NAME, ENCRYPTED_TOKEN))
            .thenReturn(COOKIE_NAME + "=" + ENCRYPTED_TOKEN);

        com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST requestBody =
            new com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST()
                .serviceAgreementId(SERVICE_AGREEMENT_ID);

        ResponseEntity<Void> responseEntity = userContextController.postUserContext(requestBody);

        assertEquals(COOKIE_NAME + "=" + ENCRYPTED_TOKEN, responseEntity.getHeaders().get("Set-Cookie").get(0));
    }

    @Test
    void shouldThrowForbiddenWhenNoAuthenticatedUserFound() {
        com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST requestBody =
            new com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST()
                .serviceAgreementId(SERVICE_AGREEMENT_ID);
        when(userContextUtil.getAuthenticatedUserName())
            .thenThrow(getForbiddenException(ERR_AG_072.getErrorMessage(), ERR_AG_072.getErrorCode()));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextController.postUserContext(requestBody));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_072.getErrorMessage(), ERR_AG_072.getErrorCode()));
    }

    @Test
    void testIsNotValidUserContext() {
        final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

        mockUserSetup();
        when(userContextService.validate(anyString(), anyString())).thenThrow(new ForbiddenException());

        com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST requestBody =
            new com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST()
                .serviceAgreementId(SERVICE_AGREEMENT_ID);
        assertThrows(ForbiddenException.class,
            () -> userContextController.postUserContext(requestBody));
    }

    @Test
    void testGetUserContextServiceAgreements() {
        int from = 0;
        int size = 5;
        final String SERVICE_AGREEMENT_ID = "saId";
        final String SERVICE_AGREEMENT_NAME = "saName";

        UserContextServiceAgreementsGetResponseBody response = new UserContextServiceAgreementsGetResponseBody()
            .withId(SERVICE_AGREEMENT_ID)
            .withName(SERVICE_AGREEMENT_NAME)
            .withIsMaster(false);

        long TOTAL_NUMBER_RECORDS = 1L;
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper =
            new ListElementsWrapper<>(singletonList(response), TOTAL_NUMBER_RECORDS);

        when(userContextService.getUserContextByUserId(anyString(), anyString(), anyInt(), anyString(), anyInt()))
            .thenReturn(listElementsWrapper);
        mockValidateFromAndSize(from, size);
        mockUserSetup();

        ResponseEntity<List<Serviceagreementpartialitem>> responseEntity = userContextController
            .getUserContextServiceAgreements("", from, "", size);

        assertEquals(TOTAL_NUMBER_RECORDS, responseEntity.getHeaders().get("X-Total-Count").size());
        assertEquals(SERVICE_AGREEMENT_NAME, responseEntity.getBody().get(0).getName());
    }

    @Test
    void testGetUserContextPermissions() {
        String serviceAgreementId = "saId";
        String userId = "userId";
        String extUserId = "extUserId";

        doNothing().when(validationConfig).validateDataGroupType(anyString());
        when(userContextUtil.getAuthenticatedUserName()).thenReturn(extUserId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "leId"));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(serviceAgreementId));

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"))
            .resourceName("Payments")
            .privileges(asList("view", "edit"));

        PermissionsDataGroup mockResponse = new PermissionsDataGroup()
            .permissionsData(singletonList(new PermissionDataGroup()
                .permissions(asList(new PermissionData()
                        .resourceName("Payments")
                        .functionName("SEPA CT")
                        .privileges(asList("view", "edit")),
                    new PermissionData()
                        .resourceName("Payments")
                        .functionName("Manage Service Agreements")
                        .privileges(asList("view", "edit"))))
                .dataGroups(asList(
                    singletonList(new PermissionDataGroupData()
                        .dataGroupType("ARRANGEMENTS")
                        .dataGroupIds(singletonList("dgId01"))),
                    asList(new PermissionDataGroupData()
                            .dataGroupType("ARRANGEMENTS")
                            .dataGroupIds(singletonList("dgId02")),
                        new PermissionDataGroupData()
                            .dataGroupType("PAYEES")
                            .dataGroupIds(singletonList("dgId03")))))))
            .dataGroupsData(asList(new DataGroupData()
                    .dataGroupId("dgId01")
                    .dataItemIds(asList("item01", "item02")),
                new DataGroupData()
                    .dataGroupId("dgId02")
                    .dataItemIds(asList("item03", "item04")),
                new DataGroupData()
                    .dataGroupId("dgId03")
                    .dataItemIds(asList("item05", "item06"))));

        when(userContextFlowService
            .getUserContextPermissions(userId, serviceAgreementId, permissionsRequest))
            .thenReturn(mockResponse);

        PermissionsDataGroup response = userContextController.getUserContextPermissions(permissionsRequest).getBody();

        verify(userContextFlowService)
            .getUserContextPermissions(userId, serviceAgreementId, permissionsRequest);

        assertEquals(mockResponse, response);
    }

    @Test
    void testFallbackToMSAWhenGetUserContextPermissionsCalled() {
        String serviceAgreementId = "saId";
        String userId = "userId";
        String extUserId = "extUserId";

        doNothing().when(validationConfig).validateDataGroupType(anyString());
        when(userContextUtil.getAuthenticatedUserName()).thenReturn(extUserId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto(userId, "leId"));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.empty());
        when(serviceAgreementIdProvider
            .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(extUserId))
            .thenReturn(serviceAgreementId);

        when(userContextFlowService
            .getUserContextPermissions(eq(userId), eq(serviceAgreementId), any()))
            .thenReturn(new com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup()
                .permissionsData(emptyList()).dataGroupsData(emptyList()));

        userContextController
            .getUserContextPermissions(new PermissionsRequest().dataGroupTypes(singletonList("ARRANGEMENTS")))
            .getBody();

        verify(userContextFlowService)
            .getUserContextPermissions(eq(userId), eq(serviceAgreementId), any());
    }


    private void mockUserSetup() {
        when(userContextUtil.getAuthenticatedUserName()).thenReturn("externalUserName");
    }

    private void mockValidateFromAndSize(int from, int size) {
        doNothing()
            .when(validationUtil).validateFromAndSizeParameter(from, size);
    }
}
