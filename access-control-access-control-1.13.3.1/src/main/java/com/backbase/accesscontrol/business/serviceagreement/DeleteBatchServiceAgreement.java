package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.DeleteBatchServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Delete Batch Service Agreement, communicate with PandP access control service.
 */
@Service
@AllArgsConstructor
public class DeleteBatchServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBatchServiceAgreement.class);

    private DeleteBatchServiceAgreementService deleteBatchServiceAgreementService;
    private BatchResponseItemMapper batchResponseItemMapper;

    /**
     * Method that listens on the direct:deleteBatchServiceAgreement endpoint
     *
     * @param request internal request with {@link PresentationDeleteServiceAgreements}
     * @return business process result of {@link BatchResponseItem} list
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_DELETE_BATCH_SERVICE_AGREEMENT)
    public InternalRequest<List<BatchResponseItem>> deleteBatchServiceAgreement(
        @Body InternalRequest<PresentationDeleteServiceAgreements> request) {

        LOGGER.info("Trying to delete batch Service Agreement.");

        List<BatchResponseItem> batchResponseItems = batchResponseItemMapper
            .toPresentation(deleteBatchServiceAgreementService
                .deleteBatchServiceAgreement(request.getData()));
        return getInternalRequest(batchResponseItems,
            request.getInternalRequestContext());
    }
}
