package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;

import com.backbase.accesscontrol.business.persistence.serviceagreement.UpdateServiceAgreementHandler;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.util.Date;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer for updating Service agreement's name and description. This class is a business process component
 * of the access-group presentation service, communicating with the P&P services.
 */
@Service
@AllArgsConstructor
public class UpdateServiceAgreement {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateServiceAgreement.class);

    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    private DateTimeService dateTimeService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private UpdateServiceAgreementHandler updateServiceAgreementHandler;

    /**
     * Method that listens on the direct:updateServiceAgreementRequestedInternal endpoint.
     *
     * @param request            Internal Request of {@link ServiceAgreementPutRequestBody} type to be send by the
     *                           client
     * @param serviceAgreementId id of the Service Agreement to be updated
     * @return Business Process Result
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_SERVICE_AGREEMENT)
    public InternalRequest<Void> updateServiceAgreement(InternalRequest<ServiceAgreementPutRequestBody> request,
        @Header("serviceAgreementId") String serviceAgreementId) {
        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = request.getData();
        LOGGER.info("Trying to update service agreement {}", serviceAgreementId);

        updateServiceAgreement(serviceAgreementId, serviceAgreementPutRequestBody);
        return getVoidInternalRequest(request.getInternalRequestContext());

    }

    /**
     * Check the business and validation rules and then makes a request to the P&P service to update Service Agreement's
     * name and description.
     *
     * @param serviceAgreementId id of the Service Agreement to be updated
     * @param putData            {@link ServiceAgreementPutRequestBody} type to be send by the client
     */
    public void updateServiceAgreement(String serviceAgreementId,
        ServiceAgreementPutRequestBody putData) {
        LOGGER.info("Trying to update name, description, external Id and status for service agreement {}",
            serviceAgreementId);

        Date fromDate = dateTimeService.getStartDateFromDateAndTime(putData.getValidFromDate(),
            putData.getValidFromTime());
        Date untilDate = dateTimeService.getEndDateFromDateAndTime(putData.getValidUntilDate(),
            putData.getValidUntilTime());

        ServiceAgreementItem serviceAgreement = checkIfServiceAgreementExists(serviceAgreementId);
        if (serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(serviceAgreementId)) {
            LOGGER.warn("Service agreement {} is in pending state.", serviceAgreementId);
            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
        serviceAgreementBusinessRulesService.checkPendingValidationsInServiceAgreement(serviceAgreementId);
        if (!serviceAgreementBusinessRulesService.isPeriodValid(fromDate, untilDate)) {
            LOGGER.warn("Invalid validity period of the service agreement.");
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_095.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_095.getErrorCode());
        } else if (statusIsDisabled(putData) && serviceAgreementBusinessRulesService
            .isServiceAgreementRootMasterServiceAgreement(serviceAgreement)) {
            LOGGER
                .warn("Service Agreement {}is master Service Agreement and can not be disabled.", putData.getName());
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_070.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_070.getErrorCode());
        } else if (serviceAgreementBusinessRulesService
            .serviceAgreementWithGivenExternalIdAlreadyExistsAndNotNull(putData, serviceAgreement)) {
            LOGGER.warn("Service agreement external id {} is not unique.", putData.getExternalId());
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_069.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_069.getErrorCode());
        } else if (serviceAgreementBusinessRulesService
            .existsPendingServiceAgreementWithExternalId(putData.getExternalId())) {
            LOGGER.warn("Service agreement external id {} is not unique.", putData.getExternalId());
            throw getBadRequestException(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode());
        } else {
            updateServiceAgreementHandler.handleRequest(new SingleParameterHolder<>(serviceAgreement.getId()), putData);
        }
    }

    private boolean statusIsDisabled(ServiceAgreementPutRequestBody putData) {
        return putData.getStatus() != null && putData.getStatus().equals(Status.DISABLED);
    }

    private ServiceAgreementItem checkIfServiceAgreementExists(String serviceAgreementId) {
        return persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId);
    }

}
