package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_009;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_012;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityUpdateCustomerStrategyTest {

    @InjectMocks
    private LegalEntityUpdateCustomerStrategy legalEntityUpdateCustomerStrategy;
    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Test
    public void shouldReturnCustomerAsType() {
        assertThat(legalEntityUpdateCustomerStrategy.getLegalEntityType(), is(LegalEntityType.CUSTOMER));
    }

    @Test
    public void shouldThrowExceptionWhenParentIsNull() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> legalEntityUpdateCustomerStrategy.updateLegalEntity(new LegalEntity()));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_009.getErrorMessage(), ERR_ACC_009.getErrorCode()));
    }

    @Test
    public void shouldThrowExceptionWhenHaveBankAsChildren() {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(new LegalEntity());
        legalEntity.setId("id");

        when(
            legalEntityJpaRepository
                .findAllByParentIdAndType(
                    eq("id"),
                    eq(LegalEntityType.BANK)
                )
        )
            .thenReturn(Lists.newArrayList(new LegalEntity()));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> legalEntityUpdateCustomerStrategy.updateLegalEntity(legalEntity));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_012.getErrorMessage(), ERR_ACC_012.getErrorCode()));
    }

    @Test
    public void shouldUpdateType() {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setParent(new LegalEntity());
        legalEntity.setId("id");

        when(
            legalEntityJpaRepository
                .findAllByParentIdAndType(
                    eq("id"),
                    eq(LegalEntityType.BANK)
                )
        )
            .thenReturn(Collections.emptyList());

        LegalEntity legalEntityReturned = legalEntityUpdateCustomerStrategy.updateLegalEntity(legalEntity);
        assertEquals(legalEntityReturned.getType(), legalEntity.getType());
    }

}