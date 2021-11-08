package com.backbase.accesscontrol.util;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_102;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PermissionSetValidationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionSetValidationUtil.class);

    public void validateUserApsIdentifiers(PresentationUserApsIdentifiers... userApsIdentifiers) {
        Arrays.stream(userApsIdentifiers).forEach(this::validateUserApsIdentifier);
    }

    /**
     * Validates APS identifiers, throws error if two or none identifiers provided.
     *
     * @param userApsIdentifiers - object containing id and name identifiers
     */
    public void validateUserApsIdentifier(PresentationUserApsIdentifiers userApsIdentifiers) {
        if (nonNull(userApsIdentifiers)) {
            Set<BigDecimal> idIdentifiers = userApsIdentifiers.getIdIdentifiers();
            Set<String> nameIdentifiers = userApsIdentifiers.getNameIdentifiers();

            if ((isEmpty(idIdentifiers) && isEmpty(nameIdentifiers))
                || (isNotEmpty(idIdentifiers) && isNotEmpty(nameIdentifiers))) {
                LOGGER.warn("Invalid APS identifiers idIdentifiers {}, nameIdentifiers {}", idIdentifiers,
                    nameIdentifiers);
                throw getBadRequestException(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode());
            }
        }
    }

}
