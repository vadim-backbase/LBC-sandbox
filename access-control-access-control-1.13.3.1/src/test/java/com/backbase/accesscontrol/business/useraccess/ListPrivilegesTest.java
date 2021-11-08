package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementClientCommunicationService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListPrivilegesTest {

    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;

    @Mock
    private ServiceAgreementClientCommunicationService serviceAgreementClientCommunicationService;

    private ListPrivileges listPrivileges;

    @Before
    public void setUp() throws Exception {
        listPrivileges = new ListPrivileges(userAccessPrivilegeService,
            serviceAgreementClientCommunicationService);
    }

    @Test
    public void shouldPassIfGetAllFunctionsIsInvoked() {

        InternalRequest<Void> internalRequest = getInternalRequest(null);

        List<String> privilegeList = new ArrayList<>();
        privilegeList.add("privilege01");
        privilegeList.add("privilege02");

        String userId = "userId";
        String serviceAgreementId = "saId";
        String functionName = "Contacts";
        String resourceName = "Contacts";
        when(userAccessPrivilegeService.getPrivileges(refEq(userId),
            refEq(serviceAgreementId),refEq(functionName),refEq(resourceName)))
            .thenReturn(privilegeList);
        when(serviceAgreementClientCommunicationService
            .getServiceAgreementIdForUserWithUserId(eq("userId"), eq("saId")))
            .thenReturn("saId");

        listPrivileges.getAllPrivileges
            (internalRequest, "userId", "saId", "Contacts", "Contacts");

        verify(userAccessPrivilegeService, times(1)).getPrivileges(refEq(userId),
            refEq(serviceAgreementId),refEq(functionName),refEq(resourceName));
    }

}
