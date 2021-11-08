package com.backbase.accesscontrol.business.datagroup.dataitems;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataItemExternalIdConverterService extends DataItemType {

    /**
     * Returns the internal id of the data item for external id provided.
     *
     * @param externalId         external id of the data item
     * @param serviceAgreementId internal SA id.
     * @return internal id of the data item.
     */
    List<String> getInternalId(String externalId, String serviceAgreementId);

    /**
     * Returns the internal ids of the data item for external ids provided.
     *
     * @param externalIds        external ids of the data items.
     * @param serviceAgreementId internal SA id.
     * @return map of external id as key and internal id as value of the data items.
     */
    Map<String, List<String>> mapExternalToInternalIds(Set<String> externalIds, String serviceAgreementId);
}
