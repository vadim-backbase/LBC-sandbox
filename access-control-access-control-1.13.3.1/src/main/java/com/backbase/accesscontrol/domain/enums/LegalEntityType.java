package com.backbase.accesscontrol.domain.enums;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_021;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LE type.
 */
public enum LegalEntityType {
    CUSTOMER,
    BANK;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityType.class);

    /**
     * Convert string to legal entity type.
     *
     * @param typeName string representation of the LegalEntityType name.
     * @return DataItemType enum.
     */
    public static LegalEntityType fromString(String typeName) {
        try {
            return LegalEntityType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(ERR_ACQ_021.getErrorMessage(), ex);
            throw getBadRequestException(ERR_ACQ_021.getErrorMessage(), ERR_ACQ_021.getErrorCode());
        }
    }
}
