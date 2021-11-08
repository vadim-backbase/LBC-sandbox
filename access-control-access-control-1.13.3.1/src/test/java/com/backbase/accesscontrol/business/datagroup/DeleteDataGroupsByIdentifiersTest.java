package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.matchers.MatcherUtil.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.MatcherUtil.containsSuccessfulResponseItem;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.DataGroupPAndPService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataGroupsByIdentifiersTest {

    @Mock
    private DataGroupPAndPService dataGroupPAndPService;

    private DeleteDataGroupsByIdentifiers deleteDataGroupsByIdentifiers;

    @Before
    public void setUp() throws Exception {
        deleteDataGroupsByIdentifiers = new DeleteDataGroupsByIdentifiers(dataGroupPAndPService);
    }

    @Test
    public void shouldDeleteDataGroupsByIdentifiers() {
        String dgId = "DG-01";
        String name = "dg-name";
        String externalServiceAgreementId = "ex-sa";
        List<PresentationIdentifier> putData = asList(new PresentationIdentifier()
                .withIdIdentifier(dgId),
            new PresentationIdentifier()
                .withNameIdentifier(new NameIdentifier()
                    .withName(name)
                    .withExternalServiceAgreementId(externalServiceAgreementId)));

        BatchResponseItemExtended errorResponseItem = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList("error"))
            .withResourceId(dgId);
        BatchResponseItemExtended successfulResponseItem = new BatchResponseItemExtended()
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withResourceId(name)
            .withExternalServiceAgreementId(externalServiceAgreementId);
        List<BatchResponseItemExtended> mockResponseData = asList(successfulResponseItem, errorResponseItem);

        when(dataGroupPAndPService.deleteDataGroupsByIdentifiers(putData)).thenReturn(mockResponseData);

        InternalRequest<List<BatchResponseItemExtended>> response = deleteDataGroupsByIdentifiers
            .deleteDataGroupsByIdentifiers(getInternalRequest(putData));

        assertEquals(mockResponseData.size(), response.getData().size());
        assertTrue(containsFailedResponseItem(response, errorResponseItem));
        assertTrue(containsSuccessfulResponseItem(response, successfulResponseItem));
    }
}
