package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupUpdate;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier;
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
public class UpdateDataGroupServiceDescriptorTest {

    @InjectMocks
    private UpdateDataGroupServiceDescriptor updateDataGroupServiceDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void invalidRequestTest() {

        PresentationDataGroupUpdate body = new PresentationDataGroupUpdate();
        when(joinPoint.getArgs()).thenReturn(new Object[]{body});

        AuditMessage initEvent = new AuditMessage().withStatus(Status.INITIATED);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor.getInitEventDataList(joinPoint);

        assertEquals(singletonList(initEvent), initDescription);

        AuditMessage failEvent = new AuditMessage().withStatus(Status.FAILED);

        List<AuditMessage> failedDescription = updateDataGroupServiceDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(singletonList(failEvent), failedDescription);
    }

    @Test
    public void getInitEventDataListTestWithIdIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .idIdentifier("dg-id"))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Initiated | ID dg-id")
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.INITIATED);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getInitEventDataListTestWithNameIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .nameIdentifier(new PresentationIdentifierNameIdentifier()
                    .name("dg-id")
                    .externalServiceAgreementId("ex-id")))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Initiated | name dg-id, external service agreement ID ex-id")
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("External Service Agreement ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier()
                    .getExternalServiceAgreementId())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Data Group Name",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.INITIATED);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListTestWithIdIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .idIdentifier("dg-id"))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Successful | ID dg-id")
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.SUCCESSFUL);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor.getSuccessEventDataList(joinPoint, null);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getSuccessEventDataListTestWithNameIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .nameIdentifier(new PresentationIdentifierNameIdentifier()
                    .name("dg-id")
                    .externalServiceAgreementId("ex-id")))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Successful | name dg-id, external service agreement ID ex-id")
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("External Service Agreement ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier()
                    .getExternalServiceAgreementId())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Data Group Name",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.SUCCESSFUL);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor
            .getSuccessEventDataList(joinPoint, null);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getFailedEventDataListTestWithIdIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .idIdentifier("dg-id"))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Failed | ID dg-id")
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.FAILED);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor
            .getFailedEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getFailedEventDataListTestWithNameIdentifier() {

        PresentationDataGroupUpdate validItemDataGroupIdIdentifier = new PresentationDataGroupUpdate()
            .name("name")
            .description("desc")
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier()
                .nameIdentifier(new PresentationIdentifierNameIdentifier()
                    .name("dg-id")
                    .externalServiceAgreementId("ex-id")))
            .dataItems(singletonList(new PresentationItemIdentifier()
                .internalIdIdentifier("item id")));

        when(joinPoint.getArgs()).thenReturn(new Object[]{validItemDataGroupIdIdentifier});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription("Update | Data Group | Failed | name dg-id, external service agreement ID ex-id")
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("External Service Agreement ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier()
                    .getExternalServiceAgreementId())
            .withEventMetaDatum("Data Group Description", validItemDataGroupIdIdentifier.getDescription())
            .withEventMetaDatum("Data Group Name",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            .withEventMetaDatum("Updated Data Group Name", validItemDataGroupIdIdentifier.getName())
            .withStatus(Status.FAILED);

        List<AuditMessage> expectedDescription = singletonList(eventData1);

        List<AuditMessage> initDescription = updateDataGroupServiceDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

}