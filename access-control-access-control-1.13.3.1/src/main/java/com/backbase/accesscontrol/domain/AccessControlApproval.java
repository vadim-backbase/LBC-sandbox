package com.backbase.accesscontrol.domain;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "access_control_approval")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@Getter
@Setter
public abstract class AccessControlApproval implements ApprovalTypeInfo {

    @Id
    @GenericGenerator(
        name = "approvalGenerator",
        strategy = "enhanced-table",
        parameters = {
            @Parameter(
                name = "table_name",
                value = "sequence_table"
            ),
            @Parameter(
                name = "optimizer",
                value = "pooled-lo"
            ),
            @Parameter(
                name = "initial_value",
                value = "1"
            ),
            @Parameter(
                name = "increment_size",
                value = "1000"
            )
        })
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "approvalGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "approval_id", updatable = false, nullable = false, length = 36)
    private String approvalId;

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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccessControlApproval that = (AccessControlApproval) o;
        return Objects.equals(approvalId, that.approvalId);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(approvalId);
    }

    /**
     * Custom constructor.
     *
     * @param approvalId - approval id parameter
     * @return {@link AccessControlApproval}
     */
    public AccessControlApproval withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }
}
