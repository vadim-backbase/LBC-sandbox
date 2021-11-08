package com.backbase.accesscontrol.service.impl.strategy.approval;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup.Type;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.mappers.FunctionGroupMapperPersistence;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalPermissionUtil {

    private final BusinessFunctionCache businessFunctionCache;
    private final ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    private final FunctionGroupMapperPersistence functionGroupMapperPersistence;

    public List<Permission> createPermissionList(Map<String, List<PrivilegeDto>> map) {
        return map.entrySet().stream().map(item ->
            new Permission().withFunctionId(item.getKey())
                .withAssignedPrivileges(item.getValue())
        ).collect(Collectors.toList());
    }

    public Map<String, List<PrivilegeDto>> getMapBusinessFuncIdAndPrivilegesForApprovalFunctionGroup(
        ApprovalFunctionGroup approvalRequest) {

        Set<ApplicableFunctionPrivilege> afps
            = businessFunctionCache.getApplicableFunctionPrivileges(approvalRequest.getPrivileges());

        Map<String, List<PrivilegeDto>> map = new HashMap<>();
        afps.stream().filter(Objects::nonNull).forEach(item -> {
            if (!map.containsKey(item.getBusinessFunction().getId())) {
                List<PrivilegeDto> privileges = new ArrayList<>();
                privileges.add(new PrivilegeDto().withPrivilege(item.getPrivilege().getName()));
                map.put(item.getBusinessFunction().getId(), privileges);
            } else {
                List<PrivilegeDto> privilegesList = map.get(item.getBusinessFunction().getId());
                privilegesList.add(new PrivilegeDto().withPrivilege(item.getPrivilege().getName()));
                map.put(item.getBusinessFunction().getId(), privilegesList);
            }
        });
        return map;
    }

    public String getLegalEntityIdFromServiceAgreement(ApprovalFunctionGroup approvalRequest) {

        ServiceAgreement sa = serviceAgreementJpaRepository.findById(approvalRequest.getServiceAgreementId())
            .orElseThrow(() -> getNotFoundException(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
        return sa.getCreatorLegalEntity().getId();
    }

    public FunctionGroupBase convertAndReturnFunctionGroupBase(ApprovalFunctionGroup approvalRequest) {

        String legalEntityId = getLegalEntityIdFromServiceAgreement(approvalRequest);
        Map<String, List<PrivilegeDto>> mapBusinessFunctionIdAndPrivileges =
            getMapBusinessFuncIdAndPrivilegesForApprovalFunctionGroup(approvalRequest);
        List<Permission> permissionList =
            createPermissionList(mapBusinessFunctionIdAndPrivileges);

        return functionGroupMapperPersistence.approvalFunctionGroupToFunctionGroupBase(
            approvalRequest, legalEntityId, permissionList, Type.DEFAULT);
    }
}
