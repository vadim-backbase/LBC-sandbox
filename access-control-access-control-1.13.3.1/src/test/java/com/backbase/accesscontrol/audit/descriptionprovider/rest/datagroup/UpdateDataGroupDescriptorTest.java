package com.backbase.accesscontrol.audit.descriptionprovider.rest.datagroup;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link UpdateDataGroupDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupDescriptorTest {

    @InjectMocks
    private UpdateDataGroupDescriptor updateDataGroupDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String dgName = "Name";

        DataGroupByIdPutRequestBody presentationFunctionGroup = new DataGroupByIdPutRequestBody()
            .withId("id")
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Update | Data Group | Successful | name Name, service agreement ID ex, type type");
        List<AuditMessage> actualEventList = updateDataGroupDescriptor
            .getSuccessEventDataList(joinPoint, null);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupByIdPutRequestBody presentationFunctionGroup = new DataGroupByIdPutRequestBody()
            .withId("id")
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Update | Data Group | Initiated | name Name, service agreement ID ex, "
                + "type type");
        List<AuditMessage> actualEventList = updateDataGroupDescriptor
            .getInitEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupByIdPutRequestBody presentationFunctionGroup = new DataGroupByIdPutRequestBody()
            .withId("id")
            .withDescription("DE-01")
            .withName(dgName)
            .withServiceAgreementId("ex")
            .withType("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventMetaDatum("Data Group ID", "id")
            .withEventMetaDatum("Data Group Name", dgName)
            .withEventMetaDatum("Data Group Description", "DE-01")
            .withEventMetaDatum("Service Agreement ID", "ex")
            .withEventMetaDatum("Data Group Type", "type")
            .withEventDescription("Update | Data Group | Failed | name Name, service agreement ID ex, "
                + "type type");
        List<AuditMessage> actualEventList = updateDataGroupDescriptor
            .getFailedEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }
}
