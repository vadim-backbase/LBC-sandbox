package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.dto.DataGroupWithApplicableFunctionPrivilege;
import java.util.List;
import java.util.Set;

public interface UserAssignedCombinationRepositoryCustom {

    List<DataGroupWithApplicableFunctionPrivilege> findAllUserDataItemsPrivileges(String userId,
        String serviceAgreement, String dataItemType, Set<String> applicableFunctionPrivilegeIds);

    List<DataGroup> findByUserIdAndServiceAgreementIdAndAfpIdsInAndDataType(
        String userId, String serviceAgreementId, Set<String> applicableFunctionPrivilegeIds,
        String dataItemType);
}
