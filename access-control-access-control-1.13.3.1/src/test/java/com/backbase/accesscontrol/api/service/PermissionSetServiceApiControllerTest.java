package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.aps.PresentationInternalIdResponseToPresentationIdMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.aps.PresentationPermissionSetMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.aps.PresentationPermissionSetPutToPresentationPermissionSetItemPutMapper;
import com.backbase.accesscontrol.mappers.model.accessgroup.service.aps.PresentationPermissionSetResponseItemMapper;
import com.backbase.accesscontrol.service.facades.PermissionSetFlowService;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationId;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSetServiceApiControllerTest {

    @Mock
    private PermissionSetFlowService flowService;

    @Mock
    private AccessToken accessToken;

    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(PresentationInternalIdResponseToPresentationIdMapper.class)),
            spy(Mappers.getMapper(PresentationPermissionSetResponseItemMapper.class)),
            spy(Mappers.getMapper(PresentationPermissionSetPutToPresentationPermissionSetItemPutMapper.class)),
            spy(Mappers.getMapper(PresentationPermissionSetMapper.class))
        ));

    @InjectMocks
    private PermissionSetServiceApiController permissionSetServiceApiController;

    @Test
    public void shouldCreatePermissionSet() {
        PresentationInternalIdResponse expectedInternalIdResponse = new PresentationInternalIdResponse()
            .withId(new BigDecimal(1234));
        PresentationId response = new PresentationId()
            .additions(new HashMap<>())
            .id(new BigDecimal(1234));

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");
        com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet request = new com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet()
            .name("apsName")
            .description("apsDescription");

        when(flowService.createPermissionSet(eq(permissionSet))).thenReturn(expectedInternalIdResponse);

        PresentationId presentationInternalIdResponse = permissionSetServiceApiController
            .postPermissionSet(request).getBody();

        verify(flowService, times(1)).createPermissionSet(eq(permissionSet));
        assertEquals(response.getId(), presentationInternalIdResponse.getId());
    }

    @Test
    public void shouldUpdatePermissionSet() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L)))
                .withNameIdentifiers(Collections.<String>emptySet()))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name"))
                .withIdIdentifiers(Collections.<BigDecimal>emptySet()));

        com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut request = new com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut()
            .externalServiceAgreementId("ex-sa-id")
            .adminUserAps(new com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers()
                .idIdentifiers(Collections.singletonList(new BigDecimal(1L)))
                .nameIdentifiers(Lists.emptyList()))
            .regularUserAps(new com.backbase.accesscontrol.service.rest.spec.model.PresentationUserApsIdentifiers()
                .nameIdentifiers(Collections.singletonList("APS name"))
                .idIdentifiers(Lists.emptyList()));

        permissionSetServiceApiController.putPermissionSet(request);

        verify(flowService).updatePermissionSet(eq(itemPut));
    }

    @Test
    public void shouldCallFlowServiceOnDeletePermissionSet() {
        String token = "token";

        doNothing().when(accessToken).validateAccessToken(eq(token), any());

        permissionSetServiceApiController
            .deleteByIdentifier("id", "1", token);
        verify(flowService).deletePermissionSet(eq("id"), eq("1"));
    }

    @Test
    public void shouldGetPermissionSetsByNameFilter() {
        String name = "name";
        permissionSetServiceApiController.getPermissionSet(name);
        verify(flowService, times(1)).getPermissionSetFilteredByName(eq(name));
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundForInvalidIdentifierType() {
        permissionSetServiceApiController.deleteByIdentifier("invalid", "1", "token");
    }
}