package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_113;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_115;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.mappers.UserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.UserContextFlowService;
import com.backbase.accesscontrol.service.facades.UserContextServiceFacade;
import com.backbase.accesscontrol.service.rest.spec.api.UserContextApi;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemIds;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.accesscontrol.service.rest.spec.model.GetContexts;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.util.UserContextUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserContextQueryController implements UserContextApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserContextQueryController.class);

    private UserContextServiceFacade userContextServiceFacade;
    private UserContextFlowService userContextFlowService;
    private UserContextUtil userContextUtil;
    private ServiceAgreementIdProvider serviceAgreementIdProvider;
    private ValidationConfig validationConfig;
    private PayloadConverter payloadConverter;
    private UserContextPermissionsMapper userContextPermissionsMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PermissionsDataGroup> getUserContextPermissions(
        @Valid PermissionsRequest permissionsRequest) {

        String authenticatedUserName = userContextUtil.getAuthenticatedUserName();
        String serviceAgreementId = serviceAgreementIdProvider.getServiceAgreementId()
            .orElseGet(() -> serviceAgreementIdProvider
                .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(authenticatedUserName));
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info("Get user context permission data groups for userId {}, service agreement Id {} and parameters {}.",
            userId, serviceAgreementId, permissionsRequest);

        permissionsRequest.getDataGroupTypes()
            .forEach(dataGroupType -> validationConfig.validateDataGroupType(dataGroupType));

        return new ResponseEntity<>(userContextPermissionsMapper.permissionsDataGroupMap(userContextFlowService
            .getUserContextPermissions(userId, serviceAgreementId,
                userContextPermissionsMapper.permissionsRequestMap(permissionsRequest))), HttpStatus.OK);
    }

    /**
     * POST /service-api/v2/accessgroups/usercontext/data-items/permissions Permissions for data items
     *
     * @param dataItemsPermissions DataItemsPermissions body (optional)
     * @return Data items permissions success response (status code 204) or BadRequest (status code 400) or Forbidden
     *     (status code 403)
     */
    @Override
    public ResponseEntity<Void> getDataItemsPermissions(DataItemsPermissions dataItemsPermissions) {

        dataItemsPermissions.getDataItems().forEach(i -> {
            if (isNull(i)) {
                throw getBadRequestException((ERR_AG_115.getErrorMessage()), ERR_AG_115.getErrorCode());
            }
            validationConfig.validateDataGroupType(i.getItemType());
        });
        Set<String> uniqueTypes = validateUniqueDataItemIdPerDataItemTypeAndGetUniqueTypes(
            dataItemsPermissions.getDataItems());

        String authenticatedUserName = userContextUtil.getAuthenticatedUserName();
        String serviceAgreementFromContext = serviceAgreementIdProvider.getServiceAgreementId()
            .orElseGet(() -> serviceAgreementIdProvider.getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(authenticatedUserName));
        String internalUserId = userContextUtil.getUserContextDetails().getInternalUserId();

        userContextServiceFacade
            .getDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);

        return ResponseEntity.noContent().build();
    }

    private Set<String> validateUniqueDataItemIdPerDataItemTypeAndGetUniqueTypes(List<DataItemIds> dataItems) {
        Set<String> uniqueType = new HashSet<>();
        Set<String> uniqueItems = new HashSet<>();
        dataItems
            .forEach(i -> {
                if (uniqueType.contains(i.getItemType()) || uniqueItems.contains(i.getItemId())) {
                    LOGGER.warn("Item or Type is not unique");
                    throw getBadRequestException((ERR_AG_113.getErrorMessage()), ERR_AG_113.getErrorCode());
                }
                uniqueItems.add(i.getItemId());
                uniqueType.add(i.getItemType());
            });
        return uniqueType;
    }

    /**
     * GET /accesscontrol/accessgroups/usercontext/{userId}/serviceAgreements/{serviceAgreementId}/validation No
     * description available
     *
     * @param userId Internal User Id (required)
     * @param serviceAgreementId Generated parameter by BOAT. Please specify the URI parameter in RAML (required)
     * @return User Context Validation success response (status code 204) or BadRequest (status code 400) or Forbidden
     *     (status code 403)
     */
    @Override
    public ResponseEntity<Void> getUserContextValidation(String userId,
        String serviceAgreementId) {

        LOGGER.info("Validating user context by user id {}, serviceAgreementId {}",
            userId, serviceAgreementId);

        userContextServiceFacade.validateUserContext(userId, serviceAgreementId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /accesscontrol/accessgroups/usercontext/{userId}/serviceAgreements : Context SA GET. # Context SA GET Request
     * method GET for fetching list of service agreements that the user can select for the context. **Warning:**   *
     * Calling this endpoint will bypass the validation of user permissions     of the user performing the action.   *
     * Calling this endpoint will bypass the validation of users     (existence of user and user belonging in legal
     * entity)     upon users on which the action is performed    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param userId Internal User Id (required)
     * @param cursor Record UUID. As an alternative for specifying &#39;from&#39; this allows to point to the record
     *     to start the selection from.  (optional)
     * @param query The search term used to search. (optional)
     * @param from Page Number. Skip over pages of elements by specifying a start value for the query (optional,
     *     default to 0)
     * @param size Limit the number of elements on the response. When used in combination with cursor, the value is
     *     allowed to be a negative number to indicate requesting records upwards from the starting point indicated by
     *     the cursor.  (optional, default to 10)
     * @return User Context Service Agreements GET response (status code 200) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<GetContexts> getUserContexts(String userId, String cursor, String query,
        Integer from, Integer size) {

        LOGGER.info("Getting user context by user id {}, query {}, from {} and size {}", userId, query, from,
            size);
        return ResponseEntity.ok(payloadConverter
            .convert(userContextServiceFacade.getUserContextsByUserId(userId, query, from, size),
                GetContexts.class));
    }
}
