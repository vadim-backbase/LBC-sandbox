package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_104;

import com.backbase.accesscontrol.business.datagroup.dataitems.CustomerItemService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomerWorker implements Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerWorker.class);
    private static final String DATA_ITEM_TYPE = "CUSTOMERS";

    private CustomerItemService customerItemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalIds(Set<String> internalIds, Collection<String> participantIds) {
        /* do nothing */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> convertToInternalIdsAndValidate(Set<String> externalIds, Collection<String> participantIds,
        String serviceAgreementId) {
        return convertToInternalIds(externalIds, serviceAgreementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidatingAgainstParticipants() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return DATA_ITEM_TYPE;
    }

    private List<String> convertToInternalIds(Set<String> externalIds, String serviceAgreementId) {
        Map<String, List<String>> mapExternalToInternalIds = customerItemService
            .mapExternalToInternalIds(externalIds, serviceAgreementId);

        if (externalIds.size() != mapExternalToInternalIds.values().size()) {
            LOGGER.warn("Customers from customer domain have size {} but sent {} for mapping",
                mapExternalToInternalIds.values().size(), externalIds.size());
            throw getBadRequestException(ERR_AG_104.getErrorMessage(), ERR_AG_104.getErrorCode());
        }

        return mapExternalToInternalIds.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

}
