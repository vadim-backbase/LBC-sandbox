package com.backbase.accesscontrol.business.functiongroup;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.FunctionGroupPAndPService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupBatchTest {

    @Mock
    private FunctionGroupPAndPService functionGroupPAndPService;

    @InjectMocks
    private DeleteFunctionGroupBatch deleteFunctionGroupBatch;

    @Test
    public void shouldDeleteFunctionGroup() {
        String fg1 = "FG-01";
        String fg2 = "FG-02";
        PresentationIdentifier body1 = new PresentationIdentifier().withIdIdentifier(fg1);
        PresentationIdentifier body2 = new PresentationIdentifier().withIdIdentifier(fg2);

        InternalRequest<List<PresentationIdentifier>> request = getInternalRequest(Arrays.asList(body1, body2));

        BatchResponseItemExtended errorResponseItem = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList("error"))
            .withResourceId(fg2);
        BatchResponseItemExtended successfulResponseItem = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId(fg1);
        List<BatchResponseItemExtended> mockResponseData = asList(successfulResponseItem, errorResponseItem);

        mockResponseFromServiceDeleteFG(Arrays.asList(body1, body2), mockResponseData);

        InternalRequest<List<BatchResponseItemExtended>> response = deleteFunctionGroupBatch
            .deleteFunctionGroup(request);

        assertEquals(mockResponseData.size(), response.getData().size());
        assertTrue(containsFailedResponseItem(response, errorResponseItem));
        assertTrue(containsSuccessfulResponseItem(response, successfulResponseItem));

    }

    private void mockResponseFromServiceDeleteFG(List<PresentationIdentifier> request,
        List<BatchResponseItemExtended> responseData) {

        when(functionGroupPAndPService.deleteFunctionGroup(request))
            .thenReturn(responseData);
    }
}
