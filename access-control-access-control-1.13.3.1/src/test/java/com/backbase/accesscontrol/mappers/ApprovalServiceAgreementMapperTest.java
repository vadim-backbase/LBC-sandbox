package com.backbase.accesscontrol.mappers;


import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalServiceAgreementMapperTest {

    @InjectMocks
    private com.backbase.accesscontrol.mappers.ApprovalServiceAgreementMapperImpl approvalServiceAgreementMapper;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Test
    public void shouldConvertServiceAgreementPostRequestBodyToApprovalServiceAgreement() {
        String approvalId = "approvalId";
        String creatorLeId = "leId";
        HashMap<String, String> additions = new HashMap<>() {{
            put("add-01", "add-01-01");
            put("add-012", "add-01-02");
        }};

        ServiceAgreementPostRequestBody addServiceAgreementRequestBody = new ServiceAgreementPostRequestBody()
            .withName("name")
            .withDescription("description")
            .withStatus(CreateStatus.DISABLED)
            .withValidFromDate("2019-03-10")
            .withValidFromTime("01:10:05")
            .withValidUntilDate("2019-05-16")
            .withValidUntilTime("10:10:10");
        addServiceAgreementRequestBody.withParticipants(asList(
            new Participant().withId("LE-01").withSharingAccounts(false).withSharingUsers(true)
                .withAdmins(newHashSet("admin1", "admin2")),
            new Participant().withId("LE-02").withSharingAccounts(true).withSharingUsers(true)
                .withAdmins(newHashSet("admin3", "admin4"))
        ));
        addServiceAgreementRequestBody.setAdditions(additions);

        ApprovalServiceAgreement approvalServiceAgreement = approvalServiceAgreementMapper
            .serviceAgreementPostRequestBodyToApprovalServiceAgreement(addServiceAgreementRequestBody, creatorLeId,
                approvalId);

        List<Entry<String, String>> entries = new ArrayList<>(addServiceAgreementRequestBody.getAdditions().entrySet());

        assertThat(approvalServiceAgreement,
            allOf(
                hasProperty("approvalId", is(approvalId)),
                hasProperty("creatorLegalEntityId", is(creatorLeId)),
                hasProperty("name", is(addServiceAgreementRequestBody.getName())),
                hasProperty("description", is(addServiceAgreementRequestBody.getDescription())),
                hasProperty("master", is(false)),
                hasProperty("state", is(ServiceAgreementState.DISABLED)),
                hasProperty("startDate", is(dateTimeService
                    .getStartDateFromDateAndTime(addServiceAgreementRequestBody.getValidFromDate(),
                        addServiceAgreementRequestBody.getValidFromTime()))),
                hasProperty("endDate", is(dateTimeService
                    .getEndDateFromDateAndTime(addServiceAgreementRequestBody.getValidUntilDate(),
                        addServiceAgreementRequestBody.getValidUntilTime()))),
                hasProperty("additions", hasEntry(is(entries.get(0).getKey()), is(entries.get(0).getValue()))),
                hasProperty("additions", hasEntry(is(entries.get(1).getKey()), is(entries.get(1).getValue())))));

        List<ApprovalServiceAgreementParticipant> approvalParticipants = new ArrayList<>(
            approvalServiceAgreement.getParticipants());
        validateParticipants(addServiceAgreementRequestBody.getParticipants().get(0), approvalParticipants.get(1));
        validateParticipants(addServiceAgreementRequestBody.getParticipants().get(1), approvalParticipants.get(0));
    }

    private void validateParticipants(Participant participant,
        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant) {

        assertEquals(participant.getId(), approvalServiceAgreementParticipant.getLegalEntityId());
        assertEquals(participant.getSharingAccounts(), approvalServiceAgreementParticipant.isShareAccounts());
        assertEquals(participant.getSharingUsers(), approvalServiceAgreementParticipant.isShareUsers());
        assertTrue(participant.getAdmins().containsAll(approvalServiceAgreementParticipant.getAdmins()));
    }
}