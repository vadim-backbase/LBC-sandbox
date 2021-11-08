package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserContextServiceFacade {

    private UserContextService userContextService;

    public UserContextsGetResponseBody getUserContextsByUserId(String userId, String query, Integer from,
        Integer size) {

        return userContextService.getUserContextsByUserId(userId, query, from, size);
    }

    public void validateUserContext(String userId, String serviceAgreementId) {

        userContextService.validateUserContext(userId, serviceAgreementId);
    }

    public void getDataItemsPermissions(Set<String> uniqueTypes,
        DataItemsPermissions dataItemsPermissions, String internalUserId, String serviceAgreementFromContext) {

        userContextService
            .checkDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);
    }
}
