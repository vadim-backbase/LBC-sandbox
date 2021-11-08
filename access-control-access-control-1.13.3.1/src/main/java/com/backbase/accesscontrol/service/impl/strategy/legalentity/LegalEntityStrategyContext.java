package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Context used for updating the legal entity type.
 */
@Service
public class LegalEntityStrategyContext {

    private Map<LegalEntityType, LegalEntityUpdateStrategy> strategyMap;

    /**
     * Constructor for {@link LegalEntityStrategyContext} class.
     *
     * @param legalEntityUpdateStrategies - list of legal entity update strategies
     */
    public LegalEntityStrategyContext(List<LegalEntityUpdateStrategy> legalEntityUpdateStrategies) {
        strategyMap = new EnumMap<>(LegalEntityType.class);
        for (LegalEntityUpdateStrategy strategy : legalEntityUpdateStrategies) {
            strategyMap.put(LegalEntityType.fromString(strategy.getLegalEntityType().toString()), strategy);
        }
        if (strategyMap.size() != LegalEntityType.values().length) {
            throw new IllegalArgumentException("Unexpected number of legal entity types");
        }
    }

    public LegalEntity updateLegalEntity(LegalEntityType legalEntityType, LegalEntity legalEntity) {
        LegalEntityUpdateStrategy strategy = strategyMap.get(legalEntityType);
        return strategy.updateLegalEntity(legalEntity);
    }
}
