package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_008;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityUpdateBankStrategyTest {

    @InjectMocks
    private LegalEntityUpdateBankStrategy legalEntityUpdateBankStrategy;

    @Test
    public void shouldReturnBankAsType() {
        assertThat(legalEntityUpdateBankStrategy.getLegalEntityType(), is(LegalEntityType.BANK));
    }


    @Test
    public void shouldUpdateRootAsBank() {

        LegalEntity legalEntity = new LegalEntity();
        LegalEntity legalEntityReturned = legalEntityUpdateBankStrategy.updateLegalEntity(legalEntity);

        assertEquals(legalEntityReturned.getType().toString(),
            LegalEntityType.BANK.toString());

    }

    @Test
    public void shouldSaveEntityWithParentBank() {
        LegalEntity parent = new LegalEntity();
        parent.setType(LegalEntityType.BANK);
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(parent);
        LegalEntity legalEntityReturned = legalEntityUpdateBankStrategy.updateLegalEntity(legalEntity);

        assertEquals(legalEntityReturned.getType().toString(),
            LegalEntityType.BANK.toString());

    }

    @Test
    public void shouldThrowExceptionWhenEntityHasParentCustomer() {
        LegalEntity parent = new LegalEntity();
        parent.setType(LegalEntityType.CUSTOMER);
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(parent);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> legalEntityUpdateBankStrategy.updateLegalEntity(legalEntity));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_008.getErrorMessage(), ERR_ACC_008.getErrorCode()));
    }
}