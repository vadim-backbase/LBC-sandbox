package com.backbase.accesscontrol.business.persistence.aps;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePermissionSetHandlerTest {

    @Mock
    private PermissionSetService permissionSetService;
    @InjectMocks
    private UpdatePermissionSetHandler updatePermissionSetHandler;

    @Test
    public void executeRequest() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, BigDecimal.valueOf(17))));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);
        when(permissionSetService.update(requestData)).thenReturn("id");

        updatePermissionSetHandler.executeRequest(new EmptyParameterHolder(), requestData);
        verify(permissionSetService, times(1)).update(requestData);
    }

    @Test
    public void createSuccessEvent() {
        PresentationUserApsIdentifiers adminUserAps = new PresentationUserApsIdentifiers()
            .withIdIdentifiers(Sets.newHashSet(asList(BigDecimal.ONE, BigDecimal.valueOf(17))));
        PresentationUserApsIdentifiers regularUserAps = new PresentationUserApsIdentifiers()
            .withNameIdentifiers(Sets.newHashSet(singletonList("name1")));

        PresentationPermissionSetItemPut requestData = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("exSaId")
            .withAdminUserAps(adminUserAps)
            .withRegularUserAps(regularUserAps);
        ServiceAgreementEvent successEvent = updatePermissionSetHandler
            .createSuccessEvent(new EmptyParameterHolder(), requestData, "id");

        assertNotNull(successEvent);
        assertEquals(Action.UPDATE, successEvent.getAction());
        assertNotNull("id",successEvent.getId());
    }

}
