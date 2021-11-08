package com.backbase.accesscontrol.dto;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_043;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@EqualsAndHashCode
public class UserParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserParameters.class);

    private String userId;
    private String userLegalEntityId;

    /**
     * Constructor for {@link UserParameters} class.
     *
     * @param userId            - user id
     * @param userLegalEntityId - user legal entity id
     */
    public UserParameters(String userId, String userLegalEntityId) {
        if (hasSingleUserParameters(userId, userLegalEntityId)) {
            LOGGER.warn("Invalid params for userId: {} and userLegalEntityId: {}", userId, userLegalEntityId);
            throw getBadRequestException(ERR_ACQ_043.getErrorMessage(), ERR_ACQ_043.getErrorCode());
        }
        this.userId = userId;
        this.userLegalEntityId = userLegalEntityId;
    }

    private boolean hasSingleUserParameters(String userId, String userLegalEntityId) {
        return StringUtils.isEmpty(userId) ^ StringUtils.isEmpty(userLegalEntityId);
    }

}
