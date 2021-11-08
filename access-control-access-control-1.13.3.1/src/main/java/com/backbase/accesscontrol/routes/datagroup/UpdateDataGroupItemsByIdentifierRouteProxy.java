package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import java.util.List;
import java.util.Map;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface UpdateDataGroupItemsByIdentifierRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> updateDataGroupItemsByIdentifier(
        @Body InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest,
        @Header("responseContainer") List<BatchResponseItemExtended> responseContainer,
        @Header("validResponses") Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses,
        @Header("internalDataItemsIdsByTypeAndExternalId") Map<String, Map<String, String>>
            internalDataItemsIdsByTypeAndExternalId);

}
