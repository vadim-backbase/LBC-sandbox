package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_077;

import com.backbase.accesscontrol.business.service.ContactsService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ContactItemService implements DataItemExternalIdConverterService {

    private static final String DATA_ITEM_TYPE = "PAYEES";

    private ContactsService contactsService;

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
        List<String> contactIds;
        try {
            contactIds = contactsService.convertSingleExternalContactId(externalId, serviceAgreementId);
        } catch (BadRequestException e) {
            throw getBadRequestException(ERR_ACQ_077.getErrorMessage(), ERR_ACQ_077.getErrorCode());
        }
        return contactIds;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, List<String>> mapExternalToInternalIds(Set<String> externalIds, String serviceAgreementId) {
        return contactsService.convertExternalContactIds(externalIds, serviceAgreementId);
    }
}
