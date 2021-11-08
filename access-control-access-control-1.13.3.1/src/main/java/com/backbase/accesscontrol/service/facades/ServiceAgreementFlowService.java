package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.businessfunction.GetBusinessFunctionsForServiceAgreementFlow;
import com.backbase.accesscontrol.business.flows.serviceagreement.ServiceAgreementUsersAssignedFunctionGroupFlow;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ServiceAgreementFlowService {

    private GetBusinessFunctionsForServiceAgreementFlow getBusinessFunctionsForServiceAgreementFlow;
    private ServiceAgreementUsersAssignedFunctionGroupFlow serviceAgreementUsersAssignedFunctionGroupFlow;

    /**
     * Get business functions for service agreement.
     *
     * @param id service agreement id
     * @return list of business functions
     */
    public List<FunctionsGetResponseBody> getBusinessFunctionsForServiceAgreement(String id) {
        log.debug("Trying to get business for service agreement with id {} ", id);
        return getBusinessFunctionsForServiceAgreementFlow.start(id);
    }

    public RecordsDto<UserAssignedFunctionGroupResponse> getUsersWithAssignedFunctionGroup(
        UserAssignedFunctionGroupDto userAssignedFunctionGroupDto) {
        log.debug("Fetching users with assigned functionGroup by serviceAgreement {} and functionGroupId {}",
            userAssignedFunctionGroupDto.getServiceAgreementId(), userAssignedFunctionGroupDto.getFunctionGroupId());
        return serviceAgreementUsersAssignedFunctionGroupFlow.start(userAssignedFunctionGroupDto);
    }
}
