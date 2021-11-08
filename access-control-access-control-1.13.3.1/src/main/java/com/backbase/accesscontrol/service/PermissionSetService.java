package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface PermissionSetService {

    /**
     * Gets permission set filtered by name.
     *
     * @param parameter - name of the permission set
     * @return list of {@link AssignablePermissionSet}
     */
    List<AssignablePermissionSet> getPermissionSetFilteredByName(String parameter);

    /**
     * Save permission set.
     *
     * @param persistencePermissionSet {@link PresentationPermissionSet}
     * @return {@link PresentationPermissionSet}
     */
    BigDecimal save(PresentationPermissionSet persistencePermissionSet);

    /**
     * Delete permission set.
     *
     * @param identifierType identifier type
     * @param identifier     identifier
     * @return id
     */
    Long delete(String identifierType, String identifier);

    /**
     * Get assignable permission sets by name.
     *
     * @param apsNames      set of names
     * @param isRegularUser boolean
     * @return set of {@link AssignablePermissionSet}
     */
    Set<AssignablePermissionSet> getAssignablePermissionSetsByName(Set<String> apsNames, boolean isRegularUser);

    /**
     * Gets assignable permission sets by id.
     *
     * @param apsIds        ids
     * @param isRegularUser boolean
     * @return set of {@link AssignablePermissionSet}
     */
    Set<AssignablePermissionSet> getAssignablePermissionSetsById(Set<Long> apsIds, boolean isRegularUser);

    /**
     * Update permission set.
     *
     * @param requestData {@link PresentationPermissionSetItemPut}
     */
    String update(PresentationPermissionSetItemPut requestData);
}
