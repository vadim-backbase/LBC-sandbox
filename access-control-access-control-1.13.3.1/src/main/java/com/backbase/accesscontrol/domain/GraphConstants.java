package com.backbase.accesscontrol.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants used for hibernate graphs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphConstants {

    public static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";
    public static final String APS_PERMISSIONS_EXTENDED = "graph.assignable.permissions.set.extended";
    public static final String DATA_GROUP_EXTENDED = "graph.DataGroup.extended";
    public static final String DATA_GROUP_WITH_ITEMS = "graph.DataGroup.withItems";
    public static final String APPROVAL_DATA_GROUP_WITH_ITEMS = "graph.ApprovalDataGroup.withItems";
    public static final String DATA_GROUP_SERVICE_AGREEMENT = "graph.DataGroup.serviceAgreement";
    public static final String DATA_GROUP_EXTENDED_WITH_SA_CREATOR = "graph.DataGroup.extended.saCreator";
    public static final String SERVICE_AGREEMENT_WITH_ADDITIONS = "graph.ServiceAgreement.additons";
    public static final String DATA_GROUP_WITH_SA_CREATOR = "graph.DataGroup.saCreator";
    public static final String SERVICE_AGREEMENT_WITH_DATAGROUPS = "graph.ServiceAgreement.withDataGroups";
    public static final String SERVICE_AGREEMENT_WITH_CREATOR = "graph.ServiceAgreement.creator";
    public static final String SERVICE_AGREEMENT_EXTENDED = "graph.ServiceAgreement.extended";
    public static final String SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS = "graph.ServiceAgreement"
        + ".withParticipantsAndAdditions";
    public static final String SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS
        = "graph.ServiceAgreement.withParticipants.and.Admins.and.functionGroups";
    public static final String SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR
        = "graph.ServiceAgreement.withParticipants.and.creator";
    public static final String SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS
        = "graph.serviceAgreement.functionGroup.and.dataGroup.and.exposed.users";

    public static final String SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS
        = "graph.serviceAgreement.functionGroup";

    public static final String SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR
        = "graph.serviceAgreement.permissionSetsRegular";
    public static final String SERVICE_AGREEMENT_WITH_PERMISSION_SETS
        = "graph.serviceAgreement.permissionSets";

    public static final String SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS =
        "graph.serviceAgreement.serviceAgreementAndLegalEntityCreatorAndPermissionSetsRegular";
    public static final String SERVICE_AGREEMENT_WITH_FGS = "graph.serviceAgreement.serviceAgreementWithFunstionGroups";

    public static final String PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS = "graph.Participant.withParticipantUsers";
    public static final String FUNCTION_GROUP_WITH_GROUPED_FUNCTION_PRIVILEGES =
        "graph.FunctionGroup.applicableFunctionPrivileges";
    public static final String FUNCTION_GROUP_WITH_SA = "graph.FunctionGroup.serviceAgreement";
    public static final String FUNCTION_GROUP_WITH_SA_AND_LEGAL_ENTITY_AND_PERMISSION_SETS_REGULAR =
        "graph.FunctionGroup.serviceAgreementAndLegalEntityAndPermissionSetsRegular";
    public static final String GRAPH_LEGAL_ENTITY_WITH_CHILDREN_AND_PARENT
        = "graph.LegalEntity.withChildrenAndParent";
    public static final String GRAPH_LEGAL_ENTITY_WITH_PARENT_AND_ADDITIONS
        = "graph.LegalEntity.withParent.WithAdditions";
    public static final String GRAPH_LEGAL_ENTITY_WITH_ANCESTORS_AND_ADDITIONS
        = "graph.LegalEntity.withAncestorsAndAdditions";
    public static final String GRAPH_LEGAL_ENTITY_WITH_ANCESTORS
        = "graph.LegalEntity.withAncestors";
    public static final String GRAPH_LEGAL_ENTITY_WITH_ADDITIONS
        = "graph.LegalEntity.withAdditions";
    public static final String APPLICABLE_FUNCTION_PRIVILEGE_WITH_BUSINESS_FUNCTION_AND_PRIVILEGE
        = "graph.ApplicableFunctionPrivilege.withBusinessFunction";
    public static final String PARTICIPANT_WITH_ADMINS = "graph.Participant.withAdmins";
    public static final String PARTICIPANT_WITH_LEGAL_ENTITY = "graph.Participant.withLegalEntity";

    public static final String PARTICIPANT_WITH_SERVICE_AGREEMENT = "graph.Participant.withServiceAgreement";
    public static final String PARTICIPANT_WITH_LEGAL_ENTITY_AND_SERVICE_AGREEMENT_CREATOR =
        "graph.Participant.withLegalEntityAndServiceAgreementCreator";
}
