package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_065;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.mappers.ArrangementPrivilegesMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.accesscontrol.service.impl.UserAccessFunctionGroupService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.service.rest.spec.api.UserQueryApi;
import com.backbase.accesscontrol.service.rest.spec.model.ArrangementPrivilegesGetResponseBody;
import com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities;
import com.backbase.accesscontrol.service.rest.spec.model.PersistenceApprovalPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.PersistenceUserDataItemPermission;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessEntitlementsResource;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessLegalEntities;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessServiceAgreement;
import com.backbase.accesscontrol.service.rest.spec.model.UserFunctionGroups;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.LegalEntityResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.UserFunctionGroupsGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserQueryController implements UserQueryApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserQueryController.class);

    private UserAccessPrivilegeService userAccessPrivilegeService;
    private UserAccessPermissionCheckService userAccessPermissionCheckService;
    private ApprovalService approvalService;
    private UserAccessFunctionGroupService userAccessFunctionGroupService;

    private ArrangementPrivilegesMapper arrangementPrivilegesMapper;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/accessgroups/users/privileges/arrangements : User Access GET. # User Access GET    Request
     * method GET for fetching all arrangements with privileges  by functionName and resourceName as required,  and
     * userId, serviceAgreementId, legalEntityId, arrangementId and privilege as optional.    **Warning:**   * Calling
     * this endpoint will bypass the validation of user permissions     of the user performing the action.   * Calling
     * this endpoint will bypass the validation of users     (existence of user and user belonging in legal entity) upon
     * users on which the action is performed    **Recommendation: Use the corresponding endpoint on presentation
     * service or use Auth Security library.**
     *
     * @param functionName Name of Function (required)
     * @param resourceName Name of resource (required)
     * @param userId User ID (optional)
     * @param serviceAgreementId Service Agreement ID (optional)
     * @param legalEntityId Legal Entity ID (optional)
     * @param arrangementId Arrangement ID (optional)
     * @param privilege Privilege (optional)
     * @return List of arrangements with privileges retrieved. (status code 200) or BadRequest (status code 400) or
     *     Forbidden (status code 403)
     */
    @Override
    public ResponseEntity<List<ArrangementPrivilegesGetResponseBody>> getArrangementPrivilegesQuery(String functionName,
        String resourceName,
        String userId, String serviceAgreementId, String legalEntityId, String arrangementId, String privilege) {

        LOGGER.info("Listing arrangement privileges for user with id {}, service agreement id {}"
                + "function name {}, resource name {}, legal entity id {}, arrangement id {}, privilege {}",
            userId, serviceAgreementId, functionName, resourceName, legalEntityId, arrangementId, privilege);

        List<ArrangementPrivilegesDto> result = userAccessPrivilegeService.getArrangementPrivileges(userId,
            serviceAgreementId, functionName,
            resourceName, privilege, legalEntityId,
            arrangementId);
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(arrangementPrivilegesMapper
                .toListArrangementPrivilegesGetResponseBody(result), ArrangementPrivilegesGetResponseBody.class));
    }

    /**
     * GET /accesscontrol/accessgroups/users/{userId}/service-agreements/{serviceAgreementId}/data-item-permissions :
     * User Permissions per Data Item GET. # User Permissions per Data Item GET    Request method GET for fetching user
     * permissions per data item for given  businessFunction or resource or privilege or dataGroupType or dataItemId.
     * **Warning: Calling this endpoint will bypass the validation of user permissions.**    **Recommendation: Use the
     * corresponding endpoint on presentation service or use Auth Security library.**
     *
     * @param userId User id (required)
     * @param serviceAgreementId Service agreement id (required)
     * @param businessFunction Name of the business function (optional)
     * @param resource Name of resource (optional)
     * @param privilege Name of the privilege (optional)
     * @param dataGroupType Name of the data group type (optional)
     * @param dataItemId Id of the data item (optional)
     * @return List of privileges retrieved. (status code 200) or BadRequest (status code 400) or NotFound (status code
     *     404)
     */
    @Override
    public ResponseEntity<List<PersistenceUserDataItemPermission>> getDataItemPermissions(
        String userId, String serviceAgreementId, String businessFunction, String resource, String privilege,
        String dataGroupType, String dataItemId) {
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(userAccessPrivilegeService
                .getUserDataItemsPrivileges(userId, serviceAgreementId, resource, businessFunction, privilege,
                    dataGroupType, dataItemId), PersistenceUserDataItemPermission.class));
    }

    /**
     * GET /accesscontrol/accessgroups/users/{userId}/service-agreements/{serviceAgreementId}/permissions : User
     * Permissions GET. # User Permissions GET    Request method GET for fetching user permissions for given user and
     * service agreement.  Response contains approvalId if there is pending approval for permissions.    **Warning:** *
     * Calling this endpoint will bypass the validation of user permissions     of the user performing the action.   *
     * Calling this endpoint will bypass the validation of users     (existence of user and user belonging in legal
     * entity)     upon users on which the action is performed    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param userId Generated parameter by BOAT. Please specify the URI parameter in RAML (required)
     * @param serviceAgreementId Generated parameter by BOAT. Please specify the URI parameter in RAML (required)
     * @return Users permissions successfully retrieved. (status code 200) or BadRequest (status code 400) or Forbidden
     *     (status code 403) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<PersistenceApprovalPermissions> getPersistenceApprovalPermissions(String userId,
        String serviceAgreementId) {
        LOGGER
            .info("Trying to get permissions for user {} under service agreement {}.", userId, serviceAgreementId);

        return ResponseEntity.ok(payloadConverter
            .convert(approvalService.getPersistenceApprovalPermissions(userId, serviceAgreementId),
                PersistenceApprovalPermissions.class));
    }

    /**
     * GET /accesscontrol/accessgroups/users/permissions : User Access GET. # User Access GET    Request method GET to
     * check if the user has the specified privileges to perform a function on a given resource.    **Warning:**   *
     * Calling this endpoint will bypass the validation of user permissions     of the user performing the action.   *
     * Calling this endpoint will bypass the validation of users     (existence of user and user belonging in legal
     * entity)     upon users on which the action is performed    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param userId User ID (required)
     * @param resource Resource name. (required)
     * @param function function name. (required)
     * @param privileges Comma-separated privileges. (required)
     * @param serviceAgreementId Service Agreement ID (optional)
     * @return User is permitted to perform the function. (status code 200) or BadRequest (status code 400) or Forbidden
     *     (status code 403)
     */
    @Override
    public ResponseEntity<Void> getUserPermissionCheckQuery(String userId, String resource, String function,
        String privileges,
        String serviceAgreementId) {
        LOGGER.info("Checking permissions for user with id {}, service agreement id {}, resource {}, function {}"
            + "privileges {}", userId, serviceAgreementId, resource, function, privileges);
        userAccessPermissionCheckService
            .checkUserPermission(userId, serviceAgreementId, function, resource, privileges);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /accesscontrol/accessgroups/users/function-groups/by-permissions : Users Function Groups - &#x60;GET&#x60;. #
     * Users Function Groups - &#x60;GET&#x60;    Request method GET for fetching user internal ids together with
     * assigned function group internal ids.
     *
     * @param serviceAgreementId Service Agreement ID (required)
     * @param functionName Function name. (required)
     * @param privilege Name of the privilege (optional)
     * @param dataGroupType Name of the data group type (optional)
     * @param dataItemId Id of the data item (optional)
     * @return List of users/function groups retrieved successfully. (status code 200) or # Reasons for getting HTTP
     *     status code 400:    * Data item type and id must be both provided or omitted.    | Message | key |  --- | ---
     *     |  |Data item type and id must be both provided or omitted|datagroup.parameters.invalid.TYPE_AND_ITEM_ID|
     *     (status code 400)
     */
    @Override
    public ResponseEntity<List<UserFunctionGroups>> getUsersFunctionGroups(String serviceAgreementId,
        String functionName, String privilege, String dataGroupType, String dataItemId) {
        LOGGER.info("Getting user internal ids together with assigned function group internal ids filtered by "
                + "service agreement id {}, function name {}, privilege {} data group type {} data item id {}",
            serviceAgreementId, functionName, privilege, dataGroupType, dataItemId);

        if (isEmpty(dataGroupType) ^ isEmpty(dataItemId)) {
            LOGGER.warn("Only one of data group type or data item id was provided.");
            throw getBadRequestException(ERR_ACQ_065.getErrorMessage(), ERR_ACQ_065.getErrorCode());
        }

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(userAccessFunctionGroupService
                .getUsersFunctionGroups(serviceAgreementId, functionName, privilege, dataGroupType, dataItemId)
                .entrySet()
                .stream().map(entry -> new UserFunctionGroupsGetResponseBody().withUserId(entry.getKey())
                    .withFunctionGroupIds(entry.getValue())).collect(Collectors.toList()), UserFunctionGroups.class));
    }

    /**
     * POST /accesscontrol/accessgroups/users/access/legalentities : User Access Legal Entities. # User Access Legal
     * Entities    Request method POST for checking to which legal entities the user has access to.    **Warning:**   *
     * Calling this endpoint will bypass the validation of user permissions     of the user performing the action.   *
     * Calling this endpoint will bypass the validation of users     (existence of user and user belonging in legal
     * entity)     upon users on which the action is performed    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param userAccessLegalEntities # User Access Legal Entities    Request method POST for checking to which
     *     legal entities the user has access to.    **Warning:**   * Calling this endpoint will bypass the validation
     *     of user permissions     of the user performing the action.   * Calling this endpoint will bypass the
     *     validation of users     (existence of user and user belonging in legal entity)     upon users on which the
     *     action is performed    **Recommendation: Use the corresponding endpoint on presentation service or use Auth
     *     Security library.**   (optional)
     * @return Legal Entities in context (status code 200) or BadRequest (status code 400) or Forbidden (status code
     *     403) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<ContextLegalEntities> postLegalEntitiesInContext(
        UserAccessLegalEntities userAccessLegalEntities) {

        LOGGER.info("Trying to get legal entities that user has access to in context service agreement: {}",
            userAccessLegalEntities.getContextServiceAgreementId());

        return ResponseEntity.ok(payloadConverter
            .convert(
                userAccessPermissionCheckService.getLegalEntitiesThatUserHasAccessTo(
                    payloadConverter.convertAndValidate(userAccessLegalEntities, LegalEntityResource.class)),
                ContextLegalEntities.class));
    }

    /**
     * POST /accesscontrol/accessgroups/users/access/resources : User Access Hierarchy. # User Access Hierarchy Request
     * method POST for checking if a provided user has access to the provided list of resource ids.  Returns list of
     * resource ids that the provided user has access to, a sub list of the provided ids.    **Warning:**   * Calling
     * this endpoint will bypass the validation of user permissions     of the user performing the action.   * Calling
     * this endpoint will bypass the validation of users     (existence of user and user belonging in legal entity) upon
     * users on which the action is performed    **Recommendation: Use the corresponding endpoint on presentation
     * service or use Auth Security library.**
     *
     * @param userAccessEntitlementsResource # User Access Hierarchy    Request method POST for checking if a
     *     provided user has access to the provided list of resource ids.  Returns list of resource ids that the
     *     provided user has access to, a sub list of the provided ids.    **Warning:**   * Calling this endpoint will
     *     bypass the validation of user permissions     of the user performing the action.   * Calling this endpoint
     *     will bypass the validation of users     (existence of user and user belonging in legal entity)     upon users
     *     on which the action is performed    **Recommendation: Use the corresponding endpoint on presentation service
     *     or use Auth Security library.**   (optional)
     * @return Legal Entities that the user has access to (status code 200) or BadRequest (status code 400) or Forbidden
     *     (status code 403) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<ContextLegalEntities> postUserAccessToEntitlementsResource(
        UserAccessEntitlementsResource userAccessEntitlementsResource) {
        LOGGER.info("Trying to check if user has access to resource: {}",
            userAccessEntitlementsResource.getLegalEntityIds());

        return ResponseEntity.ok(payloadConverter
            .convert(
                userAccessPermissionCheckService.checkUserAccessToEntitlementsResources(
                    payloadConverter.convertAndValidate(userAccessEntitlementsResource, EntitlementsResource.class)),
                ContextLegalEntities.class));

    }

    /**
     * POST /accesscontrol/accessgroups/users/access/serviceagreement : User Access Service agreement. # User Access
     * Service agreement    Request method POST for checking if a given user has access to the given service agreement.
     * **Warning:**   * Calling this endpoint will bypass the validation of user permissions     of the user performing
     * the action.   * Calling this endpoint will bypass the validation of users     (existence of user and user
     * belonging in legal entity)     upon users on which the action is performed    **Recommendation: Use the
     * corresponding endpoint on presentation service or use Auth Security library.**
     *
     * @param userAccessServiceAgreement # User Access Service agreement    Request method POST for checking if a
     *     given user has access to the given service agreement.    **Warning:**   * Calling this endpoint will bypass
     *     the validation of user permissions     of the user performing the action.   * Calling this endpoint will
     *     bypass the validation of users     (existence of user and user belonging in legal entity)     upon users on
     *     which the action is performed    **Recommendation: Use the corresponding endpoint on presentation service or
     *     use Auth Security library.**   (optional)
     * @return User Access to Service Agreement check (status code 204) or BadRequest (status code 400) or Forbidden
     *     (status code 403) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<Void> postUserAccessToServiceAgreement(
        UserAccessServiceAgreement userAccessServiceAgreement) {
        LOGGER.info("Trying to check if user has access to service agreement: {}",
            userAccessServiceAgreement.getServiceAgreementId());

        userAccessPermissionCheckService
            .checkUserAccessToServiceAgreement(payloadConverter.convertAndValidate(userAccessServiceAgreement,
                ServiceAgreementResource.class));
        return ResponseEntity.noContent().build();
    }
}
