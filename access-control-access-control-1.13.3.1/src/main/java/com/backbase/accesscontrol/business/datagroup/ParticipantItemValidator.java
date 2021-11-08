package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_056;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.service.ArrangementsService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.routes.serviceagreement.RemoveParticipantSharingAccountRouteProxy;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.camel.Consume;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ParticipantItemValidator implements RemoveParticipantSharingAccountRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantItemValidator.class);
    private static final String ARRANGEMENTS = "ARRANGEMENTS";
    public static final String UNABLE_TO_REMOVE_PARTICIPANT_FROM_SERVICE_AGREEMENT_THERE_IS_DATA_GROUP_WITH_ITS_ACCOUNTS = "Unable to remove Participant from Service Agreement,there is data group with its accounts.";

    private boolean arrangementTypeSupported;
    private ArrangementsService arrangementsService;
    private DataGroupService dataGroupService;
    @Value("${backbase.approval.validation.enabled:false}")
    private boolean includePendingDataGroups;

    /**
     * Constructor.
     *
     * @param validationConfig    validation config
     * @param arrangementsService arrangement service
     */
    public ParticipantItemValidator(ValidationConfig validationConfig,
        ArrangementsService arrangementsService,
        DataGroupService dataGroupService) {
        arrangementTypeSupported = validationConfig.getTypes().contains(ARRANGEMENTS);
        this.arrangementsService = arrangementsService;
        this.dataGroupService = dataGroupService;
    }

    /**
     * Method that listens on the direct:validateParticipantDataGroupsInternal endpoint and uses forwards the request to
     * the P&P service.
     *
     * @param internalRequest Internal Request of list {@link DataItemValidatableItem} type to be send by the client
     * @return Business Process Result of List {@link InvalidParticipantItem}
     */
    @Override
    @Consume(value = DIRECT_DEFAULT_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE)
    public List<InvalidParticipantItem> getInvalidItemsSharingAccounts(
        InternalRequest<List<DataItemValidatableItem>> internalRequest) {
        LOGGER.info("Validating participant items {}", internalRequest);
        List<InvalidParticipantItem> invalidParticipantItems = new ArrayList<>();
        if (arrangementTypeSupported) {
            LOGGER.warn("Validating is enabled for type Arrangements: {}", internalRequest);
            invalidParticipantItems = validateParticipants(internalRequest.getData());
        }
        return invalidParticipantItems;
    }

    /**
     * Validate participants sharing accounts.
     *
     * @param arrangementItems     all arrangement ids
     * @param legalEntityIdsToStay - legal entity ids that stay
     * @return true/false
     */
    public boolean canRemoveParticipantSharingAccounts(List<String> arrangementItems,
        List<String> legalEntityIdsToStay) {
        LOGGER.info("All Arrangements {}", arrangementItems);
        if (arrangementItems.isEmpty()) {
            return true;
        }
        LOGGER.info("LegalEntityIds To Stay {}", legalEntityIdsToStay);

        if (legalEntityIdsToStay.isEmpty()) {
            LOGGER.warn(
                UNABLE_TO_REMOVE_PARTICIPANT_FROM_SERVICE_AGREEMENT_THERE_IS_DATA_GROUP_WITH_ITS_ACCOUNTS);
            throw getBadRequestException(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode());
        }

        AccountArrangementsLegalEntities arrangementIds = arrangementsService
            .getArrangementsLegalEntities(arrangementItems, legalEntityIdsToStay);

        Map<String, Set<ArrangementsInLegalEntity>> arrangementsGroupedByLegalEntity = getArrangementsGroupedByLegalEntity(
            arrangementIds);

        Set<String> arrangementsItemsReceived = arrangementsGroupedByLegalEntity.values().stream()
            .flatMap(Collection::stream).map(i -> i.arrangementId).collect(toSet());
        LOGGER
            .info("arrangementsItemsReceived {} and arrangementItems {}", arrangementsItemsReceived, arrangementItems);
        if (!arrangementsItemsReceived.containsAll(arrangementItems)) {
            LOGGER.warn(
                UNABLE_TO_REMOVE_PARTICIPANT_FROM_SERVICE_AGREEMENT_THERE_IS_DATA_GROUP_WITH_ITS_ACCOUNTS);
            throw getBadRequestException(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode());
        }
        return true;
    }

    public void canRemoveParticipantSharingAccounts(List<String> arrangementItems,
        List<String> legalEntityIdsToStay, String serviceAgreementId) {
        boolean isThereAnyDataGroupInServiceAgreement = dataGroupService
            .checkIfExistsPendingDataGroupByServiceAgreementId(serviceAgreementId,
                includePendingDataGroups);
        if (isThereAnyDataGroupInServiceAgreement) {
            LOGGER.warn(
                UNABLE_TO_REMOVE_PARTICIPANT_FROM_SERVICE_AGREEMENT_THERE_IS_DATA_GROUP_WITH_ITS_ACCOUNTS);
            throw getBadRequestException(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode());
        }
    }


    private List<InvalidParticipantItem> validateParticipants(
        List<DataItemValidatableItem> dataItemValidatableItems) {
        List<DataItemValidatableItem> arrangement = getItemsWithArrangementDataType(dataItemValidatableItems);
        Map<String, Set<ArrangementsInLegalEntity>> arrangementsGroupedByLegalEntity = getArrangementsPerLegalEntity(
            arrangement);
        LOGGER.info("Filtering invalid participant items");
        List<InvalidParticipantItem> invalidParticipantItems = arrangement.stream()
            .filter(item -> haveArrangementsInServiceAgreement(arrangementsGroupedByLegalEntity, item))
            .map(item -> new InvalidParticipantItem(item, createErrors(arrangementsGroupedByLegalEntity, item)))
            .collect(Collectors.toList());
        LOGGER.info("Invalid participant items {}", invalidParticipantItems);
        return invalidParticipantItems;
    }

    private List<String> createErrors(Map<String, Set<ArrangementsInLegalEntity>> arrangementsGroupedByLegalEntity,
        DataItemValidatableItem item) {
        return Optional
            .ofNullable(arrangementsGroupedByLegalEntity.get(item.getPersistenceExtendedParticipant().getId()))
            .orElseGet(HashSet::new)
            .stream()
            .map(this::getError)
            .collect(Collectors.toList());
    }

    private String getError(ArrangementsInLegalEntity arrangementsInLegalEntity) {
        return format("Participant can not be removed. Please remove arrangement with id %s first",
            arrangementsInLegalEntity.getArrangementId()
        );
    }

    private Map<String, Set<ArrangementsInLegalEntity>> getArrangementsPerLegalEntity(
        List<DataItemValidatableItem> arrangement) {
        LOGGER.info("Retrieving arrangements for legal entity {}", arrangement);
        List<String> arrangementItems = getArrangementItems(arrangement);
        List<String> legalEntityIds = getLegalEntityIds(arrangement);
        if (CollectionUtils.isEmpty(arrangementItems) || CollectionUtils.isEmpty(legalEntityIds)) {
            LOGGER.info("No arrangements or legal entity ids, won't invoke the arrangements client");
            return new HashMap<>();
        }
        LOGGER.info("Invoking the arrangements client");
        AccountArrangementsLegalEntities arrangementIds = arrangementsService
            .getArrangementsLegalEntities(arrangementItems, legalEntityIds);

        return getArrangementsGroupedByLegalEntity(arrangementIds);
    }

    private boolean haveArrangementsInServiceAgreement(
        Map<String, Set<ArrangementsInLegalEntity>> arrangementsGroupedByLegalEntity, DataItemValidatableItem item) {
        return arrangementsGroupedByLegalEntity.containsKey(item.getPersistenceExtendedParticipant().getId());
    }

    private Map<String, Set<ArrangementsInLegalEntity>> getArrangementsGroupedByLegalEntity(
        AccountArrangementsLegalEntities arrangementIds) {
        return arrangementIds
            .getArrangementsLegalEntities()
            .stream()
            .flatMap(this::createArrangementsInLegalEntityStream)
            .collect(
                groupingBy(ArrangementsInLegalEntity::getLegalEntityId,
                    mapping(item -> item, toSet())
                ));
    }

    private Stream<ArrangementsInLegalEntity> createArrangementsInLegalEntityStream(
        AccountPresentationArrangementLegalEntityIds arrangementLegalEntityIds) {
        return arrangementLegalEntityIds.getLegalEntityIds()
            .stream()
            .map(legalEntityId -> new ArrangementsInLegalEntity(arrangementLegalEntityIds.getArrangementId(),
                legalEntityId));
    }

    private List<String> getLegalEntityIds(List<DataItemValidatableItem> arrangement) {
        LOGGER.info("Getting legal entity ids from the payload {}", arrangement);
        List<String> legalEntityIds = arrangement
            .stream()
            .map(DataItemValidatableItem::getPersistenceExtendedParticipant)
            .map(PersistenceExtendedParticipant::getId)
            .distinct()
            .collect(Collectors.toList());
        LOGGER.info("Legal entity ids from the payload {}", legalEntityIds);
        return legalEntityIds;
    }

    private List<String> getArrangementItems(List<DataItemValidatableItem> arrangement) {
        LOGGER.info("Getting arrangement ids from the payload {}", arrangement);
        List<String> arrangementItems = arrangement
            .stream()
            .flatMap(item -> item.getPersistenceDataGroupExtendedItems().stream())
            .filter(this::isArrangement)
            .flatMap(dataGroup -> dataGroup.getItems().stream())
            .distinct()
            .collect(Collectors.toList());
        LOGGER.info("Arrangement ids from the payload {}", arrangementItems);
        return arrangementItems;
    }

    private List<DataItemValidatableItem> getItemsWithArrangementDataType(List<DataItemValidatableItem> data) {
        LOGGER.info("Filtering items that contain arrangements type {}", data);
        List<DataItemValidatableItem> itemsWithArrangmentDataType = data.stream()
            .filter(this::hasArrangement)
            .collect(Collectors.toList());
        LOGGER.info("Filtered items that contain arrangements type {}", itemsWithArrangmentDataType);
        return itemsWithArrangmentDataType;
    }

    private boolean hasArrangement(DataItemValidatableItem data) {
        return data.getPersistenceDataGroupExtendedItems()
            .stream()
            .anyMatch(this::isArrangement);
    }

    private boolean isArrangement(PersistenceDataGroupExtendedItemDto dataGroupExtendedItem) {
        return ARRANGEMENTS.equals(dataGroupExtendedItem.getType());
    }

    private class ArrangementsInLegalEntity {

        private final String arrangementId;
        private final String legalEntityId;

        private ArrangementsInLegalEntity(String arrangementId, String legalEntityId) {
            this.arrangementId = arrangementId;
            this.legalEntityId = legalEntityId;
        }

        public String getArrangementId() {
            return arrangementId;
        }

        public String getLegalEntityId() {
            return legalEntityId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ArrangementsInLegalEntity that = (ArrangementsInLegalEntity) o;

            return new EqualsBuilder()
                .append(arrangementId, that.arrangementId)
                .append(legalEntityId, that.legalEntityId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(arrangementId)
                .append(legalEntityId)
                .toHashCode();
        }
    }
}
