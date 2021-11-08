package com.backbase.accesscontrol.business.serviceagreement;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateUsersInServiceAgreementRouteProxy;
import com.backbase.accesscontrol.service.batch.serviceagreement.ModifyUsersAndAdminsInServiceAgreement;
import com.backbase.accesscontrol.util.InternalRequestUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import java.util.List;
import javax.validation.Validator;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateUsersInServiceAgreement extends AbstractServiceAgreementUsers implements
    UpdateUsersInServiceAgreementRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUsersInServiceAgreement.class);

    public UpdateUsersInServiceAgreement(
        UserManagementService userManagementService,
        ModifyUsersAndAdminsInServiceAgreement modifyUsersAndAdminsInServiceAgreement,
        BatchResponseItemMapper batchResponseItemMapper, Validator validator) {
        super(userManagementService, modifyUsersAndAdminsInServiceAgreement, batchResponseItemMapper, validator);
    }

    /**
     * Update users in service agreement.
     *
     * @param request internal request with the payload of {@link PresentationServiceAgreementUsersUpdate}
     * @return {@link InternalRequest} of list of {@link BatchResponseItemExtended}
     */
    @Override
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_USERS_IN_SA)
    public InternalRequest<List<BatchResponseItemExtended>> updateUsersInServiceAgreement(
        @Body InternalRequest<PresentationServiceAgreementUsersUpdate> request) {
        LOGGER.info("{} update users in service agreements: {}", request.getData().getAction(),
            request.getData().getUsers());

        return InternalRequestUtil.getInternalRequest(executeRequest(
            request.getData(), UserType.REGULAR_USER),
            request.getInternalRequestContext());
    }
}
