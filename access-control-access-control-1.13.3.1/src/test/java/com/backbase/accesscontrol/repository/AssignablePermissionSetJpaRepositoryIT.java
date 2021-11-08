package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.APS_PERMISSIONS_EXTENDED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mapstruct.ap.internal.util.Collections.asSet;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class AssignablePermissionSetJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    private RepositoryCleaner repositoryCleaner;
    @Autowired
    private BusinessFunctionCache businessFunctionCache;

    private AssignablePermissionSet assignablePermissionSet;

    @Before
    public void init() {
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void shouldPersistAndFind() {
        assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setDescription("Test APS DESCRIPTION");
        assignablePermissionSet.setName("Test APS NAME");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);
        AssignablePermissionSet assignablePermissionSetCreated = assignablePermissionSetJpaRepository
            .save(assignablePermissionSet);

        Optional<AssignablePermissionSet> assignablePermissionSetReturned = assignablePermissionSetJpaRepository
            .findById(assignablePermissionSetCreated.getId());

        assertTrue(assignablePermissionSetReturned.isPresent());
        assertEquals(assignablePermissionSet.getDescription(), assignablePermissionSetReturned.get().getDescription());
        assertEquals(assignablePermissionSet.getName(), assignablePermissionSetReturned.get().getName());
        assertEquals(assignablePermissionSet.getType(), assignablePermissionSetReturned.get().getType());
    }

    @Test
    @Transactional
    public void shouldReturnOnlyAdminPermissionSet() {

        ApplicableFunctionPrivilege viewEntitlements = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "view");
        ApplicableFunctionPrivilege createEntitlements = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "edit");

        AssignablePermissionSet permissionSetRegularUser = new AssignablePermissionSet();
        permissionSetRegularUser.setName("regular-user-ps1");
        permissionSetRegularUser.setDescription("description");
        permissionSetRegularUser.getPermissions().add(viewEntitlements.getId());
        permissionSetRegularUser = assignablePermissionSetJpaRepository.save(permissionSetRegularUser);

        AssignablePermissionSet permissionSetAdminUser = new AssignablePermissionSet();
        permissionSetAdminUser.setName("regular-user-ps2");
        permissionSetAdminUser.setDescription("description");
        permissionSetAdminUser.getPermissions().add(createEntitlements.getId());
        permissionSetAdminUser = assignablePermissionSetJpaRepository.save(permissionSetAdminUser);

        LegalEntity legalEntity = legalEntityJpaRepository.save(new LegalEntity()
            .withName("le-name")
            .withExternalId("le-ext-id")
            .withType(LegalEntityType.CUSTOMER));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("sa-name")
            .withDescription("sa-description")
            .withCreatorLegalEntity(legalEntity)
            .withExternalId("sa-ext-id")
            .withMaster(true);

        Participant participant = new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(legalEntity);

        serviceAgreement.addParticipant(participant);
        serviceAgreement.getPermissionSetsRegular().add(permissionSetRegularUser);
        serviceAgreement.getPermissionSetsAdmin().add(permissionSetAdminUser);

        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        Set<AssignablePermissionSet> result = assignablePermissionSetJpaRepository
            .findAllByAssignedAsAdminToServiceAgreement(serviceAgreement.getId());
        assertEquals(
            1,
            result.size());

        AssignablePermissionSet resultAssignablePermissionSet = result.stream().findFirst().get();

        assertThat(resultAssignablePermissionSet, allOf(
            hasProperty("name", equalTo(permissionSetAdminUser.getName())),
            hasProperty("description", equalTo(permissionSetAdminUser.getDescription())),
            hasProperty("type", equalTo(permissionSetAdminUser.getType())),
            hasProperty("id", equalTo(permissionSetAdminUser.getId())),
            hasProperty("permissions", equalTo(Sets.newHashSet(createEntitlements.getId())))));
    }

    @Test
    @Transactional
    public void shouldFindByIdWithNamedEntityGraph() {
        ApplicableFunctionPrivilege applicableFunctionPrivilegeExecute = businessFunctionCache
            .getByFunctionIdAndPrivilege("1011", "edit");

        assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setDescription("Test APS DESCRIPTION");
        assignablePermissionSet.setName("Test APS NAME");
        assignablePermissionSet.setType(AssignablePermissionType.CUSTOM);
        assignablePermissionSet.setPermissions(asSet(applicableFunctionPrivilegeExecute.getId()));
        AssignablePermissionSet assignablePermissionSetCreated = assignablePermissionSetJpaRepository
            .save(assignablePermissionSet);

        Optional<AssignablePermissionSet> returnedById = assignablePermissionSetJpaRepository
            .findById(assignablePermissionSetCreated.getId(), APS_PERMISSIONS_EXTENDED);

        assertTrue(returnedById.isPresent());
        assertEquals(assignablePermissionSetCreated.getId(), returnedById.get().getId());
        assertEquals("Test APS NAME", returnedById.get().getName());
        assertEquals(1, assignablePermissionSet.getPermissions().size());
    }
}