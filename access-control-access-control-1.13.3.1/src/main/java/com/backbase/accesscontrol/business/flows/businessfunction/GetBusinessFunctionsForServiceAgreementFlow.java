package com.backbase.accesscontrol.business.flows.businessfunction;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.BusinessFunctionsPersistenceService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions.FunctionsGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetBusinessFunctionsForServiceAgreementFlow extends AbstractFlow<String, List<FunctionsGetResponseBody>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetBusinessFunctionsForServiceAgreementFlow.class);

    private BusinessFunctionsPersistenceService businessFunctionsPersistenceService;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<FunctionsGetResponseBody> execute(String id) {
        LOGGER.info("Getting business functions for service agreement with id  {}", id);
        return businessFunctionsPersistenceService.getBusinessFunctionsForServiceAgreement(id);
    }
}
