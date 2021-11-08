package com.backbase.accesscontrol.business.batch;

import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import java.util.Set;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DataItemValidatableItem {

    private final ProcessableBatchBody<PresentationParticipantPutBody> batchBody;
    private final PersistenceExtendedParticipant persistenceExtendedParticipant;
    private final Set<PersistenceDataGroupExtendedItemDto> persistenceDataGroupExtendedItems;

    public ProcessableBatchBody<PresentationParticipantPutBody> getBatchBody() {
        return batchBody;
    }

    public PersistenceExtendedParticipant getPersistenceExtendedParticipant() {
        return persistenceExtendedParticipant;
    }

    public Set<PersistenceDataGroupExtendedItemDto> getPersistenceDataGroupExtendedItems() {
        return persistenceDataGroupExtendedItems;
    }
}
