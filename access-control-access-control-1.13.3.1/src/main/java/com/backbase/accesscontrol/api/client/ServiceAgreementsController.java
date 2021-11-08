package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_ASSIGN_PERMISSONS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.FUNCTION_MANAGE_USERS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.RESOURCE_MANAGE_USERS;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.approval.scope.ApprovalOnRequestScope;
import com.backbase.accesscontrol.client.rest.spec.model.FunctionsGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.ListOfFunctionGroupsWithDataGroups;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalPermissions;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalStatus;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementSave;
import com.backbase.accesscontrol.client.rest.spec.model.ServiceAgreementUsersGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.UnexposedUsersGetResponseBody;
import com.backbase.accesscontrol.client.rest.spec.model.UsersForServiceAgreement;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementFlowService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
public class ServiceAgreementsController implements com.backbase.accesscontrol.client.rest.spec.api.ServiceAgreementsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsController.class);
    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private ServiceAgreementService serviceAgreementService;
    private PermissionValidationService permissionValidationService;
    private ParameterValidationService parameterValidationService;
    private UserContextUtil userContextUtil;
    private ServiceAgreementFlowService serviceAgreementFlowService;
    private ApprovalOnRequestScope approvalOnRequestScope;
    private PayloadConverter payloadConverter;


    private String retrieveAndValidateCreatorId(String creatorId) {
        LOGGER.info("Trying to retrieve creator id");
        if (StringUtils.isEmpty(creatorId)) {
            creatorId = userContextUtil.getUserContextDetails().getLegalEntityId();
        }
        permissionValidationService.validateAccessToLegalEntityResource(creatorId, AccessResourceType.NONE);
        return creatorId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + FUNCTION_ASSIGN_PERMISSONS + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<PresentationApprovalPermissions> getAssignUsersPermissions(String id, String userId) {

            permissionValidationService.validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
            LOGGER.info("Getting permissions for user id{} in the service agreement id {}", userId, id);
        return new ResponseEntity<>(
            payloadConverter.convert(
                serviceAgreementService.getAssignedUsersPermissions(id, userId),
                PresentationApprovalPermissions.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.ENTITLEMENTS_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS + "', "
        + "{'" + ResourceAndFunctionNameConstants.PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<FunctionsGetResponseBody>> getBusinessfunctions(String id) {
        LOGGER.info("Getting business function for Service Agreement with id {}", id);
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(payloadConverter
            .convertListPayload(serviceAgreementFlowService.getBusinessFunctionsForServiceAgreement(id),
                com.backbase.accesscontrol.client.rest.spec.model.FunctionsGetResponseBody.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementParticipantsGetResponseBody>> getContextServiceAgreementParticipants() {
        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        LOGGER.info("Getting participants on Service Agreement from context {}", serviceAgreementId);
        return new ResponseEntity<>(
            payloadConverter
                .convertListPayload(serviceAgreementService.getServiceAgreementParticipants(serviceAgreementId),
                    ServiceAgreementParticipantsGetResponseBody.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementParticipantsGetResponseBody>> getServiceAgreementParticipants(
        String serviceAgreementId) {
        LOGGER.info("Getting participants on Service Agreement from context {}", serviceAgreementId);
        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.USER_OR_ACCOUNT);
        return new ResponseEntity<>(
            payloadConverter
                .convertListPayload(serviceAgreementService.getServiceAgreementParticipants(serviceAgreementId),
                    ServiceAgreementParticipantsGetResponseBody.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("checkPermission('" + RESOURCE_MANAGE_USERS + "', "
        + "'" + FUNCTION_MANAGE_USERS + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<ServiceAgreementUsersGetResponseBody>> getServiceAgreementUsersSA(String id,
        String query, Integer from, String cursor, Integer size) {
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
        LOGGER.info("Getting users for service agreement id {}, full name like {}, from {} and size {}",
            id, query, from, size);
        ListElementsWrapper<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody> data = serviceAgreementService
            .getUsersForServiceAgreement(id, query, from, size, cursor);

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
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<PresentationServiceAgreement>> getServiceAgreements(String creatorId, String userId,
        String query, Integer from, String cursor, Integer size) {
        creatorId = retrieveAndValidateCreatorId(creatorId);

        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        LOGGER.info("Getting all service agreements");
        PaginationDto<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement> response = serviceAgreementService
            .listServiceAgreements(creatorId, userId, query, from, size, cursor);

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, response.getTotalNumberOfRecords().toString());

        return new ResponseEntity<>(payloadConverter
            .convertListPayload(response.getRecords(),
                PresentationServiceAgreement.class),
            headers,
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<UnexposedUsersGetResponseBody>> getUnexposedUsersSA(String id, String query,
        Integer from, String cursor, Integer size) {
        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);

        LOGGER.info(
            "Provides a list of all unexposed users for Service Agreement {} with size {}, from {}, query {}",
            id, size, from, query);
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
        PaginationDto<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UnexposedUsersGetResponseBody> response = serviceAgreementService
            .getUnexposedUsers(id, from, size, query, cursor);

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(response.getTotalNumberOfRecords()));

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            response.getRecords(),
            UnexposedUsersGetResponseBody.class), headers, HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME
        + "', " + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.ADD_USERS, objectType = AuditObjectType.SERVICE_AGREEMENTS_USERS_ADD)
    public ResponseEntity<Void> postUsersAddSA(String id, UsersForServiceAgreement usersForServiceAgreement) {
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
        LOGGER.info("add users with ids {} for service agreement id {}", usersForServiceAgreement.getUsers(),
            id);
        serviceAgreementService.addUsersInServiceAgreement(payloadConverter.convertAndValidate(
            usersForServiceAgreement,
            PresentationUsersForServiceAgreementRequestBody.class), id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + ASSIGN_USERS_RESOURCE_NAME_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    @AuditEvent(eventAction = EventAction.REMOVE_USERS, objectType = AuditObjectType.SERVICE_AGREEMENTS_USERS_REMOVE)
    public ResponseEntity<Void> postUsersRemoveSA(String id, UsersForServiceAgreement usersForServiceAgreement) {
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
        LOGGER.info("Removing users with ids {} for service agreement id {}", usersForServiceAgreement.getUsers(),
            id);
        serviceAgreementService.removeUsersFromServiceAgreement(payloadConverter
            .convert(usersForServiceAgreement, PresentationUsersForServiceAgreementRequestBody.class), id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + FUNCTION_ASSIGN_PERMISSONS + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    public ResponseEntity<PresentationApprovalStatus> putAssignUsersPermissions(String id, String userId,
        ListOfFunctionGroupsWithDataGroups listOfFunctionGroupsWithDataGroups) {
        LOGGER.info("Assigning permissions {} to user id{} in the service agreement id {}",
            listOfFunctionGroupsWithDataGroups, userId, id);
        permissionValidationService
            .validateAccessToServiceAgreementResource(id, AccessResourceType.USER_AND_ACCOUNT);
        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationApprovalStatus result = serviceAgreementService
            .putAssignUsersPermissions(payloadConverter
                    .convertAndValidate(listOfFunctionGroupsWithDataGroups, PresentationFunctionDataGroupItems.class), id,
                userId);
        return new ResponseEntity<>(new PresentationApprovalStatus().approvalStatus(Objects.nonNull(result.getApprovalStatus()) ?
            com.backbase.accesscontrol.client.rest.spec.model.ApprovalStatus
                .valueOf(result.getApprovalStatus().toString()) : null),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("checkPermission('" + SERVICE_AGREEMENT_RESOURCE_NAME + "', "
        + "'" + MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME + "', "
        + "{'" + PRIVILEGE_EDIT + "'})")
    public ResponseEntity<Void> putServiceAgreementSave(String id, ServiceAgreementSave serviceAgreementSave) {
        LOGGER.info("Update Service Agreement by id {}", id);
        permissionValidationService.validateAccessToServiceAgreementResource(id, AccessResourceType.NONE);

        serviceAgreementService.editServiceAgreement(
            payloadConverter.convertAndValidate(serviceAgreementSave,
                com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave.class),
            id);

        HttpStatus status = HttpStatus.OK;

        if (approvalOnRequestScope.isApproval()) {
            status = HttpStatus.ACCEPTED;
        }

        return new ResponseEntity<>(status);
    }
}
