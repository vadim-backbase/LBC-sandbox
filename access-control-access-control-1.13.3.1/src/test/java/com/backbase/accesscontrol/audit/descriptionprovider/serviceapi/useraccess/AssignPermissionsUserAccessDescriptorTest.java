package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.useraccess;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItemExtended.StatusEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationAssignUserPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.HashSet;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class AssignPermissionsUserAccessDescriptorTest {

    @InjectMocks
    private AssignPermissionsUserAccessDescriptor testy;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private static final String UPDATE_PERMISSIONS_PREFIX = "Update Permissions";
    private static final String INITIATE_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Initiated | for user %s in service agreement %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Successful | for user %s in service agreement %s";
    private static final String FAILED_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Failed | for user %s in service agreement %s";

    @Test
    public void getInitEventDataListTest() {
        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa1")
            .externalUserId("ex-u-1");
        PresentationAssignUserPermissions body2 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa2")
            .externalUserId("ex-u-2");

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(body1, body2)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withStatus(Status.INITIATED);
        AuditMessage eventData2 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-2", "ex-sa2"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa2")
            .withEventMetaDatum("External User ID", "ex-u-2")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);

        List<AuditMessage> initAuditMessages = testy.getInitEventDataList(joinPoint);
        assertEquals(expectedAuditMessages, initAuditMessages);
    }

    @Test
    public void getSuccessInitEventDataListWithFunctionGroupIdAndDataGroupId() {
        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa1")
            .externalUserId("ex-u-1")
            .functionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .functionGroupIdentifier(new PresentationIdentifier()
                    .idIdentifier("fg-id1"))
                .dataGroupIdentifiers(newArrayList(new PresentationIdentifier()
                    .idIdentifier("dg-id1"), new PresentationIdentifier()
                    .idIdentifier("dg-id2")))));

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(body1)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group ID", "fg-id1")
            .withEventMetaDatum("Data Group ID", "dg-id1")
            .withStatus(Status.INITIATED);
        AuditMessage eventData1_1 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group ID", "fg-id1")
            .withEventMetaDatum("Data Group ID", "dg-id2")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData1_1);

        List<AuditMessage> initAuditMessages = testy.getInitEventDataList(joinPoint);
        assertEquals(expectedAuditMessages, initAuditMessages);
    }

    @Test
    public void getSuccessInitEventDataListWithFunctionGroupNameIdAndDataGroupNameId() {
        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa2")
            .externalUserId("ex-u-2")
            .functionGroupDataGroups(newArrayList(new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier().idIdentifier("fg-id2")),
                new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("fg-name1")
                            .externalServiceAgreementId("ex-sa2")))
                    .dataGroupIdentifiers(singletonList(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("dg-name1")
                            .externalServiceAgreementId("ex-sa2"))))));

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(body1)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-2", "ex-sa2"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa2")
            .withEventMetaDatum("External User ID", "ex-u-2")
            .withEventMetaDatum("Function Group ID", "fg-id2")
            .withStatus(Status.INITIATED);
        AuditMessage eventData1_1 = new AuditMessage()
            .withEventDescription(String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-2", "ex-sa2"))
            .withEventMetaDatum("External Service Agreement ID", "ex-sa2")
            .withEventMetaDatum("External User ID", "ex-u-2")
            .withEventMetaDatum("Function Group Name", "fg-name1")
            .withEventMetaDatum("Data Group Name", "dg-name1")
            .withStatus(Status.INITIATED);
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData1_1);

        List<AuditMessage> initAuditMessages = testy.getInitEventDataList(joinPoint);
        assertEquals(expectedAuditMessages, initAuditMessages);
    }

    @Test
    public void getSuccessEventDataListWithFailedEventTests() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .resourceId("ex-u-1")
            .externalServiceAgreementId("ex-sa1")
            .status(StatusEnum.HTTP_STATUS_BAD_REQUEST)
            .errors(singletonList("Error assigning permissions."));

        List<BatchResponseItemExtended> data = asList(serviceAgreementIngestPostResponseBody1);

        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa1")
            .externalUserId("ex-u-1")
            .functionGroupDataGroups(newArrayList(new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier().idIdentifier("fg-id1")),
                new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("fg-name1")
                            .externalServiceAgreementId("ex-sa1")))
                    .dataGroupIdentifiers(singletonList(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("dg-name1")
                            .externalServiceAgreementId("ex-sa1"))))));

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(body1)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group ID", "fg-id1")
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "Error assigning permissions.")
            .withStatus(Status.FAILED)
            .withEventDescription(String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"));
        AuditMessage eventData1_1 = new AuditMessage()
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group Name", "fg-name1")
            .withEventMetaDatum("Data Group Name", "dg-name1")
            .withEventMetaDatum("Error code", "400")
            .withEventMetaDatum("Error message", "Error assigning permissions.")
            .withStatus(Status.FAILED)
            .withEventDescription(String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData1_1);

        List<AuditMessage> successAuditMessages = testy
            .getSuccessEventDataList(joinPoint, new ResponseEntity<>(data, HttpStatus.OK));
        assertEquals(expectedAuditMessages, successAuditMessages);
    }

    @Test
    public void getSuccessEventDataListWithSuccessEventTests() {
        BatchResponseItemExtended serviceAgreementIngestPostResponseBody1 = new BatchResponseItemExtended()
            .resourceId("ex-u-1")
            .externalServiceAgreementId("ex-sa1")
            .status(StatusEnum.HTTP_STATUS_OK);

        List<BatchResponseItemExtended> data = asList(serviceAgreementIngestPostResponseBody1);

        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa1")
            .externalUserId("ex-u-1")
            .functionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .functionGroupIdentifier(new PresentationIdentifier()
                    .idIdentifier("fg-id1"))
                .dataGroupIdentifiers(newArrayList(new PresentationIdentifier()
                    .idIdentifier("dg-id1"), new PresentationIdentifier()
                    .idIdentifier("dg-id2")))));

        when(joinPoint.getArgs()).thenReturn(new Object[]{asList(body1)});

        AuditMessage eventData1 = new AuditMessage()
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("Data Group ID", "dg-id1")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group ID", "fg-id1")
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"));
        AuditMessage eventData1_1 = new AuditMessage()
            .withEventMetaDatum("External Service Agreement ID", "ex-sa1")
            .withEventMetaDatum("Data Group ID", "dg-id2")
            .withEventMetaDatum("External User ID", "ex-u-1")
            .withEventMetaDatum("Function Group ID", "fg-id1")
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, "ex-u-1", "ex-sa1"));
        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData1_1);

        List<AuditMessage> successAuditMessages = testy
            .getSuccessEventDataList(joinPoint, new ResponseEntity<>(data, HttpStatus.OK));
        assertEquals(expectedAuditMessages, successAuditMessages);
    }

    @Test
    public void testMessageIds() {
        PresentationAssignUserPermissions body1 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa1")
            .externalUserId("ex-u-1")
            .functionGroupDataGroups(singletonList(new PresentationFunctionGroupDataGroup()
                .functionGroupIdentifier(new PresentationIdentifier()
                    .idIdentifier("fg-id1"))
                .dataGroupIdentifiers(newArrayList(new PresentationIdentifier()
                    .idIdentifier("dg-id1"), new PresentationIdentifier()
                    .idIdentifier("dg-id2")))));
        PresentationAssignUserPermissions body2 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa2")
            .externalUserId("ex-u-2")
            .functionGroupDataGroups(newArrayList(new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier().idIdentifier("fg-id2")),
                new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("fg-name1")
                            .externalServiceAgreementId("ex-sa2")))
                    .dataGroupIdentifiers(singletonList(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("dg-name1")
                            .externalServiceAgreementId("ex-sa2"))))));
        PresentationAssignUserPermissions body3 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa3")
            .externalUserId("ex-u-3")
            .functionGroupDataGroups(newArrayList(new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier().idIdentifier("fg-id3")),
                new PresentationFunctionGroupDataGroup()
                    .functionGroupIdentifier(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("fg-name3")
                            .externalServiceAgreementId("ex-sa3")))
                    .dataGroupIdentifiers(singletonList(new PresentationIdentifier()
                        .nameIdentifier(new PresentationIdentifierNameIdentifier()
                            .name("dg-name3")
                            .externalServiceAgreementId("ex-sa3"))))));
        PresentationAssignUserPermissions body4 = new PresentationAssignUserPermissions()
            .externalServiceAgreementId("ex-sa4")
            .externalUserId("ex-u-4");

        List<PresentationAssignUserPermissions> presentationAssignUserPermissionsList = asList(body1, body2, body3,
            body4);

        when(joinPoint.getArgs()).thenReturn(new Object[]{presentationAssignUserPermissionsList});

        List<String> messageIds = testy.getMessageIds(joinPoint);

        assertEquals(presentationAssignUserPermissionsList.size(), new HashSet<>(messageIds).size());
    }
}
