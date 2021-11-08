package com.backbase.accesscontrol.business.serviceagreement;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.dto.ExtendedResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.ModifyUsersAndAdminsInServiceAgreement;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestAdminsUpdateTest {

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ModifyUsersAndAdminsInServiceAgreement modifyUsersAndAdminsInServiceAgreement;
    @Mock
    private Validator validator;
    @Mock
    private ConstraintViolation<PresentationServiceAgreementUserPair> validationError;
    @Spy
    private BatchResponseItemMapper batchResponseItemMapper = Mappers.getMapper(BatchResponseItemMapper.class);

    @InjectMocks
    private IngestAdminsUpdate ingestAdminsUpdate;

    @Test
    public void testIngestAdminsUpdateAdd() {
        String invalidExternalUserId = "U-02";
        String validExternalUserId = "U-01";
        String validInternalUserId = "0001";
        String legalEntityId = "LE-01";
        String externalServiceAgreementId = "SA-01";
        String invalidServiceAgreementId = "SA-02";
        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(asList(new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(invalidServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(invalidExternalUserId)));

        List<PresentationServiceAgreementUserPair> validUserRequest = Collections
            .singletonList(new PresentationServiceAgreementUserPair()
                .withExternalUserId(validExternalUserId)
                .withExternalServiceAgreementId(externalServiceAgreementId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(validInternalUserId);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(validExternalUserId);
        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse = Collections
            .singletonList(user1);

        mockGetUsers(usersResponse);

        when(validator.validate(any(PresentationServiceAgreementUserPair.class), any())).thenReturn(new HashSet<>());
        when(validator.validate(eq(new PresentationServiceAgreementUserPair()
            .withExternalServiceAgreementId(invalidServiceAgreementId)
            .withExternalUserId(validExternalUserId)))).thenReturn(Sets.newHashSet(validationError));

        InternalRequest<PresentationServiceAgreementUsersUpdate> request = RequestUtils.getInternalRequest(
            data);

        when(modifyUsersAndAdminsInServiceAgreement.processBatch(eq(UserType.ADMIN_USER),
            any(PresentationServiceAgreementUsersUpdate.class), anyMap()))
            .thenReturn(asList(new ExtendedResponseItem(validExternalUserId, ItemStatusCode.HTTP_STATUS_NOT_FOUND,
                asList("error"), externalServiceAgreementId)));

        List<BatchResponseItemExtended> response = ingestAdminsUpdate.updateBatchAdmins(request).getData();

        verify(modifyUsersAndAdminsInServiceAgreement)
            .processBatch(eq(UserType.ADMIN_USER), eq(new PresentationServiceAgreementUsersUpdate()
                    .withUsers(new ArrayList(validUserRequest))
                    .withAction(PresentationAction.ADD)),
                eq(Maps.asMap(Sets.newHashSet(validExternalUserId), item -> usersResponse.get(0))));

        verify(userManagementService).getUsersByExternalIds(
            argThat(list -> list.containsAll(Lists.newArrayList(validExternalUserId, invalidExternalUserId))));

        verify(validator, times(3)).validate(any(), any());

        assertEquals(Lists.newArrayList(new BatchResponseItemExtended()
            .withResourceId(validExternalUserId)
            .withExternalServiceAgreementId(invalidServiceAgreementId)
            .withErrors(Lists.newArrayList("null null"))
            .withAction(PresentationAction.ADD)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST), new BatchResponseItemExtended()
            .withResourceId(invalidExternalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withErrors(Lists.newArrayList("Invalid external user id"))
            .withAction(PresentationAction.ADD)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST), new BatchResponseItemExtended()
            .withResourceId(validExternalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withAction(PresentationAction.ADD)
            .withErrors(Lists.newArrayList("error"))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND)), response);
    }

    @Test
    public void testIngestAdminsUpdateRemove() {
        String invalidExternalUserId = "U-02";
        String validExternalUserId = "U-01";
        String validInternalUserId = "0001";
        String legalEntityId = "LE-01";
        String externalServiceAgreementId = "SA-01";
        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.REMOVE)
            .withUsers(asList(new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(invalidExternalUserId)));

        List<PresentationServiceAgreementUserPair> validUserRequest = Collections
            .singletonList(new PresentationServiceAgreementUserPair()
                .withExternalUserId(validExternalUserId)
                .withExternalServiceAgreementId(externalServiceAgreementId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(validInternalUserId);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(validExternalUserId);
        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse = Collections
            .singletonList(user1);
        mockGetUsers(usersResponse);

        InternalRequest<PresentationServiceAgreementUsersUpdate> request = RequestUtils.getInternalRequest(
            data);

        when(modifyUsersAndAdminsInServiceAgreement.processBatch(eq(UserType.ADMIN_USER),
            any(PresentationServiceAgreementUsersUpdate.class), anyMap()))
            .thenReturn(asList(new ExtendedResponseItem(validExternalUserId, ItemStatusCode.HTTP_STATUS_OK,
                new ArrayList<>(), externalServiceAgreementId)));

        List<BatchResponseItemExtended> response = ingestAdminsUpdate.updateBatchAdmins(request).getData();

        verify(modifyUsersAndAdminsInServiceAgreement)
            .processBatch(eq(UserType.ADMIN_USER), eq(new PresentationServiceAgreementUsersUpdate()
                    .withUsers(new ArrayList(validUserRequest))
                    .withAction(PresentationAction.REMOVE)),
                eq(Maps.asMap(Sets.newHashSet(validExternalUserId), item -> usersResponse.get(0))));

        verify(userManagementService).getUsersByExternalIds(
            argThat(list -> list.containsAll(Lists.newArrayList(validExternalUserId, invalidExternalUserId))));

        assertEquals(Lists.newArrayList(new BatchResponseItemExtended()
            .withResourceId(invalidExternalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withErrors(Lists.newArrayList("Invalid external user id"))
            .withAction(PresentationAction.REMOVE)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST), new BatchResponseItemExtended()
            .withResourceId(validExternalUserId)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withAction(PresentationAction.REMOVE)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)), response);
    }

    @Test
    public void shouldCallAddAdminsPandpWhenActionIsAdd() {
        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD);
        InternalRequest<PresentationServiceAgreementUsersUpdate> request = RequestUtils.getInternalRequest(
            data);

        ingestAdminsUpdate.updateBatchAdmins(request);

        verify(modifyUsersAndAdminsInServiceAgreement)
            .processBatch(eq(UserType.ADMIN_USER), eq(new PresentationServiceAgreementUsersUpdate()
                .withUsers(new ArrayList())
                .withAction(PresentationAction.ADD)), argThat(arg -> arg.entrySet().size() == 0));

        verify(batchResponseItemMapper)
            .toExtendedPresentationList(argThat(arg -> arg.size() == 0)
            );
    }

    @Test
    public void shouldCallAddAdminsPandpWhenActionIsRemove() {
        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.REMOVE);
        InternalRequest<PresentationServiceAgreementUsersUpdate> request = RequestUtils.getInternalRequest(
            data);

        ingestAdminsUpdate.updateBatchAdmins(request);

        verify(modifyUsersAndAdminsInServiceAgreement)
            .processBatch(eq(UserType.ADMIN_USER), eq(new PresentationServiceAgreementUsersUpdate()
                .withUsers(new ArrayList())
                .withAction(PresentationAction.REMOVE)), argThat(arg -> arg.entrySet().size() == 0));

        verify(batchResponseItemMapper)
            .toExtendedPresentationList(argThat(arg -> arg.size() == 0)
            );
    }

    private void mockGetUsers(List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse) {
        when(userManagementService
            .getUsersByExternalIds(anyList()))
            .thenReturn(usersResponse);
    }

    @Test
    public void testIngestDuplicateAdminsUpdateAdd() {
        String validExternalUserId = "U-01";
        String validInternalUserId = "0001";
        String legalEntityId = "LE-01";
        String externalServiceAgreementId = "SA-01";
        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(asList(new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId)));

        List<PresentationServiceAgreementUserPair> validUserRequest = Lists
            .newArrayList(new PresentationServiceAgreementUserPair()
                .withExternalUserId(validExternalUserId)
                .withExternalServiceAgreementId(externalServiceAgreementId), new PresentationServiceAgreementUserPair()
                .withExternalUserId(validExternalUserId)
                .withExternalServiceAgreementId(externalServiceAgreementId));

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(validInternalUserId);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(validExternalUserId);
        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse = Collections
            .singletonList(user1);
        mockGetUsers(usersResponse);

        when(validator.validate(any(PresentationServiceAgreementUserPair.class), any())).thenReturn(new HashSet<>());

        InternalRequest<PresentationServiceAgreementUsersUpdate> request = RequestUtils.getInternalRequest(
            data);

        ingestAdminsUpdate.updateBatchAdmins(request);

        verify(modifyUsersAndAdminsInServiceAgreement)
            .processBatch(eq(UserType.ADMIN_USER), eq(new PresentationServiceAgreementUsersUpdate()
                    .withUsers(new ArrayList(validUserRequest))
                    .withAction(PresentationAction.ADD)),
                eq(Maps.asMap(Sets.newHashSet(validExternalUserId), item -> usersResponse.get(0))));

        verify(userManagementService)
            .getUsersByExternalIds(argThat(list -> list.containsAll(Lists.newArrayList(validExternalUserId))));

        verify(validator, times(2)).validate(any(), any());
    }
}