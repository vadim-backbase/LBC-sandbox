package com.backbase.accesscontrol.business.persistence.functiongroup;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class UpdateFunctionGroupHandlerTest {

    @InjectMocks
    private UpdateFunctionGroupHandler updateFunctionGroupHandler;

    @Mock
    private FunctionGroupMapper functionGroupMapper = Mappers.getMapper(FunctionGroupMapper.class);

    @Mock
    private FunctionGroupService functionGroupService;

    @Test
    public void testExecuteRequest() {
        FunctionGroupByIdPutRequestBody requestData = new FunctionGroupByIdPutRequestBody()
            .withDescription("desc")
            .withName("name")
            .withPermissions(emptyList())
            .withServiceAgreementId("sa id")
            .withType(Type.REGULAR);
        String id = "id";

        FunctionGroupBase functionGroupBase = new FunctionGroupBase().withName("name").withDescription("desc")
            .withPermissions(emptyList()).withServiceAgreementId("sa id").withType(
                PersistenceFunctionGroup.Type.DEFAULT);

        when(functionGroupMapper.presentationToFunctionGroupBase(refEq(requestData))).thenReturn(
            functionGroupBase);

        updateFunctionGroupHandler.executeRequest(new SingleParameterHolder<>(id), requestData);

        verify(functionGroupService).updateFunctionGroup(eq(id), eq(functionGroupBase));
    }

    @Test
    public void testCreateSuccessEvent() {
        FunctionGroupEvent successEvent = updateFunctionGroupHandler
            .createSuccessEvent(new SingleParameterHolder<>("id"),
                new FunctionGroupByIdPutRequestBody(), null);

        assertNotNull(successEvent);
        assertEquals(Action.UPDATE, successEvent.getAction());
        assertEquals("id", successEvent.getId());

    }

    @Test
    public void testCreateFailureEvent() {
        String id = "id";
        String errorMessage = "message";

        Event failureEvent = updateFunctionGroupHandler
            .createFailureEvent(new SingleParameterHolder<>(id), new FunctionGroupByIdPutRequestBody(), new Exception(errorMessage));

        assertNull(failureEvent);
    }

}