package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.CommonUtils.getBatchRequestOnChunks;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import com.backbase.dbs.contact.api.client.v2.ContactsApi;
import com.backbase.dbs.contact.api.client.v2.model.ContactsInternalIdsFilterPostRequestBody;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ContactsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsService.class);

    private ContactsApi contactsApi;

    public List<String> convertSingleExternalContactId(String contactExternalId, String serviceAgreementId) {

        Map<String, List<String>> contactId = convertExternalContactIds(singletonList(contactExternalId),
            serviceAgreementId);
        if (contactId.isEmpty()) {
            return new ArrayList<>();
        }
        return contactId.get(contactExternalId);

    }

    public Map<String, List<String>> convertExternalContactIds(Collection<String> externalContractIds,
        String serviceAgreementId) {

        if (isNull(serviceAgreementId)) {
            LOGGER.warn("Service agreement id cannot be null for contacts.");
            throw getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode());
        }

        Map<String, List<String>> responseBodies = new HashMap<>();

        getBatchRequestOnChunks(new ArrayList<>(externalContractIds), 10000)
            .forEach(chunk -> responseBodies.putAll(contactsApi
                .postContactsInternalIdsFilter(serviceAgreementId,
                    new ContactsInternalIdsFilterPostRequestBody()
                        .externalContactIds(new ArrayList<>(externalContractIds)))));

        return responseBodies;
    }
}