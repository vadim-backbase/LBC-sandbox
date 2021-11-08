package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.DataGroupItem;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DataGroupItemJpaRepository extends JpaRepository<DataGroupItem, String> {

    List<DataGroupItem> findAllByDataGroupIdIn(Collection<String> dataGroupIds);

    List<DataGroupItem> findAllByDataItemIdAndDataGroupIdIn(String dataItemId, Collection<String> dataGroupIds);

    boolean existsByDataGroupIdAndDataItemIdIn(String dataGroupId, Collection<String> dataItems);

    List<DataGroupItem> findByDataGroupIdAndDataItemIdIn(String dataGroup, Collection<String> dataItems);

    List<DataGroupItem> findAllByDataGroupId(String id);

    @Modifying
    @Query("delete from DataGroupItem d where d.dataGroupId = ?1")
    void deleteAllByDataGroupId(String dataGroupId);

    @Modifying
    @Query("delete from DataGroupItem d where d.dataGroupId = ?1 and d.dataItemId in ?2")
    void deleteAllByDataGroupIdAndItemIdIn(String dataGroupId, Collection<String> dataItems);
}
