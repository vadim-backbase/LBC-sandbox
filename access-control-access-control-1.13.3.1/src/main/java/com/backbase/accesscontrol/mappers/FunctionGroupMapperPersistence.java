package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup.Type;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FunctionGroupMapperPersistence {

    /**
     * Creates function group from ApprovalFunctionGroup without the privileges.
     *
     * @param approvalFunctionGroup function group in pending tables
     * @param type the type of the function group (should be DEFAULT)
     * @return {@link FunctionGroup}
     */
    @Mapping(target = "id", expression = "java(approvalFunctionGroup.getFunctionGroupId())")
    FunctionGroup approvalFunctionGroupToFunctionGroup(ApprovalFunctionGroup approvalFunctionGroup,
        FunctionGroupType type);

    /**
     * Creates function group base from ApprovalFunctionGroup without the privileges.
     *
     * @param approvalFunctionGroup function group in pending tables
     * @param legalEntityId legal Entity id
     * @param permissions list of permissions
     * @param type the type of the function group (should be DEFAULT)
     * @return {@link FunctionGroup}
     */
    @Mapping(target = "validFrom", expression = "java(approvalFunctionGroup.getStartDate())")
    @Mapping(target = "validUntil", expression = "java(approvalFunctionGroup.getEndDate())")
    FunctionGroupBase approvalFunctionGroupToFunctionGroupBase(ApprovalFunctionGroup approvalFunctionGroup,
        String legalEntityId, List<Permission> permissions, Type type);

    @Mapping(target = "validFrom", expression = "java(approvalFunctionGroup.getStartDate())")
    @Mapping(target = "validUntil", expression = "java(approvalFunctionGroup.getEndDate())")
    FunctionGroupBase approvalFunctionGroupToFunctionGroupBaseDto(
        ApprovalFunctionGroup approvalFunctionGroup,
        String legalEntityId, List<Permission> permissions, Type type);

    PresentationFunctionGroupApprovalDetailsItem createFromApprovalFunctionGroup(
        ApprovalFunctionGroup approvalFunctionGroup);

    @Mapping(target = "functionGroupId", source = "id")
    PresentationFunctionGroupApprovalDetailsItem createFromFunctionGroup(FunctionGroup functionGroup);

    com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase toFunctionGroupBasePost(com.backbase.accesscontrol.client.rest.spec.model.FunctionGroupItemBase functionGroupItemBase);
}
