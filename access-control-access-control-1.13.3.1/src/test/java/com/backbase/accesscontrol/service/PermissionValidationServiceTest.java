package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class PermissionValidationServiceTest {

    @InjectMocks
    private PermissionValidationService permissionValidationService;
    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private AccessControlValidator accessControlValidator;
    @Mock
    private DataGroupService dataGroupService;

    @Spy
    DataGroupMapper dataGroupMapper = Mappers.getMapper(DataGroupMapper.class);

    @Before
    public void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void shouldGetDataGroupById() {
        String id = "id";
        String description = "description";
        String name = "dg.name";
        String dataItemType = "CONTACTS";
        String saId = "saId";
        Set<String> items = Sets.newHashSet("item1", "item2");

        DataGroup dataGroup = new DataGroup();
        dataGroup.setId(id);
        dataGroup.setDescription(description);
        dataGroup.setDataItemType(dataItemType);
        dataGroup.setName(name);
        dataGroup.setServiceAgreementId(saId);
        dataGroup.setDataItemIds(items);

        when(dataGroupService.getByIdWithExtendedData(eq(id))).thenReturn(dataGroup);

        DataGroupItemBase dataGroupItemBase = permissionValidationService.getDataGroupById(id);

        assertThat(dataGroupItemBase, allOf(
            hasProperty("id", equalTo(id)),
            hasProperty("name", equalTo(name)),
            hasProperty("description", equalTo(description)),
            hasProperty("serviceAgreementId", equalTo(saId)),
            hasProperty("type", equalTo(dataItemType)),
            hasProperty("items", containsInAnyOrder(items.toArray()))
        ));
    }

    @Test
    public void shouldDoNothingOnValidateAccessToServiceAgreementResource() {
        String serviceAgreementId = "001";

        permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.ACCOUNT);

        verify((accessControlValidator), times(1))
            .userHasNoAccessToServiceAgreement(eq(serviceAgreementId),
                eq(AccessResourceType.ACCOUNT));
    }

    @Test
    public void testShouldGetFunctionGrouypById() {

        when(functionGroupService.getFunctionGroupById(eq("1")))
            .thenReturn(new FunctionGroupByIdGetResponseBody().withId("1"));

        FunctionGroupByIdGetResponseBody response = permissionValidationService.getFunctionGroupById("1");

        Assert.assertEquals("1", response.getId());
    }


    @Test
    public void shouldThrowExceptionOnValidateAccessToServiceAgreementResource() {
        String serviceAgreementId = "001";

        when(accessControlValidator
            .userHasNoAccessToServiceAgreement(eq(serviceAgreementId),
                eq(AccessResourceType.ACCOUNT)))
            .thenReturn(true);

        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> permissionValidationService
            .validateAccessToServiceAgreementResource(serviceAgreementId, AccessResourceType.ACCOUNT));

        verify((accessControlValidator), times(1))
            .userHasNoAccessToServiceAgreement(eq(serviceAgreementId),
                eq(AccessResourceType.ACCOUNT));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()));
    }
}