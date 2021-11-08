package com.backbase.accesscontrol.business.flows.permissonset;

import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.flows.permissionset.UpdatePermissionSetFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePermissionSetFlowTest {

    @Mock
    private PermissionSetPersistenceService persistenceService;

    @InjectMocks
    private UpdatePermissionSetFlow testy;

    @Test
    public void shouldCallPersistenceService() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        testy.start(itemPut);
        verify(persistenceService).updatePermissionSet(eq(itemPut));
    }

}
