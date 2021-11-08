package com.backbase.accesscontrol.service.facades;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.flows.businessfunction.GetBusinessFunctionsForServiceAgreementFlow;
import com.backbase.accesscontrol.business.flows.serviceagreement.ServiceAgreementUsersAssignedFunctionGroupFlow;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementFlowServiceTest {

    @Mock
    private GetBusinessFunctionsForServiceAgreementFlow getBusinessFunctionsForServiceAgreementFlow;

    @Mock
    private ServiceAgreementUsersAssignedFunctionGroupFlow serviceAgreementUsersAssignedFunctionGroupFlow;

    @InjectMocks
    private ServiceAgreementFlowService serviceAgreementFlowService;

    @Test
    public void shouldCallServiceAgreementFlowService() {

        serviceAgreementFlowService.getBusinessFunctionsForServiceAgreement("id");
        verify(getBusinessFunctionsForServiceAgreementFlow).start(eq("id"));
    }

    @Test
    public void shouldCallServiceAgreementUserAssignedFunctionGroupFlowService() {
        UserAssignedFunctionGroupDto userAssignedFunctionGroupDto = mock(UserAssignedFunctionGroupDto.class);

        serviceAgreementFlowService.getUsersWithAssignedFunctionGroup(userAssignedFunctionGroupDto);
        verify(serviceAgreementUsersAssignedFunctionGroupFlow).start(userAssignedFunctionGroupDto);
    }
}