package com.backbase.accesscontrol.service.facades;


import com.backbase.accesscontrol.business.flows.permissionset.CreatePermissionSetFlow;
import com.backbase.accesscontrol.business.flows.permissionset.DeletePermissionSetFlow;
import com.backbase.accesscontrol.business.flows.permissionset.GetPermissionSetsFlow;
import com.backbase.accesscontrol.business.flows.permissionset.UpdatePermissionSetFlow;
import com.backbase.accesscontrol.dto.DeletePermissionSetParameters;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PermissionSetFlowService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetFlowService.class);

    private CreatePermissionSetFlow createPermissionSetFlow;
    private DeletePermissionSetFlow deletePermissionSetFlow;
    private GetPermissionSetsFlow getPermissionSetsFlow;
    private UpdatePermissionSetFlow updatePermissionSetFlow;

    /**
     * Calls create permission set flow handler.
     *
     * @param presentationPermissionSet permission set definition
     * @return {@link PresentationInternalIdResponse}
     */
    public PresentationInternalIdResponse createPermissionSet(PresentationPermissionSet presentationPermissionSet) {
        LOGGER.info("Create permission set flow service invoked with data {}", presentationPermissionSet);
        return createPermissionSetFlow.start(presentationPermissionSet);
    }

    /**
     * Calls update permission set flow handler.
     *
     * @param permissionSetItemPut permission set definition
     */
    public void updatePermissionSet(PresentationPermissionSetItemPut permissionSetItemPut) {
        LOGGER.info("Update permission set flow service invoked with data {}", permissionSetItemPut);
        updatePermissionSetFlow.start(permissionSetItemPut);
    }

    /**
     * Calls delete permission flow handler.
     *
     * @param identifierType type of identifier
     * @param identifier identifier
     */
    public void deletePermissionSet(String identifierType, String identifier) {

        LOGGER.info("Trying to delete assignable permission set {} by {}", identifier, identifierType);

        deletePermissionSetFlow.start(new DeletePermissionSetParameters(identifierType, identifier));
    }

    /**
     * Retrieves all permission sets filtered by name.
     *
     * @param name - query parameter
     * @return list of {@link  PresentationPermissionSetResponseItem}
     */
    public List<PresentationPermissionSetResponseItem> getPermissionSetFilteredByName(String name) {
        LOGGER.info("Getting assignable permission sets filter by name {}", name);
        return getPermissionSetsFlow.start(name);
    }
}
