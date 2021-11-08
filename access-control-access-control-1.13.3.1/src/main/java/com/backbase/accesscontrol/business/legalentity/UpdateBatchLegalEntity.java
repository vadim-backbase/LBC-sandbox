package com.backbase.accesscontrol.business.legalentity;

import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.legalentity.LegalEntityBatchService;
import com.backbase.accesscontrol.util.InternalRequestUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Updates Batch Legal Entity, communicate with PandP access control service.
 */
@Service
@AllArgsConstructor
public class UpdateBatchLegalEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateBatchLegalEntity.class);

    private LegalEntityBatchService legalEntityBatchService;
    private BatchResponseItemMapper batchResponseItemMapper;

    /**
     * Method that listens on {@link EndpointConstants#DIRECT_DEFAULT_UPDATE_BATCH_LEGAL_ENTITY} endpoint.
     *
     * @param request - internal request containing list of {@link LegalEntityPut}
     * @return void  internal request list of {@link BatchResponseItem}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_BATCH_LEGAL_ENTITY)
    public InternalRequest<List<BatchResponseItem>> updateBatchLegalEntity(
        @Body InternalRequest<List<LegalEntityPut>> request) {

        LOGGER.info("Trying to update batch Legal Entity ");

        return InternalRequestUtil.getInternalRequest(
            batchResponseItemMapper
                .toLegalEntityPresentation(legalEntityBatchService.processBatchItems(request.getData())),
            request.getInternalRequestContext());
    }
}
