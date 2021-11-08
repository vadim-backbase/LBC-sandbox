package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.backbase.accesscontrol.business.persistence.legalentity.AddLegalEntityHandler;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateLegalEntitiesTest {

    private CreateLegalEntities createLegalEntities;

    @Mock
    private AddLegalEntityHandler addLegalEntityHandler;

    @Captor
    private ArgumentCaptor<LegalEntitiesPostRequestBody> captor;

    @Before
    public void setUp() {
        createLegalEntities = spy(new CreateLegalEntities(addLegalEntityHandler));
    }

    @Test
    public void shouldGetLegalEntityByIdWhenLegalEntityIsCreated() {
        LegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new LegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(addLegalEntityHandler)
            .handleRequest(any(), any(LegalEntitiesPostRequestBody.class));

        InternalRequest<LegalEntitiesPostRequestBody> internalRequest = getInternalRequest(
            new LegalEntitiesPostRequestBody());

        InternalRequest<LegalEntitiesPostResponseBody> pandpResponse = createLegalEntities
            .createLegalEntity(internalRequest);

        assertEquals(legalEntitiesPostResponseBody, pandpResponse.getData());
    }

    @Test
    public void shouldGetLegalEntityByIdWhenRootLegalEntityIsCreated() {
        LegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new LegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(addLegalEntityHandler)
            .handleRequest(any(), any(LegalEntitiesPostRequestBody.class));

        LegalEntitiesPostRequestBody data = new LegalEntitiesPostRequestBody()
            .withParentExternalId(null);

        InternalRequest<LegalEntitiesPostRequestBody> internalRequest = getInternalRequest(data);

        InternalRequest<LegalEntitiesPostResponseBody> pandpResponse = createLegalEntities
            .createLegalEntity(internalRequest);

        assertEquals(legalEntitiesPostResponseBody, pandpResponse.getData());
    }

    @Test
    public void shouldConvertLegalEntityType() {
        LegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new LegalEntitiesPostResponseBody()
            .withId("id-01");

        doReturn(legalEntitiesPostResponseBody).when(addLegalEntityHandler)
            .handleRequest(any(), captor.capture());

        LegalEntitiesPostRequestBody data = new LegalEntitiesPostRequestBody()
            .withParentExternalId(null)
            .withType(LegalEntityType.BANK);

        createLegalEntities.createLegalEntity(getInternalRequest(data));
        LegalEntitiesPostRequestBody capturedData = captor.getValue();
        assertEquals(data.getType().toString(), capturedData.getType().toString());
    }
}
