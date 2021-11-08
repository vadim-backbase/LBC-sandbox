package com.backbase.accesscontrol.business.datagroup.dataitems;

import java.util.List;

public interface DataItemsValidationService extends DataItemType {

    /**
     * Validate data items can be used in service agreement.
     */
    void validate(List<String> dataItems, String serviceAgreementId);

    /**
     * Validate data items can be used in service agreement.
     */
    void validate(List<String> dataItems, List<String> participantIds);
}
