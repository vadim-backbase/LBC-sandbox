package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.client.rest.spec.model.IdItem;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementItem;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementPost;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementStatePut;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementUsersGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.UnexposedUsersGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.UpdateAdmins;
import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for Service Agreements.
 */
@RestController
@AllArgsConstructor
public class ServiceAgreementController implements com.backbase.accesscontrol.client.rest.spec.api.ServiceAgreementApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementController.class);
    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private ServiceAgreementService serviceAgreementService;
    private ParameterValidationService parameterValidationService;
    private UserContextUtil userContextUtil;
    private PermissionValidationService permissionValidationService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private PayloadConverter payloadConverter;


    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementItem>> getServiceAgreement(String creatorId, String cursor, Integer from,
        Integer size, String query) {
        permissionValidationService.validateAccessToLegalEntityResource(creatorId, AccessResourceType.NONE);
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        LOGGER.info("Getting all service agreements");
        PaginationDto<ServiceAgreementGetResponseBody> response = serviceAgreementService
            .getServiceAgreements(creatorId, query, from, size, cursor);
        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, response.getTotalNumberOfRecords().toString());
        return new ResponseEntity<>(payloadConverter.convertListPayload(
            response.getRecords(),
            ServiceAgreementItem.class), headers, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementUsersGetResponseBody>> getServiceAgreementAdmins(
        String serviceAgreementId) {
        LOGGER.info("Getting admins of service agreements with id {}", serviceAgreementId);

        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(payloadConverter.convertListPayload(
            serviceAgreementService.getServiceAgreementAdmins(serviceAgreementId),
            ServiceAgreementUsersGetResponseBody.class), null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ServiceAgreementItem> getServiceAgreementContext() {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        LOGGER.info("Get Service Agreement by id from context {}", serviceAgreementId);

        return new ResponseEntity<>(
            payloadConverter.convert(serviceAgreementService.getServiceAgreementById(serviceAgreementId),
                ServiceAgreementItem.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<ServiceAgreementItem> getServiceAgreementItem(String serviceAgreementId) {
        LOGGER.info("Get Service Agreement by id {}", serviceAgreementId);
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(
            payloadConverter.convert(
                serviceAgreementService.getServiceAgreementById(serviceAgreementId),
                ServiceAgreementItem.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementUsersGetResponseBody>> getServiceAgreementUsers(Integer from,
        Integer size, String cursor, String query) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);

        LOGGER.info("Getting users for service agreement id {}, full name like {}, from {} and size {}",
            serviceAgreementId, query, from, size);

        ListElementsWrapper<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody> data = serviceAgreementService
            .getUsersForServiceAgreement(serviceAgreementId, query, from, size, cursor);

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(data.getTotalNumberOfRecords()));

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            data.getRecords(),
            ServiceAgreementUsersGetResponseBody.class), headers, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<UnexposedUsersGetResponseBody>> getUnexposedUsers(String query, Integer from,
        String cursor, Integer size) {
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        LOGGER.info(
            "Provides a list of all unexposed users for Service Agreement {} with size {}, from {}, query {}",
            serviceAgreementId, size, from, query);
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);
        PaginationDto<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.
            UnexposedUsersGetResponseBody> response = serviceAgreementService
            .getUnexposedUsers(serviceAgreementId, from, size, query, cursor);

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, response.getTotalNumberOfRecords().toString());

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            response.getRecords(),
            UnexposedUsersGetResponseBody.class), headers, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.REMOVE_USERS, objectType = AuditObjectType.SERVICE_AGREEMENT_USERS_REMOVE)
    public ResponseEntity<Void> postUsersRemove(UsersForServiceAgreement usersForServiceAgreement) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);
        LOGGER
            .info("removing users with ids {} from service agreement id {}", usersForServiceAgreement.getUsers(),
                serviceAgreementId);
        serviceAgreementService.removeUsersFromServiceAgreement(payloadConverter
                .convert(usersForServiceAgreement, PresentationUsersForServiceAgreementRequestBody.class),
            serviceAgreementId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_CREATE + "'})")
    public ResponseEntity<IdItem> postServiceAgreement(ServiceAgreementPost serviceAgreementPost) {
        LOGGER.info("Creating new service agreement {}", serviceAgreementPost);
        permissionValidationService
            .validateAccessToLegalEntityResource(getServiceAgreementLegalEntities(serviceAgreementPost),
                AccessResourceType.NONE);

        ServiceAgreementPostResponseBody responseBody = serviceAgreementService
            .addServiceAgreement(
                payloadConverter.convertAndValidate(serviceAgreementPost, ServiceAgreementPostRequestBody.class));
        HttpStatus status = HttpStatus.CREATED;

        if (approvalOnRequestScope.isApproval()) {
            status = HttpStatus.ACCEPTED;
        }

        return new ResponseEntity<>(new IdItem().id(responseBody.getId()), status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.ADD_USERS, objectType = AuditObjectType.SERVICE_AGREEMENT_USERS_ADD)
    public ResponseEntity<Void> postUsersAdd(UsersForServiceAgreement usersForServiceAgreement) {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.USER_AND_ACCOUNT);
        LOGGER.info("add users with ids {} for service agreement id {}", usersForServiceAgreement.getUsers(),
            serviceAgreementId);
        serviceAgreementService.addUsersInServiceAgreement(payloadConverter.convertAndValidate(
            usersForServiceAgreement,
            PresentationUsersForServiceAgreementRequestBody.class), serviceAgreementId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.UPDATE_ADMINS, objectType = AuditObjectType.SERVICE_AGREEMENT_ADMINS)
    public ResponseEntity<Void> putAdmins(String id, UpdateAdmins updateAdmins) {
        LOGGER.info("Updating Admins of Service Agreement with Id {}", id);
        validateServiceAgreementContext(id, AccessResourceType.NONE);
        serviceAgreementService
            .updateAdmins(payloadConverter.convertAndValidate(updateAdmins, AdminsPutRequestBody.class), id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.UPDATE_STATE, objectType = AuditObjectType.SERVICE_AGREEMENT_STATE)
    public ResponseEntity<Void> putServiceAgreementState(String serviceAgreementId,
        ServiceAgreementStatePut serviceAgreementStatePut) {
        LOGGER.info("Update Service Agreement Status by id {}", serviceAgreementId);
        validateServiceAgreementContext(serviceAgreementId, AccessResourceType.NONE);
        serviceAgreementService.updateServiceAgreementState(
            payloadConverter
                .convertAndValidate(serviceAgreementStatePut, ServiceAgreementStatePutRequestBody.class),
            serviceAgreementId);
        return ResponseEntity.ok().build();
    }

    private void validateServiceAgreementContext(String serviceAgreementId, AccessResourceType user) {
        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, user);
    }

    private List<String> getServiceAgreementLegalEntities(
        ServiceAgreementPost serviceAgreementPostRequestBody) {
        return Optional
            .ofNullable(serviceAgreementPostRequestBody.getParticipants())
            .orElse(new ArrayList<>())
            .stream()
            .map(com.backbase.accesscontrol.client.rest.spec.model.Participant::getId)
            .collect(toList());
    }
}
