package com.backbase.accesscontrol.util.helpers;

import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.PresentationPermissionFunctionGroupUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class FunctionGroupDataProvider {

    public static PresentationFunctionGroupPutRequestBody createPresentationFunctionGroup(String id, String idName,
        String idSA, String name, String description, String functionName, List<String> privileges) {
        FunctionGroup functionGroupBody1 = new FunctionGroup()
            .withName(name)
            .withDescription(description)
            .withPermissions(Collections.singletonList(new PresentationPermissionFunctionGroupUpdate()
                .withFunctionName(functionName)
                .withPrivileges(privileges)));

        PresentationFunctionGroupPutRequestBody returnValue = new PresentationFunctionGroupPutRequestBody()
            .withIdentifier(new PresentationIdentifier())
            .withFunctionGroup(functionGroupBody1);

        if (!StringUtils.isEmpty(id)) {
            returnValue.getIdentifier().setIdIdentifier(id);
        }

        if (!StringUtils.isEmpty(idName)) {
            returnValue.getIdentifier().setNameIdentifier(new NameIdentifier()
                .withName(idName)
                .withExternalServiceAgreementId(idSA));
        }

        return returnValue;
    }

    public static PresentationFunctionGroupPutRequestBody createPresentationFunctionGroupPutRequestBody(String id,
        String idName,
        String idSA, String name, String description, String functionName, List<String> privileges, String fromDate,
        String fromTime, String untilDate, String untilTime) {
        PresentationFunctionGroupPutRequestBody presentationFunctionGroup = createPresentationFunctionGroup(id, idName,
            idSA, name, description, functionName, privileges);

        presentationFunctionGroup.getFunctionGroup().withValidFromDate(fromDate).withValidFromTime(fromTime)
            .withValidUntilDate(untilDate).withValidUntilTime(untilTime);

        return presentationFunctionGroup;
    }

    public static com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody createPresentationFunctionGroupModel(
        String id, String idName,
        String idSA, String name, String description, String functionName, List<String> privileges) {
        com.backbase.accesscontrol.service.rest.spec.model.Functiongroupupdate functionGroupBody1 = new com.backbase.accesscontrol.service.rest.spec.model.Functiongroupupdate()
            .name(name)
            .description(description)
            .permissions(Collections.singletonList(
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationPermissionFunctionGroupUpdate()
                    .functionName(functionName)
                    .privileges(privileges)));

        com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody returnValue = new com.backbase.accesscontrol.service.rest.spec.model.PresentationFunctionGroupPutRequestBody()
            .identifier(new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifier())
            .functionGroup(functionGroupBody1);

        if (!StringUtils.isEmpty(id)) {
            returnValue.getIdentifier().setIdIdentifier(id);
        }

        if (!StringUtils.isEmpty(idName)) {
            returnValue.getIdentifier().setNameIdentifier(
                new com.backbase.accesscontrol.service.rest.spec.model.PresentationIdentifierNameIdentifier()
                    .name(idName)
                    .externalServiceAgreementId(idSA));
        }

        return returnValue;
    }
}
