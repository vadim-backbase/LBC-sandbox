package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.BAD_REQUEST_MESSAGE;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status.ENABLED;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.AddServiceAgreementApprovalHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.AddServiceAgreementHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.EditServiceAgreementApprovalHandler;
import com.backbase.accesscontrol.business.persistence.serviceagreement.EditServiceAgreementHandler;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementValidator;
import com.backbase.accesscontrol.dto.parameterholder.LegalEntityIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.ServiceAgreementIdApprovalIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.mappers.ServiceAgreementDtoMapper;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementSave;
import java.lang.annotation.ElementType;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementApprovalServiceTest {

    @Mock
    private AddServiceAgreementHandler addServiceAgreementHandler;

    @Mock
    private AddServiceAgreementApprovalHandler addServiceAgreementApprovalHandler;

    @Mock
    private EditServiceAgreementHandler editServiceAgreementHandler;

    @Mock
    private EditServiceAgreementApprovalHandler editServiceAgreementApprovalHandler;

    @Mock
    private ServiceAgreementValidator serviceAgreementValidator;

    @Mock
    private Validator validator;

    @Spy
    private ServiceAgreementDtoMapper serviceAgreementDtoMapper = Mappers.getMapper(ServiceAgreementDtoMapper.class);

    @InjectMocks
    private ServiceAgreementApprovalService serviceAgreementApprovalService;

    @Test
    public void shouldSuccessfullyCreateServiceAgreement() {
        String serviceAgreementId = "SA-01";
        String legalEntityId = "LE-01";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        doReturn(new ServiceAgreementPostResponseBody()
            .withId(serviceAgreementId))
            .when(addServiceAgreementHandler).handleRequest(any(SingleParameterHolder.class), eq(postRequest));

        ServiceAgreementPostResponseBody response = serviceAgreementApprovalService
            .createServiceAgreement(postRequest, legalEntityId);

        ArgumentCaptor<SingleParameterHolder> captor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(addServiceAgreementHandler).handleRequest(captor.capture(), eq(postRequest));
        assertEquals(legalEntityId, captor.getValue().getParameter());
        assertEquals(serviceAgreementId, response.getId());
    }

    @Test
    public void shouldStopExecutionWhenValidationFailsOnCreateServiceAgreement() {
        String legalEntityId = "LE-01";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        doThrow(new BadRequestException("error")).when(serviceAgreementValidator).validatePayload(any());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementApprovalService.createServiceAgreement(postRequest, legalEntityId));

        verify(addServiceAgreementHandler, times(0)).handleRequest(any(), any());
        assertEquals("error", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyCreateServiceAgreementWithApproval() {
        String legalEntityId = "LE-01";
        String approvalId = "approvalId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        doReturn(new ServiceAgreementPostResponseBody()
            .withId(approvalId))
            .when(addServiceAgreementApprovalHandler)
            .handleRequest(any(LegalEntityIdApprovalIdParameterHolder.class), eq(postRequest));

        ServiceAgreementPostResponseBody response = serviceAgreementApprovalService
            .createServiceAgreementWithApproval(postRequest, legalEntityId, approvalId);

        ArgumentCaptor<LegalEntityIdApprovalIdParameterHolder> captor = ArgumentCaptor
            .forClass(LegalEntityIdApprovalIdParameterHolder.class);
        verify(addServiceAgreementApprovalHandler).handleRequest(captor.capture(), eq(postRequest));
        assertEquals(legalEntityId, captor.getValue().getLegalEntityId());
        assertEquals(approvalId, captor.getValue().getApprovalId());
        assertEquals(approvalId, response.getId());
    }

    @Test
    public void shouldStopExecutionWhenValidationFailsOnCreateServiceAgreementWithApproval() {
        String legalEntityId = "LE-01";
        String approvalId = "approvalId";
        ServiceAgreementPostRequestBody postRequest = new ServiceAgreementPostRequestBody();

        doThrow(new BadRequestException("error")).when(serviceAgreementValidator).validatePayload(any());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementApprovalService
            .createServiceAgreementWithApproval(postRequest, legalEntityId, approvalId));

        verify(addServiceAgreementApprovalHandler, times(0)).handleRequest(any(), any());
        assertEquals("error", exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyEditServiceAgreement() {
        String serviceAgreementId = "SA-01";
        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(ENABLED)
            .withParticipants(newHashSet(participant1, participant2));

        doNothing().when(editServiceAgreementHandler)
            .handleRequest(any(SingleParameterHolder.class), eq(serviceAgreementSaveBody));

        serviceAgreementApprovalService.updateServiceAgreement(serviceAgreementSaveBody, serviceAgreementId);

        ArgumentCaptor<SingleParameterHolder<String>> parameterCaptor = ArgumentCaptor
            .forClass(SingleParameterHolder.class);

        verify(editServiceAgreementHandler).handleRequest(parameterCaptor.capture(), eq(serviceAgreementSaveBody));
        assertEquals(serviceAgreementId, parameterCaptor.getValue().getParameter());
    }

    @Test
    public void shouldStopExecutionWhenValidationFailsOnEditServiceAgreement() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave();

        doThrow(new BadRequestException("error")).when(serviceAgreementValidator).validatePayload(any());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementApprovalService.updateServiceAgreement(serviceAgreementSaveBody, serviceAgreementId));

        verify(editServiceAgreementHandler, times(0)).handleRequest(any(), any());
        assertEquals("error", exception.getMessage());
    }

    @Test
    public void shouldStopExecutionWhenSpecValidationFailsOnEditServiceAgreement() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave();

        ConstraintViolation<ServiceAgreementSave> violation1 = ConstraintViolationImpl
            .forBeanValidation("11", null, null, "1",
                null, null, null, null, PathImpl.createPathFromString("path1"),
                null, ElementType.TYPE);

        ConstraintViolation<ServiceAgreementSave> violation2 = ConstraintViolationImpl
            .forBeanValidation("12", null, null, "2",
                null, null, null, null, PathImpl.createPathFromString("path2"),
                null, ElementType.TYPE);

        when(validator.validate(serviceAgreementSaveBody)).thenReturn(newHashSet(violation1, violation2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementApprovalService.updateServiceAgreement(serviceAgreementSaveBody, serviceAgreementId));

        verify(serviceAgreementValidator, times(0)).validatePayload(any());
        verify(editServiceAgreementHandler, times(0)).handleRequest(any(), any());

        assertEquals(BAD_REQUEST_MESSAGE, exception.getMessage());
    }

    @Test
    public void shouldSuccessfullyEditServiceAgreementWithApproval() {
        String approvalId = "approvalId";
        String serviceAgreementId = "SA-01";
        Participant participant1 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(true)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin1", "admin2"));
        Participant participant2 = new Participant()
            .withId("LE-01")
            .withSharingAccounts(false)
            .withSharingUsers(true)
            .withAdmins(newHashSet("admin3", "admin4"));

        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave()
            .withName("sa-name")
            .withDescription("sa-description")
            .withExternalId("sa-external")
            .withStatus(ENABLED)
            .withParticipants(newHashSet(participant1, participant2));

        doNothing().when(editServiceAgreementApprovalHandler)
            .handleRequest(any(ServiceAgreementIdApprovalIdParameterHolder.class), eq(serviceAgreementSaveBody));

        serviceAgreementApprovalService.updateServiceAgreementWithApproval(serviceAgreementSaveBody, serviceAgreementId,
            approvalId);

        ArgumentCaptor<ServiceAgreementIdApprovalIdParameterHolder> parameterCaptor = ArgumentCaptor
            .forClass(ServiceAgreementIdApprovalIdParameterHolder.class);

        verify(editServiceAgreementApprovalHandler)
            .handleRequest(parameterCaptor.capture(), eq(serviceAgreementSaveBody));
        assertEquals(serviceAgreementId, parameterCaptor.getValue().getServiceAgreementId());
        assertEquals(approvalId, parameterCaptor.getValue().getApprovalId());
    }

    @Test
    public void shouldStopExecutionWhenValidationFailsOnEditServiceAgreementWithApproval() {
        String approvalId = "approvalId";
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave();

        doThrow(new BadRequestException("error")).when(serviceAgreementValidator).validatePayload(any());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementApprovalService.updateServiceAgreementWithApproval(serviceAgreementSaveBody,
                serviceAgreementId, approvalId));

        verify(editServiceAgreementApprovalHandler, times(0)).handleRequest(any(), any());
        assertEquals("error", exception.getMessage());
    }

    @Test
    public void shouldStopExecutionWhenSpecValidationFailsOnEditServiceAgreementWithApproval() {
        String approvalId = "approvalId";
        String serviceAgreementId = "SA-01";
        ServiceAgreementSave serviceAgreementSaveBody = new ServiceAgreementSave();

        ConstraintViolation<ServiceAgreementSave> violation1 = ConstraintViolationImpl
            .forBeanValidation("11", null, null, "1",
                null, null, null, null, PathImpl.createPathFromString("path1"),
                null, ElementType.TYPE);

        ConstraintViolation<ServiceAgreementSave> violation2 = ConstraintViolationImpl
            .forBeanValidation("12", null, null, "2",
                null, null, null, null, PathImpl.createPathFromString("path2"),
                null, ElementType.TYPE);

        when(validator.validate(serviceAgreementSaveBody)).thenReturn(newHashSet(violation1, violation2));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> serviceAgreementApprovalService
            .updateServiceAgreementWithApproval(serviceAgreementSaveBody, serviceAgreementId, approvalId));

        verify(serviceAgreementValidator, times(0)).validatePayload(any());
        verify(editServiceAgreementApprovalHandler, times(0)).handleRequest(any(), any());

        assertEquals(BAD_REQUEST_MESSAGE,  exception.getMessage());
    }
}