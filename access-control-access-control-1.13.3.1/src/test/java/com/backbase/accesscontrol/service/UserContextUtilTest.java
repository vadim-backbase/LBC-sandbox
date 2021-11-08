package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_071;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.communication.context.UserContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestUtil;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class UserContextUtilTest {

    @Mock
    private InternalRequestUtil internalRequestUtil;

    private UserContextUtil userContextUtil;

    @Mock
    private SecurityContextUtil securityContextUtil;
    @Mock
    private UserManagementService userManagementService;

    @Before
    public void setUp() {

        userContextUtil = new UserContextUtil(securityContextUtil, true, true, "Strict", userManagementService);
    }

    @Test
    public void getServiceAgreementFromUserContext() {
        String serviceAgreementId = "serviceAgreementId";
        String legalEntityId = "legalEntityId";
        when(internalRequestUtil.getUserContext()).thenReturn(new UserContext(serviceAgreementId, legalEntityId));
        when(securityContextUtil.getUserTokenClaim(anyString(), eq(String.class)))
            .thenReturn(Optional.of(serviceAgreementId));

        String saIdFromContext = userContextUtil.getServiceAgreementId();
        assertEquals(serviceAgreementId, saIdFromContext);
    }

    @Test
    public void getServiceAgreementFromUserContextFailure() {
        when(securityContextUtil.getUserTokenClaim(anyString(), eq(String.class))).thenReturn(Optional.empty());

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextUtil.getServiceAgreementId());

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_071.getErrorMessage(), ERR_AG_071.getErrorCode()));
    }

    @Test
    public void shouldReturnCookieDataWithAllAttributes() {

        assertThat(userContextUtil.getCookie("key", "value"), Matchers.allOf(
            Matchers.containsString("key=value"),
            Matchers.containsString("HttpOnly"),
            Matchers.containsString("Secure"),
            Matchers.containsString("SameSite=Strict")));
    }

    @Test
    public void shouldGetUserIdAndLeIdFromContext() {
        String legalEntityId = "legalEntityId";
        String userId = "userId";

        when(securityContextUtil.getInternalId())
            .thenReturn(Optional.of(userId));
        when(securityContextUtil.getUserTokenClaim(eq("leid"), eq(String.class)))
            .thenReturn(Optional.of(legalEntityId));

        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        assertEquals(userId, userContextDetails.getInternalUserId());
        assertEquals(legalEntityId, userContextDetails.getLegalEntityId());
    }

    @Test
    public void shouldGetUserIdAndLeIdFromPersistenceIfUserIdIsNotPresent() {
        String legalEntityId = "legalEntityId";
        String userId = "userId";

        String externalUserId = "username";
        when(securityContextUtil.getUserTokenClaim(eq("sub"), eq(String.class)))
            .thenReturn(Optional.of(externalUserId));
        when(securityContextUtil.getInternalId())
            .thenReturn(Optional.empty());

        com.backbase.dbs.user.api.client.v2.model.GetUser data = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        data.setLegalEntityId(legalEntityId);
        data.setId(userId);
        data.setExternalId(externalUserId);
        when(
            userManagementService.getUserByExternalId(eq(externalUserId)))
            .thenAnswer(ans -> data);
        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        assertEquals(userId, userContextDetails.getInternalUserId());
        assertEquals(legalEntityId, userContextDetails.getLegalEntityId());
    }

    @Test
    public void shouldGetUserIdAndLeIdFromPersistenceIfLegalEntityIdIsNotPresent() {
        String legalEntityId = "legalEntityId";
        String userId = "userId";

        String externalUserId = "username";
        when(securityContextUtil.getUserTokenClaim(eq("sub"), eq(String.class)))
            .thenReturn(Optional.of(externalUserId));
        when(securityContextUtil.getUserTokenClaim(eq("leid"), eq(String.class)))
            .thenReturn(Optional.empty());

        com.backbase.dbs.user.api.client.v2.model.GetUser data = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        data.setLegalEntityId(legalEntityId);
        data.setId(userId);
        data.setExternalId(externalUserId);
        when(
            userManagementService.getUserByExternalId(eq(externalUserId)))
            .thenAnswer(ans -> data);

        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        assertEquals(userId, userContextDetails.getInternalUserId());
        assertEquals(legalEntityId, userContextDetails.getLegalEntityId());
    }
}