package com.backbase.accesscontrol.auth;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementation of the {@link ServiceAgreementIdProvider}.
 */
@Component
@RequiredArgsConstructor
public class ServiceAgreementIdProviderImpl implements ServiceAgreementIdProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementIdProviderImpl.class);
    private static final String USER_IS_NOT_AUTHENTICATED = "User is not authenticated.";

    private final UserManagementService userManagementService;
    private final ServiceAgreementServiceFacade serviceAgreementServiceFacade;
    private final SecurityContextUtil securityContextUtil;
    private final PersistenceLegalEntityService persistenceLegalEntityService;
    private final ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;
    private final PersistenceServiceAgreementService persistenceServiceAgreementService;
    private final MasterServiceAgreementFallbackProperties fallback;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getServiceAgreementId() {
        LOGGER.info("Getting service agreement ID from context.");
        return securityContextUtil.getUserTokenClaim("said", String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ServiceAgreementItem> getServiceAgreementById(
        String serviceAgreementId) {
        ServiceAgreementItem serviceAgreementBase;

        try {
            serviceAgreementBase = persistenceServiceAgreementService
                .getServiceAgreementResponseBodyById(serviceAgreementId);
        } catch (NotFoundException exception) {
            serviceAgreementBase = null;
        }

        return Optional.ofNullable(serviceAgreementBase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Participant> getServiceAgreementParticipants(String serviceAgreementId) {
        return serviceAgreementServiceFacade.getServiceAgreementParticipants(serviceAgreementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(String username) {
        if (!fallback.isEnabled()) {
            throw getForbiddenException(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode());
        }
        return getMasterServiceAgreementBase(username).getId();
    }

    private ServiceAgreementBase getMasterServiceAgreementBase(String username) {

        String userLegalEntityId;
        ServiceAgreement masterServiceAgreement;
        try {
            userLegalEntityId = userManagementService.getUserByExternalId(username).getLegalEntityId();
            masterServiceAgreement = persistenceLegalEntityService
                .getMasterServiceAgreement(userLegalEntityId);
        } catch (NotFoundException e) {

            throw new ForbiddenException()
                .withMessage(USER_IS_NOT_AUTHENTICATED);
        }

        return serviceAgreementTransformerPersistence
            .transformServiceAgreement(ServiceAgreementBase.class, masterServiceAgreement);

    }

}
