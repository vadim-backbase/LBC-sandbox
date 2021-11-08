package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_087;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_117;

import com.backbase.accesscontrol.business.persistence.serviceagreement.IngestServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.routes.serviceagreement.IngestServiceAgreementRouteProxy;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.InternalRequestUtil;
import com.backbase.accesscontrol.util.PermissionSetValidationUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import java.util.Date;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class IngestServiceAgreement implements IngestServiceAgreementRouteProxy {

    private UserManagementService userManagementService;
    private DateTimeService dateTimeService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    private PermissionSetValidationUtil permissionSetValidationUtil;
    private IngestServiceAgreementHandler ingestServiceAgreementHandler;

    /**
     * Sends request to pandp for ingesting a service agreement.
     *
     * @param internalRequest internal request of {@link ServiceAgreementIngestPostResponseBody}
     * @return internal request of {@link ServiceAgreementIngestPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_INGEST_SERVICE_AGREEMENT)
    public InternalRequest<ServiceAgreementIngestPostResponseBody> ingestServiceAgreement(
        @Body InternalRequest<ServiceAgreementIngestPostRequestBody> internalRequest) {
        log.debug("Ingesting service agreement");

        validateRequest(internalRequest.getData());

        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> serviceAgreementData =
            new ServiceAgreementData<>(internalRequest.getData(), userManagementService
                .getUsersGroupedByExternalId(internalRequest.getData()));

        return InternalRequestUtil.getInternalRequest(ingestServiceAgreementHandler
                .handleRequest(new EmptyParameterHolder(), serviceAgreementData),
            internalRequest.getInternalRequestContext());
    }

    private void validateRequest(ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody) {
        if (isHasNullParticipants(serviceAgreementIngestPostRequestBody)) {
            log.warn("List of participant items contains null value.");
            throw getBadRequestException(ERR_AG_087.getErrorMessage(),
                ERR_AG_087.getErrorCode());
        }
        Date from = dateTimeService
            .getStartDateFromDateAndTime(serviceAgreementIngestPostRequestBody.getValidFromDate(),
                serviceAgreementIngestPostRequestBody.getValidFromTime());
        Date until = dateTimeService
            .getEndDateFromDateAndTime(serviceAgreementIngestPostRequestBody.getValidUntilDate(),
                serviceAgreementIngestPostRequestBody.getValidUntilTime());

        if (!serviceAgreementBusinessRulesService.isPeriodValid(from, until)) {
            log.warn("From {} and time {} are not valid", from, until);
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_095.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_095.getErrorCode());
        } else if (serviceAgreementBusinessRulesService
            .existsPendingServiceAgreementWithExternalId(serviceAgreementIngestPostRequestBody.getExternalId())) {

            log.warn("Service agreement external id {} is not unique.",
                serviceAgreementIngestPostRequestBody.getExternalId());
            throw getBadRequestException(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode());
        }

        if (serviceAgreementIngestPostRequestBody.getIsMaster()
            && serviceAgreementIngestPostRequestBody.getCreatorLegalEntity() != null) {
            log.warn("An attempt to pass creatorLegalEntity value {} to master service agreement {}",
                serviceAgreementIngestPostRequestBody.getCreatorLegalEntity(),
                serviceAgreementIngestPostRequestBody.getExternalId());
            throw getBadRequestException(ERR_AG_117.getErrorMessage(), ERR_AG_117.getErrorCode());
        }

        permissionSetValidationUtil
            .validateUserApsIdentifiers(serviceAgreementIngestPostRequestBody.getRegularUserAps(),
                serviceAgreementIngestPostRequestBody.getAdminUserAps());
    }

    private boolean isHasNullParticipants(ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody) {
        return serviceAgreementIngestPostRequestBody
            .getParticipantsToIngest()
            .stream()
            .anyMatch(Objects::isNull);
    }
}
