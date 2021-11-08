package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.mappers.LegalEntitiesGetResponseBodyMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * List Legal Entities business consumer, the business process component of the Legal Entities presentation service,
 * communicating with the integration services.
 */
@Service
@AllArgsConstructor
public class ListLegalEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListLegalEntities.class);

    private LegalEntitiesGetResponseBodyMapper mapper;
    private PersistenceLegalEntityService persistenceLegalEntityService;
    private UserContextUtil userContextUtil;

    /**
     * Method that listens on direct:listLegalEntitiesRequestedInternal endpoint. Lists legal entities by parent legal
     * entity id.
     *
     * @param request        - internal request
     * @param parentEntityId parent legal entity filter
     * @return internal request with list of {@link LegalEntitiesGetResponseBody}
     */

    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_LEGAL_ENTITIES)
    public InternalRequest<List<LegalEntitiesGetResponseBody>> getLegalentities(
        @Body InternalRequest<Void> request,
        @Header("parentEntityId") String parentEntityId) {

        String internalLegalEntityId = userContextUtil.getUserContextDetails().getLegalEntityId();
        LOGGER.info("Trying to fetch legal entity by internal legal entity id {}", internalLegalEntityId);
        List<LegalEntity> legalEntities;
        if (isUserRoot(parentEntityId)) {
            LOGGER.info("Trying to fetch a list of Legal Entities with parentEntityId {}", parentEntityId);
            legalEntities = getRootLegalEntity(internalLegalEntityId);
        } else {
            LOGGER.info("Resolving user entity by legal entity parent id {}", parentEntityId);
            legalEntities = persistenceLegalEntityService.getLegalEntities(parentEntityId);
        }
        List<LegalEntitiesGetResponseBody> result = mapper.toPresentation(legalEntities);

        return getInternalRequest(result, request.getInternalRequestContext());
    }

    private LegalEntity getLegalEntity(String internalLegalEntityId) {
        LOGGER.info("Trying to fetch Legal Entity with ID {}", internalLegalEntityId);
        return persistenceLegalEntityService.getLegalEntityById(internalLegalEntityId);
    }

    private List<LegalEntity> getRootLegalEntity(String userLegalEntityId) {
        LegalEntity legalEntityPandpResponse = getLegalEntity(userLegalEntityId);
        return singletonList(legalEntityPandpResponse);
    }

    private boolean isUserRoot(String legalEntityParentId) {
        return legalEntityParentId == null || legalEntityParentId.isEmpty();
    }

}

