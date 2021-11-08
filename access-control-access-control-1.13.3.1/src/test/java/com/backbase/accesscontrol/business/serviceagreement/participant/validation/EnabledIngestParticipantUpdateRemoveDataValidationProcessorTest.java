package com.backbase.accesscontrol.business.serviceagreement.participant.validation;

import static com.backbase.accesscontrol.matchers.MatcherUtil.hasPayload;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.routes.serviceagreement.RemoveParticipantSharingAccountRouteProxy;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnabledIngestParticipantUpdateRemoveDataValidationProcessorTest {

    @InjectMocks
    private EnabledIngestParticipantUpdateRemoveDataValidationProcessor enabledIngestParticipantUpdateRemoveDataValidationProcessor;
    @Mock
    private RemoveParticipantSharingAccountRouteProxy removeParticipantSharingAccountRouteProxy;

    @Test
    public void shouldFilterParticipantsSharingAccountsWithDataGroups() {
        List<DataItemValidatableItem> dataItemValidatableItems = Lists
            .emptyList();
        ArrayList<InvalidParticipantItem> response = new ArrayList<>();
        when(removeParticipantSharingAccountRouteProxy
            .getInvalidItemsSharingAccounts(argThat(hasPayload(dataItemValidatableItems))))
            .thenReturn(response);
        List<InvalidParticipantItem> invalidParticipantItems =
            enabledIngestParticipantUpdateRemoveDataValidationProcessor
                .processValidateParticipants(dataItemValidatableItems);
        assertThat(invalidParticipantItems, is(response));
    }
}