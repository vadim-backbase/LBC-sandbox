package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementAdmin;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementController#getServiceAgreementAdmins(String, HttpServletRequest,
 * HttpServletResponse)}
 */
public class ListAdminsForServiceAgreementIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements/{serviceAgreementId}/admins";
    String getUserUrl = "/service-api/v2/users/bulk";;
    private LegalEntity rootLegalEntity2;
    private LegalEntity rootLegalEntity3;
    private ServiceAgreement rootMsa2;

    @Before
    public void setUp() {

        rootLegalEntity2 = legalEntityJpaRepository.save(new LegalEntity()
            .withExternalId("BANK002")
            .withName("BANK2")
            .withType(LegalEntityType.BANK));
        rootLegalEntity3 = legalEntityJpaRepository.save(new LegalEntity()
            .withExternalId("BANK003")
            .withName("BANK3")
            .withType(LegalEntityType.BANK));

        Participant saveParticipant = (new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(rootLegalEntity2))
            .withServiceAgreement(rootMsa);
        participantJpaRepository.save(saveParticipant);

        ServiceAgreementAdmin serviceAgreementAdmin = new ServiceAgreementAdmin()
            .withUserId(contextUserId).withParticipant(saveParticipant);
        Map admins = new HashMap<>();
        admins.put(contextUserId, serviceAgreementAdmin);

        rootMsa2 = new ServiceAgreement()
            .withName("rootMsa2")
            .withDescription("rootMsa2")
            .withExternalId("externalRootMsa2")
            .withCreatorLegalEntity(rootLegalEntity3)
            .withMaster(true);
        rootMsa2.addParticipant(new Participant()
            .withAdmins(admins)
            .withLegalEntity(rootLegalEntity3)
            .withShareUsers(true)
            .withShareAccounts(true));
        rootMsa2 = serviceAgreementJpaRepository.save(rootMsa2);
    }

    @Test
    public void testGetAdminsForServiceAgreement() throws Exception {
        com.backbase.dbs.user.api.client.v2.model.GetUsersList list = new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setFullName("USER");
        user.setExternalId("ExternalId");
        user.setLegalEntityId(rootMsa.getCreatorLegalEntity().getId());
        user.setId(contextUserId);
        list.setUsers(Collections.singletonList(user));
        list.setTotalElements(1L);

        addStubGet(new UrlBuilder(getUserUrl)
                .addQueryParameter("id", contextUserId)
                .build(),
            list, 200);
        String contentAsString = executeClientRequest(
            new UrlBuilder(URL)
                .addPathParameter(rootMsa.getId())
                .build(), HttpMethod.GET, "USER", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<ServiceAgreementUsersGetResponseBody> result = objectMapper.readValue(contentAsString,
            new TypeReference<>() {
            });

        assertEquals(1, result.size());
        assertEquals(contextUserId, result.get(0).getId());
    }
}

