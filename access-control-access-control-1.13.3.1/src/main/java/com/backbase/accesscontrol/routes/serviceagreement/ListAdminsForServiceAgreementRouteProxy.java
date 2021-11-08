package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface ListAdminsForServiceAgreementRouteProxy {

    InternalRequest<List<ServiceAgreementUsersGetResponseBody>> getAdminsForServiceAgreement(
        @Body InternalRequest<Void> internalRequest,
        @Header("serviceAgreementId") String serviceAgreementId);
}
