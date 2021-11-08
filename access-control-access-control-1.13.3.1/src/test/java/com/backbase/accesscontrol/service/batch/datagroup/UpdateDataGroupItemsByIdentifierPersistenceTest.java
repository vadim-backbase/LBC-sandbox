package com.backbase.accesscontrol.service.batch.datagroup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupItemsByIdentifierPersistenceTest {

    @InjectMocks
    private UpdateDataGroupItemsByIdentifierPersistence updateDataGroupItemsByIdentifierPersistence;
    @Mock
    private DataGroupService dataGroupService;
    @Captor
    private ArgumentCaptor<PresentationDataGroupItemPutRequestBody> argumentCaptor;

    @Test
    public void shouldReturnUnexpectedNumberOfIdentifiers() {
        PresentationDataGroupItemPutRequestBody requestItem = new PresentationDataGroupItemPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier("someValue")
                    .withNameIdentifier(new NameIdentifier())
            );
        List<String> strings = updateDataGroupItemsByIdentifierPersistence
            .customValidateConstraintsForRequestBody(requestItem);
        assertThat(strings, hasSize(1));
        assertThat("Multiple data group identifiers detected, single expected", in(strings));
    }

    @Test
    public void shouldReturnEmptyMessageWhenIdPassed() {
        PresentationDataGroupItemPutRequestBody requestItem = new PresentationDataGroupItemPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier("someValue")
            );
        List<String> strings = updateDataGroupItemsByIdentifierPersistence
            .customValidateConstraintsForRequestBody(requestItem);
        assertThat(strings, hasSize(0));
    }

    @Test
    public void shouldReturnEmptyMessageWhenNameIdPassed() {
        PresentationDataGroupItemPutRequestBody requestItem = new PresentationDataGroupItemPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier())
            );
        List<String> strings = updateDataGroupItemsByIdentifierPersistence
            .customValidateConstraintsForRequestBody(requestItem);
        assertThat(strings, hasSize(0));
    }

    @Test
    public void shouldReturnEmptyMessageWhenNoIdentifierPassed() {
        PresentationDataGroupItemPutRequestBody requestItem = new PresentationDataGroupItemPutRequestBody();
        List<String> strings = updateDataGroupItemsByIdentifierPersistence
            .customValidateConstraintsForRequestBody(requestItem);
        assertThat(strings, hasSize(0));
    }

    @Test
    public void shouldReturnBatchResponseExtendedWithIdIdentifier() {
        PresentationDataGroupItemPutRequestBody item = new PresentationDataGroupItemPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier("someValue")
                    .withNameIdentifier(new NameIdentifier())
            );
        ArrayList<String> errorMessages = Lists.newArrayList("");
        ResponseItemExtended batchResponseItem = updateDataGroupItemsByIdentifierPersistence
            .getBatchResponseItem(item, ItemStatusCode.HTTP_STATUS_OK, errorMessages);
        assertThat(batchResponseItem,
            allOf(
                hasProperty("externalServiceAgreementId", emptyOrNullString()),
                hasProperty("resourceId", is("someValue")),
                hasProperty("status", is(ItemStatusCode.HTTP_STATUS_OK)),
                hasProperty("errors", is(errorMessages))
            )
        );
    }

    @Test
    public void shouldReturnBatchResponseExtendedWithNameIdentifier() {
        PresentationDataGroupItemPutRequestBody item = new PresentationDataGroupItemPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withNameIdentifier(new NameIdentifier().withName("name")
                        .withExternalServiceAgreementId("externalServiceAgreementId"))
            );
        ArrayList<String> errorMessages = Lists.newArrayList("");
        ResponseItemExtended batchResponseItem = updateDataGroupItemsByIdentifierPersistence
            .getBatchResponseItem(item, ItemStatusCode.HTTP_STATUS_OK, errorMessages);
        assertThat(batchResponseItem,
            allOf(
                hasProperty("externalServiceAgreementId", is("externalServiceAgreementId")),
                hasProperty("resourceId", is("name")),
                hasProperty("status", is(ItemStatusCode.HTTP_STATUS_OK)),
                hasProperty("errors", is(errorMessages))
            )
        );
    }

    @Test
    public void shouldInvokeUpdateDataGroup() {
        PresentationDataGroupItemPutRequestBody item = new PresentationDataGroupItemPutRequestBody();
        updateDataGroupItemsByIdentifierPersistence.performBatchProcess(item);
        verify(dataGroupService).updateDataGroupItemsByIdIdentifier(argumentCaptor.capture());
        PresentationDataGroupItemPutRequestBody value = argumentCaptor.getValue();
        assertEquals(item, value);
    }

    @Test
    public void shouldReturnSortFalse() {
        assertFalse(updateDataGroupItemsByIdentifierPersistence.sortResponse());
    }
}