package com.backbase.accesscontrol.util.helpers;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;

public class DataGroupUtil {

    public static DataGroup createDataGroup(String name, String dataItemType, String description,
        ServiceAgreement serviceAgreement) {

        DataGroup dataGroup = new DataGroup();
        dataGroup.setName(name);
        dataGroup.setDataItemType(dataItemType);
        dataGroup.setDescription(description);
        dataGroup.setServiceAgreement(serviceAgreement);
        if (Objects.nonNull(serviceAgreement)) {
            dataGroup.setServiceAgreementId(serviceAgreement.getId());
        }
        return dataGroup;
    }

    public static DataGroup createDataGroup(String name, String dataItemType, String description,
        ServiceAgreement serviceAgreement, List<String> itemIds) {

        DataGroup dataGroup = createDataGroup(name, dataItemType, description, serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet(itemIds));
        return dataGroup;
    }
}
