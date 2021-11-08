package com.backbase.accesscontrol.util;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_071;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_072;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.buildingblocks.backend.security.auth.config.SecurityContextUtil;
import com.backbase.buildingblocks.jwt.internal.token.InternalJwtClaimsSet;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.google.common.base.Strings;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Component for extracting user context details.
 */
@Component
public class UserContextUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserContextUtil.class);
    private static final String USER_IS_NOT_AUTHENTICATED = "User is not authenticated.";
    private static final String SAID = "said";

    private SecurityContextUtil securityContextUtil;
    private boolean addHttpOnlyToCookie;
    private boolean addSecureToCookie;
    private String addSameSite;
    private UserManagementService userManagementService;

    /**
     * Autowire constructor.
     *
     * @param securityContextUtil Security context util
     * @param addHttpOnlyToCookie should set cookie with http only
     * @param addSecureToCookie   should set secured cookie
     * @param addSameSite         should set same site
     */
    public UserContextUtil(SecurityContextUtil securityContextUtil,
        @Value("${accesscontrol.security.cookies.httpOnly.value}") boolean addHttpOnlyToCookie,
        @Value("${accesscontrol.security.cookies.secure.value}") boolean addSecureToCookie,
        @Value("${accesscontrol.security.cookies.SameSite.value:}") String addSameSite,
        UserManagementService userManagementService) {

        this.addHttpOnlyToCookie = addHttpOnlyToCookie;
        this.addSecureToCookie = addSecureToCookie;
        this.addSameSite = addSameSite;
        this.securityContextUtil = securityContextUtil;
        this.userManagementService = userManagementService;
    }

    /**
     * Read service agreement from  user token claim.
     *
     * @return service agreement
     */
    public String getServiceAgreementId() {

        Optional<String> said = securityContextUtil.getUserTokenClaim(SAID, String.class);

        if (said.isEmpty()) {
            LOGGER.warn("service agreement  is null");
            throw getForbiddenException(ERR_AG_071.getErrorMessage(), ERR_AG_071.getErrorCode());
        }
        return Strings.emptyToNull(said.get());
    }

    /**
     * Returns the name of the currently logged in user.
     *
     * @return name of the currently logged in user
     */
    public String getAuthenticatedUserName() {

        Optional<String> userName = getOptionalAuthenticatedUserName();
        return userName
            .orElseThrow(() -> getForbiddenException(ERR_AG_072.getErrorMessage(), ERR_AG_072.getErrorCode()));
    }

    /**
     * Returns the name of the currently logged in user.
     *
     * @return name of the currently logged in user
     */
    public Optional<String> getOptionalAuthenticatedUserName() {

        return securityContextUtil
            .getUserTokenClaim(InternalJwtClaimsSet.SUBJECT_CLAIM, String.class);
    }

    /**
     * Generates header value for cookie. Java Cookie doesn't support SameSite attribute. Cookie name and value should
     * be validated, but for user context cookie is not required. In the future if we need this functionality for other
     * kinds od cookies, we need to extend this.
     *
     * @param name  - name of the cookie
     * @param value - value of the cookie
     * @return String represntation of the header for the cookie.
     */
    public String getCookie(String name, String value) {
        StringBuilder res = new StringBuilder();

        res.append(name).append("=").append(value).append("; Path=/");

        if (addHttpOnlyToCookie) {
            res.append("; HttpOnly");
        }

        if (addSecureToCookie) {
            res.append("; Secure");
        }

        if (!addSameSite.isEmpty()) {
            res.append("; SameSite=").append(addSameSite);
        }

        return res.toString();
    }

    /**
     * Gets internal user id and legal entity id from context if both present, if not it gets them from user p&p.
     *
     * @return {@link UserContextDetailsDto} containing user id and legal entity id.
     */
    public UserContextDetailsDto getUserContextDetails() {
        Optional<String> internalId = securityContextUtil.getInternalId();
        Optional<String> leid = securityContextUtil.getUserTokenClaim("leid", String.class);
        if (internalId.isPresent() && leid.isPresent()) {
            return new UserContextDetailsDto(internalId.get(), leid.get());
        } else {
            return getUserDetailsFromPersistence();
        }
    }

    private UserContextDetailsDto getUserDetailsFromPersistence() {
        String authenticatedUserName = getAuthenticatedUserName();
        UserContextDetailsDto userContextDetailsDto;
        try {
            com.backbase.dbs.user.api.client.v2.model.GetUser responseBody =
                userManagementService.getUserByExternalId(authenticatedUserName);
            userContextDetailsDto = new UserContextDetailsDto(responseBody.getId(),
                responseBody.getLegalEntityId());
        } catch (NotFoundException e) {
            throw new ForbiddenException()
                .withMessage(USER_IS_NOT_AUTHENTICATED);
        }
        return userContextDetailsDto;

    }

}
