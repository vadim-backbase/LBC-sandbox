package com.backbase.accesscontrol.api.client.it.functiongroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_027;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_FUNCTION_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_DELETE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DeleteFunctionGroupIT extends TestDbWireMock {

    private static final String url = "/accessgroups/function-groups/{id}";

    private FunctionGroup functionGroup;

    @Before
    public void setUp() {
        ApplicableFunctionPrivilege apfBf1002View =
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view");
        functionGroup = createFunctionGroup("fg-name", "fg-description", rootMsa,
            Lists.newArrayList(apfBf1002View.getId()), FunctionGroupType.DEFAULT);
    }

    @Test
    public void testDeleteFunctionGroup() throws IOException, JSONException {
        ResponseEntity<String> response = executeClientRequestEntity(new UrlBuilder(url)
                .addPathParameter(functionGroup.getId())
                .build(),
            HttpMethod.DELETE, "", "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(DELETE)
            .withId(functionGroup.getId())));
    }

    @Test
    public void shouldThrowBadRequestIfFunctionGroupIsNotFound() {

        NotFoundException exception = assertThrows(NotFoundException.class, () ->executeClientRequestEntity(new UrlBuilder(url)
                .addPathParameter("invalidfg")
                .build(),
            HttpMethod.DELETE, "", "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfFunctionGroupIsAssignedToUser() {
        assignFunctionGroupToUser();

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestEntity(new UrlBuilder(url)
                .addPathParameter(functionGroup.getId())
                .build(),
            HttpMethod.DELETE, "", "USER", ENTITLEMENTS_MANAGE_FUNCTION_GROUPS, PRIVILEGE_DELETE));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_027.getErrorMessage(), ERR_ACC_027.getErrorCode()));
    }

    private void assignFunctionGroupToUser() {

        UserContext userContext = new UserContext(UUID.randomUUID().toString(), rootMsa.getId());
        userContext = userContextJpaRepository.save(userContext);

        userAssignedFunctionGroupJpaRepository.save(new UserAssignedFunctionGroup(functionGroup, userContext));
    }
}
