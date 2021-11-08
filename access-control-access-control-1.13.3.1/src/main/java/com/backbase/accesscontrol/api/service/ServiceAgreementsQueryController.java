package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_045;

import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.api.ServiceAgreementsQueryApi;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody;
import com.backbase.accesscontrol.service.rest.spec.model.ListServiceAgreements;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementByPermissionSet;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ServiceAgreementsQueryController implements ServiceAgreementsQueryApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsQueryController.class);
    private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private FunctionGroupService functionGroupService;
    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/service-agreements : Service Agreements GET. # Service Agreements GET    Request method GET
     * for fetching service agreements by creator id, and optionally user id and usersLegalEntityId.    **Warning:**   *
     * Calling this endpoint will bypass the validation of user permissions     of the user performing the action.   *
     * Calling this endpoint will bypass the validation of users     (existence of user and user belonging in legal
     * entity)     upon users on which the action is performed    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param creatorId Legal entity ID that created the service agreement. (required)
     * @param userId Id of the user that is exposed (as regular or local admin) in the service agreement.
     *     (optional)
     * @param userLegalEntityId Legal entity Id of the user that is exposed (as regular or local admin) in the
     *     service agreement. (optional)
     * @param query The search term used to search. (optional)
     * @param from Page Number. Skip over pages of elements by specifying a start value for the query (optional,
     *     default to 0)
     * @param cursor Record UUID. As an alternative for specifying &#39;from&#39; this allows to point to the record
     *     to start the selection from.  (optional, default to &quot;&quot;)
     * @param size Limit the number of elements on the response. When used in combination with cursor, the value is
     *     allowed to be a negative number to indicate requesting records upwards from the starting point indicated by
     *     the cursor.  (optional, default to 10)
     * @return Service agreements retrieved. (status code 200) or BadRequest (status code 400) or Forbidden (status code
     *     403)
     */
    @Override
    public ResponseEntity<ListServiceAgreements> getAgreements(String creatorId, String userId,
        String userLegalEntityId, String query, Integer from, String cursor, Integer size) {
        LOGGER.info("Listing all service agreements with creator ID {}, "
            + "with parameters: from {}, size {}, query {}, cursor {}", creatorId, from, size, query, cursor);

        return ResponseEntity
            .ok(payloadConverter.convert(serviceAgreementServiceFacade.listServiceAgreements(creatorId,
                new UserParameters(userId, userLegalEntityId),
                new SearchAndPaginationParameters(from, size, query, cursor)),
                ListServiceAgreements.class));
    }

    /**
     * GET /accesscontrol/service-agreements/external/{externalId}/business-functions No description available
     *
     * @param externalId Generated parameter by BOAT. Please specify the URI parameter in RAML (required)
     * @return Service agreement business functions by external id retrieved. (status code 200) or SA with external ID
     *     not found (status code 404) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<List<FunctionsGetResponseBody>> getExternalexternalIdbusinessfunctions(String externalId) {

        LOGGER.info("Listing all business functions for service agreement with external id {}", externalId);
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(
                functionGroupService.findAllBusinessFunctionsByServiceAgreement(externalId, true),
                FunctionsGetResponseBody.class));
    }

    /**
     * GET /accesscontrol/service-agreements/permission-sets/id/{id} : Service Agreements - &#x60;GET&#x60;. # Service
     * Agreements - &#x60;GET&#x60;    Request method GET for fetching service agreement by assignable permission set.
     *
     * @param id Permission sets id (required)
     * @param from Page Number. Skip over pages of elements by specifying a start value for the query (optional,
     *     default to 0)
     * @param cursor Record UUID. As an alternative for specifying &#39;from&#39; this allows to point to the record
     *     to start the selection from.  (optional, default to &quot;&quot;)
     * @param size Limit the number of elements on the response. When used in combination with cursor, the value is
     *     allowed to be a negative number to indicate requesting records upwards from the starting point indicated by
     *     the cursor.  (optional, default to 10)
     * @return List of all service agreement where provided permission set is assigned. (status code 200) or # Reasons
     *     for getting HTTP status code 400:    * Invalid id identifier of assignable permission set  * Invalid page
     *     size, size must be lower than 1000    | Message | key  --- | --- |  | Invalid id identifier of assignable
     *     permission set. | permissionSet.identifier.INVALID_VALUE  | Invalid page size, size must be lower than 1000.
     *     | list.page.error.message.E_INVALID_PAGE_SIZE   (status code 400) or # Reasons for getting HTTP status code
     *     404:    * The APS doesn&#39;t exist.    | Message | key  --- | --- |  | The APS doesn&#39;t exist. |
     *     permissionSet.identifiers.NOT_EXISTS   (status code 404)
     */
    @Override
    public ResponseEntity<List<ServiceAgreementByPermissionSet>> getGetServiceAgremeentByPermissionSetId(String id,
        Integer from, String cursor, Integer size) {
        LOGGER.info("Listing all service agreements associate with permission set by id {} "
            + "with parameters: from {}, size {}, cursor {}", id, from, size, cursor);

        validateFromAndSizeParameters(from, size);

        Page<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.
            ServiceAgreementByPermissionSet> resultList = persistenceServiceAgreementService.getByPermissionSetById(id,
            new SearchAndPaginationParameters(from, size, null, null));
        String totalElements = String.valueOf(resultList.getTotalElements());

        return ResponseEntity.ok().header(TOTAL_COUNT_HEADER, totalElements).body(
            payloadConverter
                .convertListPayload(resultList.getContent(), ServiceAgreementByPermissionSet.class));
    }

    /**
     * GET /accesscontrol/service-agreements/permission-sets/name/{name} : Service Agreements - &#x60;GET&#x60;. #
     * Service Agreements - &#x60;GET&#x60;    Request method GET for fetching service agreement by assignable
     * permission set.
     *
     * @param name No description available (required)
     * @param from Page Number. Skip over pages of elements by specifying a start value for the query (optional,
     *     default to 0)
     * @param cursor Record UUID. As an alternative for specifying &#39;from&#39; this allows to point to the record
     *     to start the selection from.  (optional, default to &quot;&quot;)
     * @param size Limit the number of elements on the response. When used in combination with cursor, the value is
     *     allowed to be a negative number to indicate requesting records upwards from the starting point indicated by
     *     the cursor.  (optional, default to 10)
     * @return List of all service agreement where provided permission set is assigned. (status code 200) or # Reasons
     *     for getting HTTP status code 400:    * Invalid page size, size must be lower than 1000    | Message | key ---
     *     | --- |  | Invalid page size, size must be lower than 1000. | list.page.error.message.E_INVALID_PAGE_SIZE
     *     (status code 400) or # Reasons for getting HTTP status code 404:    * The APS doesn&#39;t exist.    | Message
     *     | key  --- | --- |  | The APS doesn&#39;t exist. | permissionSet.identifiers.NOT_EXISTS   (status code 404)
     */
    @Override
    public ResponseEntity<List<ServiceAgreementByPermissionSet>> getGetServiceAgremeentByPermissionSetName(String name,
        Integer from, String cursor, Integer size) {

        LOGGER.info("Listing all service agreements associate with permission set by name {} "
            + "with parameters: from {}, size {}, cursor {}", name, from, size, cursor);

        validateFromAndSizeParameters(from, size);

        Page<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.
            ServiceAgreementByPermissionSet> resultList = persistenceServiceAgreementService
            .getByPermissionSetByName(name,
                new SearchAndPaginationParameters(from, size, null, null));

        String totalElements = String.valueOf(resultList.getTotalElements());
        return ResponseEntity.ok().header(TOTAL_COUNT_HEADER, totalElements).body(
            payloadConverter
                .convertListPayload(resultList.getContent(), ServiceAgreementByPermissionSet.class));
    }

    /**
     * GET /accesscontrol/service-agreements/id/{internalId}/business-functions No description available
     *
     * @param internalId Generated parameter by BOAT. Please specify the URI parameter in RAML (required)
     * @return Service agreement business functions by internal id retrieved. (status code 200) or SA with internal ID
     *     not found (status code 404) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<List<FunctionsGetResponseBody>> getIdinternalIdbusinessfunctions(String internalId) {
        LOGGER.info("Listing all business functions for service agreement with id {}", internalId);
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(
                functionGroupService.findAllBusinessFunctionsByServiceAgreement(internalId, false),
                FunctionsGetResponseBody.class));
    }

    private void validateFromAndSizeParameters(Integer from, Integer size) {
        LOGGER.info("Validating from and size query parameters");
        if (from != null && size != null && size >= 1000) {
            LOGGER.warn("Size param with value {} needs to be lower than 1000", size);
            throw getBadRequestException(ERR_ACQ_045.getErrorMessage(), ERR_ACQ_045.getErrorCode());

        }
    }
}
