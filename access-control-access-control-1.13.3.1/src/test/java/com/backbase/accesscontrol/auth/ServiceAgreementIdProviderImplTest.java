package com.backbase.accesscontrol.auth;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ServiceAgreementIdProviderImpl test.
 */
@ExtendWith(MockitoExtension.class)
class ServiceAgreementIdProviderImplTest {

    private static final String USERNAME = "admin";
    private static final String SERVICE_AGREEMENT_ID = "SA 1";
    private static final String LEGAL_ENTITY_ID = "le-id";
    private static final String ENTITY_CHILD = "CHILDREN_ID";
    private static final String SAID = "said";

    @Mock
    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContextUtil securityContextUtil;

    @Mock
    private MasterServiceAgreementFallbackProperties fallbackProperties;

    @InjectMocks
    private ServiceAgreementIdProviderImpl serviceAgreementIdProviderImpl;

    @Spy
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;

    @BeforeEach
    public void setup() {
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @Test
    void testGetServiceAgreementIdFromContext() {
        when(securityContextUtil.getUserTokenClaim(SAID, String.class))
            .thenReturn(Optional.of(SERVICE_AGREEMENT_ID));
        Optional<String> serviceAgreementIdOpt = serviceAgreementIdProviderImpl.getServiceAgreementId();
        assertTrue(serviceAgreementIdOpt.isPresent());
        assertEquals(SERVICE_AGREEMENT_ID, serviceAgreementIdOpt.get());
    }

    @Test
    void testWhenUserContextIsNull() {
        when(securityContextUtil.getUserTokenClaim(SAID, String.class))
            .thenReturn(Optional.empty());
        Optional<String> serviceAgreementIdOpt = serviceAgreementIdProviderImpl.getServiceAgreementId();
        assertFalse(serviceAgreementIdOpt.isPresent());
    }

    @Test
    void testWhenUserContextIsPresentButServiceAgreementIsNull() {
        when(securityContextUtil.getUserTokenClaim(SAID, String.class))
            .thenReturn(Optional.empty());
        Optional<String> serviceAgreementIdOpt = serviceAgreementIdProviderImpl.getServiceAgreementId();
        assertFalse(serviceAgreementIdOpt.isPresent());
    }

    @Test
    void testGetMasterServiceAgreementId() {
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setLegalEntityId(LEGAL_ENTITY_ID);
        when(userManagementService.getUserByExternalId(USERNAME))
            .thenReturn(user);

        ServiceAgreement masterServiceAgreement = getMasterServiceAgreementResponse();
        when(persistenceLegalEntityService.getMasterServiceAgreement(LEGAL_ENTITY_ID))
            .thenReturn(masterServiceAgreement);
        when(fallbackProperties.isEnabled()).thenReturn(true);

        String masterServiceAgreementId =
            serviceAgreementIdProviderImpl.getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(USERNAME);
        assertEquals(SERVICE_AGREEMENT_ID, masterServiceAgreementId);
    }

    @Test
    void testGetMasterServiceAgreementIdShouldThrowForbiddenWhenServiceAgreementNotProvidedAndFallbackDisabled() {
        when(fallbackProperties.isEnabled()).thenReturn(false);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> serviceAgreementIdProviderImpl
                .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(USERNAME));
        assertThat(forbiddenException,
            new ForbiddenErrorMatcher(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode()));
    }

    @Test
    void getServiceAgreementParticipants() {
        String serviceAgreementId = "1";

        List<Participant> participants = Arrays.asList(
            new Participant()
                .withId(ENTITY_CHILD)
                .withSharingUsers(true)
                .withSharingAccounts(false),
            new Participant()
                .withId(LEGAL_ENTITY_ID)
                .withSharingUsers(true)
                .withSharingAccounts(false)
        );

        when(serviceAgreementServiceFacade
            .getServiceAgreementParticipants(serviceAgreementId))
            .thenReturn(participants);

        List<Participant> serviceAgreementParticipants = serviceAgreementIdProviderImpl
            .getServiceAgreementParticipants(serviceAgreementId);

        verify(serviceAgreementServiceFacade)
            .getServiceAgreementParticipants(serviceAgreementId);

        assertEquals(participants, serviceAgreementParticipants);
    }

    @Test
    void shouldGetServiceAgreementById() {
        String serviceAgreementId = "1";

        ServiceAgreementItem serviceAgreement = new ServiceAgreementItem()
            .withId(serviceAgreementId);

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId))
            .thenReturn(serviceAgreement);

        Optional<ServiceAgreementItem> serviceAgreementById = serviceAgreementIdProviderImpl
            .getServiceAgreementById(serviceAgreementId);

        assertTrue(serviceAgreementById.isPresent());

        assertEquals(serviceAgreement, serviceAgreementById.get());
    }

    @Test
    void shouldReturnOptionalWithFalse() {
        String serviceAgreementId = "1";

        when(persistenceServiceAgreementService
            .getServiceAgreementResponseBodyById(serviceAgreementId))
            .thenThrow(new NotFoundException());

        Optional<ServiceAgreementItem> serviceAgreementById = serviceAgreementIdProviderImpl
            .getServiceAgreementById(serviceAgreementId);

        assertFalse(serviceAgreementById.isPresent());
    }

    private ServiceAgreement getMasterServiceAgreementResponse() {
        return new ServiceAgreement()
            .withId(SERVICE_AGREEMENT_ID)
            .withCreatorLegalEntity(new LegalEntity()
                .withId(LEGAL_ENTITY_ID))
            .withMaster(true);
    }
}
