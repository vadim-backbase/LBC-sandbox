package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for retrieving a List of Data Group. This class is the business process component of the
 * access-group presentation service, communicating with the P&P service and retrieving a List of Data Access groups.
 */
@Service
@AllArgsConstructor
public class ListDataGroups {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListDataGroups.class);

    private DataGroupService dataGroupService;

    private DataGroupMapper mapper;

    /**
     * Method that listens on the direct:listDataAccessGroupRequestedInternal endpoint and uses forwards the request to
     * the P&P service.
     *
     * @param request            Internal Request of {@link Void} type to be send by the client
     * @param serviceAgreementId Id of the Service Agreement to be queried
     * @param type               Data Group Type
     * @param includeItems       defines if should include items in response
     * @return Business Process Result of List {@link DataGroupsGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_DATA_GROUPS)
    public InternalRequest<List<DataGroupsGetResponseBody>> getDataGroups(
        InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("type") String type, @Header("includeItems") boolean includeItems) {
        LOGGER
            .info("Trying to list data groups with service agreement ID {} and type {}", serviceAgreementId, type);

        return getInternalRequest(getDataGroups(serviceAgreementId, type, includeItems),
            request.getInternalRequestContext());
    }

    private List<DataGroupsGetResponseBody> getDataGroups(String serviceAgreementId,
        String type, boolean includeItems) {

        List<DataGroupItemBase> dataGroups = dataGroupService
            .getByServiceAgreementIdAndDataItemType(serviceAgreementId, type,
                includeItems);

        return mapper.dataGroupItemBaseToDataGroupsGetResponseBody(dataGroups);
    }

}
