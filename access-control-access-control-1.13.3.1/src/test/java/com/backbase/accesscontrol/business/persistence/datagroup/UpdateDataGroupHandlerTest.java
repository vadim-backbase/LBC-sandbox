package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupHandlerTest {

    @Mock
    private DataGroupService dataGroupService;

    @InjectMocks
    private UpdateDataGroupHandler updateDataGroupHandler;

    @Test
    public void shouldSuccessfullyInvokeUpdate() {
        String dataGroupId = "001";
        String dataGroupName = "name";

        DataGroupByIdPutRequestBody dataGroupToUpdate = new DataGroupByIdPutRequestBody();
        dataGroupToUpdate.setId(dataGroupId);
        dataGroupToUpdate.setName(dataGroupName);

        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder(dataGroupId);

        updateDataGroupHandler.executeRequest(parameterHolder, dataGroupToUpdate);

        verify(dataGroupService).update(eq(dataGroupId), any(DataGroupByIdPutRequestBody.class));
    }

    @Test
    public void shouldThrowExceptionWhenInvokeUpdate() {
        String dataGroupId = "001";
        String dataGroupName = "name";

        DataGroupByIdPutRequestBody dataGroupToUpdate = new DataGroupByIdPutRequestBody();
        dataGroupToUpdate.setId(dataGroupId);
        dataGroupToUpdate.setName(dataGroupName);

        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(dataGroupId);
        doThrow(new BadRequestException()).when(dataGroupService)
            .update(eq(dataGroupId), any(DataGroupByIdPutRequestBody.class));

        assertThrows(BadRequestException.class,
            () -> updateDataGroupHandler.executeRequest(parameterHolder, dataGroupToUpdate));

        verify(dataGroupService).update(eq(dataGroupId), any(DataGroupByIdPutRequestBody.class));
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        String dataGroupId = "001";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(dataGroupId);
        DataGroupEvent successEvent = updateDataGroupHandler.createSuccessEvent(parameterHolder, null, null);
        assertEquals(dataGroupId, successEvent.getId());
        assertEquals(Action.UPDATE, successEvent.getAction());
    }

    @Test
    public void shouldCreateFailureEvent() {
        String dataGroupId = "001";
        String errorMessage = "message";
        SingleParameterHolder<String> parameterHolder = new SingleParameterHolder<>(dataGroupId);
        Event failureEvent = updateDataGroupHandler
            .createFailureEvent(parameterHolder, null, new RuntimeException(errorMessage));
        assertNull(failureEvent);
    }
}
