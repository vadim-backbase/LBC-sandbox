package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.routes.legalentity.AddLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.CreateLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByExternalIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityForCurrentUserRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.GetMasterServiceAgreementByLegalEntityIdRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.ListLegalentitiesRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.UpdateBatchLegalEntityRouteProxy;
import com.backbase.accesscontrol.routes.legalentity.UpdateLegalEntityByExternalIdRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityServiceTest {

    @InjectMocks
    private LegalEntityService legalEntityService;

    @Mock
    private ListLegalentitiesRouteProxy legalEntitiesRouteProxy;

    @Mock
    private GetLegalEntityByIdRouteProxy getLegalEntityByIdRouteProxy;

    @Mock
    private GetMasterServiceAgreementByLegalEntityIdRouteProxy getMasterServiceAgreementByLegalEntityIdRouteProxy;

    @Mock
    private GetLegalEntityByExternalIdRouteProxy getLegalEntityByExternalIdRouteProxy;

    @Mock
    private UpdateLegalEntityByExternalIdRouteProxy updateLegalEntityByExternalIdRouteProxy;

    @Mock
    private CreateLegalEntityRouteProxy createLegalEntityRouteProxy;

    @Mock
    private AddLegalEntityRouteProxy addLegalEntityRouteProxy;

    @Mock
    private GetLegalEntityForCurrentUserRouteProxy getLegalEntityForCurrentUserRouteProxy;

    @Mock
    private UpdateBatchLegalEntityRouteProxy updateBatchLegalEntityRouteProxy;

    @Mock
    private GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy getMasterServiceAgreementByExternalLegalEntityIdRouteProxy;

    @Test
    public void shouldPassWhenGetLegalEntitiesIsInvoked() {
        String parentEntityId = "1";

        List<LegalEntitiesGetResponseBody> mockResult = singletonList(
            new LegalEntitiesGetResponseBody().withId("id").withParentId(parentEntityId).withName("name"));
        InternalRequest<List<LegalEntitiesGetResponseBody>> legalEntitiesBusinessProcessResult =
            new InternalRequest<>();
        legalEntitiesBusinessProcessResult.setData(mockResult);

        when(legalEntitiesRouteProxy.getLegalentites(any(InternalRequest.class), eq(parentEntityId)))
            .thenReturn(legalEntitiesBusinessProcessResult);

        List<LegalEntitiesGetResponseBody> legalentites = legalEntityService.getLegalEntities(parentEntityId);

        verify(legalEntitiesRouteProxy, times(1)).getLegalentites(any(InternalRequest.class), eq(parentEntityId));
        assertEquals(mockResult, legalentites);
    }

    @Test
    public void shouldPassWhenGetLegalEntityByIdIsInvoked() {
        String legalEntityId = "LE-001";

        when(getLegalEntityByIdRouteProxy.getLegalEntity(any(InternalRequest.class), eq(legalEntityId)))
            .thenReturn(getInternalRequest(null));

        legalEntityService.getLegalEntityById(legalEntityId);

        verify(getLegalEntityByIdRouteProxy, times(1)).getLegalEntity(any(InternalRequest.class), eq(legalEntityId));
    }

    @Test
    public void shouldPassWhenGetMasterServiceAgreementByLegalEntityIdIsInvoked() {
        String legalEntityId = "LE-001";

        when(getMasterServiceAgreementByLegalEntityIdRouteProxy
            .getMasterServiceAgreement(any(InternalRequest.class), eq(legalEntityId)))
            .thenReturn(getInternalRequest(null));

        legalEntityService.getMasterServiceAgreement(legalEntityId);

        verify(getMasterServiceAgreementByLegalEntityIdRouteProxy, times(1))
            .getMasterServiceAgreement(any(InternalRequest.class), eq(legalEntityId));
    }

    @Test
    public void shouldReturnLegalEntitiesPostResponseBody() {
        String name = "legalEntity";

        LegalEntitiesPostResponseBody legalEntitiesPostResponseBody = new LegalEntitiesPostResponseBody()
            .withId("id");

        when(createLegalEntityRouteProxy.createLegalEntity(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(legalEntitiesPostResponseBody));

        LegalEntitiesPostRequestBody legalEntityPostRequestBody = new LegalEntitiesPostRequestBody()
            .withExternalId("exid")
            .withName(name)
            .withParentExternalId("parent");

        LegalEntitiesPostResponseBody legalEntity = legalEntityService.createLegalEntity(legalEntityPostRequestBody);

        assertEquals("id", legalEntity.getId());
    }

    @Test
    public void shouldReturnLegalEntitiesCreated() {
        String name = "legalEntity";

        CreateLegalEntitiesPostResponseBody createLegalEntitiesPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId("id");
        when(addLegalEntityRouteProxy.createLegalEntity(any(InternalRequest.class)))
            .thenReturn(getInternalRequest(createLegalEntitiesPostResponseBody));

        CreateLegalEntitiesPostRequestBody createLegalEntitiesPostRequestBody = new CreateLegalEntitiesPostRequestBody()
            .withExternalId("exid")
            .withName(name)
            .withParentExternalId("parent")
            .withType(LegalEntityType.CUSTOMER);

        CreateLegalEntitiesPostResponseBody responseCreateLegalEntity = legalEntityService
            .addLegalEntity(createLegalEntitiesPostRequestBody);

        assertEquals("id", responseCreateLegalEntity.getId());
    }

    @Test
    public void shouldReturnLegalEntityByExternalIdGetResponseBody() {
        String id = "1";
        String externalId = "123";
        String name = "Name";

        LegalEntityByExternalIdGetResponseBody data = new LegalEntityByExternalIdGetResponseBody()
            .withId(id)
            .withExternalId(externalId)
            .withName(name);

        when(getLegalEntityByExternalIdRouteProxy.getLegalEntity(any(InternalRequest.class), eq(externalId)))
            .thenReturn(getInternalRequest(data));

        LegalEntityByExternalIdGetResponseBody result = legalEntityService.getLegalEntityByExternalId(externalId);

        assertEquals(result.getId(), id);
        assertEquals(result.getExternalId(), externalId);
        assertEquals(result.getName(), name);
    }

    @Test
    public void shouldUpdateLegalEntityByExternalId() {
        LegalEntityByExternalIdPutRequestBody data = new LegalEntityByExternalIdPutRequestBody()
            .withType(LegalEntityType.BANK);
        String externalId = "externalId";

        when(updateLegalEntityByExternalIdRouteProxy
            .updateLegalEntityByExternalId(any(InternalRequest.class), eq(externalId)))
            .thenReturn(getInternalRequest(null));

        legalEntityService.updateLegalEntityByExternalId(data, externalId);

        verify(updateLegalEntityByExternalIdRouteProxy, times(1))
            .updateLegalEntityByExternalId(any(InternalRequest.class), eq(externalId));
    }

    @Test
    public void shouldPassWhenGetLegalEntityForCurrentUserIsInvoked() {
        LegalEntityForUserGetResponseBody legalEntityForUserGetResponseBody = new LegalEntityForUserGetResponseBody()
            .withExternalId("id").withExternalId("externalId").withName("name").withParentId("parentId");

        InternalRequest<LegalEntityForUserGetResponseBody> request = getInternalRequest(null);
        request.setData(legalEntityForUserGetResponseBody);

        when(getLegalEntityForCurrentUserRouteProxy.getLegalEntityForCurrentUser(any(InternalRequest.class)))
            .thenReturn(request);

        LegalEntityForUserGetResponseBody legalEntityForCurrentUser = legalEntityService.getLegalEntityForCurrentUser();

        verify(getLegalEntityForCurrentUserRouteProxy, times(1))
            .getLegalEntityForCurrentUser(any(InternalRequest.class));
        assertEquals(legalEntityForUserGetResponseBody, legalEntityForCurrentUser);
    }

    @Test
    public void shouldUpdateBatchLegalEntity() {
        LegalEntityPut legalEntityPut1 = new LegalEntityPut()
            .withExternalId("LE-01")
            .withLegalEntity(new LegalEntity().withName("name")
                .withExternalId("LE-01")
                .withParentExternalId(null)
                .withType(LegalEntityType.CUSTOMER));
        LegalEntityPut legalEntityPut2 = new LegalEntityPut()
            .withExternalId("LE-02")
            .withLegalEntity(new LegalEntity()
                .withName("name")
                .withExternalId("LE-02")
                .withParentExternalId(null)
                .withType(LegalEntityType.CUSTOMER));

        BatchResponseItem successfulBatchResponseItem = new BatchResponseItem()
            .withResourceId("id1")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItem failedBatchResponseItem = new BatchResponseItem()
            .withResourceId("id2")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(singletonList("error"));

        List<LegalEntityPut> legalEntityPutList = asList(legalEntityPut1, legalEntityPut2);
        InternalRequest<List<LegalEntityPut>> request = new InternalRequest<>(legalEntityPutList, null);

        List<BatchResponseItem> mockResponseList = asList(successfulBatchResponseItem, failedBatchResponseItem);

        when(updateBatchLegalEntityRouteProxy.updateBatchLegalEntity(request))
            .thenReturn(getInternalRequest(mockResponseList));

        List<BatchResponseItem> response = legalEntityService.updateBatchLegalEntities(legalEntityPutList);

        assertEquals(2, response.size());
    }

    @Test
    public void shouldReturnMasterServiceAgreement() {
        MasterServiceAgreementGetResponseBody masterServiceAgreementGetResponseBody = new MasterServiceAgreementGetResponseBody()
            .withExternalId("externalId");

        when(getMasterServiceAgreementByExternalLegalEntityIdRouteProxy
            .getMasterServiceAgreementByExternalLegalEntityId(any(InternalRequest.class), eq("externalId")))
            .thenReturn(getInternalRequest(masterServiceAgreementGetResponseBody));

        MasterServiceAgreementGetResponseBody externalId = legalEntityService
            .getMasterServiceAgreementByExternalId("externalId");

        assertEquals(masterServiceAgreementGetResponseBody, externalId);
    }
}
