package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateDataGroupDescriptorApprovalTest {

    @InjectMocks
    private CreateDataGroupApprovalDescriptor createDataGroupDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getMessageIdsTest() {

        DataGroupBase dataGroupBase = new DataGroupBase()
            .withServiceAgreementId("serviceAgreementId")
            .withDescription("DE-01")
            .withName("DGName")
            .withType("type");
        String approvalId = "approvalId";

        when(joinPoint.getArgs())
            .thenReturn(asList(dataGroupBase, approvalId).toArray());

        List<String> messageIds = createDataGroupDescriptor
            .getMessageIds(joinPoint);
        assertEquals(singletonList(approvalId), messageIds);
    }

    @Test
    public void getSuccessEventDataList() {
        String dgName = "Name";

        DataGroupBase presentationFunctionGroup = new DataGroupBase()
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        DataGroupsPostResponseBody dataGroupsPostResponseBody = new DataGroupsPostResponseBody();
        dataGroupsPostResponseBody.setId("idDG");


        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Request Create | Data Group | Successful | name Name, service agreement ID ex, type type");
        List<AuditMessage> actualEventList = createDataGroupDescriptor
            .getSuccessEventDataList(joinPoint, dataGroupsPostResponseBody);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupBase presentationFunctionGroup = new DataGroupBase()
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Request Create | Data Group | Initiated | name Name, service agreement ID ex, "
                + "type type");
        List<AuditMessage> actualEventList = createDataGroupDescriptor
            .getInitEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupBase presentationFunctionGroup = new DataGroupBase()
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Request Create | Data Group | Failed | name Name, service agreement ID ex, "
                + "type type");
        List<AuditMessage> actualEventList = createDataGroupDescriptor
            .getFailedEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }
}