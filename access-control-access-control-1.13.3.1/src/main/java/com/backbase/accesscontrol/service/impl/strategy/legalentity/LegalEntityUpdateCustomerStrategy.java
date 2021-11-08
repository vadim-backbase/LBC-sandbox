package com.backbase.accesscontrol.service.impl.strategy.legalentity;


import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegalEntityUpdateCustomerStrategy implements LegalEntityUpdateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityUpdateCustomerStrategy.class);

    private LegalEntityJpaRepository legalEntityJpaRepository;

    public LegalEntityUpdateCustomerStrategy(LegalEntityJpaRepository legalEntityJpaRepository) {
        this.legalEntityJpaRepository = legalEntityJpaRepository;
    }

    @Override
    public LegalEntityType getLegalEntityType() {
        return LegalEntityType.CUSTOMER;
    }

    @Override
    public LegalEntity updateLegalEntity(LegalEntity legalEntity) {
        LOGGER.info("Setting legal entity {} to customer", legalEntity.getId());
        if (legalEntity.getParent() == null) {
            // no change allowed root
            LOGGER.warn("Invalid parent of legal entity id {}", legalEntity.getId());
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_009.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_009.getErrorCode());
        }

        List<LegalEntity> bankChildren = legalEntityJpaRepository
            .findAllByParentIdAndType(
                legalEntity.getId(),
                LegalEntityType.BANK);
        if (!bankChildren.isEmpty()) {
            // no change allowed when legal entity has bank as children
            LOGGER.warn("Invalid children of legal entity id {}", legalEntity.getId());
            throw getBadRequestException(CommandErrorCodes.ERR_ACC_012.getErrorMessage(),
                CommandErrorCodes.ERR_ACC_012.getErrorCode());
        }
        legalEntity.setType(LegalEntityType.CUSTOMER);
        return legalEntity;
    }
}
