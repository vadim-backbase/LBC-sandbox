package com.backbase.accesscontrol.auth;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_017;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceDataItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ServiceAgreementResource;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * FunctionalAccessControl test class.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessControlValidatorImplTest {

    private static final String USERNAME = "admin";
    private static final String SERVICE_AGREEMENT_ID = "0001";
    private static final String LE_ID = "Le-01";
    private static final String RESOURCE = "Payment";
    private static final String FUNCTION = "Faster payment";
    private static final String PRIVILEGES = "execute,submit";
    private static final String USER_ID = "1";

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private ServiceAgreementIdProvider serviceAgreementIdProvider;
    @Mock
    private SecurityContextUtil securityContextUtil;
    @Captor
    private ArgumentCaptor<ServiceAgreementResource> serviceAgreementResourceCaptor;
    @Captor
    private ArgumentCaptor<EntitlementsResource> entitlementsResourceResourceCaptor;
    @InjectMocks
    private AccessControlValidatorImpl functionalAccessControlDBS;
    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;
    @Mock
    private UserAccessPermissionCheckService userAccessPermissionCheckService;

    @Before
    public void setup() {
        openMocks(this);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @Test
    public void shouldReturnTrueForSuccessfulPermissionCheck() {
        mockGetUserContextDetails();
        mockDefaultServiceAgreement();

        boolean successfulCheck = functionalAccessControlDBS.checkPermissions(USERNAME, RESOURCE, FUNCTION, PRIVILEGES);
        assertTrue(successfulCheck);
        verify(userAccessPermissionCheckService).checkUserPermission(
            eq(USER_ID),
            eq(SERVICE_AGREEMENT_ID),
            eq(FUNCTION),
            eq(RESOURCE),
            eq(PRIVILEGES));
    }

    @Test
    public void testShouldReturnTrueWhenServiceAgreementMissingInContextAndChecksForMasterServiceAgreement() {
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.ofNullable(null));
        when(serviceAgreementIdProvider.getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(USERNAME))
            .thenReturn(SERVICE_AGREEMENT_ID);
        mockGetUserContextDetails();

        boolean isEntitled = functionalAccessControlDBS.checkPermissions(USERNAME, RESOURCE, FUNCTION, PRIVILEGES);
        assertTrue(isEntitled);
        verify(userAccessPermissionCheckService).checkUserPermission(
            eq(USER_ID),
            eq(SERVICE_AGREEMENT_ID),
            eq(FUNCTION),
            eq(RESOURCE),
            eq(PRIVILEGES));
    }

    @Test
    public void shouldReturnFalseWhenUserIsNotFoundAndForbiddenExceptionIsThrown() {
        when(securityContextUtil.getInternalId())
            .thenThrow(new ForbiddenException()
                .withMessage("User is not authenticated."));
        mockDefaultServiceAgreement();

        boolean successfulCheck = functionalAccessControlDBS.checkPermissions(USERNAME, RESOURCE, FUNCTION, PRIVILEGES);
        assertFalse(successfulCheck);
        verify(userAccessPermissionCheckService, times(0)).checkUserPermission(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString());
    }

    @Test
    public void shouldFailCheckWhenForbiddenExceptionIsThrown() {
        mockGetUserContextDetails();
        mockDefaultServiceAgreement();

        doThrow(getForbiddenException(ERR_ACQ_017.getErrorMessage(), ERR_ACQ_017.getErrorCode()))
            .when(userAccessPermissionCheckService)
            .checkUserPermission(eq(USER_ID),
                eq(SERVICE_AGREEMENT_ID),
                eq(FUNCTION),
                eq(RESOURCE),
                eq(PRIVILEGES));

        boolean check = functionalAccessControlDBS.checkPermissions(USERNAME, RESOURCE, FUNCTION, PRIVILEGES);
        assertFalse(check);
    }

    @Test
    public void userHasNoAccessToEntitlementsResourceShouldReturnFalseWhenListContainingResourceIdIsReturnedFromPandp() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        String resourceLegalEntityId = "LE-2";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));
        mockGetUserContextDetails();
        when(userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(any(EntitlementsResource.class)))
            .thenReturn(new ContextLegalEntities()
                .withLegalEntities(Lists.newArrayList(resourceLegalEntityId)));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToEntitlementResource(resourceLegalEntityId,
                AccessResourceType.USER_AND_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToEntitlementsResources(entitlementsResourceResourceCaptor.capture());
        EntitlementsResource captorValue = entitlementsResourceResourceCaptor.getValue();
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertTrue(captorValue.getLegalEntityIds().contains(resourceLegalEntityId));
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_AND_ACCOUNT,
            captorValue.getAccessResourceType());
        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToEntitlementsResourceShouldReturnTrueWhenEmptyListIsReturnedFromPandp() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        String resourceLegalEntityId = "LE-2";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));
        mockGetUserContextDetails();
        when(userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(any(EntitlementsResource.class)))
            .thenReturn(new ContextLegalEntities().withLegalEntities(Lists.emptyList()));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToEntitlementResource(resourceLegalEntityId,
                AccessResourceType.USER_OR_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToEntitlementsResources(entitlementsResourceResourceCaptor.capture());
        EntitlementsResource captorValue = entitlementsResourceResourceCaptor.getValue();
        assertTrue(captorValue.getLegalEntityIds().contains(resourceLegalEntityId));
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            captorValue.getAccessResourceType());
        assertTrue(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToEntitlementsResourceShouldReturnTrue() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        String resourceLegalEntityId = "LE-2";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.empty());
        when(serviceAgreementIdProvider
            .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(USERNAME))
            .thenReturn(saId);
        mockGetUserContextDetails();

        when(userAccessPermissionCheckService
            .checkUserAccessToEntitlementsResources(any(EntitlementsResource.class)))
            .thenReturn(new ContextLegalEntities().withLegalEntities(Lists.emptyList()));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToEntitlementResource(resourceLegalEntityId,
                AccessResourceType.USER_OR_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToEntitlementsResources(entitlementsResourceResourceCaptor.capture());
        EntitlementsResource captorValue = entitlementsResourceResourceCaptor.getValue();
        assertTrue(captorValue.getLegalEntityIds().contains(resourceLegalEntityId));
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            captorValue.getAccessResourceType());
        assertTrue(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToServiceAgreementShouldReturnFalseWhenNoExceptionIsThrownFromPandp() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));
        mockGetUserContextDetails();

        doNothing()
            .when(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(any(ServiceAgreementResource.class));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToServiceAgreement(saId,
                AccessResourceType.USER_OR_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(serviceAgreementResourceCaptor.capture());
        ServiceAgreementResource captorValue = serviceAgreementResourceCaptor.getValue();
        assertEquals(saId, captorValue.getServiceAgreementId());
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            captorValue.getAccessResourceType());
        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToServiceAgreementShouldReturnFalseWhenNoAuthenticatedUser() {
        String saId = "saId";

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToServiceAgreement(saId,
                AccessResourceType.USER_OR_ACCOUNT);

        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToServiceAgreementShouldReturnTrueWhenNotFoundExceptionIsThrownFromPandp() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));
        mockGetUserContextDetails();
        doThrow(new NotFoundException())
            .when(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(any(ServiceAgreementResource.class));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToServiceAgreement(saId,
                AccessResourceType.USER_OR_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(serviceAgreementResourceCaptor.capture());
        ServiceAgreementResource captorValue = serviceAgreementResourceCaptor.getValue();
        assertEquals(saId, captorValue.getServiceAgreementId());
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            captorValue.getAccessResourceType());
        assertTrue(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToServiceAgreementShouldReturnTrueWhenForbiddenExceptionIsThrownFromPandp() {
        String saId = "saId";
        String userLegalEntity = "Le-01";
        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));
        mockGetUserContextDetails();
        doThrow(new ForbiddenException())
            .when(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(any(ServiceAgreementResource.class));

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToServiceAgreement(saId,
                AccessResourceType.USER_OR_ACCOUNT);

        verify(userAccessPermissionCheckService)
            .checkUserAccessToServiceAgreement(serviceAgreementResourceCaptor.capture());
        ServiceAgreementResource captorValue = serviceAgreementResourceCaptor.getValue();
        assertEquals(saId, captorValue.getServiceAgreementId());
        assertEquals(saId, captorValue.getContextServiceAgreementId());
        assertEquals(userLegalEntity, captorValue.getUserLegalEntityId());
        assertEquals(
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.AccessResourceType.USER_OR_ACCOUNT,
            captorValue.getAccessResourceType());
        assertTrue(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToDataItemShouldReturnFalseWhenUserHasPermission() {
        String saId = "saId";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        mockGetUserContextDetails();
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));

        List<PersistenceUserDataItemPermission> response = asList(
            new PersistenceUserDataItemPermission()
                .withDataItem(new PersistenceDataItem()
                    .withId("item1")
                    .withDataType("ARRANGEMENTS"))
                .withPermissions(asList(new PersistenceUserPermission()
                    .withBusinessFunction("Payments")
                    .withFunctionCode("payments")
                    .withPrivileges(asList("edit"))
                    .withResource("Manage Payments"))));

        when(userAccessPrivilegeService
            .getUserDataItemsPrivileges(eq(USER_ID), eq(saId), eq(null), eq("Payments"), eq("edit"),
                eq("ARRANGEMENTS"), eq("item1")))
            .thenReturn(response);

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToDataItem("Payments", "edit", "ARRANGEMENTS", "item1");

        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToDataItemShouldGetDetailsFromPersistenceAndReturnFalseWhenUserHasPermission() {
        String saId = "saId";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));

        com.backbase.dbs.user.api.client.v2.model.GetUser responseBody = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        responseBody.setExternalId(USERNAME);
        responseBody.setId(USER_ID);
        responseBody.setLegalEntityId(LE_ID);

        when(userManagementService.getUserByExternalId(eq(USERNAME)))
            .thenReturn(responseBody);
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));

        List<PersistenceUserDataItemPermission> response = asList(
            new PersistenceUserDataItemPermission()
                .withDataItem(new PersistenceDataItem()
                    .withId("item1")
                    .withDataType("ARRANGEMENTS"))
                .withPermissions(asList(new PersistenceUserPermission()
                    .withBusinessFunction("Payments")
                    .withPrivileges(asList("edit"))
                    .withResource("Manage Payments"))));

        when(userAccessPrivilegeService
            .getUserDataItemsPrivileges(eq(USER_ID), eq(saId), eq(null), eq("Payments"), eq("edit"),
                eq("ARRANGEMENTS"), eq("item1")))
            .thenReturn(response);

        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToDataItem("Payments", "edit", "ARRANGEMENTS", "item1");

        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToDataItemShouldFallbackToMsaIsSaIsNotProvided() {
        String msaId = "msaId";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        mockGetUserContextDetails();
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.empty());
        when(serviceAgreementIdProvider.getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(USERNAME))
            .thenReturn(msaId);

        List<PersistenceUserDataItemPermission> response = asList(
            new PersistenceUserDataItemPermission()
                .withDataItem(new PersistenceDataItem()
                    .withId("item1")
                    .withDataType("ARRANGEMENTS"))
                .withPermissions(asList(new PersistenceUserPermission()
                    .withBusinessFunction("Payments")
                    .withPrivileges(asList("edit"))
                    .withResource("Manage Payments"))));

        when(userAccessPrivilegeService
            .getUserDataItemsPrivileges(eq(USER_ID), eq(msaId), eq(null), eq("Payments"), eq("edit"),
                eq("ARRANGEMENTS"), eq("item1")))
            .thenReturn(response);
        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToDataItem("Payments", "edit", "ARRANGEMENTS", "item1");

        assertFalse(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToDataItemShouldReturnTrueWhenUserHasNoPermissions() {
        String saId = "saId";

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));
        mockGetUserContextDetails();
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(saId));

        List<PersistenceUserDataItemPermission> response = asList(
            new PersistenceUserDataItemPermission()
                .withDataItem(new PersistenceDataItem()
                    .withId("other item")
                    .withDataType("ARRANGEMENTS"))
                .withPermissions(asList(new PersistenceUserPermission()
                    .withBusinessFunction("Payments")
                    .withPrivileges(asList("edit"))
                    .withResource("Manage Payments"))));

        when(userAccessPrivilegeService.getUserDataItemsPrivileges(eq(USER_ID), eq(saId), eq(null), eq("Payments"),
            eq("edit"), eq("ARRANGEMENTS"), eq("item1")))
            .thenReturn(response);
        boolean hasNoAccess = functionalAccessControlDBS
            .userHasNoAccessToDataItem("Payments", "edit", "ARRANGEMENTS", "item1");

        assertTrue(hasNoAccess);
    }

    @Test
    public void userHasNoAccessToDataItemShouldReturnForbiddenIfNoUserIsLoggedIn() {

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.empty());

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> functionalAccessControlDBS
                .userHasNoAccessToDataItem("some BF", "view", "ARRANGEMENTS", "so eid"));
        assertEquals("User is not authenticated.", exception.getMessage());
    }

    @Test
    public void userHasNoAccessToDataItemShouldReturnForbiddenIfNoUserIsFound() {

        when(securityContextUtil.getUserTokenClaim(eq(InternalJwtClaimsSet.SUBJECT_CLAIM), eq(String.class)))
            .thenReturn(Optional.of(USERNAME));

        when(userManagementService.getUserByExternalId(eq(USERNAME)))
            .thenThrow(new NotFoundException("User does not exist"));
        assertThrows(ForbiddenException.class, () -> functionalAccessControlDBS
            .userHasNoAccessToDataItem("Payments", "edit", "ARRANGEMENTS", "item1"));
    }

    private void mockDefaultServiceAgreement() {
        when(serviceAgreementIdProvider.getServiceAgreementId()).thenReturn(Optional.of(SERVICE_AGREEMENT_ID));
    }

    private void mockGetUserContextDetails() {
        when(securityContextUtil.getInternalId()).thenReturn(Optional.of(USER_ID));
        when(securityContextUtil.getUserTokenClaim(eq("leid"), eq(String.class))).thenReturn(Optional.of(LE_ID));
    }
}
