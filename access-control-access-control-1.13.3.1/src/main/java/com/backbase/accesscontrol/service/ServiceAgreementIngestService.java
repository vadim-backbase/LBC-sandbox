package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;

public interface ServiceAgreementIngestService {

    /**
     * Method that ingests service agreement.
     *
     * @param requestData - request data
     * @return id of the service agreement
     */
    String ingestServiceAgreement(
        ServiceAgreementData<ServiceAgreementIngestPostRequestBody> requestData);
}
