package com.backbase.accesscontrol.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementSystemFunctionGroupServiceTest {

    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private BusinessFunctionCache businessFunctionCache;
    @Mock
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private EntityManagerFactory entityManagerFactory;
    @Mock
    private PersistenceUnitUtil persistenceUtil;
    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @InjectMocks
    private ServiceAgreementSystemFunctionGroupService serviceAgreementSystemFunctionGroupService;

    @Test
    public void shouldGetFunctionGroupFromServiceAgreement() {
        String systemFunctionGroupId = "sys-fg-id";

        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUtil);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(persistenceUtil.isLoaded(any(), anyString())).thenReturn(true);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("sa-id");
        serviceAgreement.getFunctionGroups().add(new FunctionGroup()
            .withId(systemFunctionGroupId)
            .withName("SYSTEM_FUNCTION_GROUP"));

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);

        assertEquals(serviceAgreement, serviceAgreementFunctionGroups.getServiceAgreement());

        assertEquals(serviceAgreementFunctionGroups.getSystemFunctionGroup(),
            systemFunctionGroupId);
    }

    @Test
    public void shouldGetFunctionGroupFromRepository() {
        String systemFunctionGroupId = "sys-fg-id";

        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUtil);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(persistenceUtil.isLoaded(any(), anyString())).thenReturn(false);

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("sa-id");

        when(functionGroupJpaRepository
            .findByNameAndServiceAgreementId(anyString(), anyString())).thenReturn(Optional.of(new FunctionGroup()
            .withId(systemFunctionGroupId)
            .withName("SYSTEM_FUNCTION_GROUP")));

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);

        assertEquals(serviceAgreement, serviceAgreementFunctionGroups.getServiceAgreement());

        assertEquals(serviceAgreementFunctionGroups.getSystemFunctionGroup(),
            systemFunctionGroupId);

        verify(functionGroupJpaRepository).findByNameAndServiceAgreementId(eq("SYSTEM_FUNCTION_GROUP"),
            eq("sa-id"));
    }

    @Test
    public void shouldCreateSystemFunctionGroupIfItDoesNotExist() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("sa-id");

        String systemFunctionGroup = "sys-fg-id";

        when(assignablePermissionSetJpaRepository.findAllByAssignedAsAdminToServiceAgreement(eq("sa-id")))
            .thenReturn(new HashSet<>());

        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUtil);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(persistenceUtil.isLoaded(any(), anyString())).thenReturn(true);

        BusinessFunction bf = new BusinessFunction();
        bf.setId("bf1");
        bf.setFunctionName("function");
        bf.setFunctionCode("function");
        bf.setResourceCode("resource");
        bf.setResourceName("resource");

        Privilege privilege1 = new Privilege();
        privilege1.setId("p1");
        privilege1.setName("p1");
        privilege1.setCode("p1");

        Privilege privilege2 = new Privilege();
        privilege2.setId("p2");
        privilege2.setName("p2");
        privilege2.setCode("p2");

        ApplicableFunctionPrivilege applicableFunctionPrivilege1 = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege1.setId("1");
        applicableFunctionPrivilege1.setBusinessFunction(bf);
        applicableFunctionPrivilege1.setPrivilege(privilege1);

        ApplicableFunctionPrivilege applicableFunctionPrivilege2 = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege2.setId("2");
        applicableFunctionPrivilege2.setBusinessFunction(bf);
        applicableFunctionPrivilege2.setPrivilege(privilege2);

        when(businessFunctionCache
            .getApplicableFunctionPrivileges(anySet()))
            .thenReturn(Sets.newHashSet(applicableFunctionPrivilege1, applicableFunctionPrivilege2));

        when(functionGroupService
            .addSystemFunctionGroup(eq(serviceAgreement), eq("SYSTEM_FUNCTION_GROUP"), anyList()))
            .thenReturn(systemFunctionGroup);

        ServiceAgreementFunctionGroups serviceAgreementFunctionGroups = serviceAgreementSystemFunctionGroupService
            .getServiceAgreementFunctionGroups(serviceAgreement);

        assertEquals(serviceAgreement, serviceAgreementFunctionGroups.getServiceAgreement());

        assertEquals(systemFunctionGroup, serviceAgreementFunctionGroups.getSystemFunctionGroup());

        verify(assignablePermissionSetJpaRepository).findAllByAssignedAsAdminToServiceAgreement(eq("sa-id"));
        verify(functionGroupService, times(1))
            .addSystemFunctionGroup(eq(serviceAgreement), eq("SYSTEM_FUNCTION_GROUP"), anyList());

    }
}