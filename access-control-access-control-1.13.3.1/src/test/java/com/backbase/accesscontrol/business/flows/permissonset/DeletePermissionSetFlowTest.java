package com.backbase.accesscontrol.business.flows.permissonset;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.flows.permissionset.DeletePermissionSetFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.accesscontrol.dto.DeletePermissionSetParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeletePermissionSetFlowTest {

    @Mock
    private PermissionSetPersistenceService persistenceService;

    @InjectMocks
    private DeletePermissionSetFlow testy;

    @Test
    public void shouldCallPersistenceService() {

        testy.start(new DeletePermissionSetParameters("id", "1"));
        verify(persistenceService).deletePermissionSet(eq("id"), eq("1"));
    }
}