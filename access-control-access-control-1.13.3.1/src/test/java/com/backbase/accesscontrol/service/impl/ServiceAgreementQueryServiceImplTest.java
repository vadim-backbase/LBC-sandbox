package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_073;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.GraphConstants;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.ApprovalServiceAgreementRefJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.ServiceAgreementApprovalDetailsItem;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementQueryServiceImplTest {

    @InjectMocks
    private ServiceAgreementQueryServiceImpl serviceAgreementQueryServiceImpl;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Mock
    private ApprovalServiceAgreementRefJpaRepository approvalServiceAgreementRefJpaRepository;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("GMT+2");

    private Date from = new Date(0);
    private Date until = new Date(1);

    @Test
    public void shouldTransformAndInvokeServiceCreate() {
        String approvalId = "01";

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setName("name");
        approvalServiceAgreement.setDescription("description");
        approvalServiceAgreement.setState(ServiceAgreementState.DISABLED);
        approvalServiceAgreement.setCreatorLegalEntityId("id");
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setStartDate(from);
        approvalServiceAgreement.setEndDate(until);

        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant.setLegalEntityId("LE-01");
        approvalServiceAgreementParticipant.setLegalEntity(new LegalEntity().withExternalId("ex1").withName("name1"));
        approvalServiceAgreementParticipant.setShareAccounts(false);
        approvalServiceAgreementParticipant.setShareUsers(true);
        approvalServiceAgreementParticipant.setAdmins(newHashSet("admin1", "admin2"));
        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant1 = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant1.setLegalEntityId("LE-02");
        approvalServiceAgreementParticipant1.setLegalEntity(new LegalEntity().withExternalId("ex2").withName("name2"));
        approvalServiceAgreementParticipant1.setShareAccounts(true);
        approvalServiceAgreementParticipant1.setShareUsers(true);
        approvalServiceAgreementParticipant1.setAdmins(newHashSet("admin3", "admin4"));

        approvalServiceAgreement
            .setParticipants(newHashSet(approvalServiceAgreementParticipant, approvalServiceAgreementParticipant1));

        when(approvalServiceAgreementRefJpaRepository.findByApprovalId(eq(approvalId)))
            .thenReturn(Optional.of(approvalServiceAgreement));

        ServiceAgreementApprovalDetailsItem result = serviceAgreementQueryServiceImpl.getByApprovalId(approvalId);

        assertNull(result.getOldState());
        assertNull(result.getServiceAgreementId());
        assertEquals("CREATE", result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());
        assertEquals("name", result.getNewState().getName());
        assertEquals("description", result.getNewState().getDescription());
        assertNotNull(result.getNewState().getValidFromDate());
        assertNotNull(result.getNewState().getValidFromTime());
        assertNotNull(result.getNewState().getValidUntilDate());
        assertNotNull(result.getNewState().getValidUntilTime());
        assertTrue(result.getNewState().getAdmins().containsAll(newHashSet("admin1", "admin2", "admin3", "admin4")));
        assertEquals(2, result.getNewState().getLegalEntities().size());
    }

    @Test
    public void shouldTransformAndInvokeServiceUpdate() {
        String approvalId = "01";
        String serviceAgreementId= "saId";

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setServiceAgreementId(serviceAgreementId);
        approvalServiceAgreement.setName("name");
        approvalServiceAgreement.setDescription("description");
        approvalServiceAgreement.setState(ServiceAgreementState.DISABLED);
        approvalServiceAgreement.setCreatorLegalEntityId("id");
        approvalServiceAgreement.setApprovalId(approvalId);
        approvalServiceAgreement.setStartDate(from);
        approvalServiceAgreement.setEndDate(until);

        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant.setLegalEntityId("LE-01");
        approvalServiceAgreementParticipant.setLegalEntity(new LegalEntity().withExternalId("ex1").withName("name1"));
        approvalServiceAgreementParticipant.setShareAccounts(false);
        approvalServiceAgreementParticipant.setShareUsers(true);
        approvalServiceAgreementParticipant.setAdmins(newHashSet("admin1", "admin2"));
        ApprovalServiceAgreementParticipant approvalServiceAgreementParticipant1 = new ApprovalServiceAgreementParticipant();
        approvalServiceAgreementParticipant1.setLegalEntityId("LE-02");
        approvalServiceAgreementParticipant1.setLegalEntity(new LegalEntity().withExternalId("ex2").withName("name2"));
        approvalServiceAgreementParticipant1.setShareAccounts(true);
        approvalServiceAgreementParticipant1.setShareUsers(true);
        approvalServiceAgreementParticipant1.setAdmins(newHashSet("admin3", "admin4"));

        approvalServiceAgreement
            .setParticipants(newHashSet(approvalServiceAgreementParticipant, approvalServiceAgreementParticipant1));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("name2");
        serviceAgreement.setDescription("description2");
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setStartDate(from);
        serviceAgreement.setEndDate(until);

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("admin1", "admin2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("admin3", "admin4")));
        serviceAgreement.setPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)));
        serviceAgreement.addParticipant(new Participant(), "LE-01", true, false);
        serviceAgreement.addParticipant(new Participant(), "LE-02", true, true);
        serviceAgreement.getParticipants().get("LE-01").addAdmin("admin1");
        serviceAgreement.getParticipants().get("LE-02").addAdmin("admin2");

        when(approvalServiceAgreementRefJpaRepository.findByApprovalId(eq(approvalId)))
            .thenReturn(Optional.of(approvalServiceAgreement));


        when(serviceAgreementJpaRepository
            .findById(serviceAgreementId,
                GraphConstants
                    .SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementApprovalDetailsItem result = serviceAgreementQueryServiceImpl.getByApprovalId(approvalId);

        assertEquals(result.getServiceAgreementId(), serviceAgreementId);
        assertEquals("EDIT", result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());
        assertEquals("name", result.getNewState().getName());
        assertEquals("description", result.getNewState().getDescription());
        assertNotNull(result.getNewState().getValidFromDate());
        assertNotNull(result.getNewState().getValidFromTime());
        assertNotNull(result.getNewState().getValidUntilDate());
        assertNotNull(result.getNewState().getValidUntilTime());
        assertTrue(result.getNewState().getAdmins().containsAll(newHashSet("admin1", "admin2", "admin3", "admin4")));
        assertEquals(2, result.getNewState().getLegalEntities().size());

        assertEquals("name2", result.getOldState().getName());
        assertEquals("description2", result.getOldState().getDescription());
        assertNotNull(result.getOldState().getValidFromDate());
        assertNotNull(result.getOldState().getValidFromTime());
        assertNotNull(result.getOldState().getValidUntilDate());
        assertNotNull(result.getOldState().getValidUntilTime());
        assertTrue(result.getOldState().getAdmins().containsAll(newHashSet("admin1", "admin2")));
        assertEquals(2, result.getOldState().getLegalEntities().size());
    }

    @Test
    public void shouldTransformAndInvokeServiceDelete() {
        String approvalId = "01";
        String serviceAgreementId= "saId";

        ApprovalServiceAgreementRef ref = new ApprovalServiceAgreementRef();
        ref.setId(1L);
        ref.setApprovalId(approvalId);
        ref.setServiceAgreementId(serviceAgreementId);

        when(approvalServiceAgreementRefJpaRepository.findByApprovalId(eq(approvalId)))
            .thenReturn(Optional.of(ref));

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);
        serviceAgreement.setName("name2");
        serviceAgreement.setDescription("description2");
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setStartDate(from);
        serviceAgreement.setEndDate(until);

        AssignablePermissionSet apsAdmin1 = new AssignablePermissionSet();
        apsAdmin1.setId(BigDecimal.ONE.longValue());
        apsAdmin1.setName("name0");
        apsAdmin1.setPermissions(Sets.newHashSet(asList("admin1", "admin2")));
        AssignablePermissionSet apsAdmin2 = new AssignablePermissionSet();
        apsAdmin2.setName("name11");
        apsAdmin2.setId(7L);
        apsAdmin2.setPermissions(Sets.newHashSet(asList("admin3", "admin4")));
        serviceAgreement.setPermissionSetsAdmin(Sets.newHashSet(asList(apsAdmin1, apsAdmin2)));
        serviceAgreement.addParticipant(new Participant(), "LE-01", true, false);
        serviceAgreement.addParticipant(new Participant(), "LE-02", true, true);
        serviceAgreement.getParticipants().get("LE-01").addAdmin("admin1");
        serviceAgreement.getParticipants().get("LE-02").addAdmin("admin2");

        when(serviceAgreementJpaRepository
            .findById(serviceAgreementId,
                GraphConstants
                    .SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR))
            .thenReturn(Optional.of(serviceAgreement));

        ServiceAgreementApprovalDetailsItem result = serviceAgreementQueryServiceImpl.getByApprovalId(approvalId);

        assertEquals(result.getServiceAgreementId(), serviceAgreementId);
        assertEquals("DELETE", result.getAction().toString());
        assertEquals(approvalId, result.getApprovalId());

        assertNull(result.getNewState());
        assertEquals("name2", result.getOldState().getName());
        assertEquals("description2", result.getOldState().getDescription());
        assertNotNull(result.getOldState().getValidFromDate());
        assertNotNull(result.getOldState().getValidFromTime());
        assertNotNull(result.getOldState().getValidUntilDate());
        assertNotNull(result.getOldState().getValidUntilTime());
        assertTrue(result.getOldState().getAdmins().containsAll(newHashSet("admin1", "admin2")));
        assertEquals(2, result.getOldState().getLegalEntities().size());
    }

    @Test
    public void shouldThrowErrorApprovalDoesNotExsist() {
        String approvalId = "01";

        when(approvalServiceAgreementRefJpaRepository.findByApprovalId(eq(approvalId)))
            .thenReturn(Optional.ofNullable(null));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> serviceAgreementQueryServiceImpl.getByApprovalId(approvalId));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_073.getErrorMessage(), ERR_ACQ_073.getErrorCode()));
    }

}