package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.aps;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.PresentationId;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.math.BigDecimal;
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
public class CreatePermissionSetDescriptorTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private CreatePermissionSetDescriptor createPermissionSetDescriptor;

    @Test
    public void getInitEventDataList() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(asList(new PresentationPermissionSetItem().functionId("id1")
                    .privileges(asList("view", "create", "delete")),
                new PresentationPermissionSetItem().functionId("id2").privileges(singletonList("approve"))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id1 / create, delete, view");
        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id2 / approve");

        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataList() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(asList(new PresentationPermissionSetItem().functionId("id1")
                    .privileges(asList("view", "create", "delete")),
                new PresentationPermissionSetItem().functionId("id2").privileges(singletonList("approve"))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Assignable Permission Set | Successful | name apsName, ID 1234")
            .withEventMetaDatum("ID", "1234")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id1 / create, delete, view");
        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Assignable Permission Set | Successful | name apsName, ID 1234")
            .withEventMetaDatum("ID", "1234")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id2 / approve");

        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(new PresentationId().id(new BigDecimal(1234)), HttpStatus.CREATED));

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getFailedEventDataList() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(asList(new PresentationPermissionSetItem().functionId("id1")
                    .privileges(asList("view", "create", "delete")),
                new PresentationPermissionSetItem().functionId("id2").privileges(singletonList("approve"))));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Assignable Permission Set | Failed | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id1 / create, delete, view");
        AuditMessage eventData2 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Assignable Permission Set | Failed | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id2 / approve");

        List<AuditMessage> expectedAuditMessages = asList(eventData1, eventData2);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getFailedEventDataListWithEmptyPermission() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(emptyList());

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription("Create | Assignable Permission Set | Failed | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData1);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getFailedEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void getSuccessEventDataListWithEmptyPermission() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(emptyList());

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription("Create | Assignable Permission Set | Successful | name apsName, ID 1234")
            .withEventMetaDatum("ID", "1234")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData1);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor
            .getSuccessEventDataList(joinPoint,
                getResponseEntity(new PresentationId().id(new BigDecimal(1234)), HttpStatus.CREATED));

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void testInitEventDataListWithEmptyPermissions() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(emptyList());

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void testInitEventDataListWithNullPermissions() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(null);

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void testInitEventDataListWithNullItemsPermissions() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(singletonList(null));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void testInitEventDataListWithEmptyPrivileges() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(
                singletonList(new PresentationPermissionSetItem().functionId("id1").privileges(emptyList())));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id1 / ");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }

    @Test
    public void testInitEventDataListWithNullPrivilegeItem() {
        List<String> privileges = new ArrayList<>();
        privileges.add(null);

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().name("apsName")
            .description("apsDescription")
            .permissions(singletonList(
                new PresentationPermissionSetItem().functionId("id1").privileges(privileges)));

        when(joinPoint.getArgs()).thenReturn(singletonList(permissionSet).toArray());

        AuditMessage eventData = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription("Create | Assignable Permission Set | Initiated | name apsName")
            .withEventMetaDatum("Assignable Permission Set Name", "apsName")
            .withEventMetaDatum("Assignable Permission Set Description", "apsDescription")
            .withEventMetaDatum("Permission Set", "id1 / ");

        List<AuditMessage> expectedAuditMessages = singletonList(eventData);
        List<AuditMessage> auditMessages = createPermissionSetDescriptor.getInitEventDataList(joinPoint);

        assertEquals(expectedAuditMessages, auditMessages);
    }
}
