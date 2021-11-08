package com.backbase.accesscontrol.business.serviceagreement.participant;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_087;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.business.service.AgreementsPersistenceService;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestParticipantUpdateRemoveProcessorTest {

    private IngestParticipantUpdateRemoveProcessor removeProcessor;
    private AgreementsPersistenceService agreementsPersistenceService;

    @Before
    public void setUp() throws Exception {
        DataGroupService dataGroupService = mock(DataGroupService.class);
        IngestParticipantUpdateRemoveDataValidationProcessor validationProcessor = mock(
            IngestParticipantUpdateRemoveDataValidationProcessor.class);
        agreementsPersistenceService = mock(AgreementsPersistenceService.class);
        removeProcessor = new IngestParticipantUpdateRemoveProcessor(agreementsPersistenceService,
            dataGroupService, validationProcessor);
    }

    @Test
    public void shouldFilterDataGroupsByRemoveAndRetrieveAllDataGroupsLinkedWithThem() {

        ProcessableBatchBody<PresentationParticipantPutBody> validatableRemoveBody
            = new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.REMOVE, "1", "p1"), 1);
        PersistenceExtendedParticipant validParticipantWithDataGroup = createParticipant("p1", "1", true, false);
        HashSet<PersistenceDataGroupExtendedItemDto> validParticipantDataGroup = Sets
            .newHashSet(new PersistenceDataGroupExtendedItemDto());
        DataItemValidatableItem dataItemValidatableItem = new DataItemValidatableItem(validatableRemoveBody,
            validParticipantWithDataGroup, validParticipantDataGroup);

        List<ProcessableBatchBody<PresentationParticipantPutBody>> processableBatchBodies = newArrayList(
            validatableRemoveBody,
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.ADD, "2", "p2"), 2),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.ADD, "3", "p3"), 3),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.REMOVE, "4", "p4"), 4),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.REMOVE, "5", "p5"), 5),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.ADD, "6", "p6"), 6),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.ADD, "7", "p7"), 7),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.REMOVE, "8", "p8"), 8),
            new ProcessableBatchBody<>(createParticipantPutBody(PresentationAction.REMOVE, "9", "p9"), 9)
        );

        HashSet<String> serviceAgreementExternalIds = Sets.newHashSet("1", "4", "5", "8", "9");

        Map<String, Set<PersistenceExtendedParticipant>> particiapntsPerExternalId = ImmutableMap.<String, Set<PersistenceExtendedParticipant>>builder()
            .put("p1", Sets.newHashSet(
                validParticipantWithDataGroup,
                createParticipant("p1", "4", true, false)
                )
            ).put("p4", Sets.newHashSet(
                createParticipant("p4", "2", true, false),
                createParticipant("p4", "1", true, false)
                )
            ).put("p5", Sets.newHashSet(
                createParticipant("p5", "2", true, false),
                createParticipant("p5", "5", false, true)
                )
            ).put("p9", Sets.newHashSet(
                createParticipant("p9", "9", true, false)
                )
            )
            .build();

        when(agreementsPersistenceService
            .getParticipantsPerExternalId(eq(serviceAgreementExternalIds)))
            .thenReturn(particiapntsPerExternalId);

        List<InvalidParticipantItem> invalidParticipantItems = removeProcessor.processItems(processableBatchBodies);

        assertThat(invalidParticipantItems, hasSize(2));
        assertThat(invalidParticipantItems, containsInAnyOrder(
            allOf(
                hasProperty("order", is(4)),
                hasProperty("errors", is(Lists.newArrayList(ERR_AG_087.getErrorMessage())))
            ),
            allOf(
                hasProperty("order", is(8)),
                hasProperty("errors", is(Lists.newArrayList(ERR_AG_087.getErrorMessage())))
            )
        ));
    }

    private PersistenceExtendedParticipant createParticipant(String externalId, String withExternalServiceAgreementId,
        boolean sharingAccounts, boolean sharingUsers) {
        PersistenceExtendedParticipant item = new PersistenceExtendedParticipant();
        item.setExternalId(externalId);
        item.setExternalServiceAgreementId(withExternalServiceAgreementId);
        item.setSharingAccounts(sharingAccounts);
        item.setSharingUsers(sharingUsers);
        return item;
    }

    private PresentationParticipantPutBody createParticipantPutBody(PresentationAction remove,
        String externalServiceAgreementId, String externalParticipantId) {
        return new PresentationParticipantPutBody()
            .withAction(remove)
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withExternalParticipantId(externalParticipantId);
    }
}