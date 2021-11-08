package com.backbase.accesscontrol.api.client;

import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.client.rest.spec.model.Serviceagreementpartialitem;
import com.backbase.accesscontrol.client.rest.spec.model.UserContextPOST;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.UserContextFlowService;
import com.backbase.accesscontrol.service.facades.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserContextController implements com.backbase.accesscontrol.client.rest.spec.api.UserContextApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserContextController.class);
    private static final String COOKIE_NAME = "USER_CONTEXT";

    private UserContextService userContextService;
    private UserContextFlowService userContextFlowService;
    private UserContextUtil userContextUtil;
    private ServiceAgreementIdProvider serviceAgreementIdProvider;
    private ParameterValidationService parameterValidationService;
    private ValidationConfig validationConfig;
    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> postUserContext(UserContextPOST userContextPOST) {
        String externalUserName = userContextUtil.getAuthenticatedUserName();
        HttpHeaders headers = new HttpHeaders();
        try {
            String token = userContextService
                .validate(externalUserName, userContextPOST.getServiceAgreementId());
            headers.add("Set-Cookie", userContextUtil.getCookie(COOKIE_NAME, token));

        } catch (ForbiddenException e) {
            throw e;
        }
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PermissionsDataGroup> getUserContextPermissions(
        @Valid PermissionsRequest permissionsRequest) {

        String authenticatedUserName = userContextUtil.getAuthenticatedUserName();
        String serviceAgreementId = serviceAgreementIdProvider.getServiceAgreementId()
            .orElseGet(() -> serviceAgreementIdProvider
                .getMasterServiceAgreementIdIfServiceAgreementNotPresentInContext(authenticatedUserName));
        String userId = userContextUtil.getUserContextDetails().getInternalUserId();

        LOGGER.info("Get user context permission data groups for userId {}, service agreement Id {} and parameters {}.",
            userId, serviceAgreementId, permissionsRequest);

        permissionsRequest.getDataGroupTypes()
            .forEach(dataGroupType -> validationConfig.validateDataGroupType(dataGroupType));

        return new ResponseEntity<>(
            userContextFlowService.getUserContextPermissions(userId, serviceAgreementId, permissionsRequest),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Serviceagreementpartialitem>> getUserContextServiceAgreements(String query, Integer from,
        String cursor, Integer size) {
        parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);
        String externalUserName = userContextUtil.getAuthenticatedUserName();
        LOGGER.info(
            "Handling REST call for getting all user context by userId {}, query {}, from {},"
                + " cursor {} and size {}",
            externalUserName, query, from, cursor, size);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> responseBody = userContextService
            .getUserContextByUserId(externalUserName, query, from, cursor, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", responseBody.getTotalNumberOfRecords().toString());

        return new ResponseEntity<>(
            payloadConverter.convertListPayload(responseBody.getRecords(), Serviceagreementpartialitem.class), headers,
            HttpStatus.OK);
    }


}
