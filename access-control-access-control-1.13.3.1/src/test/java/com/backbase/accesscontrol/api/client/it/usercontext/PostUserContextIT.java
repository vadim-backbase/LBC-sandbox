package com.backbase.accesscontrol.api.client.it.usercontext;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_073;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.backbase.accesscontrol.api.JwtService;
import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UserContextController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextPostRequestBody;
import com.google.common.collect.Lists;
import com.nimbusds.jwt.SignedJWT;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link UserContextController#postUserContext (UserContextPostRequestBody, HttpServletRequest,
 * HttpServletResponse)}
 */
public class PostUserContextIT extends TestDbWireMock {

    public static final String TEST_URL = "/accessgroups/usercontext";

    @Autowired
    private JwtService jwtService;

    @Before
    public void setUp() {

        ApplicableFunctionPrivilege viewServiceAgreement = businessFunctionCache.getApplicableFunctionPrivilegeById(
            businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList("view"))
                .stream().findFirst().get());

        GroupedFunctionPrivilege viewEntitlementsWithLimit =
            getGroupedFunctionPrivilege(null, viewServiceAgreement, null);

        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        UserContext userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementId(contextUserId, rootMsa.getId())
            .orElseGet(() -> userContextJpaRepository.save(new UserContext(contextUserId, rootMsa.getId())));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup);
    }

    @Test
    public void shouldGenerateUserContextCookie() throws Exception {

        String saId = rootMsa.getId();

        ResponseEntity<String> response = executeClientRequestEntity(new UrlBuilder(TEST_URL).build(),
            HttpMethod.POST, new UserContextPostRequestBody().withServiceAgreementId(saId),
            contextUserId);

        assertEquals(NO_CONTENT, response.getStatusCode());

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        Optional<String> userContextCookie = cookies.stream()
            .map(cookie -> cookie.split("="))
            .filter(array -> array[0].equals("USER_CONTEXT"))
            .map(array -> array[1])
            .findFirst();
        assertTrue(userContextCookie.isPresent());

        SignedJWT data = (SignedJWT) jwtService.getJsonWebTokenConsumerType()
            .parseToken(userContextCookie.get().replaceAll(" Path", ""));

        Map<String, Object> claims = data.getJWTClaimsSet().getClaims();
        assertEquals(contextUserId, claims.get("sub"));
        assertEquals(saId, claims.get("said"));
    }

    @Test
    public void shouldThrowForbiddenExceptionWithInvalidServiceAgreement() {
        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeClientRequestEntity(new UrlBuilder(TEST_URL).build(),
                HttpMethod.POST, new UserContextPostRequestBody().withServiceAgreementId(UUID.randomUUID().toString()),
                contextUserId));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_073.getErrorMessage(), ERR_AG_073.getErrorCode()));
    }
}
