package com.backbase.accesscontrol.api.service.it.aps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.PermissionSetServiceApiController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test for {@link PermissionSetServiceApiController#getPermissionSet(
 *String, HttpServletRequest, HttpServletResponse)}
 */
public class GetPermissionSetIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/permission-sets";
    protected ApplicableFunctionPrivilege apfBf1028View;
    protected ApplicableFunctionPrivilege apfBf1028Create;

    @Before
    public void setUp() {
        repositoryCleaner.clean();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        apfBf1028Create = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "create");

        transactionTemplate.execute(transactionStatus -> {
            AssignablePermissionSet assignablePermissionSet = createAssignablePermissionSet("test21",
                AssignablePermissionType.CUSTOM, "desc", apfBf1028View.getId(), apfBf1028Create.getId());
            assignablePermissionSetJpaRepository.save(assignablePermissionSet);

            AssignablePermissionSet assignablePermissionSet2 = createAssignablePermissionSet("1Test1",
                AssignablePermissionType.CUSTOM, "desc", apfBf1028View.getId(), apfBf1028Create.getId());
            assignablePermissionSetJpaRepository.save(assignablePermissionSet2);

            AssignablePermissionSet assignablePermissionSet3 = createAssignablePermissionSet("Invalid1",
                AssignablePermissionType.CUSTOM, "desc", apfBf1028View.getId(), apfBf1028Create.getId());
            assignablePermissionSetJpaRepository.save(assignablePermissionSet3);
            return true;
        });

    }

    @Test
    public void shouldGetPermissionSet() throws Exception {

        String response = executeServiceRequest(new UrlBuilder(URL)
            .addQueryParameter("name", "test").build(), null, null, null, HttpMethod.GET);

        List<PresentationPermissionSetResponseItem> data =
            readValue(response,
                new TypeReference<List<PresentationPermissionSetResponseItem>>() {
                });
        assertEquals(2, data.size());
        assertEquals("test21", findByName(data, "test21").getName());
        assertEquals("1Test1", findByName(data, "1Test1").getName());
        assertTrue(findByName(data, "test21").getPermissions().get(0).getPrivileges().contains("view"));
        assertTrue(findByName(data, "test21").getPermissions().get(0).getPrivileges().contains("create"));
        assertTrue(findByName(data, "1Test1").getPermissions().get(0).getPrivileges().contains("view"));
        assertTrue(findByName(data, "1Test1").getPermissions().get(0).getPrivileges().contains("create"));
    }

    @Test
    public void shouldReturnAllOnGetPermissionSetWhenNoQueryParameterProvided() throws Exception {


        String response = executeServiceRequest(new UrlBuilder(URL)
            .build(), null, null, null, HttpMethod.GET);

        List<PresentationPermissionSetResponseItem> responseData =
            readValue(response,
                new TypeReference<List<PresentationPermissionSetResponseItem>>() {
                });
        assertEquals(5, responseData.size());
    }

    private PresentationPermissionSetResponseItem findByName(List<PresentationPermissionSetResponseItem> list, String name) {
        return list.stream().filter(aps -> name.equals(aps.getName())).findAny().orElse(null);
    }
}
