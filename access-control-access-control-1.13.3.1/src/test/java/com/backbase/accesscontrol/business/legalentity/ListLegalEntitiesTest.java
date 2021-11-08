package com.backbase.accesscontrol.business.legalentity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.LegalEntitiesGetResponseBodyMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListLegalEntitiesTest {

    @InjectMocks
    private ListLegalEntities listLegalEntities;
    @Spy
    private LegalEntitiesGetResponseBodyMapper mapper = Mappers.getMapper(LegalEntitiesGetResponseBodyMapper.class);
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private UserContextUtil userContextUtil;

    @Test
    public void shouldGetLegalEntitiesForNullParentEntityId() {
        String externalEntityId = "LE_PARENT";
        String internalLegalEntityId = "internalLeId";
        String key = "externalId";

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userid", externalEntityId));

        LegalEntity legalEntity = new LegalEntity()
            .withExternalId(key)
            .withId(internalLegalEntityId)
            .withName("name");
        LegalEntitiesGetResponseBody responseBody = new LegalEntitiesGetResponseBody()
            .withName("name")
            .withId(internalLegalEntityId)
            .withIsParent(true)
            .withExternalId(key);
        List<LegalEntitiesGetResponseBody> legalEntityPresentation = Collections.singletonList(responseBody);
        when(persistenceLegalEntityService.getLegalEntityById(eq(externalEntityId))).thenReturn(legalEntity);

        InternalRequest<List<LegalEntitiesGetResponseBody>> legalentities = listLegalEntities
            .getLegalentities(new InternalRequest<>(), null);

        assertEquals(legalEntityPresentation, legalentities.getData());
    }

    @Test
    public void shouldGetLegalEntitiesForEmptyStringParentEntityId() {
        String externalEntityId = "LE_PARENT";
        String internalLegalEntityId = "internalLeId";
        String key = "externalId";

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userid", externalEntityId));

        LegalEntity legalEntity = new LegalEntity()
            .withExternalId(key)
            .withId(internalLegalEntityId)
            .withName("name");
        LegalEntitiesGetResponseBody responseBody = new LegalEntitiesGetResponseBody()
            .withName("name")
            .withId(internalLegalEntityId)
            .withIsParent(true)
            .withExternalId(key);
        List<LegalEntitiesGetResponseBody> legalEntityPresentation = Collections.singletonList(responseBody);
        when(persistenceLegalEntityService.getLegalEntityById(eq(externalEntityId))).thenReturn(legalEntity);

        InternalRequest<List<LegalEntitiesGetResponseBody>> legalentities = listLegalEntities
            .getLegalentities(new InternalRequest<>(), "");

        assertEquals(legalEntityPresentation, legalentities.getData());
    }

    @Test
    public void shouldGetLegalEntitiesForParentEntityId() {
        String externalEntityId = "LE_PARENT";
        String internalLegalEntityId = "internalLeId";
        String key = "externalId";

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userid", externalEntityId));

        LegalEntity parent = new LegalEntity()
            .withId(externalEntityId);
        LegalEntity legalEntity = new LegalEntity()
            .withExternalId(key)
            .withId(internalLegalEntityId)
            .withName("name")
            .withParent(parent);
        LegalEntitiesGetResponseBody responseBody = new LegalEntitiesGetResponseBody()
            .withName("name")
            .withId(internalLegalEntityId)
            .withExternalId(key)
            .withIsParent(false)
            .withParentId(parent.getId());
        List<LegalEntitiesGetResponseBody> legalEntityPresentation = Collections.singletonList(responseBody);
        when(persistenceLegalEntityService.getLegalEntities(eq(externalEntityId)))
            .thenReturn(Collections.singletonList(legalEntity));

        InternalRequest<List<LegalEntitiesGetResponseBody>> legalentities = listLegalEntities
            .getLegalentities(new InternalRequest<>(), externalEntityId);

        assertEquals(legalEntityPresentation, legalentities.getData());
    }

}
