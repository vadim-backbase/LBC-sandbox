package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.Parameters.numberOfParameters;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_074;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemExternalIdConverterService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.mappers.SearchDataGroupsMapper;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.LegalEntityIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SearchDataGroups {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchDataGroups.class);

    private Map<String, DataItemExternalIdConverterService> dataItemServices;
    private SearchDataGroupsMapper searchDataGroupsMapper;
    private ValidationConfig validationConfig;
    private DataGroupJpaRepository dataGroupJpaRepository;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;


    /**
     * SearchDataGroups autowire constructor.
     *
     * @param dataItemExternalIdConverterServices - List of data item services
     * @param searchDataGroupsMapper              - domain model mapper
     * @param validationConfig                    - validation service
     * @param dataGroupJpaRepository              - data group repository
     * @param persistenceServiceAgreementService  - service agreement service
     */
    public SearchDataGroups(
        List<DataItemExternalIdConverterService> dataItemExternalIdConverterServices,
        SearchDataGroupsMapper searchDataGroupsMapper,
        ValidationConfig validationConfig, DataGroupJpaRepository dataGroupJpaRepository,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {

        this.dataItemServices = dataItemExternalIdConverterServices.stream()
            .collect(Collectors.toMap(DataItemExternalIdConverterService::getType, item -> item));
        this.validationConfig = validationConfig;
        this.searchDataGroupsMapper = searchDataGroupsMapper;
        this.dataGroupJpaRepository = dataGroupJpaRepository;
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * Business method for searching data groups.
     *
     * @param request - filter body
     * @param type    - requested data group type
     * @return list of data groups grouped by service agreement.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_SEARCH_DATA_GROUPS)
    public InternalRequest<List<PresentationServiceAgreementWithDataGroups>> searchDataGroups(
        @Body InternalRequest<PresentationGetDataGroupsRequest> request, @Header("type") String type) {

        PresentationGetDataGroupsRequest requestData = request.getData();
        validateRequest(requestData, type);

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem(type, getServiceAgreementId(requestData),
                getServiceAgreementName(requestData), getServiceAgreementExternalId(requestData),
                getDataItemId(requestData, type), getLEExternalId(requestData), getShares(requestData));

        Map<String, PresentationServiceAgreementWithDataGroups> mapServiceAgreements = new HashMap<>();

        dataGroups.forEach(dataGroup -> {
            ServiceAgreement serviceAgreement = dataGroup.getServiceAgreement();

            if (!mapServiceAgreements.containsKey(serviceAgreement.getId())) {
                PresentationServiceAgreementWithDataGroups presentationModel =
                    new PresentationServiceAgreementWithDataGroups()
                        .withServiceAgreement(searchDataGroupsMapper.toPresentation(serviceAgreement));
                presentationModel.getDataGroups().add(searchDataGroupsMapper.toPresentation(dataGroup));
                mapServiceAgreements.put(serviceAgreement.getId(), presentationModel);
            } else {
                mapServiceAgreements.get(serviceAgreement.getId()).getDataGroups()
                    .add(searchDataGroupsMapper.toPresentation(dataGroup));
            }
        });

        return getInternalRequest(new ArrayList<>(mapServiceAgreements.values()), request.getInternalRequestContext());
    }

    private String getDataItemId(PresentationGetDataGroupsRequest request, String type) {
        return Optional
            .ofNullable(request.getDataItemIdentifier())
            .map(identifier -> Optional.ofNullable(identifier.getInternalIdIdentifier())
                .orElseGet(() -> nonNull(identifier.getExternalIdIdentifier()) ?
                    getInternalId(request, type, identifier)
                    : null))
            .orElse(null);
    }

    private String getLEExternalId(PresentationGetDataGroupsRequest request) {
        return Optional
                .ofNullable(request.getLegalEntityIdentifier())
                .map(identifier -> identifier.getExternalIdIdentifier())
                .orElse(null);
    }

    private String getInternalId(PresentationGetDataGroupsRequest request, String type,
        PresentationItemIdentifier identifier) {
        List<String> internalIds = dataItemServices.get(type)
            .getInternalId(identifier.getExternalIdIdentifier(), getServiceAgreementIdFromRequest(request));

        if (internalIds.size() != 1) {
            LOGGER.warn("Item external identifier is not unique, got {} items", internalIds.size());
            throw getBadRequestException(ERR_ACQ_074.getErrorMessage(), ERR_ACQ_074.getErrorCode());
        }
        return internalIds.get(0);
    }

    private String getServiceAgreementIdFromRequest(PresentationGetDataGroupsRequest request) {
        if (isNull(request.getServiceAgreementIdentifier())) {
            return null;
        }
        if (isNotEmpty(request.getServiceAgreementIdentifier().getIdIdentifier())) {
            return request.getServiceAgreementIdentifier().getIdIdentifier();
        } else if (isNotEmpty(request.getServiceAgreementIdentifier().getExternalIdIdentifier())) {
            return persistenceServiceAgreementService
                .getServiceAgreementByExternalId(
                    request.getServiceAgreementIdentifier().getExternalIdIdentifier()).getId();
        } else {
            return persistenceServiceAgreementService
                .getServiceAgreementsByNameIdentifier(request.getServiceAgreementIdentifier().getNameIdentifier())
                .getId();
        }
    }

    private String getServiceAgreementExternalId(PresentationGetDataGroupsRequest request) {
        return Optional
            .ofNullable(request.getServiceAgreementIdentifier())
            .map(PresentationServiceAgreementIdentifier::getExternalIdIdentifier).orElse(null);
    }

    private String getServiceAgreementName(PresentationGetDataGroupsRequest request) {
        return Optional
            .ofNullable(request.getServiceAgreementIdentifier())
            .map(PresentationServiceAgreementIdentifier::getNameIdentifier).orElse(null);
    }

    private String getServiceAgreementId(PresentationGetDataGroupsRequest request) {
        return Optional
            .ofNullable(request.getServiceAgreementIdentifier())
            .map(PresentationServiceAgreementIdentifier::getIdIdentifier).orElse(null);
    }

    private void validateRequest(PresentationGetDataGroupsRequest request, String type) {

        validationConfig.validateDataGroupType(type);

        PresentationServiceAgreementIdentifier serviceAgreementIdentifier = request
            .getServiceAgreementIdentifier();
        PresentationItemIdentifier dataItemIdentifier = request.getDataItemIdentifier();

        LegalEntityIdentifier legalEntityIdentifier = request.getLegalEntityIdentifier();

        if ((isNull(serviceAgreementIdentifier) || numberOfParameters(
            serviceAgreementIdentifier.getExternalIdIdentifier(),
            serviceAgreementIdentifier.getIdIdentifier(),
            serviceAgreementIdentifier.getNameIdentifier()) != 1)
            && (isNull(dataItemIdentifier) || numberOfParameters(dataItemIdentifier.getInternalIdIdentifier(),
            dataItemIdentifier.getExternalIdIdentifier()) != 1)
            && (isNull(legalEntityIdentifier) || numberOfParameters(legalEntityIdentifier.getExternalIdIdentifier()) != 1)
           ) {

            LOGGER.warn(ERR_AG_098.getErrorMessage());
            throw getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode());
        }
    }

    private SharesEnum getShares(PresentationGetDataGroupsRequest request) {
        return Optional
            .ofNullable(request.getLegalEntityIdentifier())
            .map(LegalEntityIdentifier::getShares).orElse(SharesEnum.ACCOUNTS);
    }
}
