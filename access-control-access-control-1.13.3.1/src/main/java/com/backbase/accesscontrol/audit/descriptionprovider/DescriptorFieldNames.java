package com.backbase.accesscontrol.audit.descriptionprovider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DescriptorFieldNames {

    public static final String DATA_GROUP_NAME_FIELD_NAME = "Data Group Name";
    public static final String DATA_GROUP_UPDATED_NAME_FIELD_NAME = "Updated Data Group Name";
    public static final String DATA_GROUP_ID_FIELD_NAME = "Data Group ID";
    public static final String DATA_GROUP_DESCRIPTION_FIELD_NAME = "Data Group Description";
    public static final String DATA_GROUP_TYPE_FIELD_NAME = "Data Group Type";

    public static final String DATA_ITEM_ID_FIELD_NAME = "Data Item ID";
    public static final String DATA_ITEM_EXTERNAL_ID_FIELD_NAME = "External Data Item ID";

    public static final String TYPE_OF_CHANGE_FIELD_NAME = "Type of change";

    public static final String FUNCTION_GROUP_ID_FIELD_NAME = "Function Group ID";
    public static final String FUNCTION_GROUP_NAME_FIELD_NAME = "Function Group Name";
    public static final String UPDATED_FUNCTION_GROUP_NAME_FIELD_NAME = "Updated Function Group Name";
    public static final String FUNCTION_GROUP_DESCRIPTION_FIELD_NAME = "Function Group Description";
    public static final String FUNCTION_GROUP_TYPE_FIELD_NAME = "Function Group Type";
    public static final String FUNCTION_GROUP_APS_ID_FIELD_NAME = "Assignable Permission Set ID";
    public static final String FUNCTION_GROUP_APS_NAME_FIELD_NAME = "Assignable Permission Set Name";

    public static final String SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME = "External Service Agreement ID";
    public static final String SERVICE_AGREEMENT_ID_FIELD_NAME = "Service Agreement ID";
    public static final String SERVICE_AGREEMENT_NAME_FIELD_NAME = "Service Agreement Name";
    public static final String SERVICE_AGREEMENT_DESCRIPTION_FIELD_NAME = "Service Agreement Description";
    public static final String SERVICE_AGREEMENT_STATUS_FIELD_NAME = "Service Agreement Status";
    public static final String SERVICE_AGREEMENT_STATE_FIELD_NAME = "Service Agreement State";
    public static final String MASTER_SERVICE_AGREEMENT_FIELD_NAME = "Master Service Agreement";

    public static final String ADMIN_ID_FIELD_NAME = "Admin ID";

    public static final String PARTICIPANT_ID_FIELD_NAME = "Participant ID";
    public static final String EXTERNAL_PARTICIPANT_ID_FIELD_NAME = "External Participant ID";
    public static final String PARTICIPANT_SHARING_USERS_FIELD_NAME = "Participant is sharing users";
    public static final String PARTICIPANT_SHARING_ACCOUNTS_FIELD_NAME = "Participant is sharing accounts";

    public static final String BUSINESS_FUNCTION_ID_FIELD_NAME = "Business Function ID";
    public static final String PRIVILEGES_FIELD_NAME = "Privileges";
    public static final String BUSINESS_FUNCTION_NAME_FIELD_NAME = "Business Function Name";

    public static final String EXTERNAL_USER_ID_FIELD_NAME = "External User ID";
    public static final String USER_ID_FIELD_NAME = "User ID";

    public static final String START_DATE_TIME_FIELD_NAME = "Start DateTime";
    public static final String END_DATE_TIME_FIELD_NAME = "End DateTime";

    public static final String OUTCOME_FIELD_NAME = "Outcome";
    public static final String APPROVAL_REQUEST_ID_FIELD_NAME = "Approval Request ID";

    public static final String ASSIGNABLE_PERMISSION_SET_ID_FIELD_NAME = "ID";
    public static final String ASSIGNABLE_PERMISSION_SET_FIELD_NAME = "Permission Set";
    public static final String ASSIGNABLE_PERMISSION_SET_NAME_FIELD_NAME = "Assignable Permission Set Name";
    public static final String ASSIGNABLE_PERMISSION_SET_DESCRIPTION_FIELD_NAME =
        "Assignable Permission Set Description";
    public static final String REGULAR_USER_APS_IDS_FIELD_NAME = "Regular user Assignable Permission Set IDs";
    public static final String REGULAR_USER_APS_NAMES_FIELD_NAME = "Regular user Assignable Permission Set Names";
    public static final String ADMIN_USER_APS_IDS_FIELD_NAME = "Admin user Assignable Permission Set IDs";
    public static final String ADMIN_USER_APS_NAMES_FIELD_NAME = "Admin user Assignable Permission Set Names";

    public static final String ERROR_CODE = "Error code";
    public static final String ERROR_MESSAGE = "Error message";
    
    public static final String SAP_POLICY = "Self approval policy %s ";
    public static final String SAP_ENABLED = SAP_POLICY + "enabled";
    public static final String SAP_BF_NAME = SAP_POLICY + "business function name";
    public static final String SAP_BOUND = SAP_POLICY + "bound %s ";
    public static final String SAP_BOUND_CURRENCY_CODE = SAP_BOUND + "currency code";
    public static final String SAP_BOUND_CURRENCY_AMOUNT = SAP_BOUND + "currency amount";
    
}
