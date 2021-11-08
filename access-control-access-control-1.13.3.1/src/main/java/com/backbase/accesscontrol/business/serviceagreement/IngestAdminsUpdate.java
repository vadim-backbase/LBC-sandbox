package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_INGEST_ADMINS_UPDATE;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UserType;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.routes.serviceagreement.UpdateBatchAdminsRouteProxy;
import com.backbase.accesscontrol.service.batch.serviceagreement.ModifyUsersAndAdminsInServiceAgreement;
import com.backbase.accesscontrol.util.InternalRequestUtil;
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
public class IngestAdminsUpdate extends AbstractServiceAgreementUsers implements UpdateBatchAdminsRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestAdminsUpdate.class);

    public IngestAdminsUpdate(UserManagementService userManagementService,
        ModifyUsersAndAdminsInServiceAgreement modifyUsersAndAdminsInServiceAgreement,
        BatchResponseItemMapper batchResponseItemMapper, Validator validator) {
        super(userManagementService, modifyUsersAndAdminsInServiceAgreement, batchResponseItemMapper, validator);
    }

    /**
     * Ingest update admins.
     *
     * @param request internal request of {@link PresentationServiceAgreementUsersUpdate}
     * @return internal request of list of {@link BatchResponseItemExtended}
     */
    @Override
    @Consume(value = DIRECT_DEFAULT_INGEST_ADMINS_UPDATE)
    public InternalRequest<List<BatchResponseItemExtended>> updateBatchAdmins(
        @Body InternalRequest<PresentationServiceAgreementUsersUpdate> request) {
        LOGGER.info("Update batch admins in service agreements");

        return InternalRequestUtil.getInternalRequest(executeRequest(
            request.getData(), UserType.ADMIN_USER),
            request.getInternalRequestContext());
    }
}
