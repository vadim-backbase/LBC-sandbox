package com.backbase.accesscontrol.pact.providers;

import static com.backbase.buildingblocks.pact.utils.PactConstants.UUID_WITH_HYPHEN;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.backbase.accesscontrol.api.service.UserContextQueryController;
import com.backbase.accesscontrol.api.service.UserQueryController;
import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.mappers.ArrangementPrivilegesMapper;
import com.backbase.accesscontrol.mappers.UserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ArrangementPrivilegesGetResponseBodyQueryConverter;
import com.backbase.accesscontrol.mappers.model.query.service.ArrangementPrivilegesGetResponseBodyToArrangementPrivilegeItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ContextLegalEntitiesToContextLegalEntitiesMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceApprovalPermissionsToPersistenceApprovalPermissionsMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceUserDataItemPermissionToPersistenceUserDataItemPermissionMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessEntitlementsResourceToEntitlementsResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessLegalEntitiesToLegalEntityResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserAccessServiceAgreementToEntitlementsResourceMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserContextsGetResponseBodyToGetContextMapper;
import com.backbase.accesscontrol.mappers.model.query.service.UserFunctionGroupsGetResponseBodyToUserFunctionGroupsMapper;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.accesscontrol.service.facades.UserContextFlowService;
import com.backbase.accesscontrol.service.facades.UserContextServiceFacade;
import com.backbase.accesscontrol.service.impl.UserAccessFunctionGroupService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.pact.provider.AbstractProviderPactTest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ContextLegalEntities;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.EntitlementsResource;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceDataItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserPermission;
import java.util.Collections;
import java.util.List;
import org.mapstruct.factory.Mappers;

@PactBroker
@Provider("accesscontrol-service")
public class AccessControlProviderPactTest extends AbstractProviderPactTest {
    
    /*
     * Mocks & mappers for UserQueryController
     */
    private final UserAccessPrivilegeService mockUserAccessPrivilegeService = mock(UserAccessPrivilegeService.class);
    private final UserAccessPermissionCheckService mockUserAccessPermissionCheckService = mock(UserAccessPermissionCheckService.class);
    private final ApprovalService mockApprovalService = mock(ApprovalService.class);
    private final UserAccessFunctionGroupService mockUserAccessFunctionGroupService = mock(UserAccessFunctionGroupService.class);
    private final ArrangementPrivilegesMapper mockArrangementPrivilegesMapper = Mappers.getMapper(ArrangementPrivilegesMapper.class);
    private final PayloadConverter mockPayloadConverterUserQueryController = new PayloadConverter(asList(
                    Mappers.getMapper(ArrangementPrivilegesGetResponseBodyToArrangementPrivilegeItemMapper.class),
                    Mappers.getMapper(ArrangementPrivilegesGetResponseBodyQueryConverter.class),
                    Mappers.getMapper(PersistenceUserDataItemPermissionToPersistenceUserDataItemPermissionMapper.class),
                    Mappers.getMapper(UserAccessEntitlementsResourceToEntitlementsResourceMapper.class),
                    Mappers.getMapper(UserAccessServiceAgreementToEntitlementsResourceMapper.class),
                    Mappers.getMapper(UserAccessLegalEntitiesToLegalEntityResourceMapper.class),
                    Mappers.getMapper(PersistenceApprovalPermissionsToPersistenceApprovalPermissionsMapper.class),
                    Mappers.getMapper(ContextLegalEntitiesToContextLegalEntitiesMapper.class),
                    Mappers.getMapper(UserFunctionGroupsGetResponseBodyToUserFunctionGroupsMapper.class))
                );
    
    /*
     * Mocks & mappers for UserContextQueryController
     */
    private final UserContextServiceFacade mockUserContextServiceFacade = mock(UserContextServiceFacade.class);
    private final UserContextFlowService mockUserContextFlowService = mock(UserContextFlowService.class);
    private final UserContextUtil mockUserContextUtil = mock(UserContextUtil.class);
    private final ServiceAgreementIdProvider mockServiceAgreementIdProvider = mock(ServiceAgreementIdProvider.class);
    private final ValidationConfig mockValidationConfig = mock(ValidationConfig.class);
    private final UserContextPermissionsMapper mockUserContextPermissionsMapper = Mappers.getMapper(UserContextPermissionsMapper.class);
    private final PayloadConverter mockPayloadConverterUserContextQueryController = new PayloadConverter(asList(
                    Mappers.getMapper(UserContextsGetResponseBodyToGetContextMapper.class))
                );

    public AccessControlProviderPactTest() {
        setControllers(new UserQueryController(mockUserAccessPrivilegeService, mockUserAccessPermissionCheckService,
                            mockApprovalService, mockUserAccessFunctionGroupService, mockArrangementPrivilegesMapper,
                            mockPayloadConverterUserQueryController),
                        new UserContextQueryController(mockUserContextServiceFacade, mockUserContextFlowService,
                            mockUserContextUtil, mockServiceAgreementIdProvider, mockValidationConfig,
                            mockPayloadConverterUserContextQueryController, mockUserContextPermissionsMapper));
    }
    
    @State(value = "arrangement privileges exist for the user")
    public void retrieveArrangementPrivileges() {
        List<ArrangementPrivilegesDto> dtos = Collections.singletonList(new ArrangementPrivilegesDto()
                        .withArrangementId(UUID_WITH_HYPHEN)
                        .withPrivileges(Collections.singletonList(new Privilege().withPrivilege("view"))));
        
        when(mockUserAccessPrivilegeService.getArrangementPrivileges(anyString(), anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString())).thenReturn(dtos);
        
        
    }
    
    @State(value = "data item permissions exist for the user")
    public void retrieveDataItemPermissions() {
        List<PersistenceUserDataItemPermission> response = Collections.singletonList(new PersistenceUserDataItemPermission()
                        .withDataItem(new PersistenceDataItem()
                                        .withId(UUID_WITH_HYPHEN)
                                        .withDataType("CUSTOMERS"))
                        .withPermissions(Collections.singletonList(new PersistenceUserPermission()
                                        .withBusinessFunction("Emulate")
                                        .withResource("Employee")
                                        .withPrivileges(Collections.singletonList("view")))));
        
        
        when(mockUserAccessPrivilegeService.getUserDataItemsPrivileges(anyString(), anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString())).thenReturn(response);
    }
    
    @State(value = "resources exist that the user has access to")
    public void retrieveUserAccessResource() {
        ContextLegalEntities response =
                        new ContextLegalEntities().withLegalEntities(Collections.singletonList(UUID_WITH_HYPHEN));

        when(mockUserAccessPermissionCheckService
                        .checkUserAccessToEntitlementsResources(any(EntitlementsResource.class))).thenReturn(response);
    }

    @State(value = "service agreements exist for the user")
    public void retrieveUserContextServiceAgreements() {
        Element element = new Element()
                        .withServiceAgreementId(UUID_WITH_HYPHEN)
                        .withServiceAgreementName("SA Name 1")
                        .withDescription("SA Name 1 joint account")
                        .withServiceAgreementMaster(true);
        UserContextsGetResponseBody response = new UserContextsGetResponseBody()
                        .withElements(Collections.singletonList(element))
                        .withTotalElements(1L);

        when(mockUserContextServiceFacade.getUserContextsByUserId(anyString(), eq(null), anyInt(), anyInt())).thenReturn(response);
    }

}
