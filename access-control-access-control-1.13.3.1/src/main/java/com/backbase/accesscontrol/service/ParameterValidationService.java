package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_064;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_065;

import com.backbase.accesscontrol.util.QueryParameters;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Class for validation on query parameters.
 */
@Component
@AllArgsConstructor
public class ParameterValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterValidationService.class);

    private Validator validator;

    /**
     * Validates the search query parameter.
     *
     * @param searchQuery query parameter
     */
    public String validateQueryParameter(String searchQuery) {
        String trimmedQueryParameter = (searchQuery == null) ? null : searchQuery.trim();

        LOGGER.info("validate query parameter {}", trimmedQueryParameter);

        QueryParameters queryParameters = new QueryParameters()
            .withSearchQuery(trimmedQueryParameter);

        Set<ConstraintViolation<QueryParameters>> validation = validator.validate(queryParameters);

        if (!validation.isEmpty()) {
            LOGGER.warn("validation failed for query parameter {}", trimmedQueryParameter);
            throw getBadRequestException(AccessGroupErrorCodes.ERR_AG_062.getErrorMessage(), AccessGroupErrorCodes.ERR_AG_062.getErrorCode());
        }

        return trimmedQueryParameter;
    }

    /**
     * Validate "from" and "size" parameters.
     *
     * @param size - size parameter
     * @param from - from parameter
     */
    public void validateFromAndSizeParameter(Integer from, Integer size) {
        LOGGER.info("validate from {} and size parameter {}", from, size);
        boolean notNullFromAndSize = !(from == null && size == null);
        boolean isFromNegative = from != null && from < 0;
        boolean isSizeNegative = size != null && size <= 0;
        boolean nullFromOrSize = from == null || size == null;

        if (notNullFromAndSize) {
            if (isFromNegative) {
                LOGGER.warn("validation failed for from parameter {}", from);
                throw getBadRequestException(ERR_AG_063.getErrorMessage(),
                    ERR_AG_063.getErrorCode());
            }
            if (isSizeNegative) {
                LOGGER.warn("validation failed for size parameter {}", size);
                throw getBadRequestException(ERR_AG_064.getErrorMessage(),
                    ERR_AG_064.getErrorCode());
            }
            if (nullFromOrSize) {
                LOGGER.warn("Invalid parameters from {} or size {}", from, size);
                throw getBadRequestException(ERR_AG_065.getErrorMessage(),
                    ERR_AG_065.getErrorCode());
            }
        }
    }
}
