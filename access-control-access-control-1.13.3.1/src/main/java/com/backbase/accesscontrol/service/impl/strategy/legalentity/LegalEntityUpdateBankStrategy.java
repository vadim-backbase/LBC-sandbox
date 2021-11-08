package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_008;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class LegalEntityUpdateBankStrategy implements LegalEntityUpdateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityUpdateBankStrategy.class);

    @Override
    public LegalEntityType getLegalEntityType() {
        return LegalEntityType.BANK;
    }

    @Override
    public LegalEntity updateLegalEntity(LegalEntity legalEntity) {
        LOGGER.info("Setting legal entity {} to bank", legalEntity.getId());
        // update to BANK
        LegalEntity parent = legalEntity.getParent();
        if (parent != null && parent.getType() != LegalEntityType.BANK) {
            LOGGER.warn("Invalid parent of legal entity id {}", legalEntity.getId());
            throw getBadRequestException(ERR_ACC_008.getErrorMessage(), ERR_ACC_008.getErrorCode());
        }
        legalEntity.setType(LegalEntityType.BANK);
        return legalEntity;
    }
}
