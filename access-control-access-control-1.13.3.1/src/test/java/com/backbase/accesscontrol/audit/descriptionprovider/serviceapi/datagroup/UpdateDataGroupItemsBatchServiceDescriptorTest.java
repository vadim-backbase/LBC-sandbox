package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAction;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationDataGroupItemPutRequestBody;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationItemIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupItemsBatchServiceDescriptorTest {

    @InjectMocks
    private UpdateDataGroupItemsBatchServiceDescriptor updateDataGroupItemsBatchServiceDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void invalidRequestTest() {

        PresentationDataGroupItemPutRequestBody body = new PresentationDataGroupItemPutRequestBody();
        body.setDataItems(new ArrayList<>());
        body.getDataItems().add(new PresentationItemIdentifier());
        when(joinPoint.getArgs()).thenReturn(new Object[]{singletonList(body)});

        AuditMessage initEvent = new AuditMessage()
            .withStatus(Status.INITIATED);

        List<AuditMessage> initDescription = updateDataGroupItemsBatchServiceDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(singletonList(initEvent), initDescription);

        AuditMessage failedEvent = new AuditMessage().withStatus(Status.FAILED);

        List<AuditMessage> failedDescription = updateDataGroupItemsBatchServiceDescriptor
            .getFailedEventDataList(joinPoint);

        assertEquals(singletonList(failedEvent), failedDescription);
    }

    @Test
    public void getInitEventDataListTest() {

        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.ADD)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().idIdentifier("dgId"))
            .dataItems(newArrayList(
                new PresentationItemIdentifier().internalIdIdentifier("itemId1"),
                new PresentationItemIdentifier().externalIdIdentifier("extItem2")));

        PresentationIdentifierNameIdentifier nameIdentifier = new PresentationIdentifierNameIdentifier()
            .name("DataGroup")
            .externalServiceAgreementId("externalSaId");

        PresentationDataGroupItemPutRequestBody validItemDataGroupNameIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.REMOVE)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().nameIdentifier(nameIdentifier))
            .dataItems(newArrayList(new PresentationItemIdentifier().internalIdIdentifier("itemId")));

        when(joinPoint.getArgs())
            .thenReturn(new Object[]{asList(validItemDataGroupIdIdentifier, validItemDataGroupNameIdentifier)});

        AuditMessage eventData11 = new AuditMessage()
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Type of change", "Add items")
            .withEventMetaDatum("Data Item ID", "itemId1")
            .withEventDescription("Update | Data Group Items | Initiated | ID dgId")
            .withStatus(Status.INITIATED);
        AuditMessage eventData12 = new AuditMessage()
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Type of change", "Add items")
            .withEventMetaDatum("External Data Item ID", "extItem2")
            .withEventDescription("Update | Data Group Items | Initiated | ID dgId")
            .withStatus(Status.INITIATED);
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum("Data Group Name",
                validItemDataGroupNameIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            .withEventMetaDatum("External Service Agreement ID",
                validItemDataGroupNameIdentifier.getDataGroupIdentifier().getNameIdentifier()
                    .getExternalServiceAgreementId())
            .withEventMetaDatum("Type of change", "Remove items")
            .withEventMetaDatum("Data Group Type", validItemDataGroupNameIdentifier.getType())
            .withEventMetaDatum("Data Item ID", "itemId")
            .withEventDescription("Update | Data Group Items | Initiated | name DataGroup, "
                + "external service agreement ID externalSaId")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedDescription = asList(eventData11, eventData12, eventData2);

        List<AuditMessage> initDescription = updateDataGroupItemsBatchServiceDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);

        List<String> messageIds = updateDataGroupItemsBatchServiceDescriptor.getMessageIds(joinPoint);

        assertEquals(3, messageIds.size());

        assertEquals(2, Sets.newHashSet(messageIds).size());
    }

    @Test
    public void getMessageIdsTest() {

        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.ADD)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().idIdentifier("dgId"))
            .dataItems(newArrayList(
                new PresentationItemIdentifier().internalIdIdentifier("itemId1"),
                new PresentationItemIdentifier().externalIdIdentifier("extItem2")));

        PresentationIdentifierNameIdentifier nameIdentifier = new PresentationIdentifierNameIdentifier()
            .name("DataGroup")
            .externalServiceAgreementId("externalSaId");

        PresentationDataGroupItemPutRequestBody validItemDataGroupNameIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.REMOVE)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().nameIdentifier(nameIdentifier))
            .dataItems(newArrayList(new PresentationItemIdentifier().internalIdIdentifier("itemId")));

        when(joinPoint.getArgs())
            .thenReturn(new Object[]{asList(validItemDataGroupIdIdentifier, validItemDataGroupNameIdentifier)});

        List<String> messageIds = updateDataGroupItemsBatchServiceDescriptor.getMessageIds(joinPoint);

        assertEquals(3, messageIds.size());

        assertEquals(2, Sets.newHashSet(messageIds).size());
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        BatchResponseItemExtended bodyValid = new BatchResponseItemExtended()
            .resourceId("dgId")
            .status(StatusEnum.HTTP_STATUS_OK)
            .action(PresentationAction.ADD);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        errors.add("something went wrong");
        BatchResponseItemExtended bodyInvalid = new BatchResponseItemExtended()
            .resourceId("DataGroup")
            .externalServiceAgreementId("externalSaId")
            .action(PresentationAction.REMOVE)
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(errors);

        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.ADD)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().idIdentifier("dgId"))
            .dataItems(newArrayList(
                new PresentationItemIdentifier().internalIdIdentifier("itemId1"),
                new PresentationItemIdentifier().externalIdIdentifier("extItem2")));

        PresentationIdentifierNameIdentifier nameIdentifier = new PresentationIdentifierNameIdentifier()
            .name("DataGroup")
            .externalServiceAgreementId("externalSaId");

        PresentationDataGroupItemPutRequestBody validItemDataGroupNameIdentifier = new PresentationDataGroupItemPutRequestBody()
            .action(PresentationAction.REMOVE)
            .type("ARRANGEMENTS")
            .dataGroupIdentifier(new PresentationIdentifier().nameIdentifier(nameIdentifier))
            .dataItems(newArrayList(new PresentationItemIdentifier().internalIdIdentifier("itemId")));

        when(joinPoint.getArgs())
            .thenReturn(new Object[]{asList(validItemDataGroupIdIdentifier, validItemDataGroupNameIdentifier)});

        AuditMessage eventData11 = new AuditMessage()
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Type of change", "Add items")
            .withEventMetaDatum("Data Item ID", "itemId1")
            .withEventDescription("Update | Data Group Items | Successful | ID dgId")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData12 = new AuditMessage()
            .withEventMetaDatum("Data Group ID",
                validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())
            .withEventMetaDatum("Data Group Type", validItemDataGroupIdIdentifier.getType())
            .withEventMetaDatum("Type of change", "Add items")
            .withEventMetaDatum("External Data Item ID", "extItem2")
            .withEventDescription("Update | Data Group Items | Successful | ID dgId")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum("Data Group Name",
                validItemDataGroupNameIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            .withEventMetaDatum("External Service Agreement ID",
                validItemDataGroupNameIdentifier.getDataGroupIdentifier().getNameIdentifier()
                    .getExternalServiceAgreementId())
            .withEventMetaDatum("Type of change", "Remove items")
            .withEventMetaDatum("Data Group Type", validItemDataGroupNameIdentifier.getType())
            .withEventMetaDatum("Data Item ID", "itemId")
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "bad request")
            .withEventDescription("Update | Data Group Items | Failed | name DataGroup, "
                + "external service agreement ID externalSaId")
            .withStatus(Status.FAILED);
        List<AuditMessage> expectedDescription = asList(eventData11, eventData12, eventData2);

        List<AuditMessage> initDescription = updateDataGroupItemsBatchServiceDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(asList(bodyValid, bodyInvalid), HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }
}