package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_071;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.rest.spec.api.ServiceAgreementQueryApi;
import com.backbase.accesscontrol.service.rest.spec.model.PersistenceServiceAgreementDataGroups;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementAdmins;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItemQuery;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementParticipantsGetResponseBody;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementUsersQuery;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ServiceAgreementQueryController implements ServiceAgreementQueryApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementQueryController.class);

    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/accessgroups/serviceagreements/{serviceAgreementId} : Service Agreements GET. # Service
     * Agreements GET    Request method GET for fetching service agreement by given ID.    **Warning: Calling this
     * endpoint will bypass the validation of user permissions.**    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param serviceAgreementId Service agreement internal id. (required)
     * @return Service agreement retrieved. (status code 200) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<ServiceAgreementItemQuery> getServiceAgreement(String serviceAgreementId) {

        LOGGER.info("Retrieving service agreement by ID {}", serviceAgreementId);

        return ResponseEntity.ok(payloadConverter
            .convert(serviceAgreementServiceFacade.getServiceAgreementResponseBodyById(serviceAgreementId),
                ServiceAgreementItemQuery.class));
    }

    /**
     * GET /accesscontrol/accessgroups/serviceagreements/{serviceAgreementId}/admins : Service Agreement Admins.
     * #Service Agreement Admins    Request method GET for retrieving a list of local admins of the service agreement.
     * **Warning: Calling this endpoint will bypass the validation of user permissions.**    **Recommendation: Use the
     * corresponding endpoint on presentation service or use Auth Security library.**
     *
     * @param serviceAgreementId Service agreement internal id. (required)
     * @return Service Agreement Admins retrieved successfully. (status code 200) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<ServiceAgreementAdmins> getServiceAgreementAdmins(String serviceAgreementId) {

        LOGGER.info("Getting admins of SA {}.", serviceAgreementId);
        return ResponseEntity.ok(payloadConverter
            .convert(serviceAgreementServiceFacade.getServiceAgreementAdmins(serviceAgreementId),
                ServiceAgreementAdmins.class));
    }

    /**
     * GET /accesscontrol/accessgroups/serviceagreements/{serviceAgreementId}/participants : Retrieves Service Agreement
     * participants. # Retrieves Service Agreement participants    Request method GET for retrieving Legal Entities that
     * are participants  in Service Agreement together with their roles in the Service Agreement.    **Warning: Calling
     * this endpoint will bypass the validation of user permissions.**    **Recommendation: Use the corresponding
     * endpoint on presentation service or use Auth Security library.**
     *
     * @param serviceAgreementId Service agreement internal id. (required)
     * @return Service Agreement Participants retrieved successfully. (status code 200) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<List<ServiceAgreementParticipantsGetResponseBody>> getServiceAgreementParticipantsQuery(
        String serviceAgreementId) {

        LOGGER.info(
            "Retrieving participants for service agreement with service agreement id {}", serviceAgreementId);
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(serviceAgreementServiceFacade.getServiceAgreementParticipants(serviceAgreementId)
                , ServiceAgreementParticipantsGetResponseBody.class));
    }

    /**
     * GET /accesscontrol/accessgroups/serviceagreements/{serviceAgreementId}/users : Service Agreement Users. #Service
     * Agreement Users    Request method GET for retrieving a list of users in the service agreement.    **Warning:
     * Calling this endpoint will bypass the validation of user permissions.**    **Recommendation: Use the
     * corresponding endpoint on presentation service or use Auth Security library.**
     *
     * @param serviceAgreementId Service agreement internal id. (required)
     * @return Service agreement users. (status code 200) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<ServiceAgreementUsersQuery> getServiceAgreementUsers(String serviceAgreementId) {

        LOGGER
            .info("Retrieving users for service agreement with service agreement id {}", serviceAgreementId);
        return ResponseEntity.ok(payloadConverter
            .convert(serviceAgreementServiceFacade.getServiceAgreementUsers(serviceAgreementId),
                ServiceAgreementUsersQuery.class));
    }

    /**
     * GET /accesscontrol/accessgroups/serviceagreements/data-items : Service Agreements Data Groups - &#x60;GET&#x60;.
     * # Service Agreements Data Groups - &#x60;GET&#x60;    Request method GET for fetching service agreement ids with
     * data group ids   and data item ids  by user id, data group type, resource name, business function name   and
     * privilege.
     *
     * @param userId User id. (required)
     * @param dataGroupType Data group type. (required)
     * @param resourceName Resource name. (optional)
     * @param functionName Function name. (optional)
     * @param privileges List of comma separated privilege names. (optional)
     * @return Service agreement ids retrieved with data group ids and data item ids. (status code 200) or # Reasons for
     *     getting HTTP status code 400:    * Privileges provided without business function name or resource name    |
     *     Message | key |  --- | --- |  |Privileges cannot be provided without business function name or resource
     *     name|serviceAgreements.get.parameters.error.message.PRIVILEGES_WITHOUT_FUNCTION_OR_RESOURCE|   (status code
     *     400)
     */
    @Override
    public ResponseEntity<List<PersistenceServiceAgreementDataGroups>> getServiceAgreementsDataGroups(String userId,
        String dataGroupType,
        String resourceName, String functionName, String privileges) {

        LOGGER.info("Retrieving list of service agreements with data groups and data items");

        if (isNotEmpty(privileges) && isEmpty(resourceName) && isEmpty(functionName)) {
            LOGGER.warn("Privileges cannot be provided without business function name or resource name");
            throw getBadRequestException(ERR_ACQ_071.getErrorMessage(), ERR_ACQ_071.getErrorCode());
        }

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(persistenceServiceAgreementService
                    .getServiceAgreementsDataGroups(userId, dataGroupType, functionName, resourceName, privileges),
                PersistenceServiceAgreementDataGroups.class));
    }
}
