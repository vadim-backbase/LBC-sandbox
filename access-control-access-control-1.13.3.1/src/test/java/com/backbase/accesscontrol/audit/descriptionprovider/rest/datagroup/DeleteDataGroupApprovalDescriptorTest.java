package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static java.util.Arrays.asList;
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

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataGroupApprovalDescriptorTest {

    @InjectMocks
    private DeleteDataGroupApprovalDescriptor deleteDataGroupApprovalDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void shouldReturnApprovalIdAsMessageId() {
        String id = "id";
        String approvalId = "approval-id";

        when(joinPoint.getArgs())
            .thenReturn(asList(id, approvalId).toArray());
        List<String> messageIds = deleteDataGroupApprovalDescriptor
            .getMessageIds(joinPoint);

        assertEquals(singletonList(approvalId), messageIds);
    }

    @Test
    public void getSuccessEventDataList() {

        when(joinPoint.getArgs())
            .thenReturn(singletonList("id").toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventDescription("Request Delete | Data Group | Successful | ID id");
        List<AuditMessage> actualEventList = deleteDataGroupApprovalDescriptor
            .getSuccessEventDataList(joinPoint, null);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataListWithExternalSaId() {

        when(joinPoint.getArgs())
            .thenReturn(singletonList("id").toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventDescription("Request Delete | Data Group | Initiated | ID id");
        List<AuditMessage> actualEventList = deleteDataGroupApprovalDescriptor
            .getInitEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataListWithExternalSaId() {

        when(joinPoint.getArgs())
            .thenReturn(singletonList("id").toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventDescription("Request Delete | Data Group | Failed | ID id");
        List<AuditMessage> actualEventList = deleteDataGroupApprovalDescriptor
            .getFailedEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

}