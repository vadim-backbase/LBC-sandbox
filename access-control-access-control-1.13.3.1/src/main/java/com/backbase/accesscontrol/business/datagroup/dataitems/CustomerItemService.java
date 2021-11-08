package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerItemService implements DataItemExternalIdConverterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerItemService.class);
    private static final String DATA_ITEM_TYPE = "CUSTOMERS";

    private PersistenceLegalEntityService persistenceLegalEntityService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return DATA_ITEM_TYPE;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public List<String> getInternalId(String externalId, String serviceAgreementId) {
        Map<String, String> result = persistenceLegalEntityService
            .findInternalByExternalIdsForLegalEntity(Collections.singleton(externalId));
        if (Objects.isNull(result) || result.isEmpty()) {
            throw getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode());
        }
        return singletonList(result.get(externalId));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Map<String, List<String>> mapExternalToInternalIds(Set<String> externalIds, String serviceAgreementId) {
        LOGGER.info("Get legal entities by external ids: {}", externalIds);
        if (externalIds.isEmpty()) {
            return emptyMap();
        }
        return persistenceLegalEntityService.findInternalByExternalIdsForLegalEntity(externalIds).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> singletonList(entry.getValue())));
    }
}
