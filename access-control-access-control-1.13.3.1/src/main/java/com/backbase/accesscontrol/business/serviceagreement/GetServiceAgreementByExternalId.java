package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_061;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.mappers.ServiceAgreementGetByExternalIdMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a Service Agreement by external Id. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving Service Agreement by external
 * Id.
 */
@Service
@AllArgsConstructor
public class GetServiceAgreementByExternalId {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetServiceAgreementByExternalId.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementGetByExternalIdMapper serviceAgreementMapper;

    /**
     * Method that listens on the direct:getServiceAgreementByExternalIdRequestedInternal endpoint and forward the
     * request to the P&P service.
     *
     * @param request    Internal Request of void type to be send by the client
     * @param externalId external id of the Service Agreement
     * @return InternalRequest of {@link ServiceAgreementExternalIdGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_BY_EXTERNAL_ID)
    public InternalRequest<ServiceAgreementExternalIdGetResponseBody> getServiceAgreementByExternalId(
        @Body InternalRequest<Void> request,
        @Header("externalId") String externalId) {
        LOGGER.info("Trying to get service agreement by external id {}", externalId);

        ServiceAgreement serviceAgreement = Optional.ofNullable(externalId)
            .map(extId -> persistenceServiceAgreementService.getServiceAgreementResponseBodyByExternalId(extId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .orElseThrow(() -> {
                LOGGER.warn("Service agreement with external id {} does not exist", externalId);
                return getNotFoundException(ERR_AG_061.getErrorMessage(), ERR_AG_061.getErrorCode());
            });

        return getInternalRequest(serviceAgreementMapper.mapSingle(serviceAgreement),
                request.getInternalRequestContext());
    }
}
