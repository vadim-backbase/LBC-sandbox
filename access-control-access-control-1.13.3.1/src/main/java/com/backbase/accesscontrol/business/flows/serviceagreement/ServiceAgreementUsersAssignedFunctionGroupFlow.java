package com.backbase.accesscontrol.business.flows.serviceagreement;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ServiceAgreementUsersAssignedFunctionGroupFlow extends
    AbstractFlow<UserAssignedFunctionGroupDto, RecordsDto<UserAssignedFunctionGroupResponse>> {

    private final UserContextService userContextService;

    @Override
    protected RecordsDto<UserAssignedFunctionGroupResponse> execute(
        UserAssignedFunctionGroupDto userAssignedFunctionGroupDto) {

        Page<String> userIdsPage = userContextService.findUserIdsByServiceAgreementIdAndFunctionGroupId(
            userAssignedFunctionGroupDto.getServiceAgreementId(),
            userAssignedFunctionGroupDto.getFunctionGroupId(),
            userAssignedFunctionGroupDto.getFrom(),
            userAssignedFunctionGroupDto.getSize()
        );

        List<UserAssignedFunctionGroupResponse> usersWithAssignedFunctionGroup = userIdsPage
            .getContent()
            .stream()
            .map(userId -> new UserAssignedFunctionGroupResponse().withId(userId))
            .collect(Collectors.toList());

        return new RecordsDto<>(userIdsPage.getTotalElements(), usersWithAssignedFunctionGroup);
    }
}