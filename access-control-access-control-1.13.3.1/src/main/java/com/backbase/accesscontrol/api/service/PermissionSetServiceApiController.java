package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.PermissionSetFlowService;
import com.backbase.accesscontrol.service.rest.spec.api.PermissionSetApi;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationId;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.accesscontrol.util.validation.ValidatePermissionSetIdentifiers;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static com.backbase.accesscontrol.audit.AuditObjectType.PERMISSION_SET;
import static com.backbase.accesscontrol.audit.EventAction.DELETE;

@RestController
@AllArgsConstructor
public class PermissionSetServiceApiController implements PermissionSetApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetServiceApiController.class);
    private static final Set<String> identifierTypes = ImmutableSet.copyOf(Sets.newHashSet("id", "name"));

    private PermissionSetFlowService permissionSetFlowService;
    private AccessToken accessToken;

    private PayloadConverter payloadConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetResponseItem>> getPermissionSet(
        String name) {
        LOGGER.info("Getting assignable permission sets filter by name {}", name);
        List<PresentationPermissionSetResponseItem> response = permissionSetFlowService
            .getPermissionSetFilteredByName(name);
        return new ResponseEntity<>(payloadConverter.convertListPayload(response,
            com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetResponseItem.class),
            HttpStatus.OK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.PERMISSION_SET)
    public ResponseEntity<PresentationId> postPermissionSet(
        @RequestBody com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSet presentationPermissionSet) {
        LOGGER.info("Create permission set endpoint invoked with data {}", presentationPermissionSet);
        PresentationInternalIdResponse response = permissionSetFlowService
            .createPermissionSet(
                payloadConverter.convertAndValidate(presentationPermissionSet, PresentationPermissionSet.class));
        return new ResponseEntity<>(payloadConverter.convert(response,
            com.backbase.accesscontrol.service.rest.spec.model.PresentationId.class), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(eventAction = EventAction.UPDATE_ASSOCIATED_APS, objectType = AuditObjectType.PERMISSION_SET)
    public ResponseEntity<Void> putPermissionSet(
        @RequestBody com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionSetPut presentationPermissionSetItemPut) {
        LOGGER.info("Updating permission set endpoint invoked with data {}", presentationPermissionSetItemPut);

        permissionSetFlowService.updatePermissionSet(
            payloadConverter.convertAndValidate(presentationPermissionSetItemPut, PresentationPermissionSetItemPut.class));
        return ResponseEntity.ok().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AuditEvent(objectType = PERMISSION_SET, eventAction = DELETE)
    public ResponseEntity<Void> deleteByIdentifier(@PathVariable("identifierType") String identifierType,
        @PathVariable("identifier") String identifier,
        @RequestHeader(value = "X-AccessControl-Token", required = true) String xAccessControlToken) {

        if (!identifierTypes.contains(identifierType)) {
            throw new NotFoundException();
        }

        accessToken.validateAccessToken(
            xAccessControlToken,
            String.format("IdentifierType: %s identifier: %s", identifierType, identifier)
        );

        ValidatePermissionSetIdentifiers.validateIdentifiers(identifierType, identifier);

        LOGGER.info("Deleting assignable permission set {} by {}", identifier, identifierType);

        permissionSetFlowService.deletePermissionSet(identifierType, identifier);

        return ResponseEntity.noContent().build();
    }
}
