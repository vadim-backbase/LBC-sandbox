package com.backbase.accesscontrol.business.useraccess;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidatePermissionsTest {

    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;
    private ValidatePermissions validatePermissions;

    @Before
    public void setUp() {
        userAccessPermissionCheckService = mock(UserAccessPermissionCheckService.class);
        serviceAgreementClientCommunicationService = mock(ServiceAgreementClientCommunicationService.class);

        validatePermissions = new ValidatePermissions(userAccessPermissionCheckService,
            serviceAgreementClientCommunicationService);
    }

    @Test
    public void shouldInvokeGetUserPermissionCheck() {

        String userId = UUID.randomUUID().toString();
        String serviceAgreementId = UUID.randomUUID().toString();
        String functionName = "Entitlements";
        String resourceName = "Entitlements";
        String privileges = "execute,read";
        when(serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(eq(userId), eq(serviceAgreementId)))
            .thenReturn(serviceAgreementId);
        doNothing().when(userAccessPermissionCheckService)
            .checkUserPermission(userId, serviceAgreementId, functionName, resourceName, privileges);
        validatePermissions
            .getUserPermissionCheck(new InternalRequest<>(), userId, serviceAgreementId, resourceName,
                functionName,
                privileges);
        verify(userAccessPermissionCheckService)
            .checkUserPermission(userId, serviceAgreementId, functionName, resourceName, privileges);
    }

}