package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.ExtendedResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ModifyUsersAndAdminsInServiceAgreement {

    private ServiceAgreementAdminService serviceAgreementAdminService;
    private EventBus eventBus;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public List<ExtendedResponseItem> processBatch(UserType userType,
        PresentationServiceAgreementUsersUpdate presentationServiceAgreementUsersUpdate,
        Map<String, GetUser> usersByExternalId) {

        PresentationAction action = presentationServiceAgreementUsersUpdate.getAction();
        List<PresentationServiceAgreementUserPair> users = presentationServiceAgreementUsersUpdate.getUsers();

        return users.stream()
            .map(user -> persist(user, userType, action, usersByExternalId))
            .collect(Collectors.toList());

    }

    private ExtendedResponseItem persist(PresentationServiceAgreementUserPair user,
        UserType userType, PresentationAction action,
        Map<String, GetUser> usersByExternalId) {

        ExtendedResponseItem result = new ExtendedResponseItem(user.getExternalUserId(),
            ItemStatusCode.HTTP_STATUS_OK, new ArrayList<>(), user.getExternalServiceAgreementId());

        GetUser userDetails = usersByExternalId.get(user.getExternalUserId());
        String serviceAgreementId;
        try {
            if (serviceAgreementBusinessRulesService
                .isServiceAgreementInPendingStateByExternalId(user.getExternalServiceAgreementId())) {
                throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
            }
            serviceAgreementBusinessRulesService
                .checkPendingValidationsInServiceAgreementExternalServiceAgreementId(
                    user.getExternalServiceAgreementId());
            serviceAgreementBusinessRulesService.checkPendingDeleteOfFunctionOrDataGroupInServiceAgreementExternalId(
                user.getExternalServiceAgreementId());
            if (userType == UserType.REGULAR_USER) {
                if (action == PresentationAction.ADD) {
                    serviceAgreementId = persistenceServiceAgreementService.addUserInServiceAgreementBatch(
                        user.getExternalServiceAgreementId(),
                        userDetails.getId(),
                        userDetails.getLegalEntityId());
                } else {
                    serviceAgreementId = persistenceServiceAgreementService.removeUserFromServiceAgreementBatch(
                        user.getExternalServiceAgreementId(),
                        userDetails.getId(),
                        userDetails.getLegalEntityId());
                }
            } else {
                if (action == PresentationAction.ADD) {
                    serviceAgreementId = serviceAgreementAdminService.addAdminInServiceAgreementBatch(
                        user.getExternalServiceAgreementId(),
                        userDetails.getId(),
                        userDetails.getLegalEntityId());
                } else {
                    serviceAgreementId = serviceAgreementAdminService.removeAdminFromServiceAgreementBatch(
                        user.getExternalServiceAgreementId(),
                        userDetails.getId(),
                        userDetails.getLegalEntityId());
                }
            }
            processEvent(new ServiceAgreementEvent().withId(serviceAgreementId).withAction(UPDATE));
        } catch (BadRequestException e) {
            return new ExtendedResponseItem(user.getExternalUserId(),
                ItemStatusCode.HTTP_STATUS_BAD_REQUEST,
                e.getErrors().stream().map(Error::getMessage).collect(Collectors.toList()),
                user.getExternalServiceAgreementId());
        } catch (NotFoundException e) {
            return new ExtendedResponseItem(user.getExternalUserId(), ItemStatusCode.HTTP_STATUS_NOT_FOUND,
                e.getErrors().stream().map(Error::getMessage).collect(Collectors.toList()),
                user.getExternalServiceAgreementId());
        } catch (Exception err) {
            if (err.getCause() instanceof BadRequestException) {
                BadRequestException e = (BadRequestException) err.getCause();
                return new ExtendedResponseItem(user.getExternalUserId(),
                    ItemStatusCode.HTTP_STATUS_BAD_REQUEST,
                    e.getErrors().stream().map(Error::getMessage).collect(Collectors.toList()),
                    user.getExternalServiceAgreementId());
            } else if (err.getCause() instanceof NotFoundException) {
                NotFoundException e = (NotFoundException) err.getCause();
                return new ExtendedResponseItem(user.getExternalUserId(),
                    ItemStatusCode.HTTP_STATUS_NOT_FOUND,
                    e.getErrors().stream().map(Error::getMessage).collect(Collectors.toList()),
                    user.getExternalServiceAgreementId());
            } else {
                return new ExtendedResponseItem(user.getExternalUserId(),
                    ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR,
                    Lists.newArrayList(err.getMessage()),
                    user.getExternalServiceAgreementId());
            }
        }
        return result;
    }


    private void processEvent(Event event) {
        if (event == null) {
            return;
        }
        EnvelopedEvent<Event> envelopedEvent = new EnvelopedEvent<>();
        envelopedEvent.setEvent(event);
        envelopedEvent.setOriginatorContext(null);

        eventBus.emitEvent(envelopedEvent);
    }
}
