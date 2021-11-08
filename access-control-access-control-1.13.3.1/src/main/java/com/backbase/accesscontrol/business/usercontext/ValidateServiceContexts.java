package com.backbase.accesscontrol.business.usercontext;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_073;

import com.backbase.accesscontrol.business.service.UserContextEncryptionService;
import com.backbase.accesscontrol.dto.UserContextDto;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.jwt.core.exception.JsonWebTokenException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.stereotype.Service;

/**
 * This class is the business process component of the access-group presentation service, communicating with the
 * UserContextPnpService.
 */
@Service
@AllArgsConstructor
public class ValidateServiceContexts {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateServiceContexts.class);

    private UserContextService userContextService;
    private UserContextEncryptionService userContextEncryptionService;
    private UserContextUtil userContextUtil;

    /**
     * Access to the pnp layer to validate the service agreement for the user id. If it is valid a encrypted token is
     * returned with these information.
     *
     * @param internalRequest    Internal Request
     * @param externalUserId     User id
     * @param serviceAgreementId Service Agreement Id
     * @return if it is valid, encrypted token with service agreement. If is not valid returns null.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_VALIDATE_SERVICE_AGREEMENT)
    public InternalRequest<String> validateServiceAgreement(
        @Body InternalRequest<Void> internalRequest,
        @Header("userId") String externalUserId,
        @Header("serviceAgreementId") String serviceAgreementId) {

        LOGGER.info("Service call for validate service agreement {} for the user id {}",
            serviceAgreementId, externalUserId);
        String token = createToken(serviceAgreementId, externalUserId);
        return getInternalRequest(token,
            internalRequest.getInternalRequestContext());
    }

    private String createToken(String serviceAgreementId, String externalUserId) {

        String internalUserId = userContextUtil.getUserContextDetails().getInternalUserId();
        UserContextDto userContextEncryptionDto = new UserContextDto(serviceAgreementId, externalUserId);
        try {
            LOGGER.info(
                "Service call to verify service agreement. "
                    + "User external user Id {}, Service Agreement Id {}",
                externalUserId, serviceAgreementId);
            userContextService.validateUserContext(internalUserId, serviceAgreementId);
            return userContextEncryptionService.encryptUserContextToJwt(userContextEncryptionDto);
        } catch (JsonWebTokenException e) {
            throw new InvalidInvocationException("Token encryption process failed.");
        } catch (ForbiddenException ex) {
            throw getForbiddenException(ERR_AG_073.getErrorMessage(), ERR_AG_073.getErrorCode());
        }
    }
}
