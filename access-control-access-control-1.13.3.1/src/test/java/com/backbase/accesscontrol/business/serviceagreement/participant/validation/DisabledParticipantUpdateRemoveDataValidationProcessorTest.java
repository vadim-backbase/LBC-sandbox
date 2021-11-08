package com.backbase.accesscontrol.business.serviceagreement.participant.validation;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_086;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DisabledParticipantUpdateRemoveDataValidationProcessorTest {

    @InjectMocks
    private DisabledParticipantUpdateRemoveDataValidationProcessor disabledParticipantUpdateRemoveDataValidationProcessor;

    @Test
    public void validateParticipants() {
        List<InvalidParticipantItem> invalidParticipantItems = disabledParticipantUpdateRemoveDataValidationProcessor
            .processValidateParticipants(Lists.newArrayList(
                new DataItemValidatableItem(
                    new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                        .withExternalServiceAgreementId("s1")
                        .withAction(PresentationAction.REMOVE)
                        .withExternalParticipantId("p1"), 5),
                    null,
                    Sets.newHashSet(new PersistenceDataGroupExtendedItemDto())
                ),
                new DataItemValidatableItem(
                    new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                        .withExternalServiceAgreementId("s2")
                        .withAction(PresentationAction.REMOVE)
                        .withExternalParticipantId("p2"), 6),
                    null,
                    Sets.newHashSet()
                ), new DataItemValidatableItem(
                    new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                        .withExternalServiceAgreementId("s2")
                        .withAction(PresentationAction.REMOVE)
                        .withExternalParticipantId("p2"), 7),
                    null,
                    null
                )
            ));

        assertThat(invalidParticipantItems, hasSize(1));
        assertThat(invalidParticipantItems,
            contains(
                allOf(
                    hasProperty("order", is(5)),
                    hasProperty("errors", is(Lists.newArrayList(ERR_AG_086.getErrorMessage())))
                )
            )
        );


    }
}