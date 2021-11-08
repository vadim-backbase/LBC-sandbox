package com.backbase.accesscontrol.domain;


import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;

import com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service agreement state.
 */
public enum ServiceAgreementState {
    ENABLED,
    DISABLED,
    DELETED;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementState.class);

    /**
     * Convert string to corresponding enum value.
     *
     * @param state string representation of the {@link ServiceAgreementState}.
     * @return {@link ServiceAgreementState} enum.
     */
    public static ServiceAgreementState fromString(String state) {
        try {
            return ServiceAgreementState.valueOf(String.valueOf(state).toUpperCase());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid converting to state", ex);

            throw getBadRequestException(QueryErrorCodes.ERR_ACQ_022.getErrorMessage(),
                QueryErrorCodes.ERR_ACQ_022.getErrorCode());
        }
    }
}
