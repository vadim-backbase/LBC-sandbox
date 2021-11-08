package com.backbase.accesscontrol.mappers;

import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class  ServiceAgreementsListInHierarchyMapper extends
    ServiceAgreementMapper<PresentationServiceAgreement, PersistenceServiceAgreement> {

}
