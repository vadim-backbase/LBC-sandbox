package com.backbase.accesscontrol.business.flows.businessfunction;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.service.BusinessFunctionsPersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetBusinessFunctionsForServiceAgreementFlowTest {

    @Mock
    private BusinessFunctionsPersistenceService businessFunctionsPersistenceService;

    @InjectMocks
    private GetBusinessFunctionsForServiceAgreementFlow getBusinessFunctionsForServiceAgreementFlow;

    @Test
    public void shouldCallPersistenceService() {
        String id = "id";
        getBusinessFunctionsForServiceAgreementFlow.start(id);
        verify(businessFunctionsPersistenceService).getBusinessFunctionsForServiceAgreement(eq(id));
    }

}