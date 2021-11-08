package com.backbase.accesscontrol.configuration;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_001;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_085;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_103;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "backbase.data-group")
public class ValidationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationConfig.class);
    private static final String DATA_GROUP_TYPE_CUSTOMERS = "CUSTOMERS";

    private List<String> types = new ArrayList<>();

    /**
     * Return types defined in application.yml.
     *
     * @return list of types
     */
    public List<String> getTypes() {
        return this.types;
    }

    /**
     * Throws bad request if type is provided and is not in te configured data group types.
     *
     * @param dataGroupType - data group type
     */
    public void validateDataGroupTypeWhenProvided(String dataGroupType) {
        if (Objects.nonNull(dataGroupType)) {
            validateDataGroupType(dataGroupType);
        }
    }

    /**
     * Validates data group type.
     *
     * @param dataGroupType data group type to validate
     */
    public void validateDataGroupType(String dataGroupType) {
        if (!getTypes().contains(dataGroupType)) {
            LOGGER.warn("Type {} is not valid", dataGroupType);
            throw getBadRequestException((ERR_AG_001.getErrorMessage()), ERR_AG_001.getErrorCode());
        }
    }

    /**
     * Validates data group type is allowed.
     *
     * @param dataGroupType data group type to validate
     */
    public void validateIfDataGroupTypeIsAllowed(String dataGroupType) {
        if (dataGroupType.equals(DATA_GROUP_TYPE_CUSTOMERS)) {
            LOGGER.warn("Type {} is not allowed", dataGroupType);
            throw getBadRequestException((ERR_AG_103.getErrorMessage()), ERR_AG_103.getErrorCode());
        }
    }

    /**
     * Validates data groups items.
     *
     * @param items list of items
     */
    public void validateDataGroupItems(List<String> items) {
        if (hasNullItems(items)) {
            LOGGER.warn("List of data group items contains null values");
            throw getBadRequestException(ERR_AG_085.getErrorMessage(), ERR_AG_085.getErrorCode());
        }
    }

    private boolean hasNullItems(List<String> items) {
        return Optional.ofNullable(items)
            .orElseGet(ArrayList::new)
            .stream()
            .anyMatch(Objects::isNull);
    }
}
