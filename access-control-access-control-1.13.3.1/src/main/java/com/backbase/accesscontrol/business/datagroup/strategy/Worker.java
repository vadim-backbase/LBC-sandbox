package com.backbase.accesscontrol.business.datagroup.strategy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Worker {

    /**
     * Validate data item ids.
     *
     * @param internalIds    internal data item ids
     * @param participantIds participant ids
     */
    void validateInternalIds(Set<String> internalIds, Collection<String> participantIds);

    /**
     * Returns and validates the internal ids of the provided external data item ids.
     *
     * @param externalIds        external data item ids
     * @param participantIds     participant ids
     * @param serviceAgreementId service agreement internal id
     * @return list of internal ids
     */
    List<String> convertToInternalIdsAndValidate(Set<String> externalIds, Collection<String> participantIds,
        String serviceAgreementId);

    /**
     * Returns true if validation should be done against participating legal entities, otherwise false.
     *
     * @return true/false
     */
    boolean isValidatingAgainstParticipants();

    /**
     * Returns the type of the worker.
     *
     * @return name of the type
     */
    String getType();
}
