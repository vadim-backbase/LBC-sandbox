
package com.backbase.accesscontrol.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class PersistenceFunctionGroup {

    private String name;
    private String description;
    private PersistenceFunctionGroup.Type type = Type.fromValue("DEFAULT");
    private Date validFrom;
    private Date validUntil;
    private BigDecimal apsId;
    private String apsName;

    public PersistenceFunctionGroup withName(String name) {
        this.name = name;
        return this;
    }

    public PersistenceFunctionGroup withDescription(String description) {
        this.description = description;
        return this;
    }

    public PersistenceFunctionGroup withType(Type type) {
        this.type = type;
        return this;
    }

    public PersistenceFunctionGroup withValidFrom(Date validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public PersistenceFunctionGroup withValidUntil(Date validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    public PersistenceFunctionGroup withApsId(BigDecimal apsId) {
        this.apsId = apsId;
        return this;
    }

    public PersistenceFunctionGroup withApsName(String apsName) {
        this.apsName = apsName;
        return this;
    }

    public enum Type {

        DEFAULT("DEFAULT"),
        SYSTEM("SYSTEM"),
        TEMPLATE("TEMPLATE");
        private final String value;
        private static final Map<String, Type> CONSTANTS = new HashMap<>();

        static {
            for (Type c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator
        public static Type fromValue(String value) {
            Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
