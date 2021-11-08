package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.service.impl.ServiceAgreementAdminService;
import com.backbase.accesscontrol.util.ServiceAgreementsUtils;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Participant;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementAdminsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementUsersGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreements;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ServiceAgreementServiceFacade {

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementAdminService serviceAgreementAdminService;
    private ServiceAgreementsUtils serviceAgreementsUtils;

    /**
     * Business service layer that communicates with persistence service agreement service in order to get its
     * participants for specified service agreement id.
     *
     * @param serviceAgreementId - service agreement internal id
     * @return list of {@link Participant}
     */
    public List<Participant> getServiceAgreementParticipants(String serviceAgreementId) {
        return persistenceServiceAgreementService.getServiceAgreementParticipants(serviceAgreementId);
    }

    /**
     * Retrieves service agreement by id and transforms it to {@link  ServiceAgreementItem}.
     *
     * @param serviceAgreementId - id of the service agreement
     * @return {@link ServiceAgreementItem}
     */
    public ServiceAgreementItem getServiceAgreementResponseBodyById(String serviceAgreementId) {

        return persistenceServiceAgreementService.getServiceAgreementResponseBodyById(serviceAgreementId);
    }

    /**
     * Return the list of admins in the service agreement.
     *
     * @param serviceAgreementId - service agreement id
     * @return - list of admins
     */
    public ServiceAgreementAdminsGetResponseBody getServiceAgreementAdmins(String serviceAgreementId) {

        return serviceAgreementAdminService.getServiceAgreementAdmins(serviceAgreementId);
    }

    /**
     * Retrieve service agreement users.
     *
     * @param serviceAgreementId internal service agreements id
     * @return {@link ServiceAgreementUsersGetResponseBody}
     */
    public ServiceAgreementUsersGetResponseBody getServiceAgreementUsers(String serviceAgreementId) {

        return persistenceServiceAgreementService.getServiceAgreementUsers(serviceAgreementId);
    }

    /**
     * Lists service agreements.
     *
     * @param creatorId                     - creator legal entity id
     * @param userParameters                - user parameters.
     * @param searchAndPaginationParameters search parameters.
     * @return {@link PersistenceServiceAgreements}
     */
    public PersistenceServiceAgreements listServiceAgreements(String creatorId, UserParameters userParameters,
        SearchAndPaginationParameters searchAndPaginationParameters) {
        Page<ServiceAgreement> serviceAgreementPage = persistenceServiceAgreementService
            .listServiceAgreements(creatorId,
                userParameters,
                searchAndPaginationParameters);
        List<PersistenceServiceAgreement> serviceAgreements = serviceAgreementsUtils
            .transformToPersistenceServiceAgreements(
                serviceAgreementPage.getContent());
        return new PersistenceServiceAgreements()
            .withServiceAgreements(serviceAgreements)
            .withTotalElements(serviceAgreementPage.getTotalElements());
    }

}
