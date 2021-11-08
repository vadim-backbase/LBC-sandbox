package com.backbase.accesscontrol.domain;

import static javax.persistence.CascadeType.REFRESH;

import com.backbase.accesscontrol.domain.listener.ApplicableFunctionPrivilegeListener;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@NamedEntityGraph(name = GraphConstants.APPLICABLE_FUNCTION_PRIVILEGE_WITH_BUSINESS_FUNCTION_AND_PRIVILEGE,
    attributeNodes = {@NamedAttributeNode(value = "businessFunction"), @NamedAttributeNode(value = "privilege")})
@EntityListeners(value = ApplicableFunctionPrivilegeListener.class)
@Table(name = "applicable_function_privilege")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicableFunctionPrivilege {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = REFRESH)
    @JoinColumn(name = "business_function_id", referencedColumnName = "id")
    private BusinessFunction businessFunction;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = REFRESH)
    @JoinColumn(name = "privilege_id")
    private Privilege privilege;

    @Column(name = "supports_limit", nullable = false, columnDefinition = "INTEGER")
    private boolean supportsLimit = false;

    @Column(name = "business_function_name", nullable = false, updatable = false, length = 32)
    private String businessFunctionName;

    @Column(name = "function_resource_name", nullable = false, updatable = false, length = 32)
    private String businessFunctionResourceName;

    @Column(name = "privilege_name", nullable = false, updatable = false, length = 16)
    private String privilegeName;

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

        ApplicableFunctionPrivilege that = (ApplicableFunctionPrivilege) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .isEquals();
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }
}
