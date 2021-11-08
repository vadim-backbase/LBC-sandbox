package com.backbase.accesscontrol.business.legalentity;

import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.legalentity.LegalEntityBatchDeleteService;
import com.backbase.accesscontrol.util.InternalRequestUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Delete Batch Legal Entity, communicate with PandP access control service.
 */
@Service
@AllArgsConstructor
public class DeleteBatchLegalEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBatchLegalEntity.class);

    private LegalEntityBatchDeleteService legalEntityBatchDeleteService;
    private BatchResponseItemMapper batchResponseItemMapper;

    /**
     * Method that listens on {@link EndpointConstants#DIRECT_DEFAULT_DELETE_BATCH_LEGAL_ENTITY} endpoint.
     *
     * @param request - internal request of type {@link PresentationBatchDeleteLegalEntities}
     * @return internal request list of type {@link BatchResponseItem}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_BATCH_LEGAL_ENTITY)
    public InternalRequest<List<BatchResponseItem>> deleteBatchLegalEntity(
        @Body InternalRequest<PresentationBatchDeleteLegalEntities> request) {

        LOGGER.info("Trying to delete batch Legal Entities");
        return InternalRequestUtil.getInternalRequest(batchResponseItemMapper
            .toLegalEntityPresentation(legalEntityBatchDeleteService.deleteBatchLegalEntities(request.getData())),
            request.getInternalRequestContext());
    }
}
