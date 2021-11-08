package com.backbase.accesscontrol.mappers.model.accessgroup;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

public interface PresentationUserApsIdentifierMapper {

    default PresentationUserApsIdentifiers presentationUserApsIdentifiersToPresentationUserApsIdentifiers(
        com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers presentationUserApsIdentifiers) {
        if (presentationUserApsIdentifiers == null) {
            return null;
        }

        PresentationUserApsIdentifiers presentationUserApsIdentifiers1 = new PresentationUserApsIdentifiers();

        List<String> list = presentationUserApsIdentifiers.getNameIdentifiers();
        if (list != null) {
            presentationUserApsIdentifiers1.setNameIdentifiers(new HashSet<>(list));
        } else {
            presentationUserApsIdentifiers1.setNameIdentifiers(null);
        }
        List<BigDecimal> list1 = presentationUserApsIdentifiers.getIdIdentifiers();
        if (list1 != null) {
            presentationUserApsIdentifiers1.setIdIdentifiers(new HashSet<>(list1));
        } else {
            presentationUserApsIdentifiers1.setIdIdentifiers(null);
        }

        return presentationUserApsIdentifiers1;
    }
}
