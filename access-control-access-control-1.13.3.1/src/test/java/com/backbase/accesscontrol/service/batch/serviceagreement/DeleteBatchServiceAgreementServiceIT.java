package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_053;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_054;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_055;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.business.serviceagreement.DeleteBatchServiceAgreement;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.AccessTokenGenerator;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {"backbase.accesscontrol.token.expiration=300"})
public class DeleteBatchServiceAgreementServiceIT {

    @Autowired
    private RepositoryCleaner repositoryCleaner;
    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Autowired
    private UserContextJpaRepository userContextJpaRepository;
    @Autowired
    private DeleteBatchServiceAgreement deleteBatchServiceAgreement;
    @Autowired
    private ParticipantJpaRepository participantJpa;
    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Autowired
    private AccessTokenGenerator accessTokenGenerator;
    @Autowired
    private BusinessFunctionCache businessFunctionCache;

    private String serviceAgreementInternalId = "";

    @Before
    public void setUp() {
        repositoryCleaner.clean();
        createDataLegalEntityAndServiceAgreement();
        createDataLegalEntityCreatorOfServiceAgreement();
        createDataUserAssignedFunctionGroupsInServiceAgreement();
        createDataUserPendingPrivilegesInServiceAgreement();
    }

    @Test
    public void checkDeleteWithExpiredToken() {
        String serviceAgreementExternalId = "sa1";
        PresentationServiceAgreementIdentifier identifier = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(serviceAgreementExternalId);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(singletonList(identifier), accessTokenGenerator.generateExpiredToken()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_054.getErrorMessage(), ERR_ACQ_054.getErrorCode()));
    }

    @Test
    public void checkIfServiceAgreementsAreDeletedAndSameOrderIsPreserved() {

        String serviceAgreementExternalId = "saOne";
        String serviceAgreementName = "saThree";

        PresentationServiceAgreementIdentifier identifierByExternalId = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(serviceAgreementExternalId);
        PresentationServiceAgreementIdentifier identifierByInternalId = new PresentationServiceAgreementIdentifier()
            .withIdIdentifier(serviceAgreementInternalId);
        PresentationServiceAgreementIdentifier identifierByName = new PresentationServiceAgreementIdentifier()
            .withNameIdentifier(serviceAgreementName);

        List<BatchResponseItem> response = executeRequest(
            asList(identifierByExternalId, identifierByInternalId, identifierByName),
            accessTokenGenerator.generateValidToken());

        Optional<ServiceAgreement> serviceAgreementByExternalId = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId,
                GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        Optional<ServiceAgreement> serviceAgreementByInternalId = serviceAgreementJpaRepository
            .findById(serviceAgreementInternalId,
                GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);
        List<ServiceAgreement> serviceAgreementsByName = serviceAgreementJpaRepository
            .findServiceAgreementsByName(serviceAgreementName);

        assertFalse(serviceAgreementByInternalId.isPresent());
        assertFalse(serviceAgreementByExternalId.isPresent());
        assertTrue(serviceAgreementsByName.isEmpty());
        assertThat(response,
            hasItems(
                getBatchResponseItemMatcher(serviceAgreementExternalId, BatchResponseStatusCode.HTTP_STATUS_OK,
                    new ArrayList<>()),
                getBatchResponseItemMatcher(serviceAgreementInternalId, BatchResponseStatusCode.HTTP_STATUS_OK,
                    new ArrayList<>()),
                getBatchResponseItemMatcher(serviceAgreementName, BatchResponseStatusCode.HTTP_STATUS_OK,
                    new ArrayList<>())
            )
        );
        assertEquals(3, response.size());
        assertTrue(response.get(0).getResourceId().equals(serviceAgreementExternalId));
        assertTrue(response.get(1).getResourceId().equals(serviceAgreementInternalId));
        assertTrue(response.get(2).getResourceId().equals(serviceAgreementName));
    }

    @Test
    public void checkIfExistsUsersFromSAWithAssignedPermissionsInServiceAgreement() {

        String serviceAgreementExternalId = "sa4";

        PresentationServiceAgreementIdentifier identifierByExternalId = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(serviceAgreementExternalId);
        List<BatchResponseItem> response = executeRequest(singletonList(identifierByExternalId),
            accessTokenGenerator.generateValidToken());

        Optional<ServiceAgreement> serviceAgreement = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId,
                GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

        assertTrue(serviceAgreement.isPresent());
        assertEquals(serviceAgreementExternalId, serviceAgreement.get().getExternalId());
        assertThat(response,
            hasItems(
                getBatchResponseItemMatcher(serviceAgreementExternalId, BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST,
                    singletonList(ERR_ACQ_053.getErrorMessage())
                )
            )
        );
    }

    @Test
    public void checkIfExistsUsersFromLegalEntityWithPendingPermissionsInServiceAgreement() {

        String serviceAgreementExternalId = "sa6";
        PresentationServiceAgreementIdentifier identifierByExternalId = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier(serviceAgreementExternalId);
        List<BatchResponseItem> response = executeRequest(singletonList(identifierByExternalId),
            accessTokenGenerator.generateValidToken());

        Optional<ServiceAgreement> serviceAgreement = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId,
                GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR);

        assertTrue(serviceAgreement.isPresent());
        assertEquals(serviceAgreementExternalId, serviceAgreement.get().getExternalId());
        assertThat(response,
            hasItems(
                getBatchResponseItemMatcher(serviceAgreementExternalId, BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST,
                    singletonList(ERR_ACQ_055.getErrorMessage())
                )
            )
        );
    }

    private void createDataLegalEntityAndServiceAgreement() {

        LegalEntity le2 = LegalEntityUtil
            .createLegalEntity(null, "le2", "le2", null, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(le2);
        LegalEntity le21 = LegalEntityUtil
            .createLegalEntity(null, "le21", "le21", null, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(le21);

        ServiceAgreement saOne = ServiceAgreementUtil
            .createServiceAgreement("saOne", "saOne", "-", le2, le2.getId(), le2.getId());
        saOne.setMaster(false);
        serviceAgreementJpaRepository.save(saOne);
        ServiceAgreement saTwo = ServiceAgreementUtil
            .createServiceAgreement("saTwo", "saTwo", "-", le2, le2.getId(), le2.getId());
        saTwo.setMaster(false);
        ServiceAgreement save = serviceAgreementJpaRepository.save(saTwo);
        serviceAgreementInternalId = save.getId();
        ServiceAgreement saThree = ServiceAgreementUtil
            .createServiceAgreement("saThree", "saThree", "-", le2, le2.getId(), le21.getId());
        saThree.setMaster(false);
        serviceAgreementJpaRepository.save(saThree);
    }

    private void createDataLegalEntityCreatorOfServiceAgreement() {

        LegalEntity le3 = LegalEntityUtil
            .createLegalEntity(null, "le3", "le3", null, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(le3);

        ServiceAgreement sa2 = ServiceAgreementUtil
            .createServiceAgreement("sa2", "sa2", "", le3, null, null);
        sa2.setMaster(false);
        serviceAgreementJpaRepository.save(sa2);
    }

    private void createDataUserAssignedFunctionGroupsInServiceAgreement() {
        String userId = UUID.randomUUID().toString();
        LegalEntity parent2 = LegalEntityUtil
            .createLegalEntity(null, "parentLe2", "parentLe2", null, LegalEntityType.CUSTOMER);
        LegalEntity le5 = LegalEntityUtil
            .createLegalEntity(null, "le5", "le5", parent2, LegalEntityType.CUSTOMER);
        ServiceAgreement sa4 = ServiceAgreementUtil
            .createServiceAgreement("sa4", "sa4", "", le5, null, null);
        sa4.setMaster(false);
        legalEntityJpaRepository.save(parent2);

        le5 = legalEntityJpaRepository.save(le5);
        sa4 = serviceAgreementJpaRepository.save(sa4);

        Participant participant = new Participant();
        participant.setServiceAgreement(sa4);
        participant.setLegalEntity(le5);
        Participant saveParticipant = participantJpa.save(participant);
        Set<ParticipantUser> participantUsers = new HashSet<>();
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setParticipant(saveParticipant);
        participantUser.setUserId(userId);
        participantUsers.add(participantUser);
        saveParticipant.setParticipantUsers(participantUsers);

        Map<String, Participant> participantMap = new HashMap<>();
        participantMap.put(saveParticipant.getId(), saveParticipant);
        sa4.setParticipants(participantMap);

        UserContext userContext = new UserContext(userId, sa4.getId());
        userContextJpaRepository.save(userContext);

        ApplicableFunctionPrivilege editPrivilege = businessFunctionCache
            .getByFunctionIdAndPrivilege("1028", "edit");

        FunctionGroup functionGroup = new FunctionGroup();

        GroupedFunctionPrivilege groupedPriv = new GroupedFunctionPrivilege();
        groupedPriv.setFunctionGroup(functionGroup);
        groupedPriv.setApplicableFunctionPrivilegeId(editPrivilege.getId());

        functionGroup.setName("name1");
        functionGroup.setDescription("description");
        functionGroup.setType(FunctionGroupType.DEFAULT);
        functionGroup.setServiceAgreement(sa4);
        functionGroup.getPermissions().add(groupedPriv);
        functionGroupJpaRepository.save(functionGroup);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(
            groupedPriv.getFunctionGroup(), userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

    }

    private void createDataUserPendingPrivilegesInServiceAgreement() {
        LegalEntity parent4 = LegalEntityUtil
            .createLegalEntity(null, "parentLe4", "parentLe4", null, LegalEntityType.CUSTOMER);
        LegalEntity le7 = LegalEntityUtil
            .createLegalEntity(null, "le7", "le7", parent4, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(parent4);
        legalEntityJpaRepository.save(le7);

        ServiceAgreement sa6 = ServiceAgreementUtil
            .createServiceAgreement("sa6", "sa6", "", le7, null, null);
        sa6.setMaster(false);
        serviceAgreementJpaRepository.save(sa6);
        Participant participant = new Participant();
        participant.setServiceAgreement(sa6);
        participant.setLegalEntity(le7);
        Participant saveParticipant = participantJpa.save(participant);
        Set<ParticipantUser> participantUsers = new HashSet<>();
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setParticipant(saveParticipant);
        participantUsers.add(participantUser);
        saveParticipant.setParticipantUsers(participantUsers);

        Map<String, Participant> participantMap = new HashMap<>();
        participantMap.put(saveParticipant.getId(), saveParticipant);
        sa6.setParticipants(participantMap);

        ApprovalUserContext auc = new ApprovalUserContext()
            .withLegalEntityId(le7.getId())
            .withServiceAgreementId(sa6.getId())
            .withUserId("123");
        auc.setApprovalId("123e4567-e89b-12d3-a456-426655440000");

        approvalUserContextJpaRepository.save(auc);
    }

    private List<BatchResponseItem> executeRequest(List<PresentationServiceAgreementIdentifier> identifiers,
        String accessToken) {

        PresentationDeleteServiceAgreements batchDelete = new PresentationDeleteServiceAgreements()
            .withServiceAgreementIdentifiers(identifiers).withAccessToken(accessToken);
        InternalRequest<PresentationDeleteServiceAgreements> request = RequestUtils.getInternalRequest(
            batchDelete);
        return deleteBatchServiceAgreement.deleteBatchServiceAgreement(request).getData();
    }

    private Matcher<BatchResponseItem> getBatchResponseItemMatcher(String validUpdate,
        BatchResponseStatusCode batchResponseStatusCode,
        List<String> errors) {
        return samePropertyValuesAs(new BatchResponseItem()
            .withResourceId(validUpdate)
            .withErrors(errors)
            .withStatus(batchResponseStatusCode));
    }
}