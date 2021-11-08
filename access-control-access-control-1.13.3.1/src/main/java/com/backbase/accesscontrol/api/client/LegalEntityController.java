package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_012;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_021;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_045;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.client.rest.spec.api.LegalEntitiesApi;
import com.backbase.accesscontrol.client.rest.spec.model.GetServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantItemId;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityExternalDataItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.client.rest.spec.model.SearchSubEntitiesParameters;
import com.backbase.accesscontrol.dto.ExternalLegalEntitySearchParameters;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SegmentationLegalEntitiesSearchParameters;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.PermissionValidationService;
import com.backbase.accesscontrol.service.facades.LegalEntityFlowService;
import com.backbase.accesscontrol.service.facades.LegalEntityService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Legal Entities. The controller implements {@link LegalEntitiesApi} where all request mappings and
 * handler method contracts are defined.
 */
@RestController
@AllArgsConstructor
public class LegalEntityController implements LegalEntitiesApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityController.class);
    private static final String PAGINATION_ITEM_COUNT_HEADER = "X-Total-Count";

    private LegalEntityService legalEntityService;
    private LegalEntityFlowService legalEntityFlowService;

    private ParameterValidationService parameterValidationService;

    private AccessControlValidator accessControlValidator;
    private PermissionValidationService permissionValidationService;
    private UserContextUtil userContextUtil;
    private PayloadConverter payloadConverter;

    private ServiceAgreementIdProvider serviceAgreementProvider;
    private UserAccessPermissionCheckService userAccessPermissionCheckService;

    /**
     * Controller method for fetching a {@link List {@link LegalEntitiesGetResponseBody}} LegalEntities can be queried,
     * if present by their Parent ID.
     *
     * @param parentEntityId Legal Entity's Parent ID
     * @return {@link List {@link LegalEntitiesGetResponseBody}}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<LegalEntityItem>> getLegalEntities(
        @RequestParam(value = "parentEntityId", required = false) String parentEntityId) {

        LOGGER.info("Fetching a list of Legal Entities with parentEntityId {}", parentEntityId);

        validateAccessToLegalEntity(parentEntityId);

        return new ResponseEntity<>(payloadConverter
            .convertListPayload(legalEntityService.getLegalEntities(parentEntityId), LegalEntityItem.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for creating Legal Entity with internal parent id.
     *
     * @param requestBody of type {@link PresentationCreateLegalEntityItemPostRequestBody}
     * @return responseBody of type {@link LegalEntitiesPostResponseBody}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_CREATE + "'})")
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.LEGAL_ENTITY_CREATE)
    public ResponseEntity<LegalEntityItemId> postLegalEntities(
        @RequestBody LegalEntityCreateItem requestBody) {

        PresentationCreateLegalEntityItemPostRequestBody postRequestBody = payloadConverter
            .convertAndValidate(requestBody, PresentationCreateLegalEntityItemPostRequestBody.class);

        permissionValidationService
            .validateAccessToLegalEntityResource(postRequestBody.getParentInternalId(),
                AccessResourceType.NONE);
        LOGGER.info("Creating legal entity {}", requestBody);

        return new ResponseEntity<>(payloadConverter
            .convert(legalEntityFlowService.createLegalEntityWithInternalParentIdFlow(postRequestBody),
                LegalEntityItemId.class), HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_CREATE + "'})")
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.LEGAL_ENTITY_CREATE_AS_PARTICIPANT)
    public ResponseEntity<LegalEntityAsParticipantItemId> postLegalEntitiesAsParticipant(
        @RequestBody LegalEntityAsParticipantCreateItem requestBody) {

        String serviceAgreementId = userContextUtil.getServiceAgreementId();
        String action = requestBody.getParticipantOf().getExistingCustomServiceAgreement() == null
            ? ResourceAndFunctionNameConstants.PRIVILEGE_CREATE
            : ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;

        userAccessPermissionCheckService
            .checkUserPermission(userContextUtil.getUserContextDetails().getInternalUserId(),
                serviceAgreementId, ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME,
                ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME,
                action);

        LegalEntityAsParticipantPostRequestBody postRequestBody =
            payloadConverter.convertAndValidate(requestBody, LegalEntityAsParticipantPostRequestBody.class);

        // Set LE parent id to SA creator LE if not supplied
        Optional<ServiceAgreementItem> sa = serviceAgreementProvider.getServiceAgreementById(serviceAgreementId);
        String parentId = postRequestBody.getLegalEntityParentId();
        postRequestBody.setLegalEntityParentId(
            parentId == null && sa.isPresent() ? sa.get().getCreatorLegalEntity() : parentId);

        validateAccessToLegalEntity(postRequestBody.getLegalEntityParentId());
        validateServiceAgreementIsMaster(sa);

        LOGGER.info("Creating legal entity and assigning to service agreement: {}", postRequestBody);

        return new ResponseEntity<>(payloadConverter.convert(
            legalEntityFlowService.createLegalEntityAsParticipant(postRequestBody),
            LegalEntityAsParticipantItemId.class), HttpStatus.CREATED);
    }

    /**
     * Controller method for fetching {@link LegalEntityByIdGetResponseBody}.
     *
     * @param legalEntityId internal id og the legal entity
     * @return responseBody of type {@link LegalEntityByIdGetResponseBody}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<LegalEntityItem> getLegalEntityById(
        @PathVariable("legalEntityId") String legalEntityId) {
        LOGGER.info("Fetching the Legal Entity with legalEntityId {}", legalEntityId);

        return new ResponseEntity<>(payloadConverter
            .convert(legalEntityService.getLegalEntityById(legalEntityId), LegalEntityItem.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link List {@link SubEntitiesPostResponseBody}} LegalEntities can be queried,
     * if present by Legal Entity ID.
     *
     * @param parameters request body which contain parent legal entity id, ids of legal entities which should be
     *                   excluded, pagination parameters (from, size, cursor) and search parameter (query)
     * @return {@link List {@link SubEntitiesPostResponseBody}}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<LegalEntityItemBase>> postSubEntities(
        @RequestBody SearchSubEntitiesParameters parameters) {

        LOGGER.info("Fetching a list of all Legal Entity's children of the logged User or parent entity parameter.");

        String parentEntityId = null;
        Set<String> excludeIds = Collections.emptySet();
        Integer from = 0;
        Integer size = 10;
        String cursor = null;
        String query = null;

        if (nonNull(parameters)) {
            com.backbase.presentation.legalentity.rest.spec.v2.legalentities
                .SearchSubEntitiesParameters searchSubEntitiesParameters = payloadConverter
                .convertAndValidate(parameters,
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SearchSubEntitiesParameters.class);

            parentEntityId = searchSubEntitiesParameters.getParentEntityId();
            excludeIds = removeNullElements(searchSubEntitiesParameters.getExcludeIds());

            validateIfOneOfFromOrSizeIsNullAndOtherNot(searchSubEntitiesParameters.getFrom(),
                searchSubEntitiesParameters.getSize());
            if (nonNull(searchSubEntitiesParameters.getFrom())) {
                from = searchSubEntitiesParameters.getFrom();
            }
            if (nonNull(searchSubEntitiesParameters.getSize())) {
                size = searchSubEntitiesParameters.getSize();
            }
            cursor = searchSubEntitiesParameters.getCursor();
            query = searchSubEntitiesParameters.getQuery();
        }

        validateFromAndSizeParameters(from, size);

        query = parameterValidationService.validateQueryParameter(query);
        parameterValidationService.validateFromAndSizeParameter(from, size);

        RecordsDto<SubEntitiesPostResponseBody> subLegalEntitiesDto = legalEntityFlowService
            .getSubLegalEntities(new GetLegalEntitiesRequestDto(parentEntityId, excludeIds, cursor, from, size, query));

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(subLegalEntitiesDto.getTotalNumberOfRecords()));

        return new ResponseEntity<>(payloadConverter.convertListPayload(
            subLegalEntitiesDto.getRecords(), LegalEntityItemBase.class), headers, HttpStatus.OK);
    }

    /**
     * Controller method for fetching {@link MasterServiceAgreementGetResponseBody}.
     *
     * @param legalEntityId id of the legal entity
     * @return responseBody of type {@link MasterServiceAgreementGetResponseBody}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<GetServiceAgreement> getMasterServiceAgreement(
        @PathVariable("legalEntityId") String legalEntityId) {
        LOGGER.info("Getting Master Service Agreement for legalEntityId {}", legalEntityId);

        return new ResponseEntity<>(payloadConverter
            .convert(legalEntityService.getMasterServiceAgreement(legalEntityId), GetServiceAgreement.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for fetching {@link LegalEntityForUserGetResponseBody}.
     *
     * @return {@link LegalEntityForUserGetResponseBody}
     */
    @Override
    public ResponseEntity<LegalEntityItem> getLegalEntityForUser() {
        LOGGER.info("Getting Legal Entity for current user");

        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.getLegalEntityForCurrentUser(), LegalEntityItem.class),
            HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link LegalEntityByExternalIdGetResponseBody}.
     *
     * @param externalId - external id of wanted legal entity
     * @return {@link LegalEntityByExternalIdGetResponseBody}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<LegalEntityItemBase> getLegalEntityByExternalId(
        @PathVariable("externalId") String externalId) {
        LOGGER.info("Fetching the Legal Entity with externalId {}", externalId);

        return new ResponseEntity<>(
            payloadConverter.convert(legalEntityService.getLegalEntityByExternalId(externalId),
                LegalEntityItemBase.class), HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link List {@link SegmentationGetResponseBody}}.
     *
     * @param query            - name or external ID for search
     * @param businessFunction - name of business function
     * @param privilege        - name of privilege
     * @param from             - from parameter used for pagination
     * @param size             - size used for pagination
     * @return {@link List {@link SegmentationGetResponseBody}}
     */
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    @Override
    public ResponseEntity<List<LegalEntityItem>> getSegmentation(
        @RequestParam(value = "businessFunction", required = true) String businessFunction,
        @RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "privilege", required = false) String privilege,
        @RequestParam(value = "from", required = false, defaultValue = "0") Integer from,
        @RequestParam(value = "cursor", required = false, defaultValue = "") String cursor,
        @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        LOGGER.info("Fetching a list of Legal Entities that User has access to with query {}, "
            + "businessFunction {}, privilege {}, size {}, "
            + "from {}, ", query, businessFunction, privilege, size, from);
        parameterValidationService.validateFromAndSizeParameter(from, size);

        UserContextDetailsDto userContextDetails = userContextUtil.getUserContextDetails();
        String legalEntityId = userContextDetails.getLegalEntityId();
        String internalUserId = userContextDetails.getInternalUserId();
        SegmentationLegalEntitiesSearchParameters segmentationLegalEntitiesSearchParameters =
            new SegmentationLegalEntitiesSearchParameters(query, businessFunction, internalUserId, privilege,
                from, cursor, size);

        String serviceAgreementId = userContextUtil.getServiceAgreementId();

        if (Strings.isNullOrEmpty(serviceAgreementId)) {
            segmentationLegalEntitiesSearchParameters.setLegalEntityId(legalEntityId);
        } else {
            segmentationLegalEntitiesSearchParameters.setServiceAgreementId(serviceAgreementId);
        }

        RecordsDto<SegmentationGetResponseBody> data = legalEntityFlowService
            .getSegmentationLegalEntity(segmentationLegalEntitiesSearchParameters);

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(data.getTotalNumberOfRecords()));
        return new ResponseEntity<>(payloadConverter.convertListPayload(data.getRecords(), LegalEntityItem.class),
            headers, HttpStatus.OK);
    }

    /**
     * Controller method for fetching a {@link List {@link LegalEntityExternalData}}.
     *
     * @param field - used for search
     * @param term  - used for search
     * @param from  - from parameter used for pagination
     * @param size  - size used for pagination
     * @return {@link List {@link LegalEntityExternalData}}
     */
    @Override
    @PreAuthorize("checkPermission('" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_RESOURCE_NAME + "', "
        + "'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME + "', "
        + "{'" + ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_PRIVILEGE_VIEW + "'})")
    public ResponseEntity<List<LegalEntityExternalDataItem>> getLegalEntityExternalData(
        @RequestParam(value = "field", required = false) String field,
        @RequestParam(value = "term", required = false) String term,
        @RequestParam(value = "from", required = false) Integer from,
        @RequestParam(value = "cursor", required = false) String cursor,
        @RequestParam(value = "size", required = false) Integer size) {

        parameterValidationService.validateFromAndSizeParameter(from, size);

        RecordsDto<LegalEntityExternalData> data =
            legalEntityFlowService.getExternalLegalEntityData(
                new ExternalLegalEntitySearchParameters(
                    field,
                    term,
                    from,
                    cursor,
                    size
                )
            );

        HttpHeaders headers = new HttpHeaders();
        headers.add(PAGINATION_ITEM_COUNT_HEADER, String.valueOf(data.getTotalNumberOfRecords()));

        return new ResponseEntity<>(
            payloadConverter.convertListPayload(data.getRecords(), LegalEntityExternalDataItem.class), headers,
            HttpStatus.OK);
    }

    private void validateAccessToLegalEntity(String legalEntityId) {
        if (legalEntityId != null
            && accessControlValidator.userHasNoAccessToEntitlementResource(legalEntityId, AccessResourceType.NONE)) {

            LOGGER.warn(ERR_AG_013.getErrorMessage());
            throw getForbiddenException(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode());
        }
    }

    private void validateServiceAgreementIsMaster(Optional<ServiceAgreementItem> sa) {
        if (sa.isPresent() && Boolean.FALSE.equals(sa.get().getIsMaster())) {
            LOGGER.warn(ERR_LE_021.getErrorMessage());
            throw getForbiddenException(ERR_LE_021.getErrorMessage(), ERR_LE_021.getErrorCode());
        }
    }

    private void validateFromAndSizeParameters(Integer from, Integer size) {
        LOGGER.info("Validating from and size query parameters");
        if (from != null && size != null && size >= 1000) {
            LOGGER.warn("Size param with value {} needs to be lower than 1000", size);
            throw getBadRequestException(ERR_ACQ_045.getErrorMessage(), ERR_ACQ_045.getErrorCode());
        }
    }

    private void validateIfOneOfFromOrSizeIsNullAndOtherNot(Integer from, Integer size) {
        if ((isNull(from) && !isNull(size)) || (!isNull(from) && isNull(size))) {
            LOGGER.warn("Invalid query parameters, one of size or from is null");
            throw getBadRequestException(ERR_LE_012.getErrorMessage(), ERR_LE_012.getErrorCode());
        }
    }

    private Set<String> removeNullElements(Collection<String> excludeIds) {
        return Optional.ofNullable(excludeIds)
            .orElseGet(Collections::emptySet)
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
