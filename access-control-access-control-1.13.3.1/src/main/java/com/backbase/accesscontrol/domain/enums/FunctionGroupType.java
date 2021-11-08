package com.backbase.accesscontrol.domain.enums;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_020;

import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum FunctionGroupType {

    DEFAULT,
    SYSTEM,
    TEMPLATE;

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupType.class);

    /**
     * Convert string to enum value.
     *
     * @param typeName string representation of the Function Group type.
     * @return FunctionGroupType enum.
     */
    public static FunctionGroupType fromString(String typeName) {
        try {
            return FunctionGroupType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn(ERR_ACQ_020.getErrorMessage(), ex);
            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_020.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_020.getErrorCode());
        }
    }
}
