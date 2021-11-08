package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for retrieving a Data Group by ID . This class is the business process component of the
 * access-group presentation service, communicating with the P&P.
 */
@Service
@AllArgsConstructor
public class GetDataGroupById {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDataGroupById.class);

    private DataGroupService dataGroupService;
    private DataGroupMapper dataGroupMapper;
    private ValidationConfig validationConfig;

    /**
     * Method that listens on the direct:getDataGroupByIdRequestedInternal endpoint.
     *
     * @param request void internal request to be send to the client
     * @return Business Process Result of {@link DataGroupByIdGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_DATA_GROUP_BY_ID)
    public InternalRequest<DataGroupByIdGetResponseBody> getDataGroupById(@Body InternalRequest<Void> request,
        @Header("id") String dataGroupId) {
        LOGGER.info("Trying to get data group by id {}", dataGroupId);

        DataGroup dataGroup = dataGroupService.getById(dataGroupId);
        validationConfig.validateIfDataGroupTypeIsAllowed(dataGroup.getDataItemType());

        DataGroupByIdGetResponseBody dataGroupById = dataGroupMapper
            .dataGroupToDataGroupByIdGetResponseBody(dataGroupService.getByIdWithExtendedData(dataGroupId));

        return getInternalRequest(dataGroupById, request.getInternalRequestContext());
    }
}
