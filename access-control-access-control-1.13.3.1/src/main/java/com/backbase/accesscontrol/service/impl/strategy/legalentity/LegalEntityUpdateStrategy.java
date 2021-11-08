package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;

/**
 * Strategy to update the legal entity to desired type.
 */
public interface LegalEntityUpdateStrategy {

    /**
     * The type of the update {@link LegalEntityType#values()}.
     *
     * @return the type of the legal entity which should be used of the entity
     */
    LegalEntityType getLegalEntityType();

    /**
     * Update legal entity to be of the corresponding type of the {@link LegalEntityUpdateStrategy#getLegalEntityType()}
     * or throws {@link BadRequestException} if the operation is not allowed.
     *
     * @param legalEntity legal entity that needs to be updated
     * @throws BadRequestException if the operation is not allowed.
     */
    LegalEntity updateLegalEntity(LegalEntity legalEntity);
}
