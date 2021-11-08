package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataGroupMapper {

    @Mapping(target = "type", source = "dataItemType")
    @Mapping(target = "items", source = "dataItemIds")
    DataGroupItemBase dataGroupToDataGroupItemBase(DataGroup dataGroup);

    @Mapping(target = "type", source = "dataItemType")
    DataGroupItemBase dataGroupToDataGroupItemBaseWithoutItems(DataGroup dataGroup);

    @Mapping(target = "type", source = "dataItemType")
    @Mapping(target = "items", source = "dataItemIds")
    DataGroupByIdGetResponseBody dataGroupToDataGroupByIdGetResponseBody(DataGroup dataGroup);

    List<DataGroupsGetResponseBody> dataGroupItemBaseToDataGroupsGetResponseBody(List<DataGroupItemBase> dataGroups);
}
