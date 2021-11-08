package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityHandler;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddLegalEntitiesTest {

    private AddLegalEntities addLegalEntities;
    @Captor
    private ArgumentCaptor<CreateLegalEntitiesPostRequestBody> captor;
    @Mock
    private CreateLegalEntityHandler crateLegalEntityHandler;

    @Before
    public void setUp() {
        addLegalEntities = spy(new AddLegalEntities(crateLegalEntityHandler));
    }

    @Test
    public void shouldGetLegalEntityByIdWhenLegalEntityIsCreated() {
        CreateLegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(crateLegalEntityHandler)
            .handleRequest(any(), any(CreateLegalEntitiesPostRequestBody.class));

        InternalRequest<CreateLegalEntitiesPostRequestBody> internalRequest = getInternalRequest(
            new CreateLegalEntitiesPostRequestBody());

        InternalRequest<CreateLegalEntitiesPostResponseBody> pandpResponse = addLegalEntities
            .createLegalEntity(internalRequest);

        assertEquals(legalEntitiesPostResponseBody.getId(), pandpResponse.getData().getId());
    }

    @Test
    public void shouldGetLegalEntityByIdWhenRootLegalEntityIsCreated() {
        CreateLegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(crateLegalEntityHandler)
            .handleRequest(any(), any(CreateLegalEntitiesPostRequestBody.class));

        CreateLegalEntitiesPostRequestBody data = new CreateLegalEntitiesPostRequestBody()
            .withParentExternalId(null);

        InternalRequest<CreateLegalEntitiesPostRequestBody> internalRequest = getInternalRequest(data);

        InternalRequest<CreateLegalEntitiesPostResponseBody> pandpResponse = addLegalEntities
            .createLegalEntity(internalRequest);

        assertEquals(legalEntitiesPostResponseBody.getId(), pandpResponse.getData().getId());
    }

    @Test
    public void shouldConvertLegalEntityType() {
        CreateLegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(crateLegalEntityHandler)
            .handleRequest(any(), captor.capture());

        CreateLegalEntitiesPostRequestBody data = new CreateLegalEntitiesPostRequestBody()
            .withParentExternalId(null)
            .withType(LegalEntityType.BANK);

        InternalRequest<CreateLegalEntitiesPostRequestBody> internalRequest = getInternalRequest(data);

        addLegalEntities.createLegalEntity(internalRequest);
        CreateLegalEntitiesPostRequestBody capturedData = captor.getValue();
        assertEquals(data.getType().toString(), capturedData.getType().toString());
    }
}
