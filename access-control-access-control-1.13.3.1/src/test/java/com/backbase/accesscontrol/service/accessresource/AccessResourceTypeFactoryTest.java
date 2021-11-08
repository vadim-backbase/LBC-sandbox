package com.backbase.accesscontrol.service.accessresource;

import static com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory.ACCOUNT;
import static com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory.NONE;
import static com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory.USER;
import static com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory.USER_AND_ACCOUNT;
import static com.backbase.accesscontrol.service.accessresource.AccessResourceTypeFactory.USER_OR_ACCOUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccessResourceTypeFactoryTest {

    private List<Participant> participants;

    @Before
    public void setUp() {
        participants = Lists.newArrayList(
            createParticipant("leId-1", true, false),
            createParticipant("leId-2", true, false),
            createParticipant("leId-3", false, true),
            createParticipant("leId-4", false, true),
            createParticipant("leId-5", true, true)
        );
    }

    @Test
    public void shouldReturnEmptyListForNoneResourceType() {
        List<String> validLegalEntities = NONE.getValidResources(participants, "leId-1");
        assertThat(validLegalEntities, hasSize(0));
    }

    @Test
    public void shouldReturnListOfUsers() {
        List<String> validLegalEntities = AccessResourceTypeFactory.USER.getValidResources(
            participants, "leId-1");

        assertThat(validLegalEntities, hasSize(3));
        assertThat(validLegalEntities, hasItems(
            "leId-1",
            "leId-2",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListOfUserWhenNotSharingLegalEntity() {
        List<String> validLegalEntities = USER.getValidResources(participants, "leId-3");
        assertThat(validLegalEntities, hasSize(0));

    }

    @Test
    public void shouldReturnZeroElementForOfUsersWhenOtherLegalEntitySent() {
        List<String> validLegalEntities = USER.getValidResources(participants, "leId-other");
        assertThat(validLegalEntities, hasSize(0));
    }

    @Test
    public void shouldReturnListForOfAccount() {
        List<String> validLegalEntities = ACCOUNT.getValidResources(participants, "leId-3");
        assertThat(validLegalEntities, hasSize(3));
        assertThat(validLegalEntities, hasItems(
            "leId-3",
            "leId-4",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListForOfAccountWhenNotSharingLegalEntity() {
        List<String> validLegalEntities = ACCOUNT.getValidResources(participants, "leId-1");
        assertThat(validLegalEntities, hasSize(0));

    }

    @Test
    public void shouldReturnZeroElementForOfUserAndAccountWhenOtherLegalEntitySent() {
        List<String> validLegalEntities = USER_OR_ACCOUNT.getValidResources(participants, "leId-other");
        assertThat(validLegalEntities, hasSize(0));
    }

    @Test
    public void shouldReturnListForOfUserAndAccountSharingAccount() {
        List<String> validLegalEntities = USER_OR_ACCOUNT.getValidResources(participants, "leId-3");
        assertThat(validLegalEntities, hasSize(3));
        assertThat(validLegalEntities, hasItems(
            "leId-3",
            "leId-4",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListForOfUserAndAccountSharingUser() {
        List<String> validLegalEntities = USER_OR_ACCOUNT.getValidResources(participants, "leId-1");
        assertThat(validLegalEntities, hasSize(3));
        assertThat(validLegalEntities, hasItems(
            "leId-1",
            "leId-2",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListForOfUserAndAccountSharingUserAndAccount() {
        List<String> validLegalEntities = USER_OR_ACCOUNT.getValidResources(participants, "leId-5");
        assertThat(validLegalEntities, hasSize(5));
        assertThat(validLegalEntities, hasItems(
            "leId-1",
            "leId-2",
            "leId-3",
            "leId-4",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListForOfUserAndAccountWhenNotSharingLegalEntity() {
        List<String> validLegalEntities = USER_OR_ACCOUNT.getValidResources(participants, "other-legal-entity");
        assertThat(validLegalEntities, hasSize(0));

    }

    @Test
    public void shouldReturnListOfAllParticipantIdsForShareUsers() {

        List<String> validLegalEntities = USER_AND_ACCOUNT.getValidResources(participants, "leId-1");
        assertThat(validLegalEntities, hasSize(5));
        assertThat(validLegalEntities, hasItems(
            "leId-1",
            "leId-2",
            "leId-3",
            "leId-4",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnListOfAllParticipantIdsForShareAccounts() {

        List<String> validLegalEntities = USER_AND_ACCOUNT.getValidResources(participants, "leId-3");
        assertThat(validLegalEntities, hasSize(5));
        assertThat(validLegalEntities, hasItems(
            "leId-1",
            "leId-2",
            "leId-3",
            "leId-4",
            "leId-5"
        ));
    }

    @Test
    public void shouldReturnEmptyListIfLegalEntityIsNotParticipant() {

        List<String> validLegalEntities = USER_AND_ACCOUNT.getValidResources(participants, "other-le");
        assertThat(validLegalEntities, hasSize(0));
    }

    private Participant createParticipant(String legalEntityId, boolean shareUsers, boolean shareAccounts) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(legalEntityId);
        Participant participant = new Participant();
        participant.setId(legalEntityId);
        participant.setLegalEntity(legalEntity);
        participant.setShareUsers(shareUsers);
        participant.setShareAccounts(shareAccounts);
        return participant;
    }
}