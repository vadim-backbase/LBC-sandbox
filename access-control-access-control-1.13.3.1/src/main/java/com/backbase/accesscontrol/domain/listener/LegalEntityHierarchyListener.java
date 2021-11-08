package com.backbase.accesscontrol.domain.listener;

import com.backbase.accesscontrol.domain.LegalEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.PrePersist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegalEntityHierarchyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityHierarchyListener.class);

    /**
     * Set  ancestors and children of legal entity.
     *
     * @param entity legal entity;
     */
    @PrePersist
    public void setAncestorsAndChildren(LegalEntity entity) {
        Optional.ofNullable(entity.getParent()).ifPresent(
            parent -> {
                LOGGER.info("Setting ancestors of {}", entity);
                List<LegalEntity> legalEntityAncestors = new ArrayList<>(parent.getLegalEntityAncestors());
                LOGGER.info("Setting children of {}", parent);
                legalEntityAncestors.add(parent);
                entity.setLegalEntityAncestors(legalEntityAncestors);
            }
        );
    }
}
