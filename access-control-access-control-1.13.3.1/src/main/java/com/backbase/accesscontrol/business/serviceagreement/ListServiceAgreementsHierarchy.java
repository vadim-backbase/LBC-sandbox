package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.mappers.ServiceAgreementsListInHierarchyMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.ServiceAgreementsUtils;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a List of Service Agreements. This class is the business process component of the
 * access-group presentation service, communicating with the p&p service and retrieving all created Service Agreements
 * under hierarchy of the creator id.
 */
@Service
@AllArgsConstructor
public class ListServiceAgreementsHierarchy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListServiceAgreementsHierarchy.class);
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private UserManagementService userManagementService;
    private ServiceAgreementsListInHierarchyMapper serviceAgreementMapper;
    private ServiceAgreementsUtils serviceAgreementsUtils;

    /**
     * Method that listens on the direct:listServiceAgreementsHierarchyRequestedInternal endpoint and uses persistence
     * service.
     *
     * @param internalRequest Internal Request of void type to be send by the client
     * @param creatorId       creator id
     * @param userId          user id
     * @param query           query parameter
     * @param from            Beginning of the page
     * @param size            Pagination size
     * @param cursor          Pagination cursor
     * @return Internal Request of {@link PaginationDto}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_SERVICE_AGREEMENTS_HIERARCHY)
    public InternalRequest<PaginationDto<PresentationServiceAgreement>> getServiceAgreements(
        InternalRequest<Void> internalRequest,
        @Header("creatorId") String creatorId,
        @Header("userId") String userId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("size") Integer size,
        @Header("cursor") String cursor) {

        LOGGER.info("Trying to list service agreements under hierarchy of creator {}", creatorId);

        UserParameters userParameters = new UserParameters("", "");
        if (StringUtils.isNotEmpty(userId)) {
            GetUser userByInternalId = userManagementService.getUserByInternalId(userId);
            userParameters = new UserParameters(userId, userByInternalId.getLegalEntityId());
        }

        SearchAndPaginationParameters searchAndPaginationParameters = new SearchAndPaginationParameters(from, size,
            query, cursor);
        Page<ServiceAgreement> request = persistenceServiceAgreementService
            .listServiceAgreements(creatorId, userParameters, searchAndPaginationParameters);

        List<PersistenceServiceAgreement> serviceAgreements = serviceAgreementsUtils
            .transformToPersistenceServiceAgreements(
                request.getContent());
        PersistenceServiceAgreements result = new PersistenceServiceAgreements()
            .withServiceAgreements(serviceAgreements)
            .withTotalElements(request.getTotalElements());
        return getInternalRequest(createServiceAgreementGetResponseBodyPaginationDto(result),
            internalRequest.getInternalRequestContext());
    }

    private PaginationDto<PresentationServiceAgreement> createServiceAgreementGetResponseBodyPaginationDto(
        PersistenceServiceAgreements data) {
        return new PaginationDto<>(
            data.getTotalElements(),
            serviceAgreementMapper.mapList(data.getServiceAgreements())
        );
    }
}
