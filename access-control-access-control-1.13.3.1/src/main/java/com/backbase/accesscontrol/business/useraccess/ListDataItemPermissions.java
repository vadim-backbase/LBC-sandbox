package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer retrieving a List of Privileges. This class is the business process component of the access-group
 * presentation service, communicating with the p&p service and retrieving all privileges by user and service
 * agreement.
 */
@Service
@AllArgsConstructor
public class ListDataItemPermissions {

    public static final String CANNOT_FETCH_DATA = "Cannot fetch data.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ListDataItemPermissions.class);

    private UserAccessPrivilegeService userAccessPrivilegeService;
    private ObjectConverter objectConverter;

    /**
     * Sends request to pandp service for retrieving data item privileges.
     *
     * @param internalRequest the internal request
     * @param dataItemType    type of the data item
     * @param dataItemId      id of the data item
     * @return Business Process Result of List{@link PresentationUserDataItemPermission}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_DATA_ITEM_PRIVILEGES)
    public InternalRequest<List<PresentationUserDataItemPermission>> getDataItemPrivileges(
        InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest,
        @Header("dataItemType") String dataItemType,
        @Header("dataItemId") String dataItemId) {

        DataItemPermissionsSearchParametersHolder holder = internalRequest.getData();
        LOGGER.info(
            "Trying to fetch all privileges for user with id {}, service Agreement Id {}, "
                + "function with name {}, resource with name {}, "
                + "privilege {}, data group type {} and data item id {}",
            holder.getUserId(), holder.getServiceAgreementId(), holder.getFunctionName(), holder.getResourceName(),
            holder.getPrivilege(), dataItemType, dataItemId);

        List<PersistenceUserDataItemPermission> responseFromPAndP = userAccessPrivilegeService
            .getUserDataItemsPrivileges(holder.getUserId(), holder.getServiceAgreementId(), holder.getResourceName(),
                holder.getFunctionName(), holder.getPrivilege(), dataItemType, dataItemId);

        return getInternalRequest(
            objectConverter.convertList(responseFromPAndP, PresentationUserDataItemPermission.class),
            internalRequest.getInternalRequestContext());
    }
}
