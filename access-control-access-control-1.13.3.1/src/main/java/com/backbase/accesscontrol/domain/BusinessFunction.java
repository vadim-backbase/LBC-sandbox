package com.backbase.accesscontrol.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "business_function")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessFunction {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "function_name", updatable = false, nullable = false, unique = true, length = 32)
    private String functionName;

    @Column(name = "function_code", updatable = false, nullable = false, length = 32)
    private String functionCode;

    @Column(name = "resource_name", updatable = false, nullable = false, length = 32)
    private String resourceName;

    @Column(name = "resource_code", updatable = false, nullable = false, length = 32)
    private String resourceCode;

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

        BusinessFunction that = (BusinessFunction) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(functionName, that.functionName)
            .append(functionCode, that.functionCode)
            .append(resourceName, that.resourceName)
            .append(resourceCode, that.resourceCode)
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
            .append(functionName)
            .append(functionCode)
            .append(resourceName)
            .append(resourceCode)
            .toHashCode();
    }

    /**
     * To string class method.
     *
     * @return arguments description string
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("functionName", functionName)
            .append("functionCode", functionCode)
            .append("resourceName", resourceName)
            .append("resourceCode", resourceCode)
            .toString();
    }
}
