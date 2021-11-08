package com.backbase.accesscontrol.api.service.it.aps;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.junit.Assert.assertNotNull;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.PermissionSetServiceApiController;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import com.google.common.collect.Sets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test method {@link PermissionSetServiceApiController#postPermissionSet(PresentationPermissionSet, HttpServletRequest,
 * HttpServletResponse)}
 */
public class CreatePermissionSetIT extends TestDbWireMock {

    private static final String CREATE_PERMISSION_SET_URL = "/accessgroups/permission-sets";

    @Test
    public void shouldCreatePermissionSet() throws Exception {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        String responseJson = executeRequest(CREATE_PERMISSION_SET_URL, objectMapper.writeValueAsString(permissionSet),
            HttpMethod.POST);

        PresentationInternalIdResponse internalIdResponse = objectMapper
            .readValue(responseJson, PresentationInternalIdResponse.class);

        assertNotNull(internalIdResponse.getId());

        verifyAssignablePermissionSetEvents(Sets.newHashSet(new AssignablePermissionSetEvent()
            .withAction(ADD)
            .withId(internalIdResponse.getId())));
    }
}
