package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.dto.PersistenceDataGroupExtendedItemDto;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import java.util.Collection;
import java.util.List;

public interface DataGroupService {

    /**
     * Returns true if there is a data group withing the given service agreement.
     *
     * @param serviceAgreementId       - service agreement id
     * @param returnApprovalDataGroups true/false
     * @return true/false
     */
    Boolean checkIfExistsPendingDataGroupByServiceAgreementId(String serviceAgreementId,
        boolean returnApprovalDataGroups);

    /**
     * Gets extended data group by id.
     *
     * @param id data group id
     * @return {@link DataGroup}
     */
    DataGroup getByIdWithExtendedData(String id);

    /**
     * Gets data group by id.
     *
     * @param id data group id
     * @return {@link DataGroup}
     */
    DataGroup getById(String id);

    /**
     * Get data group by approval id.
     *
     * @param approvalId approval id
     * @return {@link PresentationDataGroupApprovalDetailsItem}
     */
    PresentationDataGroupApprovalDetailsItem getByApprovalId(String approvalId);

    /**
     * Get bulk data groups by ids.
     *
     * @param ids data group ids
     * @return list of {@link DataGroup}
     */
    List<DataGroup> getBulkDataGroups(Collection<String> ids);

    /**
     * Get data groups by external service agreement ids.
     *
     * @param ids                      service agreement ids.
     * @param returnApprovalDataGroups boolean
     * @return list of {@link PersistenceDataGroupExtendedItemDto}
     */
    List<PersistenceDataGroupExtendedItemDto> getDataGroupsByExternalServiceAgreementIds(Collection<String> ids,
        boolean returnApprovalDataGroups);

    /**
     * Saves data group.
     *
     * @param dataGroupBase {@link DataGroupBase}
     * @return id
     */
    String save(DataGroupBase dataGroupBase);

    /**
     * Save data group approval.
     *
     * @param dataGroupApprovalCreate {@link DataGroupBase}
     * @return id
     */
    String saveDataGroupApproval(
        DataGroupBase dataGroupApprovalCreate, String approvalId);

    /**
     * Update data group.
     *
     * @param dataGroupId data group id
     * @param dataGroup   DataGroup
     */
    void update(String dataGroupId, DataGroupByIdPutRequestBody dataGroup);

    /**
     * Update data group.
     *
     * @param dataGroup {@link PresentationSingleDataGroupPutRequestBody}
     * @return dataGroupId
     */
    String update(PresentationSingleDataGroupPutRequestBody dataGroup);

    /**
     * Update data group approval.
     *
     * @param dataGroupApprovalUpdate {@link DataGroupByIdPutRequestBody}
     */
    void updateDataGroupApproval(DataGroupByIdPutRequestBody dataGroupApprovalUpdate);

    /**
     * delete data group approval.
     *
     * @param dataGroupId String
     * @param approvalId  String
     */
    void deleteDataGroupApproval(String dataGroupId, String approvalId);

    /**
     * Delete data group.
     *
     * @param dataGroupId data group id
     */
    void delete(String dataGroupId);

    /**
     * Get data group by service agreement id and data group type.
     *
     * @param serviceAgreementId service agreement id
     * @param type               data group type
     * @param includeItems       should include items in the response
     * @return list of {@link DataGroupItemBase}
     */
    List<DataGroupItemBase> getByServiceAgreementIdAndDataItemType(String serviceAgreementId, String type,
        boolean includeItems);

    /**
     * Retrieves id of the data group by name and external service agreement id.
     *
     * @param dataGroupIdentifier - identifier to retrieve id from
     * @return data group id
     */
    String retrieveDataGroupIdFromIdentifier(PresentationIdentifier dataGroupIdentifier);

    /**
     * Updates items in data group.
     *
     * @param item - data group item
     */
    String updateDataGroupItemsByIdIdentifier(PresentationDataGroupItemPutRequestBody item);

    /**
     * Gets Data Group by given id.
     *
     * @param dataGroupId - data group id
     * @return {@link DataGroup}
     */
     DataGroup getDataGroupIfExists(String dataGroupId);
}
