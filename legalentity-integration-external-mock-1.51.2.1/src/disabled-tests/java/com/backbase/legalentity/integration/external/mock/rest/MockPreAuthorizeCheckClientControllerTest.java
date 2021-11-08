package com.backbase.legalentity.integration.external.mock.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.legalentity.integration.external.mock.TestWireMock;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MockPreAuthorizeCheckClientControllerTest extends TestWireMock {

    private String url = "/authorize/permission-check";

    @Test
    public void testSuccessfulPreAuthorize() throws Exception {
        ResponseEntity<String> response =
            executeClientRequestReturnFullResponse(url, null, "user", "said", true, HttpMethod.GET,
                new HashMap<>());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testFailPreAuthorizeNoPermissions() throws IOException, JSONException {
        assertThrows(ForbiddenException.class,
            () -> executeClientRequestReturnFullResponse(url, null, "user", "said", false, HttpMethod.GET,
                new HashMap<>()));
    }
}
