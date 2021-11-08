package com.backbase.accesscontrol.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApprovalServiceAgreement;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementParticipant;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalServiceAgreementUtilTest {

    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;
    @Mock
    private AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;

    private ApprovalServiceAgreementUtil approvalServiceAgreementUtil;

    @Before
    public void setUp() {
        approvalServiceAgreementUtil = new ApprovalServiceAgreementUtil(legalEntityJpaRepository,
            assignablePermissionSetJpaRepository);
    }

    @Test
    public void transformServiceAgreementTest() {

        LegalEntity le1 = new LegalEntity().withId("leId1");
        LegalEntity le2 = new LegalEntity().withId("leId2");
        ApprovalServiceAgreementParticipant asap1 = new ApprovalServiceAgreementParticipant();
        asap1.setAdmins(Sets.newHashSet("admin1", "admin2"));
        asap1.setShareUsers(false);
        asap1.setShareAccounts(true);
        asap1.setLegalEntityId(le1.getId());
        ApprovalServiceAgreementParticipant asap2 = new ApprovalServiceAgreementParticipant();
        asap2.setAdmins(Sets.newHashSet("admin3", "admin4"));
        asap2.setShareUsers(true);
        asap2.setShareAccounts(false);
        asap2.setLegalEntityId(le2.getId());

        ApprovalServiceAgreement approvalServiceAgreement = new ApprovalServiceAgreement();
        approvalServiceAgreement.setApprovalId("approvalId");
        approvalServiceAgreement.setServiceAgreementId(null);
        approvalServiceAgreement.setCreatorLegalEntityId(le1.getId());
        approvalServiceAgreement.setExternalId("saId");
        approvalServiceAgreement.setMaster(true);
        approvalServiceAgreement.setName("saName");
        approvalServiceAgreement.setDescription("desc");
        approvalServiceAgreement.setStartDate(new Date());
        approvalServiceAgreement.setState(ServiceAgreementState.ENABLED);
        approvalServiceAgreement.setParticipants(Sets.newHashSet(asap1, asap2));
        approvalServiceAgreement.setPermissionSetsRegular(Sets.newHashSet(1L, 2L));
        AssignablePermissionSet aps1 = new AssignablePermissionSet();
        aps1.setId(1L);
        aps1.setName("aps1");

        when(legalEntityJpaRepository.findById(eq(le1.getId()))).thenReturn(Optional.of(le1));
        when(legalEntityJpaRepository.findById(eq(le2.getId()))).thenReturn(Optional.of(le2));
        when(assignablePermissionSetJpaRepository.findAllByIdIn(any())).thenReturn(Sets.newHashSet(aps1));

        ServiceAgreement sa =
            approvalServiceAgreementUtil.transformApprovalServiceAgreementToServiceAgreement(approvalServiceAgreement);
        assertThat(sa,
            allOf(
                hasProperty("creatorLegalEntity", is(le1)),
                hasProperty("id", nullValue()),
                hasProperty("name", is(approvalServiceAgreement.getName())),
                hasProperty("externalId", is(approvalServiceAgreement.getExternalId())),
                hasProperty("description", is(approvalServiceAgreement.getDescription())),
                hasProperty("master", is(true)),
                hasProperty("state", is(ServiceAgreementState.ENABLED)),
                hasProperty("startDate", is(approvalServiceAgreement.getStartDate())),
                hasProperty("permissionSetsRegular", hasItems(aps1))
            )
        );
        assertFalse(sa.getParticipants().get(le1.getId()).isShareUsers());
        assertTrue(sa.getParticipants().get(le1.getId()).isShareAccounts());
        assertEquals("admin1", sa.getParticipants().get(le1.getId()).getAdmins().get("admin1").getUserId());
        assertEquals("admin2", sa.getParticipants().get(le1.getId()).getAdmins().get("admin2").getUserId());
        assertTrue(sa.getParticipants().get(le2.getId()).isShareUsers());
        assertFalse(sa.getParticipants().get(le2.getId()).isShareAccounts());
        assertEquals("admin3", sa.getParticipants().get(le2.getId()).getAdmins().get("admin3").getUserId());
        assertEquals("admin4", sa.getParticipants().get(le2.getId()).getAdmins().get("admin4").getUserId());
    }

}
