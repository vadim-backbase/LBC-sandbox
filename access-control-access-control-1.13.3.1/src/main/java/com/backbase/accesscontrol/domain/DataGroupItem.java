package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.idclass.DataGroupItemIdClass;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "data_group_item")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DataGroupItemIdClass.class)
@Setter
@Getter
@EqualsAndHashCode
public class DataGroupItem {

    @Id
    @Column(name = "data_item_id", nullable = false, length = 36)
    private String dataItemId;

    @Id
    @Column(name = "data_group_id", nullable = false)
    private String dataGroupId;

    /**
     * Data item id wither.
     *
     * @param dataItemId - data item id
     * @return {@link DataGroupItem}
     */
    public DataGroupItem withDataItemId(String dataItemId) {
        this.setDataItemId(dataItemId);
        return this;
    }

    /**
     * Data group id wither.
     *
     * @param dataGroupId - data group id
     * @return {@link DataGroupItem}
     */
    public DataGroupItem withDataGroupId(String dataGroupId) {
        this.setDataGroupId(dataGroupId);
        return this;
    }
}
