package com.backbase.accesscontrol.business.serviceagreement;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.dto.ExtendedResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.ModifyUsersAndAdminsInServiceAgreement;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractServiceAgreementUsers {

    private static final String INVALID_EXTERNAL_USER_ID = "Invalid external user id";
    private UserManagementService userManagementService;
    private ModifyUsersAndAdminsInServiceAgreement modifyUsersAndAdminsInServiceAgreement;
    private BatchResponseItemMapper batchResponseItemMapper;
    private Validator validator;

    /**
     * Executes request for adding/removing users/admins on service agreement.
     *
     * @param request
     * @param userType
     */
    @SuppressWarnings("squid:S3864")
    public List<BatchResponseItemExtended> executeRequest(
        PresentationServiceAgreementUsersUpdate request,
        UserType userType) {

        List<BatchResponseItemExtended> invalidRequests = new ArrayList<>();

        List<PresentationServiceAgreementUserPair> validRequests = request.getUsers().stream()
            .collect(Multimaps.toMultimap(user -> user, this::validateUser, ArrayListMultimap::create)).entries()
            .stream()
            .peek(entry -> invalidRequests.add(
                entry.getValue().getStatus() != ItemStatusCode.HTTP_STATUS_OK ? batchResponseItemMapper
                    .toExtendedPresentation(entry.getValue()) : null))
            .filter(entry -> entry.getValue().getStatus() == ItemStatusCode.HTTP_STATUS_OK)
            .map(Map.Entry::getKey).collect(Collectors.toList());

        Map<String, GetUser> usersByExternalId = getUsersByExternalIdsMap(
            validRequests);

        invalidRequests.addAll(filterInvalidPayloads(validRequests, usersByExternalId, request.getAction()));

        validRequests = createValidRequestForPandpService(
            validRequests, usersByExternalId);

        List<BatchResponseItemExtended> pandpResponse = persist(new PresentationServiceAgreementUsersUpdate()
            .withAction(request.getAction()).withUsers(new ArrayList<>(validRequests)), usersByExternalId, userType);

        return Stream
            .concat(invalidRequests.stream().filter(Objects::nonNull), pandpResponse.stream())
            .peek(response -> response.setAction(request.getAction()))
            .collect(Collectors.toList());
    }

    private List<PresentationServiceAgreementUserPair> createValidRequestForPandpService(
        List<PresentationServiceAgreementUserPair> userServiceAgreementPairs,
        Map<String, GetUser> usersByExternalId) {
        return userServiceAgreementPairs.stream()
            .filter(
                userServiceAgreementPair -> usersByExternalId.containsKey(userServiceAgreementPair.getExternalUserId()))
            .collect(Collectors.toList());
    }

    private Map<String, GetUser> getUsersByExternalIdsMap(
        List<PresentationServiceAgreementUserPair> userServiceAgreementPairs) {

        List<String> admins = userServiceAgreementPairs.stream().map(
            PresentationServiceAgreementUserPair::getExternalUserId)
            .collect(Collectors.toList());

        List<GetUser> users = userManagementService
            .getUsersByExternalIds(admins);

        return users.stream()
            .collect(Collectors.toMap(GetUser::getExternalId, user -> user));
    }

    private List<BatchResponseItemExtended> filterInvalidPayloads(
        List<PresentationServiceAgreementUserPair> userServiceAgreementPairs,
        Map<String, GetUser> usersByExternalId,
        PresentationAction action) {
        return userServiceAgreementPairs.stream()
            .filter(
                userServiceAgreementPair -> !usersByExternalId
                    .containsKey(userServiceAgreementPair.getExternalUserId()))
            .map(userServiceAgreementPair -> new BatchResponseItemExtended()
                .withResourceId(userServiceAgreementPair.getExternalUserId())
                .withExternalServiceAgreementId(userServiceAgreementPair.getExternalServiceAgreementId())
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withAction(action)
                .withErrors(Collections.singletonList(INVALID_EXTERNAL_USER_ID)))
            .collect(Collectors.toList());
    }

    /**
     * Calls the presentation client with the prepared data.
     *
     * @param request           @return list of {@link BatchResponseItemExtended}
     * @param usersByExternalId
     * @param userType
     */
    private List<BatchResponseItemExtended> persist(
        PresentationServiceAgreementUsersUpdate request,
        Map<String, GetUser> usersByExternalId,
        UserType userType) {

        return batchResponseItemMapper.toExtendedPresentationList(
            modifyUsersAndAdminsInServiceAgreement.processBatch(userType, request, usersByExternalId));
    }

    private ExtendedResponseItem validateUser(PresentationServiceAgreementUserPair user) {
        List<String> errors = validator.validate(user).stream()
            .map(this::createErrorMessageFromViolation)
            .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            return new ExtendedResponseItem(user.getExternalUserId(), ItemStatusCode.HTTP_STATUS_BAD_REQUEST,
                errors, user.getExternalServiceAgreementId());
        }
        return new ExtendedResponseItem(user.getExternalUserId(), ItemStatusCode.HTTP_STATUS_OK,
            errors, user.getExternalServiceAgreementId());
    }

    private String createErrorMessageFromViolation(
        ConstraintViolation<PresentationServiceAgreementUserPair> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }
}
