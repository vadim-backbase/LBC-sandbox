package com.backbase.accesscontrol.audit.descriptionprovider.rest.functiongroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link DeleteFunctionGroupDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupDescriptorTest {

    public static final String DELETE_FUNCTION_GROUP = "Delete Function Group";
    @InjectMocks
    private DeleteFunctionGroupDescriptor deleteFunctionGroupDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String id = "id";
        when(joinPoint.getArgs())
            .thenReturn(singletonList(id).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withEventDescription("Delete | Function Group | Successful | ID id")
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "id");
        List<AuditMessage> actualEventList = deleteFunctionGroupDescriptor
            .getSuccessEventDataList(joinPoint, id);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataList() {
        String id = "id";
        when(joinPoint.getArgs())
            .thenReturn(singletonList(id).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(("Delete | Function Group | Initiated | ID id"))
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "id");
        List<AuditMessage> actualEventList = deleteFunctionGroupDescriptor
            .getInitEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataList() {
        String id = "id";
        when(joinPoint.getArgs())
            .thenReturn(singletonList(id).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Delete | Function Group | Failed | ID id")
            .withEventMetaDatum(FUNCTION_GROUP_ID_FIELD_NAME, "id");
        List<AuditMessage> actualEventList = deleteFunctionGroupDescriptor
            .getFailedEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }
}
