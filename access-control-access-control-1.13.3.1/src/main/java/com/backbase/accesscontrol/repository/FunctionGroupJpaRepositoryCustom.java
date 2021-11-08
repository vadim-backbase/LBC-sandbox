package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.FunctionGroup;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface FunctionGroupJpaRepositoryCustom {

    Optional<FunctionGroup> readByServiceAgreementExternalIdAndName(String externalServiceAgreementId,
        String name, String entityGraph);

    List<FunctionGroup> readByIdIn(Collection<String> idList, String entityGraph);
}
