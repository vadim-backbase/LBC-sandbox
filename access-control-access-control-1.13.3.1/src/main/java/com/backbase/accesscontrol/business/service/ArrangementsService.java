package com.backbase.accesscontrol.business.service;

import com.backbase.dbs.arrangement.api.client.v2.ArrangementsApi;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementItems;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsFilter;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArrangementsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrangementsService.class);

    private ArrangementsApi arrangementsApi;

    public String getInternalId(String externalId) {
        LOGGER.info("Get arrangement internal id by external id: {}", externalId);
        return arrangementsApi.getInternalId(externalId).getInternalId();
    }

    public AccountArrangementItems postFilter(AccountArrangementsFilter accountArrangementsFilter) {
        LOGGER.info("Filter account arrangements items by: {}", accountArrangementsFilter);
        return arrangementsApi.postFilter(accountArrangementsFilter);
    }

    public AccountArrangementsLegalEntities getArrangementsLegalEntities(List<String> arrangementIds,
        List<String> legalEntityIds) {
        LOGGER.info("Get arrangements legal entities by arrangement ids: {} and legal entity ids: {}", arrangementIds,
            legalEntityIds);
        return arrangementsApi.getArrangementsLegalEntities(arrangementIds, legalEntityIds);
    }
}