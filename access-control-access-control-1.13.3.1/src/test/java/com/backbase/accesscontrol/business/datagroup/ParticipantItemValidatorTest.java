package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_056;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.business.service.ArrangementsService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class ParticipantItemValidatorTest {

    private static final String ARRANGEMENTS = "ARRANGEMENTS";
    private static final String INVALID_ARRANGMENT_MESSAGE = "Participant can not be removed. Please remove arrangement with id %s first";
    private ArrangementsService arrangementsService;
    private ValidationConfig validationConfig;
    private ParticipantItemValidator participantItemValidator;
    private DataGroupService dataGroupService;

    @Before
    public void setUp() throws Exception {
        this.arrangementsService = mock(ArrangementsService.class);
        this.validationConfig = mock(ValidationConfig.class);
        when(validationConfig.getTypes())
            .thenReturn(newArrayList(ARRANGEMENTS));
        participantItemValidator = new ParticipantItemValidator(validationConfig,
            arrangementsService, dataGroupService);
        when(arrangementsService
            .getArrangementsLegalEntities(anyList(), anyList()))
            .thenReturn(new AccountArrangementsLegalEntities());
    }

    @Test
    public void shouldProcessArrangementsWhenThereIsArrangementsType() {
        ParticipantItemValidator participantItemValidator = new ParticipantItemValidator(validationConfig,
            arrangementsService, dataGroupService);
        Object arrangementTypeSupported = ReflectionTestUtils
            .getField(participantItemValidator, "arrangementTypeSupported");
        assertTrue(Boolean.class.cast(arrangementTypeSupported));
    }

    @Test
    public void shouldNotProcessArrangementsWhenThereIsNoArrangementsType() {
        when(validationConfig.getTypes())
            .thenReturn(newArrayList("Something else"));

        ParticipantItemValidator participantItemValidator = new ParticipantItemValidator(validationConfig,
            arrangementsService, dataGroupService);
        Object arrangementTypeSupported = ReflectionTestUtils
            .getField(participantItemValidator, "arrangementTypeSupported");
        assertFalse(Boolean.class.cast(arrangementTypeSupported));
    }

    @Test
    public void shouldRetrieveDataGroups() {
        PersistenceDataGroupExtendedItemDto item1 = new PersistenceDataGroupExtendedItemDto();
        item1.setType(ARRANGEMENTS);
        item1.setItems(newArrayList("i1", "i2"));
        PersistenceDataGroupExtendedItemDto item2 = new PersistenceDataGroupExtendedItemDto();
        item2.setType(ARRANGEMENTS);
        item2.setItems(newArrayList("i3", "i4"));
        PersistenceDataGroupExtendedItemDto item3 = new PersistenceDataGroupExtendedItemDto();
        item3.setType("other-type");
        item3.setItems(newArrayList("o1", "o1"));
        PersistenceDataGroupExtendedItemDto item4 = new PersistenceDataGroupExtendedItemDto();
        item4.setType("other-type");
        item4.setItems(newArrayList("o3", "o4"));
        PersistenceDataGroupExtendedItemDto item5 = new PersistenceDataGroupExtendedItemDto();
        item5.setType(ARRANGEMENTS);
        item5.setItems(newArrayList("i5", "i6"));
        PersistenceDataGroupExtendedItemDto item6 = new PersistenceDataGroupExtendedItemDto();
        item6.setType(ARRANGEMENTS);
        item6.setItems(newArrayList("i7", "i8"));
        PersistenceExtendedParticipant participant = new PersistenceExtendedParticipant();
        participant.setExternalId("p1");
        participant.setExternalServiceAgreementId("1");
        participant.setId("l1");
        PersistenceExtendedParticipant participant2 = new PersistenceExtendedParticipant();
        participant2.setExternalId("p2");
        participant2.setExternalServiceAgreementId("2");
        participant2.setId("l2");
        PersistenceExtendedParticipant participant3 = new PersistenceExtendedParticipant();
        participant3.setExternalId("p3");
        participant3.setExternalServiceAgreementId("3");
        participant3.setId("l3");
        PersistenceExtendedParticipant participant4 = new PersistenceExtendedParticipant();
        participant4.setExternalId("p4");
        participant4.setExternalServiceAgreementId("4");
        participant4.setId("l4");
        List<DataItemValidatableItem> body = newArrayList(
            getDataItemValidatableItem(
                new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                    .withAction(PresentationAction.REMOVE)
                    .withExternalParticipantId("p1")
                    .withExternalServiceAgreementId("1"), 1),
                participant,
                item1,
                item2,
                item3
            ),
            getDataItemValidatableItem(
                new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                    .withAction(PresentationAction.REMOVE)
                    .withExternalParticipantId("p2")
                    .withExternalServiceAgreementId("2"), 2),
                participant2,
                item1,
                item2
            ),
            getDataItemValidatableItem(
                new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                    .withAction(PresentationAction.REMOVE)
                    .withExternalParticipantId("p3")
                    .withExternalServiceAgreementId("3"), 3),
                participant3,
                item3,
                item4
            ),
            getDataItemValidatableItem(
                new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                    .withAction(PresentationAction.REMOVE)
                    .withExternalParticipantId("p4")
                    .withExternalServiceAgreementId("4"), 4),
                participant4,
                item5,
                item6
            )
        );
        AccountArrangementsLegalEntities persistenceArrangementsLegalEntitiesBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(
                newArrayList(
                    new AccountPresentationArrangementLegalEntityIds().arrangementId("i1")
                        .legalEntityIds(singletonList("l1")),
                    new AccountPresentationArrangementLegalEntityIds().arrangementId("i2")
                        .legalEntityIds(singletonList("l1")),
                    new AccountPresentationArrangementLegalEntityIds().arrangementId("i3")
                        .legalEntityIds(singletonList("l1")),
                    new AccountPresentationArrangementLegalEntityIds().arrangementId("i4")
                        .legalEntityIds(singletonList("l1"))
                )
            );
        ArrayList<String> arrangementIds = newArrayList("i1", "i2", "i3", "i4", "i5", "i6", "i7", "i8");
        ArrayList<String> legalEntityIds = newArrayList("l1", "l2", "l4");

        when(arrangementsService.getArrangementsLegalEntities(eq(arrangementIds), eq(legalEntityIds)))
            .thenReturn(persistenceArrangementsLegalEntitiesBody);

        List<InvalidParticipantItem> invalidItemsSharingAccounts = participantItemValidator
            .getInvalidItemsSharingAccounts(RequestUtils.getInternalRequest(body));
        assertThat(invalidItemsSharingAccounts, hasSize(1));
        assertThat(invalidItemsSharingAccounts,
            hasItem(
                allOf(
                    hasProperty("order", is(1)),
                    hasProperty("errors",
                        containsInAnyOrder(
                            String.format(INVALID_ARRANGMENT_MESSAGE, "i1"),
                            String.format(INVALID_ARRANGMENT_MESSAGE, "i2"),
                            String.format(INVALID_ARRANGMENT_MESSAGE, "i3"),
                            String.format(INVALID_ARRANGMENT_MESSAGE, "i4")
                        )
                    )
                )
            )
        );
    }

    @Test
    public void shouldThrowBadRequestWhenNoArrangementItemsAndLegalEntityToStayArePresentWhenRemovingParticipants() {
        List<String> arrangementItems = singletonList("arrangement1");
        List<String> legalEntityIdsToStay = new ArrayList<>();

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> participantItemValidator.canRemoveParticipantSharingAccounts(arrangementItems, legalEntityIdsToStay));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_056.getErrorMessage(), ERR_ACC_056.getErrorCode()));
    }

    @Test
    public void canRemoveParticipantIfNoArrangementsArePresent() {
        List<String> arrangementItems = new ArrayList<>();
        List<String> legalEntityIdsToStay = singletonList("leId");

        boolean canRemove = participantItemValidator
            .canRemoveParticipantSharingAccounts(arrangementItems, legalEntityIdsToStay);

        assertTrue(canRemove);
    }

    @Test
    public void shouldReturnEmptyListWhenNoDataGroupOfTypeArrangementsIsPresent() {
        PersistenceExtendedParticipant participant3 = new PersistenceExtendedParticipant();
        participant3.setExternalId("p3");
        participant3.setExternalServiceAgreementId("3");
        participant3.setId("l3");
        PersistenceDataGroupExtendedItemDto item3 = new PersistenceDataGroupExtendedItemDto();
        item3.setType("other-type");
        item3.setItems(newArrayList("o1", "o1"));
        PersistenceDataGroupExtendedItemDto item4 = new PersistenceDataGroupExtendedItemDto();
        item4.setType("other-type");
        item4.setItems(newArrayList("o3", "o4"));
        List<DataItemValidatableItem> body = newArrayList(
            getDataItemValidatableItem(
                new ProcessableBatchBody<>(new PresentationParticipantPutBody()
                    .withAction(PresentationAction.REMOVE)
                    .withExternalParticipantId("p3")
                    .withExternalServiceAgreementId("3"), 3),
                participant3,
                item3,
                item4
            )
        );

        List<InvalidParticipantItem> invalidItemsSharingAccounts = participantItemValidator
            .getInvalidItemsSharingAccounts(RequestUtils.getInternalRequest(body));
        assertThat(invalidItemsSharingAccounts, hasSize(0));
        verify(arrangementsService, never()).getArrangementsLegalEntities(anyList(), anyList());
    }

    private DataItemValidatableItem getDataItemValidatableItem(
        ProcessableBatchBody<PresentationParticipantPutBody> batchBody,
        PersistenceExtendedParticipant persistenceExtendedParticipant,
        PersistenceDataGroupExtendedItemDto... PersistenceDataGroupExtendedItemDto) {
        return new DataItemValidatableItem(
            batchBody,
            persistenceExtendedParticipant,
            Sets.newHashSet(PersistenceDataGroupExtendedItemDto)
        );
    }
}