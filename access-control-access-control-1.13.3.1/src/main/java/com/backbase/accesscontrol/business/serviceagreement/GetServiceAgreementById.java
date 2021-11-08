package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.mappers.ServiceAgreementGetByIdMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a Service Agreement by Id. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving Service Agreement by Id.
 */
@Service
@AllArgsConstructor
public class GetServiceAgreementById {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetServiceAgreementById.class);

    private ServiceAgreementGetByIdMapper serviceAgreementMapper;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    /**
     * Method that listens on the direct:getServiceAgreementByIdRequestedInternal endpoint and uses persistence service
     *
     * @param request            Internal Request of void type to be send by the client
     * @param serviceAgreementId id of the Service Agreement
     * @return Business Process Result with {@link ServiceAgreementItemGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_ID)
    public InternalRequest<ServiceAgreementItemGetResponseBody> getServiceAgreementById(
        @Body InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to get service agreement by id {}", serviceAgreementId);

        return getInternalRequest(getServiceAgreementById(serviceAgreementId),
            request.getInternalRequestContext());
    }

    private ServiceAgreementItemGetResponseBody getServiceAgreementById(String serviceAgreementId) {

        ServiceAgreementItem serviceAgreement =
            persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId);
        ServiceAgreementItemGetResponseBody serviceAgreementItemGetResponseBody = serviceAgreementMapper
            .mapSingle(serviceAgreement);
        Optional<ApprovalServiceAgreementRef> serviceAgreementIfPending = persistenceServiceAgreementService
            .getServiceAgreementIfPending(serviceAgreement.getId());

        if (serviceAgreementIfPending.isPresent()) {
            ApprovalServiceAgreementRef approvalServiceAgreement = serviceAgreementIfPending.get();
            serviceAgreementItemGetResponseBody.setApprovalId(approvalServiceAgreement.getApprovalId());
            return serviceAgreementItemGetResponseBody;
        }
        return serviceAgreementItemGetResponseBody;
    }
}
