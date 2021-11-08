package com.backbase.accesscontrol.service.impl.strategy.legalentity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityStrategyContextTest {

    @Mock
    private LegalEntityUpdateStrategy bankStrategy;
    @Mock
    private LegalEntityUpdateStrategy customerStrategy;

    @Before
    public void setUp() {
        when(bankStrategy.getLegalEntityType())
            .thenReturn(LegalEntityType.BANK);
        when(customerStrategy.getLegalEntityType())
            .thenReturn(LegalEntityType.CUSTOMER);
    }

    @Test
    public void shouldFailCreatingWhenMissingTypes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new LegalEntityStrategyContext(new ArrayList<>()));

        assertEquals("Unexpected number of legal entity types", exception.getMessage());
    }

    @Test
    public void shouldCreateService() {
        LegalEntityStrategyContext legalEntityStrategyContext = new LegalEntityStrategyContext(
            createUpdateStrategies());
        assertNotNull(legalEntityStrategyContext);
    }

    @Test
    public void shouldInvokeBankService() {
        LegalEntityStrategyContext legalEntityStrategyContext = new LegalEntityStrategyContext(
            createUpdateStrategies());
        legalEntityStrategyContext.updateLegalEntity(LegalEntityType.BANK, new LegalEntity());
        verify(bankStrategy).updateLegalEntity(eq(new LegalEntity()));
        verify(customerStrategy, never()).updateLegalEntity(eq(new LegalEntity()));
    }

    @Test
    public void shouldInvokeCustomerService() {
        LegalEntityStrategyContext legalEntityStrategyContext = new LegalEntityStrategyContext(
            createUpdateStrategies());
        legalEntityStrategyContext.updateLegalEntity(LegalEntityType.CUSTOMER, new LegalEntity());
        verify(customerStrategy).updateLegalEntity(eq(new LegalEntity()));
        verify(bankStrategy, never()).updateLegalEntity(eq(new LegalEntity()));
    }

    private List<LegalEntityUpdateStrategy> createUpdateStrategies() {
        return Lists.newArrayList(
            bankStrategy,
            customerStrategy
        );
    }

}