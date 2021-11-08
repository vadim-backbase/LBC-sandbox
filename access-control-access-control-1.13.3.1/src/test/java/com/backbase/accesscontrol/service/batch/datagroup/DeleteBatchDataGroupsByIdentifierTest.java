package com.backbase.accesscontrol.service.batch.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.google.common.collect.Sets;
import java.lang.annotation.ElementType;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.hamcrest.Matcher;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteBatchDataGroupsByIdentifierTest {

    @Mock
    private Validator validator;

    @Mock
    private DataGroupService dataGroupService;

    @InjectMocks
    private DeleteDataGroupPersistence deleteBatchDataGroupsByIdentifier;

    @Mock
    private EventBus eventBus;

    @Test
    public void shouldDeleteDataGroups() {
        String dataGroupId = "id";
        String dataGroupId2 = "id2";
        String dataGroupId3 = "id3";
        String dataGroupId4 = "id4";
        String errorMessage = "raml error";
        String name = "name";

        PresentationIdentifier dataGroupIdentifier1 = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId);
        PresentationIdentifier dataGroupIdentifier2 = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId2);
        PresentationIdentifier dataGroupIdentifier3 = new PresentationIdentifier()
            .withIdIdentifier(dataGroupId3);
        PresentationIdentifier dataGroupIdentifier4 = new PresentationIdentifier()
            .withNameIdentifier(new NameIdentifier().withName(name).withExternalServiceAgreementId(null));

        List<PresentationIdentifier> dataGroupIdentifiers = asList(dataGroupIdentifier1, dataGroupIdentifier2,
            dataGroupIdentifier3,
            dataGroupIdentifier4);

        doNothing().when(dataGroupService).delete(eq(dataGroupId));
        NotFoundException notFoundException = getNotFoundException("error message", null);
        doThrow(notFoundException).when(dataGroupService).delete(eq(dataGroupId2));
        doNothing().when(dataGroupService).delete(eq(dataGroupId3));

        ConstraintViolation<PresentationIdentifier> violation1 = ConstraintViolationImpl
            .forBeanValidation("11", null, null, "error",
                null, null, null, null, PathImpl.createPathFromString("raml"),
                null, ElementType.TYPE);
        when(validator.validate(dataGroupIdentifier4)).thenReturn(Sets.newHashSet(violation1));

        mockGetDataGroupIdFromIdentifier(dataGroupIdentifier1, dataGroupId);
        mockGetDataGroupIdFromIdentifier(dataGroupIdentifier2, dataGroupId2);
        mockGetDataGroupIdFromIdentifier(dataGroupIdentifier3, dataGroupId3);
        mockGetDataGroupIdFromIdentifier(dataGroupIdentifier4, dataGroupId4);

        List<ResponseItemExtended> batchResponseItems = deleteBatchDataGroupsByIdentifier
            .processBatchItems(dataGroupIdentifiers);
        verify(dataGroupService, times(3)).delete(anyString());

        assertThat(batchResponseItems, hasSize(4));
        assertThat(batchResponseItems,
            contains(
                getBatchResponseItemMatcher(is(dataGroupId), is(ItemStatusCode.HTTP_STATUS_OK), hasSize(0)),
                getBatchResponseItemMatcher(is(dataGroupId2), is(ItemStatusCode.HTTP_STATUS_NOT_FOUND),
                    hasItems(notFoundException.getErrors().get(0).getMessage())),
                getBatchResponseItemMatcher(is(dataGroupId3), is(ItemStatusCode.HTTP_STATUS_OK), hasSize(0)),
                getBatchResponseItemMatcher(is(name), nullValue(), is(ItemStatusCode.HTTP_STATUS_BAD_REQUEST),
                    hasItems(errorMessage))
            )
        );
    }

    private void mockGetDataGroupIdFromIdentifier(PresentationIdentifier dataGroupIdentifier, String dataGroupId) {
        when(dataGroupService.retrieveDataGroupIdFromIdentifier(dataGroupIdentifier))
            .thenReturn(dataGroupId);
    }

    private Matcher<ResponseItemExtended> getBatchResponseItemMatcher(Matcher<?> idMatcher,
        Matcher<?> batchResponseStatusCodeMatcher,
        Matcher<?> errorsMatcher) {
        return allOf(
            hasProperty("resourceId", idMatcher),
            hasProperty("status", batchResponseStatusCodeMatcher),
            hasProperty("errors", errorsMatcher)
        );
    }

    private Matcher<ResponseItemExtended> getBatchResponseItemMatcher(Matcher<?> idMatcher,
        Matcher<?> externalServiceAgreementIdMatcher,
        Matcher<?> batchResponseStatusCodeMatcher,
        Matcher<?> errorsMatcher) {
        return allOf(
            hasProperty("resourceId", idMatcher),
            hasProperty("externalServiceAgreementId", externalServiceAgreementIdMatcher),
            hasProperty("status", batchResponseStatusCodeMatcher),
            hasProperty("errors", errorsMatcher)
        );
    }
}