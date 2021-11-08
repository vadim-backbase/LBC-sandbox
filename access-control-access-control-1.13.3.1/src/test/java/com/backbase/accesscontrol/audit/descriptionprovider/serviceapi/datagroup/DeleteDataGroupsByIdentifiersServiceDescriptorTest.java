package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

/**
 * Tests for {@link DeleteDataGroupsByIdentifiersServiceDescriptorTest}
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteDataGroupsByIdentifiersServiceDescriptorTest {

    @InjectMocks
    private DeleteDataGroupsByIdentifiersServiceDescriptor deleteDataGroupsByIdentifiersServiceDescriptor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void invalidRequestTest() {
        PresentationIdentifier identifier = new PresentationIdentifier();

        when(joinPoint.getArgs()).thenReturn(new Object[]{singletonList(identifier)});

        AuditMessage initEvent = new AuditMessage()
            .withStatus(Status.INITIATED);

        List<AuditMessage> initDescription = deleteDataGroupsByIdentifiersServiceDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(singletonList(initEvent), initDescription);
    }

    @Test
    public void getInitEventDataListTest() {

        PresentationIdentifier idIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(idIdentifier, nameIdentifier)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("Data Group ID", "123456")
            .withEventDescription("Delete | Data Group | Initiated | ID 123456")
            .withStatus(Status.INITIATED);
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum("Data Group Name", "NameId")
            .withEventMetaDatum("External Service Agreement ID", "ExSa")
            .withEventDescription("Delete | Data Group | Initiated | name NameId, external service agreement ID ExSa")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteDataGroupsByIdentifiersServiceDescriptor
            .getInitEventDataList(joinPoint);

        assertEquals(expectedDescription, initDescription);
    }

    @Test
    public void getMessageIdsTest() {

        PresentationIdentifier idIdentifier = new PresentationIdentifier()
            .idIdentifier("123456");
        PresentationIdentifierNameIdentifier nameId = new PresentationIdentifierNameIdentifier()
            .externalServiceAgreementId("ExSa")
            .name("NameId");
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .nameIdentifier(nameId);

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(idIdentifier, nameIdentifier)});

        Set<String> messageIds = new HashSet<>(deleteDataGroupsByIdentifiersServiceDescriptor
            .getMessageIds(joinPoint));

        assertEquals(2, messageIds.size());
    }

    @Test
    public void getSuccessEventDataListWithBothFailedAndSuccessfulEventsTest() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .resourceId("123456")
            .status(StatusEnum.HTTP_STATUS_OK);
        List<String> errors = new ArrayList<>();
        errors.add("bad request");
        errors.add("something went wrong");
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody2 = new BatchResponseItemExtended()
            .resourceId("NameId")
            .externalServiceAgreementId("ExSa")
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(errors);

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("Data Group ID", "123456")
            .withEventDescription("Delete | Data Group | Successful | ID 123456")
            .withStatus(Status.SUCCESSFUL);
        AuditMessage eventData2 = new AuditMessage()
            .withEventMetaDatum("Data Group Name", "NameId")
            .withEventMetaDatum("External Service Agreement ID", "ExSa")
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "bad request")
            .withEventDescription("Delete | Data Group | Failed | name NameId, external service agreement ID ExSa")
            .withStatus(Status.FAILED);
        List<AuditMessage> expectedDescription = asList(eventData1, eventData2);

        List<AuditMessage> initDescription = deleteDataGroupsByIdentifiersServiceDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(
                asList(serviceAgreementIngestPostResponseBody1, serviceAgreementIngestPostResponseBody2),
                HttpStatus.MULTI_STATUS));

        assertEquals(expectedDescription, initDescription);
    }
}
