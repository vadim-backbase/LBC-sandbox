package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListFunctionGroupsTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;

    @Mock
    private FunctionGroupMapper functionGroupMapper;

    private ListFunctionGroups listFunctionGroups;

    @Before
    public void setUp() throws Exception {

        listFunctionGroups = new ListFunctionGroups(functionGroupMapper,
            functionGroupPAndPService);
    }

    @Test
    public void shouldSuccessfullyGetFunctionGroupsByServiceAgreementId() {
        Date fromDateTime = new Date(10000);
        Date untilDateTime = new Date(50000);
        String serviceAgreementId = "001";
        InternalRequest<Void> request = getInternalRequest(null);

        List<FunctionGroupsGetResponseBody> data = Collections.singletonList(new FunctionGroupsGetResponseBody()
            .withServiceAgreementId(serviceAgreementId));

        mockFunctionGroupMapper(serviceAgreementId, fromDateTime, untilDateTime);
        when(functionGroupPAndPService.getFunctionGroups(serviceAgreementId))
            .thenReturn(data);

        InternalRequest<List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups
            .FunctionGroupsGetResponseBody>> result =
            listFunctionGroups.getAllFunctionGroups(request, serviceAgreementId);

        assertEquals(data.size(), result.getData().size());
        assertEquals(serviceAgreementId, result.getData().get(0).getServiceAgreementId());
    }

    private void mockFunctionGroupMapper(String serviceAgreementId, Date startDateTime, Date endDateTime) {
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody item =
            new com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody()
                .withServiceAgreementId(serviceAgreementId)
                .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(startDateTime))
                .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(startDateTime))
                .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(endDateTime))
                .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(endDateTime));
        when(functionGroupMapper
            .pandpFunctionGroupsToPresentationFunctionGroups(anyList()))
            .thenReturn(asList(item));
    }
}
