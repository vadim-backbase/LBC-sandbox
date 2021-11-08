package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.backbase.accesscontrol.business.service.ArrangementsService;
import com.backbase.accesscontrol.business.serviceagreement.GetServiceAgreementParticipants;
import com.backbase.accesscontrol.util.ExceptionUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItem;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItems;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsFilter;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArrangementItemService implements DataItemExternalIdConverterService, DataItemsValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrangementItemService.class);
    private static final String DATA_ITEM_TYPE = "ARRANGEMENTS";

    private ArrangementsService arrangementsService;
    private GetServiceAgreementParticipants getServiceAgreementParticipants;
    private InternalRequestContext internalRequestContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return DATA_ITEM_TYPE;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<String> getInternalId(String externalId, String serviceAgreementId) {

        try {
            return singletonList(arrangementsService.getInternalId(externalId));
        } catch (NotFoundException e) {
            LOGGER.warn("Arrangement with external id {} does not exists.", externalId);
            throw ExceptionUtil.getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, List<String>> mapExternalToInternalIds(Set<String> externalIds, String serviceAgreementId) {

        if (externalIds.isEmpty()) {
            return emptyMap();
        }

        AccountArrangementItems arrangements = arrangementsService.postFilter(
            new AccountArrangementsFilter().externalArrangementIds(new ArrayList<>(externalIds))
                .size(externalIds.size()));

        return arrangements.getArrangementElements().stream()
            .collect(toMap(AccountArrangementItem::getExternalArrangementId, item -> singletonList(item.getId())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(List<String> dataItems, String serviceAgreementId) {
        if (!dataItems.isEmpty()) {
            List<ServiceAgreementParticipantsGetResponseBody> participants =
                getServiceAgreementParticipants
                    .getServiceAgreementParticipants(
                        getVoidInternalRequest(internalRequestContext),
                        serviceAgreementId).getData();

            List<String> participantIds = participants.stream()
                .filter(ServiceAgreementParticipantsGetResponseBody::getSharingAccounts)
                .map(ServiceAgreementParticipantsGetResponseBody::getId)
                .collect(toList());

            LOGGER.info("Validating data items {} and service agreement {}", dataItems, serviceAgreementId);

            AccountArrangementsLegalEntities arrangementIdsBody = arrangementsService
                .getArrangementsLegalEntities(dataItems, participantIds);

            validateArrangementItems(dataItems, arrangementIdsBody);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(List<String> dataItems, List<String> participantIds) {
        if (!dataItems.isEmpty()) {
            AccountArrangementsLegalEntities arrangementIdsBody = arrangementsService
                .getArrangementsLegalEntities(dataItems, participantIds);

            validateArrangementItems(dataItems, arrangementIdsBody);
        }
    }

    private void validateArrangementItems(List<String> dataItems, AccountArrangementsLegalEntities arrangementIdsBody) {
        HashSet<AccountPresentationArrangementLegalEntityIds> receivedIds = Sets
            .newHashSet(arrangementIdsBody.getArrangementsLegalEntities());

        LOGGER.info("Returned arrangements {}", receivedIds);
        HashSet<String> sentIds = Sets.newHashSet(dataItems);
        LOGGER.info("Sent arrangements {}", sentIds);

        if (receivedIds.size() != sentIds.size()) {
            LOGGER.warn("Arrangements from arrangement domain have size {} but sent for validation are {}",
                arrangementIdsBody.getArrangementsLegalEntities().size(), dataItems.size());
            throw getBadRequestException(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode());
        }
    }
}
