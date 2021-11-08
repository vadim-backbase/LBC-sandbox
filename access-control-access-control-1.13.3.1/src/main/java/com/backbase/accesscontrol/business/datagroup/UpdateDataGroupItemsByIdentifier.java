package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_GROUP_VALIDATE;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_081;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_099;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.HTTP_STATUS_OK;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemExternalIdConverterService;
import com.backbase.accesscontrol.business.serviceagreement.GetServiceAgreementByExternalId;
import com.backbase.accesscontrol.dto.DataItemsValidatable;
import com.backbase.accesscontrol.dto.ServiceAgreementIdAndDataGroupType;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.routes.datagroup.ValidateDataGroupRouteProxy;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.batch.datagroup.UpdateDataGroupItemsByIdentifierPersistence;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.Produce;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Business consumer for updating data group items. This class is a business process component of the access group
 * presentation service, communicating with the P&P services.
 */
@Service
public class UpdateDataGroupItemsByIdentifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataGroupItemsByIdentifier.class);

    @Value("${backbase.data-group.validation.enabled}")
    private boolean dataValidationEnabled;
    @Produce(value = DIRECT_BUSINESS_GROUP_VALIDATE)
    private ValidateDataGroupRouteProxy validateDataGroup;
    private DataGroupService dataGroupService;
    private Validator validator;
    private GetServiceAgreementByExternalId getServiceAgreementByExternalId;
    private Map<String, DataItemExternalIdConverterService> dataItemServices;
    private UpdateDataGroupItemsByIdentifierPersistence updateDataGroupItemsByIdentifierPersistence;
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper;

    /**
     * Constructor.
     */
    public UpdateDataGroupItemsByIdentifier(
        DataGroupService dataGroupService,
        Validator validator,
        GetServiceAgreementByExternalId getServiceAgreementByExternalId,
        List<DataItemExternalIdConverterService> dataItemExternalIdConverterServices,
        UpdateDataGroupItemsByIdentifierPersistence updateDataGroupItemsByIdentifierPersistence,
        BatchResponseItemExtendedMapper batchResponseItemExtendedMapper
    ) {
        this.dataGroupService = dataGroupService;
        this.validator = validator;
        this.getServiceAgreementByExternalId = getServiceAgreementByExternalId;
        this.dataItemServices = dataItemExternalIdConverterServices.stream()
            .collect(Collectors.toMap(DataItemExternalIdConverterService::getType, item -> item));
        this.updateDataGroupItemsByIdentifierPersistence = updateDataGroupItemsByIdentifierPersistence;
        this.batchResponseItemExtendedMapper = batchResponseItemExtendedMapper;
    }

    /**
     * Validates data group type.
     */
    @Consume(value = EndpointConstants.DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_VALIDATE)
    public void validateDataGroupType(@Body InternalRequest<List<PresentationDataGroupItemPutRequestBody>> request,
        @Header("responseContainer") List<BatchResponseItemExtended> responseContainer,
        @Header("validResponses") Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses,
        @Header("internalDataItemsIdsByTypeAndExternalId") Map<String, Map<String, String>>
            internalDataItemsIdsByTypeAndExternalId) {

        responseContainer.addAll(request.getData()
            .stream()
            .map(dataGroupsPutRequestBody ->
                getBatchResponseItemExtended(dataGroupsPutRequestBody,
                    new BatchResponseItemExtended()
                        .withStatus(HTTP_STATUS_OK)))
            .collect(Collectors.toList()));

        validResponses
            .putAll(validateBatchRequestItems(request, responseContainer, internalDataItemsIdsByTypeAndExternalId));

    }

    /**
     * Method that listens on the direct:updateDataGroupItemsBatchRequestedInternal endpoint and forwards the request to
     * the persistence service.
     *
     * @param request                                 Internal Request of {@link PresentationDataGroupItemPutRequestBody}
     *                                                list type to be send by the client
     * @param responseContainer                       list of {@link BatchResponseItemExtended}
     * @param validResponses                          valid responses
     * @param internalDataItemsIdsByTypeAndExternalId internal data item ids by type and external id
     * @return Internal request of {@link BatchResponseItemExtended} list
     */
    @Consume(value = EndpointConstants.DIRECT_UPDATE_DATA_GROUP_ITEMS_BY_IDENTIFIER_PERSIST)
    public InternalRequest<List<BatchResponseItemExtended>> updateDataGroupItemsByIdentifier(
        @Body InternalRequest<List<PresentationDataGroupItemPutRequestBody>> request,
        @Header("responseContainer") List<BatchResponseItemExtended> responseContainer,
        @Header("validResponses") Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses,
        @Header("internalDataItemsIdsByTypeAndExternalId") Map<String, Map<String, String>>
            internalDataItemsIdsByTypeAndExternalId) {

        LOGGER.info("Trying to update data group items by Identifier");

        callDataGroupPersistence(responseContainer, internalDataItemsIdsByTypeAndExternalId, validResponses);

        return getInternalRequest(responseContainer, request.getInternalRequestContext());
    }

    private LinkedHashMap<Integer, PresentationDataGroupItemPutRequestBody> validateBatchRequestItems(
        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> request,
        List<BatchResponseItemExtended> responseContainer,
        Map<String, Map<String, String>> validatedInternalFromExternalIds) {

        Map<ServiceAgreementIdAndDataGroupType, Set<String>> externalToInternal = new HashMap<>();

        LinkedHashMap<Integer, PresentationDataGroupItemPutRequestBody> valid = new LinkedHashMap<>();

        int elementCounter = 0;
        for (PresentationDataGroupItemPutRequestBody dg : request.getData()) {
            if (dg.getDataItems().stream().allMatch(item -> item != null && item.getInternalIdIdentifier() != null)) {
                continue;
            }
            try {
                String serviceAgreementId = getServiceAgreementId(dg, request.getInternalRequestContext());
                Set<String> externalIds = externalToInternal
                    .get(new ServiceAgreementIdAndDataGroupType(serviceAgreementId, dg.getType()));
                if (Objects.nonNull(externalIds)) {
                    externalIds
                        .addAll(dg.getDataItems().stream()
                            .filter(dataGroup -> Objects.nonNull(dataGroup.getExternalIdIdentifier()))
                            .map(PresentationItemIdentifier::getExternalIdIdentifier).collect(Collectors.toSet()));
                } else {
                    externalToInternal.put(new ServiceAgreementIdAndDataGroupType(serviceAgreementId, dg.getType()),
                        dg.getDataItems().stream()
                            .filter(dataGroup -> Objects.nonNull(dataGroup.getExternalIdIdentifier()))
                            .map(PresentationItemIdentifier::getExternalIdIdentifier).collect(Collectors.toSet()));
                }
            } catch (NotFoundException nfe) {

                responseContainer.set(elementCounter, createResponseForNotFoundDataGroup(dg, nfe));
            }
        }
        Map<String, Map<String, String>> externalToInternalMap = getInternalDataItemsIdsByTypeAndExternalId(
            externalToInternal);
        validatedInternalFromExternalIds.putAll(externalToInternalMap);
        populateUnsuccessfulResponseItems(request, responseContainer, valid, elementCounter, externalToInternalMap);

        LOGGER.info("Processing valid items {}", valid);
        return valid;
    }

    private void populateUnsuccessfulResponseItems(
        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> request,
        List<BatchResponseItemExtended> responseContainer,
        LinkedHashMap<Integer, PresentationDataGroupItemPutRequestBody> valid, int elementCounter,
        Map<String, Map<String, String>> externalToInternalMap) {
        for (PresentationDataGroupItemPutRequestBody dg : request.getData()) {
            BatchResponseItemExtended prevResponse = responseContainer.get(elementCounter);
            if (prevResponse.getStatus() == HTTP_STATUS_OK) {
                if (!areIdentifiersAndExternalIdsCorrect(dg)) {
                    responseContainer.set(elementCounter, createResponseForInvalidIdentifier(dg, ERR_AG_081));
                } else if (!allExternalIdsArePresent(dg, externalToInternalMap)) {
                    responseContainer.set(elementCounter, createResponseForInvalidIdentifier(dg, ERR_AG_099));
                } else {
                    ArrayList<String> dataItemValidationErrors = validateDataItems(request, dg,
                        externalToInternalMap.getOrDefault(dg.getType(), new HashMap<>()));
                    if (dataItemValidationErrors.isEmpty()) {
                        valid.put(elementCounter, dg);
                    } else {
                        responseContainer.set(
                            elementCounter,
                            getBatchResponseItemExtended(dg, new BatchResponseItemExtended()
                                .withStatus(HTTP_STATUS_BAD_REQUEST)
                                .withErrors(dataItemValidationErrors))
                        );
                    }
                }
            }
            elementCounter++;
        }
    }

    private BatchResponseItemExtended createResponseForNotFoundDataGroup(PresentationDataGroupItemPutRequestBody dg,
        NotFoundException nfe) {
        BatchResponseItemExtended itemExtended = new BatchResponseItemExtended()
            .withAction(dg.getAction())
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND)
            .withErrors(Arrays.asList(nfe.getErrors().get(0).getMessage()));
        if (Objects.nonNull(dg.getDataGroupIdentifier().getIdIdentifier())) {
            itemExtended.setResourceId(dg.getDataGroupIdentifier().getIdIdentifier());
        } else {
            itemExtended.setResourceId(dg.getDataGroupIdentifier().getNameIdentifier().getName());
            itemExtended.setExternalServiceAgreementId(
                dg.getDataGroupIdentifier().getNameIdentifier().getExternalServiceAgreementId());
        }
        return itemExtended;
    }

    private ArrayList<String> validateDataItems(InternalRequest<List<PresentationDataGroupItemPutRequestBody>> request,
        PresentationDataGroupItemPutRequestBody dataGroup, Map<String, String> externalToInternalMap) {

        if (dataValidationEnabled) {
            try {
                validateDataGroup
                    .validate(
                        getInternalRequest(
                            new DataItemsValidatable(
                                dataGroup.getType(),
                                getInternalIds(dataGroup, externalToInternalMap),
                                getServiceAgreementId(dataGroup, request.getInternalRequestContext())
                            ),
                            request.getInternalRequestContext()
                        ));
            } catch (NotFoundException exception) {
                return newArrayList(exception.getMessage());
            } catch (BadRequestException exception) {
                return newArrayList(exception.getErrors().get(0).getMessage());
            }
        }
        return new ArrayList<>();
    }

    private List<String> getInternalIds(PresentationDataGroupItemPutRequestBody dataGroup,
        Map<String, String> externalToInternalMap) {
        return dataGroup.getDataItems().stream()
            .map(dataItemIdentifier -> {
                if (nonNull(dataItemIdentifier.getExternalIdIdentifier())) {
                    return externalToInternalMap.get(dataItemIdentifier.getExternalIdIdentifier());
                }

                return dataItemIdentifier.getInternalIdIdentifier();
            })
            .collect(toList());
    }

    private void callDataGroupPersistence(List<BatchResponseItemExtended> responseContainer,
        Map<String, Map<String, String>> externalToInternal,
        Map<Integer, PresentationDataGroupItemPutRequestBody> valid) {
        List<BatchResponseItemExtended> response;
        if (valid.isEmpty()) {
            response = Collections.emptyList();
        } else {
            Map<Integer, PresentationDataGroupItemPutRequestBody> dataItems
                = replaceExternalItemsIdsWithInternal(externalToInternal, valid);

            response = batchResponseItemExtendedMapper.mapList(
                updateDataGroupItemsByIdentifierPersistence.processBatchItems(newArrayList(dataItems.values())));
        }

        List<Integer> validIndexes = new ArrayList<>(valid.keySet());
        mergeResponses(response, responseContainer, validIndexes);
    }

    private String getServiceAgreementId(
        PresentationDataGroupItemPutRequestBody presentationDataGroupItemPutRequestBody,
        InternalRequestContext internalRequestContext) {
        String serviceAgreementId = null;
        PresentationIdentifier identifier = presentationDataGroupItemPutRequestBody.getDataGroupIdentifier();
        if (identifier != null) {
            if (isNotEmpty(identifier.getIdIdentifier())) {
                serviceAgreementId = dataGroupService.getById(identifier.getIdIdentifier())
                    .getServiceAgreementId();
            } else {
                NameIdentifier nameIdentifier = identifier.getNameIdentifier();
                if (!ObjectUtils.isEmpty(nameIdentifier)) {
                    ServiceAgreementExternalIdGetResponseBody serviceAgreementExternalIdGetResponseBody =
                        getServiceAgreementByExternalId
                            .getServiceAgreementByExternalId(getVoidInternalRequest(internalRequestContext),
                                nameIdentifier.getExternalServiceAgreementId()).getData();
                    serviceAgreementId = serviceAgreementExternalIdGetResponseBody.getId();
                }
            }
        }
        return serviceAgreementId;
    }

    //          type        exId    intId
    private Map<String, Map<String, String>> getInternalDataItemsIdsByTypeAndExternalId(
        Map<ServiceAgreementIdAndDataGroupType, Set<String>> externalIdsByType) {

        return externalIdsByType.entrySet().stream()
            .collect(toMap(
                entry -> entry.getKey().getDataGroupType(),
                entry -> {
                    if (dataItemServices.containsKey(entry.getKey().getDataGroupType())) {
                        if (entry.getValue().isEmpty()) {
                            return new HashMap<>();
                        }

                        return dataItemServices.get(entry.getKey().getDataGroupType())
                            .mapExternalToInternalIds(entry.getValue(), entry.getKey().getServiceAgreementId())
                            .entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
                                .collect(Collectors.joining(","))));
                    } else {
                        LOGGER.error("Data Item converter for {} does not exist", entry.getKey().getDataGroupType());
                        throw getInternalServerErrorException(
                            String.format("Data Item converter for %s does not exist",
                                entry.getKey().getDataGroupType()));
                    }
                },
                (existing, replacement) -> {
                    existing.putAll(replacement);
                    return existing;
                }
            ));
    }

    private Map<Integer, PresentationDataGroupItemPutRequestBody> replaceExternalItemsIdsWithInternal(
        Map<String, Map<String, String>> externalToInternal,
        Map<Integer, PresentationDataGroupItemPutRequestBody> valid) {

        valid.values()
            .forEach(item -> {
                List<PresentationItemIdentifier> dataItems = item.getDataItems();
                dataItems.forEach(dataItem -> {
                    if (nonNull(dataItem.getExternalIdIdentifier())) {
                        dataItem.setInternalIdIdentifier(
                            externalToInternal
                                .get(item.getType())
                                .get(dataItem.getExternalIdIdentifier()));
                        dataItem.setExternalIdIdentifier(null);
                    }
                });
            });
        return valid;
    }

    private void mergeResponses(List<BatchResponseItemExtended> processedData,
        List<BatchResponseItemExtended> responseData, List<Integer> gapIndexes) {
        if (gapIndexes.size() != processedData.size()) {
            LOGGER.info("Processed and gap indexes are mismatching");
            throw getInternalServerErrorException("Unexpected response state");
        }
        int elementCounter = 0;
        for (BatchResponseItemExtended dg : processedData) {
            responseData.set(gapIndexes.get(elementCounter), dg);
            elementCounter++;
        }
    }

    private BatchResponseItemExtended createResponseForInvalidIdentifier(
        PresentationDataGroupItemPutRequestBody dataGroupItemsRequest,
        AccessGroupErrorCodes accessGroupErrorCodes) {
        String resourceId = null;
        String externalServiceAgreementId = null;
        PresentationIdentifier identifier = dataGroupItemsRequest.getDataGroupIdentifier();
        if (identifier != null) {

            if (!StringUtils.isEmpty(identifier.getIdIdentifier())) {
                resourceId = identifier.getIdIdentifier();
            } else {
                NameIdentifier nameIdentifier = identifier.getNameIdentifier();
                if (!isEmpty(nameIdentifier)) {
                    resourceId = nameIdentifier.getName();
                    externalServiceAgreementId = nameIdentifier.getExternalServiceAgreementId();
                }
            }
        }
        return new BatchResponseItemExtended()
            .withResourceId(resourceId)
            .withAction(dataGroupItemsRequest.getAction())
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withStatus(HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList(accessGroupErrorCodes.getErrorMessage()));

    }

    private boolean areIdentifiersAndExternalIdsCorrect(PresentationDataGroupItemPutRequestBody request) {
        return request.getDataGroupIdentifier() != null
            && hasSingleIdentifier(request)
            && validator.validate(request.getDataGroupIdentifier()).isEmpty()
            && onlyInternalOrExternalIdentifiersAreSet(request);
    }

    private boolean allExternalIdsArePresent(PresentationDataGroupItemPutRequestBody request,
        Map<String, Map<String, String>> externalToInternal) {
        return request.getDataItems().stream()
            .filter(item -> nonNull(item.getExternalIdIdentifier()))
            .allMatch(item -> externalToInternal.get(request.getType()).containsKey(item.getExternalIdIdentifier()));
    }

    private boolean hasSingleIdentifier(PresentationDataGroupItemPutRequestBody request) {
        return request.getDataGroupIdentifier().getNameIdentifier() != null
            ^ request.getDataGroupIdentifier().getIdIdentifier() != null;
    }

    private boolean onlyInternalOrExternalIdentifiersAreSet(PresentationDataGroupItemPutRequestBody request) {
        return request.getDataItems().stream().allMatch(item -> item != null && item.getInternalIdIdentifier() != null)
            || request.getDataItems().stream().allMatch(item -> item != null && item.getExternalIdIdentifier() != null);
    }

    private BatchResponseItemExtended getBatchResponseItemExtended(
        PresentationDataGroupItemPutRequestBody presentationDataGroupPutRequestBody,
        BatchResponseItemExtended response) {

        PresentationIdentifier identifier = presentationDataGroupPutRequestBody.getDataGroupIdentifier();
        response.setAction(presentationDataGroupPutRequestBody.getAction());

        if (!hasSingleIdentifier(presentationDataGroupPutRequestBody)) {
            response.setStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
            response.setErrors(newArrayList(ERR_AG_081.getErrorMessage()));
        }

        if (identifier != null) {
            if (!StringUtils.isEmpty(identifier.getIdIdentifier())) {
                response.setResourceId(identifier.getIdIdentifier());
            } else {
                NameIdentifier nameIdentifier = identifier.getNameIdentifier();
                if (!isEmpty(nameIdentifier)) {
                    response.setResourceId(nameIdentifier.getName());
                    response.setExternalServiceAgreementId(nameIdentifier.getExternalServiceAgreementId());
                }
            }
        }
        return response;
    }

}
