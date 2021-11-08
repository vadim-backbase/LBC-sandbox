package com.backbase.accesscontrol.business.service;

import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * A service class that communicates with the P&P services via the clients and returns responses.
 */
@Service
@AllArgsConstructor
public class BusinessFunctionsPersistenceService {

    private FunctionGroupService functionGroupService;
    private ObjectConverter objectConverter;

    /**
     * Get all functions from P&P.
     *
     * @param id service agreement id
     * @return list of all functions
     */
    public List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions
        .FunctionsGetResponseBody> getBusinessFunctionsForServiceAgreement(String id) {
        List<FunctionsGetResponseBody> response = functionGroupService
            .findAllBusinessFunctionsByServiceAgreement(id, false);
        return objectConverter
            .convertList(response, com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.config.functions
                .FunctionsGetResponseBody.class);

    }
}
