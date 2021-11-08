package com.backbase.accesscontrol.api.service.it.aps;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.PermissionSetServiceApiController;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.util.helpers.AccessTokenGenerator;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.event.spec.v1.AssignablePermissionSetEvent;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link PermissionSetServiceApiController#deleteByIdentifier(String, String, String, HttpServletRequest,
 * HttpServletResponse)}
 */
public class DeletePermissionSetIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/permission-sets/{identifierType}/{identifier}";

    @Autowired
    private AccessTokenGenerator accessTokenGenerator;

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldDeletePermissionSet() throws Exception {

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName("Test Admin");
        assignablePermissionSet.setDescription("Test Admin");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);

        assignablePermissionSet = assignablePermissionSetJpaRepository.save(assignablePermissionSet);

        String identifierType = "id";
        String identifier = assignablePermissionSet.getId().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("X-AccessControl-Token", accessTokenGenerator.generateValidToken());

        executeServiceRequest(new UrlBuilder(URL)
            .addPathParameter(identifierType)
            .addPathParameter(identifier).build(), null, null, null, null, null, HttpMethod.DELETE, headers);

        verifyAssignablePermissionSetEvents(Sets.newHashSet(new AssignablePermissionSetEvent()
            .withAction(DELETE)
            .withId(new BigDecimal(identifier))));
    }
}
