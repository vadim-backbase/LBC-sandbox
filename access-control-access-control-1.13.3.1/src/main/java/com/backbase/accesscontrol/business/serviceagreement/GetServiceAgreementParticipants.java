package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementParticipantsGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Business consumer retrieving a List of Service Agreement Participants. This class is the business process component
 * of the access-group presentation service, communicating with the p&p service and retrieving all participants on given
 * Service Agreement.
 */
@Service
@AllArgsConstructor
public class GetServiceAgreementParticipants {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetServiceAgreementParticipants.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ObjectConverter objectConverter;

    /**
     * Method that listens on the direct:listServiceAgreementParticipantsRequestedInternal endpoint and forwards the
     * request to the P&P service.
     *
     * @param internalRequest Internal Request of void type to be send by the client
     * @param serviceAgreementId service agreement id
     * @return Internal Request of list of {@link ServiceAgreementParticipantsGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_SERVICE_AGREEMENT_PARTICIPANTS)
    public InternalRequest<List<ServiceAgreementParticipantsGetResponseBody>> getServiceAgreementParticipants(
        InternalRequest<Void> internalRequest, @Header("serviceAgreementId") String serviceAgreementId) {
        LOGGER.info("Trying to get participants in service agreement with id {}", serviceAgreementId);

        List<Participant> serviceAgreementParticipants = persistenceServiceAgreementService
            .getServiceAgreementParticipants(serviceAgreementId);

        List<ServiceAgreementParticipantsGetResponseBody> participants = convertParticipantsFromPandPToPresentation(
            serviceAgreementParticipants);

        return getInternalRequest(participants, internalRequest.getInternalRequestContext());
    }

    private List<ServiceAgreementParticipantsGetResponseBody> convertParticipantsFromPandPToPresentation(
        List<Participant> participantsFromPandP) {
        return participantsFromPandP
            .stream()
            .map(participant -> objectConverter
                .convertValue(participant, ServiceAgreementParticipantsGetResponseBody.class))
            .collect(Collectors.toList());
    }
}
