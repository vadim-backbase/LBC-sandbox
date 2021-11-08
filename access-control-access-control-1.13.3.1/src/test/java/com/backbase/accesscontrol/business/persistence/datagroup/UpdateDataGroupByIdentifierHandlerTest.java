package com.backbase.accesscontrol.business.persistence.datagroup;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupByIdentifierHandlerTest {

    @Mock
    private DataGroupService dataGroupService;

    @InjectMocks
    private UpdateDataGroupByIdentifierHandler updateDataGroupByIdentifierHandler;

    private EmptyParameterHolder emptyParameterHolder;

    @Before
    public void setUp() throws Exception {
        emptyParameterHolder = new EmptyParameterHolder();
    }

    @Test
    public void executeRequest() {
        PresentationSingleDataGroupPutRequestBody requestData = new PresentationSingleDataGroupPutRequestBody();
        updateDataGroupByIdentifierHandler.executeRequest(emptyParameterHolder,
            requestData);
        verify(dataGroupService).update(eq(requestData));
    }

    @Test
    public void shouldCreateSuccessfulEvent() {
        PresentationSingleDataGroupPutRequestBody requestData = new PresentationSingleDataGroupPutRequestBody();
        DataGroupEvent successEvent = updateDataGroupByIdentifierHandler.createSuccessEvent(emptyParameterHolder,
            requestData, "dgId");
        assertEquals("dgId", successEvent.getId());
        assertEquals(Action.UPDATE, successEvent.getAction());
    }
}