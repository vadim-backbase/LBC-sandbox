package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.auth.AccessResourceType.USER_AND_ACCOUNT;
import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionValidationService.class);
    private static final String ACCESS_TO_MANAGE_ENTITLEMENTS = "User does not have access to Manage Entitlements";

    private final AccessControlValidator accessControlValidator;
    private final FunctionGroupService functionGroupService;
    private final DataGroupService dataGroupService;
    private final DataGroupMapper dataGroupMapper;

    /**
     * Check if the logged user has permissions in service agreement.
     *
     * @param serviceAgreementIdFromPayload - service agreement id
     * @param accessResourceType            - type of access
     */
    public void validateAccessToServiceAgreementResource(String serviceAgreementIdFromPayload,
        AccessResourceType accessResourceType) {

        if (accessControlValidator
            .userHasNoAccessToServiceAgreement(
                serviceAgreementIdFromPayload, accessResourceType)) {
            LOGGER.warn(ACCESS_TO_MANAGE_ENTITLEMENTS + " for service agreement id {}", serviceAgreementIdFromPayload);
            throw getForbiddenException(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_032.getErrorCode());
        }
    }

    /**
     * Check if the logged user has permissions in service agreement.
     *
     * @param legalEntity        - legal entity id
     * @param accessResourceType - type of access
     */
    public void validateAccessToLegalEntityResource(String legalEntity, AccessResourceType accessResourceType) {
        if (accessControlValidator
            .userHasNoAccessToEntitlementResource(legalEntity,
                accessResourceType)) {
            LOGGER.warn(ACCESS_TO_MANAGE_ENTITLEMENTS + " for legal entity id {}", legalEntity);
            throw getForbiddenException(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_032.getErrorCode());
        }
    }

    /**
     * Check if the logged user has permissions in service agreement.
     *
     * @param legalEntities      - legal entity ids
     * @param accessResourceType - type of access
     */
    public void validateAccessToLegalEntityResource(List<String> legalEntities, AccessResourceType accessResourceType) {
        if (accessControlValidator
            .userHasNoAccessToEntitlementResource(
                legalEntities, accessResourceType)) {
            LOGGER.warn(ACCESS_TO_MANAGE_ENTITLEMENTS + " for legal entity ids {}", legalEntities);
            throw getForbiddenException(AccessGroupErrorCodes.ERR_AG_032.getErrorMessage(),
                AccessGroupErrorCodes.ERR_AG_032.getErrorCode());
        }
    }


    /**
     * Retrieves Function Group by id from P&P service.
     *
     * @param id - Function Group id
     * @return Function Group
     */
    public FunctionGroupByIdGetResponseBody getFunctionGroupById(String id) {
        return functionGroupService.getFunctionGroupById(id);
    }

    /**
     * Returns the data group by id from persistence layer.
     *
     * @param id - Data Group id
     * @return the Data Group
     */
    public DataGroupItemBase getDataGroupById(String id) {
        DataGroup dataGroup = dataGroupService.getByIdWithExtendedData(id);

        return dataGroupMapper.dataGroupToDataGroupItemBase(dataGroup);
    }
}
