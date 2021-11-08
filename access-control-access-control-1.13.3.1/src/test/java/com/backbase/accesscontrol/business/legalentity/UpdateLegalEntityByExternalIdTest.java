package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.legalentity.UpdateLegalEntityHandler;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateLegalEntityByExternalIdTest {

    @InjectMocks
    private UpdateLegalEntityByExternalId updateLegalEntityByExternalId;

    @Mock
    private UpdateLegalEntityHandler updateLegalEntityHandler;

    @Test
    public void shouldUpdateLegalEntityByExternalId() {
        String externalId = "externalId";

        ArgumentCaptor<LegalEntityByExternalIdPutRequestBody> captor = ArgumentCaptor
            .forClass(LegalEntityByExternalIdPutRequestBody.class);
        when(updateLegalEntityHandler.handleRequest(any(SingleParameterHolder.class), captor.capture()))
            .thenReturn("id");

        LegalEntityByExternalIdPutRequestBody legalEntityByExternalIdPutRequestBody = new LegalEntityByExternalIdPutRequestBody()
            .withType(
                LegalEntityType.BANK);
        InternalRequest<LegalEntityByExternalIdPutRequestBody> legalEntityByExternalId = getInternalRequest(
            legalEntityByExternalIdPutRequestBody);
        updateLegalEntityByExternalId.updateLegalEntityByExternalId(legalEntityByExternalId, externalId);

        verify(updateLegalEntityHandler, times(1))
            .handleRequest(any(SingleParameterHolder.class), any(LegalEntityByExternalIdPutRequestBody.class));

        LegalEntityByExternalIdPutRequestBody data = captor.getValue();
        assertEquals(data.getType().toString(), LegalEntityType.BANK.toString());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenLegalEntityDoesNotExist() {
        String externalId = "externalId";

        NotFoundException notFoundException = getNotFoundException("Message", "Code");

        when(updateLegalEntityHandler.handleRequest(any(
            SingleParameterHolder.class), any(LegalEntityByExternalIdPutRequestBody.class))).
            thenThrow(notFoundException);

        LegalEntityByExternalIdPutRequestBody legalEntityByExternalIdPutRequestBody = new LegalEntityByExternalIdPutRequestBody()
            .withType(LegalEntityType.BANK);
        InternalRequest<LegalEntityByExternalIdPutRequestBody> legalEntityByExternalId = getInternalRequest(
            legalEntityByExternalIdPutRequestBody);

        NotFoundException notFoundException1 = assertThrows(NotFoundException.class,
            () -> updateLegalEntityByExternalId.updateLegalEntityByExternalId(legalEntityByExternalId, externalId));

        assertThat(notFoundException1,
            is(new NotFoundErrorMatcher(notFoundException.getErrors().get(0).getMessage(),
                notFoundException.getErrors().get(0).getKey())));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenLegalEntityDoesNotExist() {
        InternalRequest<LegalEntityByExternalIdPutRequestBody> internalRequest = getInternalRequest(
            new LegalEntityByExternalIdPutRequestBody());

        BadRequestException exception = getBadRequestException("Message", "Code");
        when(updateLegalEntityHandler.handleRequest(any(
            SingleParameterHolder.class), any(LegalEntityByExternalIdPutRequestBody.class))).
            thenThrow(exception);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> updateLegalEntityByExternalId.updateLegalEntityByExternalId(internalRequest, "exId"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(exception.getErrors().get(0).getMessage(),
                exception.getErrors().get(0).getKey())));
    }

}