package com.backbase.accesscontrol.service.batch.serviceagreement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.ExtendedResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModifyUsersAndAdminsInServiceAgreementTest {

    @InjectMocks
    private ModifyUsersAndAdminsInServiceAgreement modifyUsersAndAdminsInServiceAgreement;

    @Mock
    private ServiceAgreementAdminService serviceAgreementAdminService;
    @Mock
    private EventBus eventBus;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    @Test
    public void shouldAddAdmin() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);

        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingStateByExternalId(eq("extSa1"));
        assertEquals(response, Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_OK, new ArrayList<>(), "extSa1")));
    }

    @Test
    public void shouldRemoveAdmin() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.REMOVE)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).removeAdminFromServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        verify(serviceAgreementBusinessRulesService).isServiceAgreementInPendingStateByExternalId(eq("extSa1"));
        assertEquals(response, Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_OK, new ArrayList<>(), "extSa1")));
    }

    @Test
    public void shouldAddUser() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.REGULAR_USER, request, usersByExternalIds);
        verify(persistenceServiceAgreementService).addUserInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        verify(serviceAgreementBusinessRulesService, times(1))
            .isServiceAgreementInPendingStateByExternalId(anyString());
        assertEquals(response, Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_OK, new ArrayList<>(), "extSa1")));
    }

    @Test
    public void shouldRemoveUser() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.REMOVE)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.REGULAR_USER, request, usersByExternalIds);
        verify(persistenceServiceAgreementService)
            .removeUserFromServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        verify(serviceAgreementBusinessRulesService, times(1))
            .isServiceAgreementInPendingStateByExternalId(anyString());
        assertEquals(response, Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_OK, new ArrayList<>(), "extSa1")));
    }

    @Test
    public void shouldReturnBadRequestResponse() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        when(serviceAgreementAdminService.addAdminInServiceAgreementBatch(anyString(), anyString(), anyString()))
            .thenThrow(new BadRequestException()
                .withErrors(Lists.newArrayList(new Error().withMessage("error").withKey("err"))));
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        assertEquals(Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_BAD_REQUEST, Lists.newArrayList("error"),
                "extSa1")),
            response);
    }

    @Test
    public void shouldReturnNotFoundResponse() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        when(serviceAgreementAdminService.addAdminInServiceAgreementBatch(anyString(), anyString(), anyString()))
            .thenThrow(new NotFoundException()
                .withErrors(Lists.newArrayList(new Error().withMessage("error").withKey("err"))));
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        assertEquals(Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_NOT_FOUND, Lists.newArrayList("error"),
                "extSa1")),
            response);
    }

    @Test
    public void shouldReturnBadRequestResponseFromCause() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        when(serviceAgreementAdminService.addAdminInServiceAgreementBatch(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException(new BadRequestException()
                .withErrors(Lists.newArrayList(new Error().withMessage("error").withKey("err")))));
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        assertEquals(Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_BAD_REQUEST, Lists.newArrayList("error"),
                "extSa1")),
            response);
    }

    @Test
    public void shouldReturnNotFoundResponseFromCause() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        when(serviceAgreementAdminService.addAdminInServiceAgreementBatch(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException(new NotFoundException()
                .withErrors(Lists.newArrayList(new Error().withMessage("error").withKey("err")))));
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        assertEquals(Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_NOT_FOUND, Lists.newArrayList("error"),
                "extSa1")),
            response);
    }

    @Test
    public void shouldReturnInternalExceptionResponse() {
        PresentationServiceAgreementUsersUpdate request = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(Lists.newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId("extSa1")
                .withExternalUserId("u1")));
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId("id");
        user1.setLegalEntityId("le");
        user1.setExternalId("u1");
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersByExternalIds = Maps
            .asMap(Sets.newHashSet("u1"), key -> user1);
        when(serviceAgreementAdminService.addAdminInServiceAgreementBatch(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("error"));
        List<ExtendedResponseItem> response = modifyUsersAndAdminsInServiceAgreement.processBatch(
            UserType.ADMIN_USER, request, usersByExternalIds);
        verify(serviceAgreementAdminService).addAdminInServiceAgreementBatch(eq("extSa1"), eq("id"), eq("le"));
        assertEquals(Lists.newArrayList(
            new ExtendedResponseItem("u1", ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR,
                Lists.newArrayList("error"), "extSa1")),
            response);
    }
}