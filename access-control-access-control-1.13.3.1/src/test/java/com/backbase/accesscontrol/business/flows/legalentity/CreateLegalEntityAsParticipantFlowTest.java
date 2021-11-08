package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_020;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityAsParticipantHandler;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.NewMasterServiceAgreement;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.ParticipantOf;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.Status;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntityAsParticipantFlowTest {
    
    @Mock
    private CreateLegalEntityAsParticipantHandler handler;
    
    @Mock
    private UserContextUtil contextUtil;
    
    private CreateLegalEntityAsParticipantFlow testFlow;
    
    @Before
    public void setUp() {
        testFlow = new CreateLegalEntityAsParticipantFlow(handler, contextUtil);
    }
    
    @Test
    public void shouldCreateLegalEntityAsParticipant() {
        String legalEntityId = "legalEntityId";
        when(contextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("internalUserId", legalEntityId));
        
        LegalEntityAsParticipantPostRequestBody legalEntityObject = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("name")
                        .withLegalEntityExternalId("externalId")
                        .withLegalEntityParentId("5643e686d3ae4216b3ff5d66a6ad897d")
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withNewMasterServiceAgreement(new NewMasterServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")
                                                        .withServiceAgreementState(Status.ENABLED)));
        
        when(handler.handleRequest(any(SingleParameterHolder.class), any(LegalEntityAsParticipantPostRequestBody.class)))
                        .thenReturn(new LegalEntityAsParticipantPostResponseBody().withLegalEntityId("legalEntityId")
                                        .withServiceAgreementId("serviceAgreementId"));
        
        LegalEntityAsParticipantPostResponseBody response = testFlow.execute(legalEntityObject);
        
        ArgumentCaptor<SingleParameterHolder> parameterCaptor = ArgumentCaptor.forClass(SingleParameterHolder.class);
        verify(handler).handleRequest(parameterCaptor.capture(), eq(legalEntityObject));
        assertEquals(legalEntityId, parameterCaptor.getValue().getParameter());
        
        assertNotNull(response);
        assertEquals("legalEntityId", response.getLegalEntityId());
        assertEquals("serviceAgreementId", response.getServiceAgreementId());
    }
    
    @Test
    public void shouldThrowBadRequestWhenBankDoesNotProvideExternalId() {
        LegalEntityAsParticipantPostRequestBody legalEntityObject = new LegalEntityAsParticipantPostRequestBody()
                        .withLegalEntityName("name")
                        .withLegalEntityParentId("5643e686d3ae4216b3ff5d66a6ad897d")
                        .withLegalEntityType(LegalEntityType.BANK)
                        .withParticipantOf(new ParticipantOf()
                                        .withNewMasterServiceAgreement(new NewMasterServiceAgreement()
                                                        .withServiceAgreementName("name")
                                                        .withServiceAgreementDescription("desc")
                                                        .withServiceAgreementState(Status.ENABLED)));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testFlow.execute(legalEntityObject));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_LE_020.getErrorMessage(), ERR_LE_020.getErrorCode())));
    }
    

}
