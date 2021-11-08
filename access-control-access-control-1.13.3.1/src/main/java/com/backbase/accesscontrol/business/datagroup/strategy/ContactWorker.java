package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_104;

import com.backbase.accesscontrol.business.service.ContactsService;
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
public class ContactWorker implements Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactWorker.class);
    private static final String DATA_ITEM_TYPE = "PAYEES";

    private ContactsService contactsService;

    @Override
    public void validateInternalIds(Set<String> internalIds, Collection<String> participantIds) {
        /* do nothing */
    }

    @Override
    public List<String> convertToInternalIdsAndValidate(Set<String> externalIds, Collection<String> participantIds,
        String serviceAgreementId) {

        Map<String, List<String>> externalContactIds = contactsService
            .convertExternalContactIds(externalIds, serviceAgreementId);
        if (externalIds.size() != externalContactIds.values().size()) {
            LOGGER.warn("Contacts from contact domain have size {} but sent {} for mapping",
                externalContactIds.values().size(), externalIds.size());
            throw getBadRequestException(ERR_AG_104.getErrorMessage(), ERR_AG_104.getErrorCode());
        }

        return externalContactIds.values()
            .stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public boolean isValidatingAgainstParticipants() {
        return false;
    }

    @Override
    public String getType() {
        return DATA_ITEM_TYPE;
    }
}
