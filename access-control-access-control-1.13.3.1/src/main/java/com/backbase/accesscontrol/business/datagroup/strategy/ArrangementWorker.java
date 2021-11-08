package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;

import com.backbase.accesscontrol.business.datagroup.dataitems.ArrangementItemService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArrangementWorker implements Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrangementWorker.class);

    private static final String DATA_ITEM_TYPE = "ARRANGEMENTS";

    private ArrangementItemService arrangementItemService;

    private boolean validationEnabled;

    /**
     * Arrangement worker constructor.
     *
     * @param arrangementItemService service
     * @param validationEnabled      validation flag
     */
    public ArrangementWorker(ArrangementItemService arrangementItemService,
        @Value("${backbase.data-group.validation.enabled:false}") boolean validationEnabled) {
        this.arrangementItemService = arrangementItemService;
        this.validationEnabled = validationEnabled;
    }

    private List<String> convertToInternalIds(Set<String> externalIds, String serviceAgreementId) {
        Map<String, List<String>> mapExternalToInternalIds = arrangementItemService
            .mapExternalToInternalIds(externalIds, serviceAgreementId);

        if (externalIds.size() != mapExternalToInternalIds.values().size()) {
            LOGGER.warn("Arrangements from arrangement domain have size {} but sent {} for mapping",
                mapExternalToInternalIds.values().size(), externalIds.size());
            throw getBadRequestException(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode());
        }

        return mapExternalToInternalIds.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalIds(Set<String> internalIds, Collection<String> participantIds) {
        if (validationEnabled) {
            arrangementItemService.validate(new ArrayList<>(internalIds), new ArrayList<>(participantIds));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> convertToInternalIdsAndValidate(Set<String> externalIds, Collection<String> participantIds,
        String serviceAgreementId) {
        List<String> internalIds = convertToInternalIds(externalIds, serviceAgreementId);

        validateInternalIds(new HashSet<>(internalIds), participantIds);

        return internalIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidatingAgainstParticipants() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return DATA_ITEM_TYPE;
    }
}
