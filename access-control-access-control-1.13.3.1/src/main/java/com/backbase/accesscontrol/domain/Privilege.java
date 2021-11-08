package com.backbase.accesscontrol.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "privilege")
@NoArgsConstructor
@Setter
@Getter
@With
@AllArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "name", updatable = false, nullable = false, unique = true, length = 16)
    private String name;

    @Column(name = "code", updatable = false, nullable = false, unique = true, length = 8)
    private String code;

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

        Privilege privilege = (Privilege) o;

        return new EqualsBuilder()
            .append(id, privilege.id)
            .append(name, privilege.name)
            .append(code, privilege.code)
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

    /**
     * To string class method.
     *
     * @return arguments description string
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .toString();
    }
}
