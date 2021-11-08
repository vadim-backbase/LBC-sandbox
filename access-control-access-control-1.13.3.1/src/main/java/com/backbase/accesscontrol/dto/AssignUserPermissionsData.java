
package com.backbase.accesscontrol.dto;

import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AssignUserPermissionsData {

    private PresentationAssignUserPermissions assignUserPermissions;
    private Map<String, GetUser> usersByExternalId;
}
