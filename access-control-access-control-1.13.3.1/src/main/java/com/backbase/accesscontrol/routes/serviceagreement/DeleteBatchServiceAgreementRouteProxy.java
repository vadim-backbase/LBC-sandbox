package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import java.util.List;
import org.apache.camel.Body;

/**
 * Delete batch service agreement method specification that will be used to automatically inject a Camel Producer.
 */
public interface DeleteBatchServiceAgreementRouteProxy {

    InternalRequest<List<BatchResponseItem>> deleteBatchServiceAgreement(
        @Body InternalRequest<PresentationDeleteServiceAgreements> request);
}
