package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.BulkFunctionGroupsPostResponseBodyToFunctionGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.FunctionGroupByIdGetResponseBodyToFunctionGroupItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.FunctionGroupsGetResponseBodyToFunctionGroupItemMapper;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupItem;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.BulkFunctionGroupsPostResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupQueryControllerTest {

    @Mock
    private FunctionGroupService functionGroupService;

    @InjectMocks
    private FunctionGroupQueryController functionGroupQueryController;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(FunctionGroupsGetResponseBodyToFunctionGroupItemMapper.class)),
            spy(Mappers.getMapper(FunctionGroupByIdGetResponseBodyToFunctionGroupItemMapper.class)),
            spy(Mappers.getMapper(BulkFunctionGroupsPostResponseBodyToFunctionGroupItemMapper.class))
        ));

    @Test
    public void shouldGetAllFunctionGroupsByServiceAgreementId() {

        List<FunctionGroupsGetResponseBody> functionGroups = new ArrayList<>();
        functionGroups.add(new FunctionGroupsGetResponseBody()
            .withId("fgId1")
            .withServiceAgreementId("id"));

        when(functionGroupService
            .getFunctionGroupsByServiceAgreementId(eq("id")))
            .thenReturn(functionGroups);

        List<FunctionGroupItem> responseBodies = functionGroupQueryController
            .getFunctionGroups("id").getBody();

        assertEquals(functionGroups.size(), responseBodies.size());
        assertEquals(functionGroups.get(0).getId(), responseBodies.get(0).getId());
        assertEquals(functionGroups.get(0).getServiceAgreementId(), responseBodies.get(0).getServiceAgreementId());
    }

    @Test
    public void shouldGeFunctionGroupById() {
        String fgId = "FG-01";
        FunctionGroupByIdGetResponseBody functionGroup = new FunctionGroupByIdGetResponseBody()
            .withId(fgId)
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("SA-01");

        when(functionGroupService.getFunctionGroupById(eq(fgId))).thenReturn(functionGroup);

        FunctionGroupItem functionGroupById = functionGroupQueryController
            .getFunctionGroupById(fgId).getBody();

        assertEquals(functionGroup.getId(), functionGroupById.getId());
        assertEquals(functionGroup.getServiceAgreementId(), functionGroupById.getServiceAgreementId());
    }

    @Test
    public void shouldGetAllFunctionGroups() {

        com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupsIds requestBody =
            new com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupsIds();
        requestBody.setIds(asList("fg1", "fg2"));

        when(functionGroupService.getBulkFunctionGroups(any()))
            .thenReturn(
                Lists.newArrayList(
                    new BulkFunctionGroupsPostResponseBody(), new BulkFunctionGroupsPostResponseBody()));

        List<FunctionGroupItem> functionGroupItems = functionGroupQueryController
            .postBulkFunctionGroups(requestBody).getBody();

        assertEquals(2, functionGroupItems.size());
        verify(functionGroupService).getBulkFunctionGroups(eq(requestBody.getIds()));
    }
}
