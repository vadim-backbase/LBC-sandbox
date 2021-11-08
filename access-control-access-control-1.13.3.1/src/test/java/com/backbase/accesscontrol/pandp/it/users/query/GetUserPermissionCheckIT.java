package com.backbase.accesscontrol.pandp.it.users.query;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_017;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.UserQueryController;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Tests for {@link UserQueryController#getUserPermissionCheck}.
 */
public class GetUserPermissionCheckIT extends TestConfig {

    private static final String url = "/service-api/v2/accesscontrol/accessgroups/users/permissions";
    private static final String USER_ID = UUID.randomUUID().toString();

    private ServiceAgreement serviceAgreement;

    @Before
    public void setUp() {

        repositoryCleaner.clean();
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setName("le-name");
        legalEntity.setExternalId("le-name");
        legalEntity.setType(LegalEntityType.CUSTOMER);

        legalEntity = legalEntityJpaRepository.save(legalEntity);

        serviceAgreement = new ServiceAgreement();
        serviceAgreement.setCreatorLegalEntity(legalEntity);
        serviceAgreement.setName("sa-01");
        serviceAgreement.setDescription("sa-01");

        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        UserContext userContext = new UserContext(USER_ID, serviceAgreement.getId());
        userContext = userContextJpaRepository.save(userContext);

        FunctionGroup functionGroup = new FunctionGroup();
        functionGroup.setName("name");
        functionGroup.setDescription("description");
        functionGroup.setType(FunctionGroupType.SYSTEM);
        functionGroup.setServiceAgreement(serviceAgreement);

        GroupedFunctionPrivilege groupedFunctionPrivilegeExecute = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeExecute.setApplicableFunctionPrivilegeId(apfBf1003View.getId());
        groupedFunctionPrivilegeExecute.setFunctionGroup(functionGroup);

        GroupedFunctionPrivilege groupedFunctionPrivilegeRead = new GroupedFunctionPrivilege();
        groupedFunctionPrivilegeRead.setApplicableFunctionPrivilegeId(apfBf1003Edit.getId());
        groupedFunctionPrivilegeRead.setFunctionGroup(functionGroup);

        functionGroup
            .setPermissions(newHashSet(groupedFunctionPrivilegeExecute, groupedFunctionPrivilegeRead));
        functionGroup = functionGroupJpaRepository.save(functionGroup);
        functionGroupJpaRepository.flush();
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);
    }


    @Test
    public void shouldPassOnCheckUserPermissions() throws Exception {
        mockMvc.perform(get(url)
            .param("userId", USER_ID)
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("resource", bf1003.getResourceName())
            .param("function", bf1003.getFunctionName())
            .param("privileges", "edit")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldPassOnDuplicateAssignedPrivilegeOnCheckUserPermissions() throws Exception {
        mockMvc.perform(get(url)
            .param("userId", USER_ID)
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("resource", bf1003.getResourceName())
            .param("function", bf1003.getFunctionName())
            .param("privileges", "edit,edit")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenUserDoesNotHaveAllPrivileges() throws Exception {
        mockMvc.perform(get(url)
            .param("userId", USER_ID)
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("resource", bf1003.getResourceName())
            .param("function", bf1003.getFunctionName())
            .param("privileges", "execute,read")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(ERR_ACQ_017.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(ERR_ACQ_017.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowForbiddenForPrivilegesThatDoNotExist() throws Exception {
        mockMvc.perform(get(url)
            .param("userId", USER_ID)
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("resource", bf1003.getResourceName())
            .param("function", bf1003.getFunctionName())
            .param("privileges", "privileges,that,does,not,exist")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals(ERR_ACQ_017.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(ERR_ACQ_017.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }
}