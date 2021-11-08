package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.mappers.MasterServiceAgreementMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetMasterServiceAgreementByLegalEntity {

    private static final Logger LOGGER = LoggerFactory
        .getLogger(GetMasterServiceAgreementByLegalEntity.class);

    private MasterServiceAgreementMapper masterServiceAgreementMapper;
    private AccessControlValidator accessControlValidator;
    private PersistenceLegalEntityService persistenceLegalEntityService;

    /**
     * Method that listens on direct:getMasterServiceAgreementByExternalLegalEntityIdRequestedInternal endpoint.
     *
     * @param request    Internal Request of {@link Void} type to be send by the client
     * @param externalId - external legal entity id
     * @return Internal Request of List {@link MasterServiceAgreementGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_EXTERNAL_LEGAL_ENTITY_ID)
    public InternalRequest<MasterServiceAgreementGetResponseBody> getMasterServiceAgreementByExternalLegalEntityId(
        @Body InternalRequest<Void> request, @Header("externalId") String externalId) {

        LOGGER.info("Trying to fetch Master Service Agreement with legalEntityExternalId {}", externalId);

        ServiceAgreement masterServiceAgreement = persistenceLegalEntityService
            .getMasterServiceAgreementByExternalId(externalId);

        if (accessControlValidator
            .userHasNoAccessToServiceAgreement(Objects.requireNonNull(masterServiceAgreement).getId(),
                AccessResourceType.NONE)) {

            LOGGER.warn("Master Service Agreement can't be fetched using legalEntityExternalId {}!",
                externalId);
            throw getForbiddenException(LegalEntityErrorCodes.ERR_AG_013.getErrorMessage(),
                LegalEntityErrorCodes.ERR_AG_013.getErrorCode());
        }

        MasterServiceAgreementGetResponseBody responseBody = masterServiceAgreementMapper
            .convertToResponse(masterServiceAgreement);

        return getInternalRequest(responseBody, request.getInternalRequestContext());
    }

    /**
     * Method that listens on direct:getMasterServiceAgreementByLegalEntityIdRequestedInternal endpoint.
     *
     * @param request       Internal Request of {@link Void} type to be send by the client
     * @param legalEntityId Legal Entity's id
     * @return Internal Request of List{@link MasterServiceAgreementGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_MASTER_SERVICE_AGREEMENT_BY_LEGAL_ENTITY_ID)
    public InternalRequest<MasterServiceAgreementGetResponseBody> getMasterServiceAgreementByLegalEntityId(
        @Body InternalRequest<Void> request,
        @Header("legalEntityId") String legalEntityId) {

        LOGGER.info("Trying to fetch the Legal Entity with legalEntityId {}", legalEntityId);
        if (accessControlValidator
            .userHasNoAccessToEntitlementResource(
                legalEntityId, AccessResourceType.NONE)) {

            throw getForbiddenException(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode());
        }

        ServiceAgreement serviceAgreement = persistenceLegalEntityService
            .getMasterServiceAgreement(legalEntityId);

        MasterServiceAgreementGetResponseBody responseBody = masterServiceAgreementMapper
            .convertToResponse(serviceAgreement);

        return getInternalRequest(responseBody, request.getInternalRequestContext());
    }
}
