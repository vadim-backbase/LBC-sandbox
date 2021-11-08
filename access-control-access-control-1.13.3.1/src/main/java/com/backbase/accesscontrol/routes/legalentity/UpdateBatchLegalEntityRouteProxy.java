package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import java.util.List;
import org.apache.camel.Body;

/**
 * Update batch legal entity method specification that will be used to automatically inject a Camel Producer.
 */
public interface UpdateBatchLegalEntityRouteProxy {

    InternalRequest<List<BatchResponseItem>> updateBatchLegalEntity(
        @Body InternalRequest<List<LegalEntityPut>> request);
}
