package com.backbase.accesscontrol.api.service.it.functiongroup;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.FunctionGroupServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link FunctionGroupServiceApiController#postFunctionGroupsDelete(List, HttpServletRequest,
 * HttpServletResponse) method}.
 */
public class DeleteFunctionGroupBatchIT extends TestDbWireMock {

    private static final String DELETE_FUNCTION_GROUPS_BATCH_URL = "/accessgroups/function-groups/batch/delete";
    private FunctionGroup functionGroup1;
    private FunctionGroup functionGroup2;

    @Before
    public void setUp() {
        ApplicableFunctionPrivilege apfBf1002View =
            businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view");
        functionGroup1 = createFunctionGroup("fg-name1", "fg-description1", rootMsa,
            Lists.newArrayList(apfBf1002View.getId()), FunctionGroupType.DEFAULT);
        functionGroup2 = createFunctionGroup("fg-name2", "fg-description2", rootMsa,
            Lists.newArrayList(apfBf1002View.getId()), FunctionGroupType.DEFAULT);
    }

    @Test
    public void testDeleteFunctionGroup() throws Exception {

        PresentationIdentifier body1 = new PresentationIdentifier().withIdIdentifier(functionGroup1.getId());
        PresentationIdentifier body2 = new PresentationIdentifier().withIdIdentifier("fgId2");

        BatchResponseItemExtended batchResponseItemSuccessful = new BatchResponseItemExtended()
            .withResourceId(functionGroup1.getId())
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);

        BatchResponseItemExtended batchResponseItemFailed = new BatchResponseItemExtended()
            .withResourceId("fgId2")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND)
            .withErrors(asList("Function group does not exist."));

        String responseJson = executeRequest(DELETE_FUNCTION_GROUPS_BATCH_URL,
            Arrays.asList(body1, body2), HttpMethod.POST);

        List<BatchResponseItemExtended> batchResponseItemExtendedList = asList(
            readValue(responseJson, BatchResponseItemExtended[].class));

        assertTrue(containsSuccessfulResponseItem(batchResponseItemExtendedList,
            convertValue(batchResponseItemSuccessful, BatchResponseItemExtended.class)));
        assertTrue(containsFailedResponseItem(batchResponseItemExtendedList,
            convertValue(batchResponseItemFailed, BatchResponseItemExtended.class)));

        verifyFunctionGroupEvents(Sets.newHashSet(new FunctionGroupEvent()
            .withAction(DELETE)
            .withId(functionGroup1.getId())));
    }

    @Test
    public void shouldReturnErrorsOnInvalidIdentifiers() throws Exception {
        String randomId = getUuid();
        PresentationIdentifier idIdentifier = new PresentationIdentifier()
            .withIdIdentifier(randomId);
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .withNameIdentifier(
                new NameIdentifier()
                    .withName(functionGroup2.getName())
                    .withExternalServiceAgreementId("invalid-service-agreement-external-id")
            );
        PresentationIdentifier nameIdentifier2 = new PresentationIdentifier()
            .withNameIdentifier(
                new NameIdentifier()
                    .withName("invalid-name")
                    .withExternalServiceAgreementId(rootMsa.getExternalId())
            );

        String responseJson = executeRequest(DELETE_FUNCTION_GROUPS_BATCH_URL,
            Arrays.asList(idIdentifier, nameIdentifier, nameIdentifier2), HttpMethod.POST);

        List<BatchResponseItemExtended> responseItemsExtended = asList(
            readValue(responseJson, BatchResponseItemExtended[].class));

        assertEquals(3, responseItemsExtended.size());
        assertEquals(randomId, responseItemsExtended.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseItemsExtended.get(0).getStatus());
        assertEquals(ERR_ACQ_003.getErrorMessage(), responseItemsExtended.get(0).getErrors().get(0));

        assertEquals(functionGroup2.getName(), responseItemsExtended.get(1).getResourceId());
        assertEquals("invalid-service-agreement-external-id",
            responseItemsExtended.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseItemsExtended.get(1).getStatus());
        assertEquals(ERR_ACQ_006.getErrorMessage(), responseItemsExtended.get(1).getErrors().get(0));

        assertEquals("invalid-name", responseItemsExtended.get(2).getResourceId());
        assertEquals(rootMsa.getExternalId(),
            responseItemsExtended.get(2).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseItemsExtended.get(2).getStatus());
        assertEquals(ERR_ACQ_003.getErrorMessage(), responseItemsExtended.get(2).getErrors().get(0));
    }

    public FunctionGroup createFunctionGroup(String name, String description,
        ServiceAgreement serviceAgreement, List<ApplicableFunctionPrivilege> applicableFunctionPrivileges) {

        final FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, name, description, new HashSet<>(), FunctionGroupType.DEFAULT,
                serviceAgreement);

        Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList = GroupedFunctionPrivilegeUtil
            .getGroupedFunctionPrivileges(
                applicableFunctionPrivileges.stream()
                    .map(applicableFunctionPrivilege -> getGroupedFunctionPrivilege(null, applicableFunctionPrivilege,
                        functionGroup))
                    .toArray(GroupedFunctionPrivilege[]::new)
            );

        functionGroup.setPermissions(groupedFunctionPrivilegeList);

        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.flush();

        return savedFunctionGroup;
    }
}