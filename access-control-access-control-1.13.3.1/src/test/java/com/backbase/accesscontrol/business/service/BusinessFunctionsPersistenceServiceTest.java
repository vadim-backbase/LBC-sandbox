package com.backbase.accesscontrol.business.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessFunctionsPersistenceServiceTest {

    @Spy
    private ObjectConverter objectConverter = new ObjectConverter(spy(ObjectMapper.class));
    @Mock
    private FunctionGroupService functionGroupService;

    @InjectMocks
    private BusinessFunctionsPersistenceService functionPAndPService;

    @Test
    public void testGetBusinessFunctionsForServiceAgreement() {
        String saId = "saId";
        String functionName = "Function Name";
        String resourceName = "Resource Name";
        String functionCode = "Function code";
        String fagId = "001";

        mockGetBusinessFunctionsForSa(saId, fagId, functionName, resourceName, functionCode);

        List<FunctionsGetResponseBody> functions = functionPAndPService.getBusinessFunctionsForServiceAgreement(saId);

        assertEquals(fagId, functions.get(0).getFunctionId());
        assertEquals(functionName, functions.get(0).getName());
        assertEquals(resourceName, functions.get(0).getResource());
        assertEquals(functionCode, functions.get(0).getFunctionCode());
    }

    private void mockGetBusinessFunctionsForSa(String saId, String fagId, String functionName, String resourceName,
        String functionCode) {
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody> functions = new ArrayList<>();
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody function = new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody()
            .withFunctionId(fagId).withName(functionName).withResource(resourceName).withFunctionCode(functionCode);
        functions.add(function);

        when(functionGroupService.findAllBusinessFunctionsByServiceAgreement(eq(saId), eq(false)))
            .thenReturn(functions);
    }
}
