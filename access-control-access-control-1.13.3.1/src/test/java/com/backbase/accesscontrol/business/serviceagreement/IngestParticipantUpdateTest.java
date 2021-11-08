package com.backbase.accesscontrol.business.serviceagreement;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.serviceagreement.participant.IngestParticipantUpdateProcessor;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestParticipantUpdateTest {

    @InjectMocks
    private IngestParticipantUpdate ingestParticipantUpdate;
    @Mock
    private IngestParticipantUpdateProcessor ingestParticipantUpdateProcessor;

    @Test
    public void shouldInvokeProcessor() {
        PresentationParticipantsPut body = new PresentationParticipantsPut()
            .withParticipants(asList(
                new PresentationParticipantPutBody().withExternalParticipantId("exId")
                    .withExternalServiceAgreementId("exSaId")
                    .withAction(PresentationAction.ADD)
            ));
        List<BatchResponseItemExtended> response = Lists.newArrayList(new BatchResponseItemExtended()
            .withAction(PresentationAction.ADD)
            .withExternalServiceAgreementId("exSaId")
            .withResourceId("exId")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
            .withErrors(new ArrayList<>()));
        when(ingestParticipantUpdateProcessor.processParticipantUpdate(any())).thenReturn(response);

        InternalRequest<List<BatchResponseItemExtended>> listInternalRequest = ingestParticipantUpdate
            .updateParticipants(RequestUtils.getInternalRequest(body));
        assertThat(listInternalRequest.getData(), is(response));
    }
}