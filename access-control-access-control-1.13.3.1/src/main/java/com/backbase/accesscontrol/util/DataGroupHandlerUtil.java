package com.backbase.accesscontrol.util;

import com.backbase.accesscontrol.domain.DataGroup;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataGroupHandlerUtil {

    /**
     * Returns list of data group ids.
     *
     * @param dataGroup    - Data group
     * @param includeItems - boolean value that indicates whether data group items should be returned
     * @return List of ids of the data group items
     */
    public static List<String> getDataGroupItemsIds(DataGroup dataGroup, boolean includeItems) {
        if (includeItems) {
            return new ArrayList<>(dataGroup.getDataItemIds());
        }
        return new ArrayList<>();
    }

}
