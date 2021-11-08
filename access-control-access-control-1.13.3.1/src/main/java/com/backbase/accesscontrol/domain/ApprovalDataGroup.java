package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "approval_data_group")
@Data
@NoArgsConstructor
public class ApprovalDataGroup extends AccessControlApproval {

    @Column(name = "data_group_id", nullable = true)
    private String dataGroupId;

    @Override
    public ApprovalAction getApprovalAction() {
        return ApprovalAction.DELETE;
    }

    @Override
    public ApprovalCategory getApprovalCategory() {
        return ApprovalCategory.MANAGE_DATA_GROUPS;
    }

    /**
     * Proper equals method.
     *
     * @param o - object for comparision
     * @return true/false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApprovalDataGroup)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ApprovalDataGroup that = (ApprovalDataGroup) o;
        return Objects.equals(dataGroupId, that.dataGroupId);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataGroupId);
    }
}
