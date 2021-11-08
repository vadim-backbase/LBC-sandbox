package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.business.persistence.transformer.LegalEntityTransformer;
import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.rest.spec.api.LegalEntityApi;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementItemQuery;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.RootLegalEntityGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class LegalEntityQueryController implements LegalEntityApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityQueryController.class);
    private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    private LegalEntityPAndPService legalEntityPAndPService;
    private PersistenceLegalEntityService service;
    private LegalEntityTransformer legalEntityTransformer;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/legalentities/{id} : Legal Entity GET. # Legal Entity GET    Request method GET for fetching
     * legal entity by legal entity internal id.    **Warning: Calling this endpoint will bypass the validation of user
     * permissions.**    **Recommendation: Use the corresponding endpoint on presentation service or use Auth Security
     * library.**
     *
     * @param id Legal entity internal id (required)
     * @return Single legal entity retrieved (status code 200) or BadRequest (status code 400) or NotFound (status code
     *     404)
     */
    @Override
    public ResponseEntity<LegalEntityItem> getLegalEntity(String id) {
        LOGGER.info("Getting Legal Entity with ID {}", id);
        return ResponseEntity.ok(payloadConverter
            .convert(legalEntityPAndPService.getLegalEntityByIdAsResponseBody(id), LegalEntityItem.class));
    }

    /**
     * GET /accesscontrol/legalentities/{id}/serviceagreements/master : Legal Entities MSA GET. # Legal Entities MSA GET
     * Request method GET for fetching master service agreement of the given legal entity.    **Warning: Calling this
     * endpoint will bypass the validation of user permissions.**    **Recommendation: Use the corresponding endpoint on
     * presentation service or use Auth Security library.**
     *
     * @param id Legal entity internal id (required)
     * @return Master service agreement retrieved for given legal Entity. (status code 200) or BadRequest (status code
     *     400) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<ServiceAgreementItemQuery> getMasterServiceAgreement(String id) {
        LOGGER.info("Getting the master service agreement for legal entity");
        return ResponseEntity.ok(payloadConverter
            .convert(legalEntityPAndPService.getMasterServiceAgreement(id), ServiceAgreementItemQuery.class));
    }

    /**
     * GET /accesscontrol/legalentities/root : Legal Entity GET. # Legal Entity GET    Request method GET for fetching
     * Root legal entity.
     *
     * @return Root legal entity retrieved. (status code 200) or BadRequest (status code 400) or NotFound (status code
     *     404)
     */
    @Override
    public ResponseEntity<LegalEntityItemBase> getRootLegalEntity() {
        LOGGER.info("Getting the root legal entity");
        return ResponseEntity.ok(payloadConverter
            .convert(legalEntityTransformer
                    .transformLegalEntity(RootLegalEntityGetResponseBody.class, service.getRootLegalEntity()),
                LegalEntityItemBase.class));

    }

    /**
     * GET /accesscontrol/legalentities/segmentation : Legal Entities Segmentation GET. # Legal Entities Segmentation
     * GET    List customers that user has access to.
     *
     * @param businessFunction Name of the business function (required)
     * @param userId User id (required)
     * @param query Search by name or external ID: partial term for name and whole term for external ID (optional)
     * @param serviceAgreementId Service agreement id (optional)
     * @param legalEntityId Legal Entity Id (optional)
     * @param privilege Name of the privilege (optional)
     * @param from Page Number. Skip over pages of elements by specifying a start value for the query (optional,
     *     default to 0)
     * @param cursor Record UUID. As an alternative for specifying &#39;from&#39; this allows to point to the record
     *     to start the selection from.  (optional, default to &quot;&quot;)
     * @param size Limit the number of elements on the response. When used in combination with cursor, the value is
     *     allowed to be a negative number to indicate requesting records upwards from the starting point indicated by
     *     the cursor.  (optional, default to 10)
     * @return # Legal Entities Segmentation GET    List customers that user has access to.    (status code 200) or #
     *     Reasons for getting HTTP status code 400:    * Service agreement id or legal entity id must be provided    |
     *     Message | key   --- | --- |  |Service Agreement Id or Legal Entity Id must be
     *     provided|serviceAgreement.id.NOT_PROVIDED| (status code 400) or # Reasons for getting HTTP status code 404: *
     *     Service agreement does not exist    | Message | key   --- | --- |  |Service agreement does not
     *     exist|serviceAgreements.get.error.message.E_NOT_EXISTS|   (status code 404)
     */
    @Override
    public ResponseEntity<List<LegalEntityItem>> getSegmentation(String businessFunction, String userId, String query,
        String serviceAgreementId, String legalEntityId, String privilege, Integer from, String cursor, Integer size) {
        LOGGER.info("List Legal entities with data group of type customers with query parameter {}", query);

        SearchAndPaginationParameters parameters = new SearchAndPaginationParameters(
            from,
            size,
            query,
            cursor
        );
        GetLegalEntitySegmentationHolder holder = new GetLegalEntitySegmentationHolder()
            .withBusinessFunction(businessFunction)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withUserId(userId)
            .withPrivilege(privilege)
            .withSearchAndPaginationParameters(parameters);
        Page<SegmentationGetResponseBodyQuery> legalEntitiesPage = service
            .getLegalEntitySegmentation(holder);
        String totalElements = String.valueOf(legalEntitiesPage.getTotalElements());
        return ResponseEntity.ok()
            .header(TOTAL_COUNT_HEADER, totalElements)
            .body(payloadConverter
                .convertListPayload(legalEntitiesPage.get().collect(Collectors.toList()), LegalEntityItem.class));
    }
}
