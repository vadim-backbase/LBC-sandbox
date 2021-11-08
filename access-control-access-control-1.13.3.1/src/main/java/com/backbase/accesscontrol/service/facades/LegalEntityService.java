package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.routes.legalentity.AddLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.CreateLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.DeleteBatchLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByExternalIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityForCurrentUserRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetMasterServiceAgreementByLegalEntityIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.ListLegalentitiesRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.UpdateBatchLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.UpdateLegalEntityByExternalIdRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.List;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service request/reply interface that is transport agnostic. Forwards on to relevant component
 * depending on service type.
 */
@Service
public class LegalEntityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityService.class);

    @Autowired
    private InternalRequestContext internalRequestContext;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_LEGAL_ENTITIES)
    private ListLegalentitiesRouteProxy listLegalentitiesRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_BY_ID)
    private GetLegalEntityByIdRouteProxy getLegalEntityByIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID)
    private GetMasterServiceAgreementByLegalEntityIdRouteProxy getMasterServiceAgreementByLegalEntityIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_ID)
    private GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy getMsaByExternalLegalEntityIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_CREATE_LEGAL_ENTITY)
    private CreateLegalEntityRouteProxy createLegalEntityRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ADD_LEGAL_ENTITY)
    private AddLegalEntityRouteProxy addLegalEntityRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_BY_EXTERNAL_ID)
    private GetLegalEntityByExternalIdRouteProxy getLegalEntityByExternalIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_LEGAL_ENTITY_BY_EXTERNAL_ID)
    private UpdateLegalEntityByExternalIdRouteProxy updateLegalEntityByExternalIdRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_LEGAL_ENTITY_FOR_CURRENT_USER)
    private GetLegalEntityForCurrentUserRouteProxy getLegalEntityForCurrentUser;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_UPDATE_BATCH_LEGAL_ENTITY)
    private UpdateBatchLegalEntityRouteProxy updateBatchLegalEntityRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_DELETE_BATCH_LEGAL_ENTITY)
    private DeleteBatchLegalEntityRouteProxy deleteBatchLegalEntityRouteProxy;


    /**
     * Produces an Exchange to the direct:business.ListLegalentites endpoint. This method dispatches the command for
     * getting Legal Entities.
     *
     * @param parentEntityId Legal Entity's Parent ID
     * @return {@link InternalRequest} with {@link List {@link LegalEntitiesGetResponseBody}} as data.
     */
    public List<LegalEntitiesGetResponseBody> getLegalEntities(String parentEntityId) {
        LOGGER.info("Trying to fetch a list of Legal Entities with parentEntityId {}", parentEntityId);
        return listLegalentitiesRouteProxy
            .getLegalentites(getVoidInternalRequest(internalRequestContext), parentEntityId)
            .getData();
    }

    /**
     * Forwards the request to {@link GetLegalEntityByIdRouteProxy}.
     *
     * @param legalEntityId - internal id of the legal entity
     * @return {@link LegalEntityByIdGetResponseBody} as data
     */
    public LegalEntityByIdGetResponseBody getLegalEntityById(String legalEntityId) {
        LOGGER.info("Trying to fetch the Legal Entity that the logged in user belongs to");
        return getLegalEntityByIdRouteProxy
            .getLegalEntity(getVoidInternalRequest(internalRequestContext), legalEntityId)
            .getData();
    }

    /**
     * Forwards the request to {@link GetMasterServiceAgreementByLegalEntityIdRouteProxy}.
     *
     * @param legalEntityId - id of the legal entity by which we are getting the master service agreement
     * @return {@link MasterServiceAgreementGetResponseBody}
     */
    public MasterServiceAgreementGetResponseBody getMasterServiceAgreement(String legalEntityId) {
        LOGGER.info("Trying to fetch Master Service Agreement for legalEntityId {}", legalEntityId);
        return getMasterServiceAgreementByLegalEntityIdRouteProxy
            .getMasterServiceAgreement(getVoidInternalRequest(internalRequestContext), legalEntityId).getData();
    }

    /**
     * Forwards the request to {@link CreateLegalEntityRouteProxy}.
     *
     * @param legalEntitiesPostRequestBody of type {@link LegalEntitiesPostRequestBody}
     * @return {@link LegalEntitiesPostResponseBody} as data
     */
    public LegalEntitiesPostResponseBody createLegalEntity(LegalEntitiesPostRequestBody legalEntitiesPostRequestBody) {
        LOGGER.info("Service for creating legal entity invoked {}", legalEntitiesPostRequestBody);
        InternalRequest<LegalEntitiesPostRequestBody> internalRequest = getInternalRequest(
            legalEntitiesPostRequestBody, internalRequestContext);
        return createLegalEntityRouteProxy.createLegalEntity(internalRequest).getData();
    }

    /**
     * Forwards the request to {@link AddLegalEntityRouteProxy}.
     *
     * @param createLegalEntitiesPostRequestBody of type {@link CreateLegalEntitiesPostRequestBody}
     * @return {@link CreateLegalEntitiesPostResponseBody}
     */
    public CreateLegalEntitiesPostResponseBody addLegalEntity(
        CreateLegalEntitiesPostRequestBody createLegalEntitiesPostRequestBody) {
        LOGGER.info("Service for creating legal entity invoked {}", createLegalEntitiesPostRequestBody);
        InternalRequest<CreateLegalEntitiesPostRequestBody> internalRequest = getInternalRequest(
            createLegalEntitiesPostRequestBody, internalRequestContext);

        return addLegalEntityRouteProxy.createLegalEntity(internalRequest).getData();
    }

    /**
     * Forwards the request to {@link UpdateLegalEntityByExternalIdRouteProxy}.
     *
     * @param legalEntityByExternalIdPutRequestBody of type {@link LegalEntityByExternalIdPutRequestBody}
     * @param externalId                            external legal entity
     */
    public void updateLegalEntityByExternalId(
        LegalEntityByExternalIdPutRequestBody legalEntityByExternalIdPutRequestBody, String externalId) {
        LOGGER.info("Service for updating legal entity invoked {}", legalEntityByExternalIdPutRequestBody);
        InternalRequest<LegalEntityByExternalIdPutRequestBody> internalRequest = getInternalRequest(
            legalEntityByExternalIdPutRequestBody, internalRequestContext);
        updateLegalEntityByExternalIdRouteProxy.updateLegalEntityByExternalId(internalRequest, externalId);
    }

    /**
     * Forwards the request to {@link GetLegalEntityByExternalIdRouteProxy}.
     *
     * @param externalId external legal entity id.
     * @return {@link LegalEntityByExternalIdGetResponseBody}
     */
    public LegalEntityByExternalIdGetResponseBody getLegalEntityByExternalId(String externalId) {
        LOGGER.info("Trying to fetch the Legal Entity with externalId {}", externalId);
        return getLegalEntityByExternalIdRouteProxy
            .getLegalEntity(getVoidInternalRequest(internalRequestContext), externalId).getData();
    }


    /**
     * Forwards the request to {@link GetLegalEntityForCurrentUserRouteProxy}.
     *
     * @return {@link LegalEntityForUserGetResponseBody} as data
     */
    public LegalEntityForUserGetResponseBody getLegalEntityForCurrentUser() {
        LOGGER.info("Trying to fetch the Legal Entity for current user");

        return getLegalEntityForCurrentUser.getLegalEntityForCurrentUser(getVoidInternalRequest(internalRequestContext))
            .getData();
    }

    /**
     * Forwards the request to {@link UpdateBatchLegalEntityRouteProxy}.
     *
     * @param legalEntityPutList - legal entities to be updated
     * @return list of {@link BatchResponseItem}
     */
    public List<BatchResponseItem> updateBatchLegalEntities(List<LegalEntityPut> legalEntityPutList) {
        LOGGER.info("Trying to update batch legalentities");

        InternalRequest<List<LegalEntityPut>> internalRequest = getInternalRequest(
            legalEntityPutList, internalRequestContext);
        return updateBatchLegalEntityRouteProxy.updateBatchLegalEntity(internalRequest).getData();
    }


    /**
     * Forwards the request to {@link GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy}.
     *
     * @return {@link MasterServiceAgreementGetResponseBody}
     */
    public MasterServiceAgreementGetResponseBody getMasterServiceAgreementByExternalId(String externalId) {
        LOGGER.info("Trying to fetch Master Service Agreement for legalEntityId {}", externalId);
        return getMsaByExternalLegalEntityIdRouteProxy
            .getMasterServiceAgreementByExternalLegalEntityId(getVoidInternalRequest(internalRequestContext),
                externalId)
            .getData();
    }

    /**
     * Forwards the request to {@link DeleteBatchLegalEntityRouteProxy}.
     *
     * @param presentationBatchDeleteLegalEntities - legal entities to be deleted
     * @return list of {@link BatchResponseItem}
     */
    public List<BatchResponseItem> batchDeleteLegalEntities(
        PresentationBatchDeleteLegalEntities presentationBatchDeleteLegalEntities) {
        LOGGER.info("Trying to delete batch legal entities {} ", presentationBatchDeleteLegalEntities);
        InternalRequest<PresentationBatchDeleteLegalEntities> internalRequest = getInternalRequest(
            presentationBatchDeleteLegalEntities, internalRequestContext);
        return deleteBatchLegalEntityRouteProxy.deleteBatchLegalEntity(internalRequest).getData();
    }
}
