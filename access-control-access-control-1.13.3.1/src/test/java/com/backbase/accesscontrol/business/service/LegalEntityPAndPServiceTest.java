package com.backbase.accesscontrol.business.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityPAndPServiceTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Spy
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence =
        new ServiceAgreementTransformerPersistence();

    @InjectMocks
    private LegalEntityPAndPService legalEntityPAndPService;

    @Test
    public void testGetMasterServiceAgreement() {
        String id = "ID";

        when(persistenceLegalEntityService.getMasterServiceAgreement(anyString()))
            .thenReturn(new ServiceAgreement().withId(id));

        legalEntityPAndPService.getMasterServiceAgreement(id);
        verify(persistenceLegalEntityService).getMasterServiceAgreement(eq(id));
    }
}