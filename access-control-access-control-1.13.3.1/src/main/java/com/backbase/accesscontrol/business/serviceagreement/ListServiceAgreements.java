package com.backbase.accesscontrol.business.serviceagreement;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.mappers.ServiceAgreementsListMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.InternalRequestUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a List of Service Agreements. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving all created Service Agreements.
 */
@Service
@AllArgsConstructor
public class ListServiceAgreements {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListServiceAgreements.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementsListMapper serviceAgreementMapper;

    /**
     * Method that listens on the direct:listServiceAgreementsRequestedInternal endpoint and retrieving all created
     * Service Agreements from P&P service.
     *
     * @param internalRequest Internal Request of void type to be send by the client
     * @param creatorId       creator id
     * @param query           query parameter
     * @param from            Beginning of the page
     * @param size            Pagination size
     * @param cursor          Pagination cursor
     * @return InternalRequest of {@link PaginationDto}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS)
    public InternalRequest<PaginationDto<ServiceAgreementGetResponseBody>> getServiceAgreements(
        InternalRequest<Void> internalRequest,
        @Header("creatorId") String creatorId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("cursor") String cursor) {

        LOGGER.info("Trying to list service agreements with creator {}", creatorId);

        Page<ServiceAgreement> serviceAgreementPage = persistenceServiceAgreementService
            .getServiceAgreements(null, creatorId,
                new SearchAndPaginationParameters(from, size, query, cursor));

        PaginationDto<ServiceAgreementGetResponseBody> data = new PaginationDto<>(
            serviceAgreementPage.getTotalElements(), serviceAgreementMapper.mapList(serviceAgreementPage.getContent()));

        return InternalRequestUtil.getInternalRequest(data, internalRequest.getInternalRequestContext());
    }
}
