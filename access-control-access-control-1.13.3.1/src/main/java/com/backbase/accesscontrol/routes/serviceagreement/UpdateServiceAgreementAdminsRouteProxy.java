package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.AdminsPutRequestBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface UpdateServiceAgreementAdminsRouteProxy {

    InternalRequest<Void> updateAdmins(@Body InternalRequest<AdminsPutRequestBody> request,
        @Header("id") String id);
}
