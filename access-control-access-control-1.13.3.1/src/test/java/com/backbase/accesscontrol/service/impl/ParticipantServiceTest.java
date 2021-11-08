package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_055;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.util.helpers.ValidationFixture;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.google.common.collect.Sets;
import java.util.HashSet;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParticipantServiceTest {

    @InjectMocks
    private ParticipantService participantService;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private Validator validator;

    @Before
    public void setUp() throws Exception {
        when(validator.validate(any()))
            .thenReturn(new HashSet<>());
    }

    @Test
    public void shouldCallRemoveServiceAgreementParticipantsWhenActionRemove() {

        PresentationParticipantPutBody PresentationParticipantPutBody = new PresentationParticipantPutBody()
            .withExternalParticipantId("ex-le-id")
            .withAction(PresentationAction.REMOVE)
            .withExternalServiceAgreementId("ex-sa-id");

        when(persistenceServiceAgreementService.removeParticipant(any(PresentationParticipantPutBody.class)))
            .thenReturn("saId");

        assertEquals("saId", participantService
            .updateServiceAgreementParticipants(PresentationParticipantPutBody));

        verify(persistenceServiceAgreementService, times(1)).removeParticipant(PresentationParticipantPutBody);
    }

    @Test
    public void shouldCallAddServiceAgreementParticipantsAndTransformWhenActionAdd() {

        PresentationParticipantPutBody PresentationParticipantPutBody = new PresentationParticipantPutBody()
            .withExternalParticipantId("ex-le-id")
            .withAction(PresentationAction.ADD)
            .withExternalServiceAgreementId("ex-sa-id")
            .withSharingUsers(true)
            .withSharingAccounts(false);

        when(persistenceServiceAgreementService.addParticipant(any(PresentationParticipantPutBody.class)))
            .thenReturn("saId");

        assertEquals("saId", participantService
            .updateServiceAgreementParticipants(PresentationParticipantPutBody));

        ArgumentCaptor<PresentationParticipantPutBody> captor = ArgumentCaptor
            .forClass(PresentationParticipantPutBody.class);

        verify(persistenceServiceAgreementService, times(1)).addParticipant(captor.capture());
        assertEquals(captor.getValue().getExternalParticipantId(),
            PresentationParticipantPutBody.getExternalParticipantId());
        assertEquals(captor.getValue().getExternalServiceAgreementId(),
            PresentationParticipantPutBody.getExternalServiceAgreementId());
        assertFalse(captor.getValue().getSharingAccounts());
        assertTrue(captor.getValue().getSharingUsers());
    }

    @Test
    public void shouldThrowBarRequestOnAddServiceAgreementParticipantsWhenInvalidBody() {
        PresentationParticipantPutBody PresentationParticipantPutBody = new PresentationParticipantPutBody()
            .withExternalParticipantId(null)
            .withAction(PresentationAction.ADD)
            .withExternalServiceAgreementId("ex-sa-id")
            .withSharingAccounts(false);

        ConstraintViolation constraintViolation = ValidationFixture.
            createConstraintViolation(new PresentationParticipantPutBody(),
                PresentationParticipantPutBody.class,
                NotNull.class,
                new PresentationParticipantPutBody(),
                "externalParticipantId",
                null);
        when(validator.validate(eq(PresentationParticipantPutBody)))
            .thenReturn(Sets.newHashSet(constraintViolation));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> participantService.updateServiceAgreementParticipants(PresentationParticipantPutBody));
        assertEquals(ERR_ACC_055.getErrorMessage(), exception.getMessage());
    }

}