package com.backbase.accesscontrol.business.functiongroup;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetFunctionGroupByIdTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;

    @Mock
    private FunctionGroupMapper functionGroupMapper;

    @InjectMocks
    private GetFunctionGroupById getFunctionGroupById;

    @Before
    public void setUp() throws Exception {
        getFunctionGroupById = new GetFunctionGroupById(functionGroupPAndPService, functionGroupMapper);
    }

    @Test
    public void shouldPassIfGetFunctionGroupIsInvokedInClientWithIdParameter() {
        String serviceAgreementId = "001";
        String fgName = "FAG-name";
        String fgId = "fag-id";
        Date fromDateTime = new Date(10000);
        Date untilDateTime = new Date(50000);

        mockGetFunctionGroupById(serviceAgreementId, fgName, fgId);
        mockFunctionGroupMapper(serviceAgreementId, fgName, fromDateTime, untilDateTime);

        InternalRequest<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups
            .FunctionGroupByIdGetResponseBody> functionGroupById = getFunctionGroupById
            .getFunctionGroupById(new InternalRequest<>(), fgId);
        verify(functionGroupPAndPService, times(1)).getFunctionGroupById(anyString());

        assertEquals(serviceAgreementId, functionGroupById.getData().getServiceAgreementId());
        assertEquals(fgName, functionGroupById.getData().getName());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(fromDateTime), functionGroupById.getData().getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(fromDateTime), functionGroupById.getData().getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(untilDateTime),
            functionGroupById.getData().getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(untilDateTime),
            functionGroupById.getData().getValidUntilTime());
    }

    @Test
    public void shouldAllowToGetFunctionGroupByIdWhenFGTypeIsDefault() {
        String fgId = "00001";
        String serviceAgreementId = "001";
        String fgName = "FG-name";
        Date fromDateTime = new Date(10000);
        Date untilDateTime = new Date(50000);

        FunctionGroupByIdGetResponseBody data = new FunctionGroupByIdGetResponseBody()
            .withServiceAgreementId(serviceAgreementId).withName(fgName).withType(FunctionGroupBase.Type.DEFAULT);

        when(functionGroupPAndPService.getFunctionGroupById(eq(fgId)))
            .thenReturn(data);
        mockFunctionGroupMapper(serviceAgreementId, fgName, fromDateTime, untilDateTime);

        InternalRequest<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups
            .FunctionGroupByIdGetResponseBody> functionGroupById = getFunctionGroupById
            .getFunctionGroupById(new InternalRequest<>(), fgId);
        verify(functionGroupPAndPService, times(1)).getFunctionGroupById(anyString());

        assertEquals(serviceAgreementId, functionGroupById.getData().getServiceAgreementId());
        assertEquals(fgName, functionGroupById.getData().getName());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(fromDateTime), functionGroupById.getData().getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(fromDateTime), functionGroupById.getData().getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(untilDateTime),
            functionGroupById.getData().getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(untilDateTime),
            functionGroupById.getData().getValidUntilTime());
    }

    private void mockGetFunctionGroupById(String serviceAgreementId, String fgName, String fgId) {
        FunctionGroupByIdGetResponseBody data = new FunctionGroupByIdGetResponseBody()
            .withServiceAgreementId(serviceAgreementId).withName(fgName);

        when(functionGroupPAndPService.getFunctionGroupById(eq(fgId)))
            .thenReturn(data);
    }

    private void mockFunctionGroupMapper(String serviceAgreementId, String name, Date startDateTime, Date endDateTime) {
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody item =
            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody();
        item.withServiceAgreementId(serviceAgreementId).withName(name)
            .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(startDateTime))
            .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(startDateTime))
            .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(endDateTime))
            .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(endDateTime));
        when(functionGroupMapper
            .persistenceFunctionGroupByIdToPresentationFunctionGroupById(
                any(com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody.class)))
            .thenReturn(item);
    }
}
