package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "approval_function_group_ref")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ApprovalFunctionGroupRef extends AccessControlApproval {

    @Column(name = "function_group_id", length = 36)
    private String functionGroupId;

    @Override
    public ApprovalAction getApprovalAction() {

        return ApprovalAction.DELETE;
    }

    @Override
    public ApprovalCategory getApprovalCategory() {
        return ApprovalCategory.MANAGE_FUNCTION_GROUPS;
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
        if (!(o instanceof ApprovalFunctionGroupRef)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ApprovalFunctionGroupRef that = (ApprovalFunctionGroupRef) o;
        return Objects.equals(functionGroupId, that.functionGroupId);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), functionGroupId);
    }
}
