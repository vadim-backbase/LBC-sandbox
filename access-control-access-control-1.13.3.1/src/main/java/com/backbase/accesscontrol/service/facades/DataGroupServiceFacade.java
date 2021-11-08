package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.DataGroupHandlerUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.BulkSearchDataGroupsPostRequestBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class DataGroupServiceFacade {

    private DataGroupService dataGroupService;
    private DataGroupMapper dataGroupMapper;

    public DataGroupItemBase getDataGroupById(String dataGroupId, boolean includeItems) {
        if (includeItems) {
            return dataGroupMapper.dataGroupToDataGroupItemBase(dataGroupService.getByIdWithExtendedData(dataGroupId));
        } else {
            return dataGroupMapper.dataGroupToDataGroupItemBaseWithoutItems(dataGroupService.getById(dataGroupId));
        }
    }

    public List<DataGroupItemBase> getDataGroups(String serviceAgreementId, String type, Boolean includeItems) {
        return dataGroupService.getByServiceAgreementIdAndDataItemType(serviceAgreementId, type, includeItems);
    }

    public List<DataGroupItemBase> getBulkDataGroups(BulkSearchDataGroupsPostRequestBody requestData) {
        List<DataGroup> dataGroups = dataGroupService.getBulkDataGroups(requestData.getIds());

        return dataGroups.stream().map(dataGroup -> new DataGroupItemBase()
            .withId(dataGroup.getId())
            .withItems(DataGroupHandlerUtil.getDataGroupItemsIds(dataGroup, true))
            .withDescription(dataGroup.getDescription())
            .withName(dataGroup.getName())
            .withType(dataGroup.getDataItemType())
        ).collect(Collectors.toList());
    }
}
