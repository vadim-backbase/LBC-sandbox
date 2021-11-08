package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;

public class LegalEntityUtil {

    public static LegalEntity createLegalEntity(String id, String name, String externalId, LegalEntity parent,
        LegalEntityType type) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(id);
        legalEntity.setParent(parent);
        legalEntity.setExternalId(externalId);
        legalEntity.setName(name);
        legalEntity.setType(type);
        return legalEntity;
    }

    public static LegalEntity createLegalEntity(String name, String externalId, LegalEntity parent) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(parent);
        legalEntity.setExternalId(externalId);
        legalEntity.setName(name);
        legalEntity.setType(LegalEntityType.CUSTOMER);
        return legalEntity;
    }

}
