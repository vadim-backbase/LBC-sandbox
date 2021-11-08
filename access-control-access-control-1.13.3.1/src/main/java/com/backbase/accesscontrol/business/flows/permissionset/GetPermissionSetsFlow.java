package com.backbase.accesscontrol.business.flows.permissionset;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.mappers.PermissionSetMapper;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class GetPermissionSetsFlow extends AbstractFlow<String, List<PresentationPermissionSetResponseItem>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPermissionSetsFlow.class);

    private PermissionSetService permissionSetService;
    private PermissionSetMapper permissionSetMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<PresentationPermissionSetResponseItem> execute(String name) {
        LOGGER.info("Getting assignable permission sets filter by name {}", name);

        return permissionSetMapper.sourceToDestination(
            permissionSetService.getPermissionSetFilteredByName(name));
    }
}
