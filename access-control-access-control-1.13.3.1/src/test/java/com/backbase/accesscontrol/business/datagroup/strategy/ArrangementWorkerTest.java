package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.dataitems.ArrangementItemService;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrangementWorkerTest {

    private static final String DATA_ITEM_TYPE = "ARRANGEMENTS";

    @Mock
    private ArrangementItemService arrangementItemService;

    private ArrangementWorker arrangementWorker;
    private ArrangementWorker arrangementWorkerWithoutValidation;

    private List<String> externalItems;
    private List<String> internalItems;
    private Map<String, List<String>> externalInternalIdsMap;

    @Before
    public void setUp() {
        externalItems = new ArrayList<String>() {{
            add("externalItem1");
            add("externalItem2");
        }};

        internalItems = new ArrayList<String>() {{
            add("internalItem1");
            add("internalItem2");
        }};

        externalInternalIdsMap = new HashMap<String, List<String>>() {{
            put("externalItem1", singletonList("internalItem1"));
            put("externalItem2", singletonList("internalItem2"));
        }};

        arrangementWorker = new ArrangementWorker(arrangementItemService, true);
        arrangementWorkerWithoutValidation = new ArrangementWorker(arrangementItemService, false);
    }

    @Test
    public void shouldReturnArrangementAsType() {
        String type = arrangementWorker.getType();
        assertEquals(DATA_ITEM_TYPE, type);
    }

    @Test
    public void shouldSupportParticipants() {
        boolean supportParticipants = arrangementWorker.isValidatingAgainstParticipants();
        assertTrue(supportParticipants);
    }

    @Test
    public void shouldValidateInternalIdsSuccessfullyWhenValidationEnabled() {
        arrangementWorker.validateInternalIds(new HashSet<>(internalItems), Sets.newHashSet("participantId"));
        verify(arrangementItemService, times(1)).validate(any(List.class), any(List.class));
    }

    @Test
    public void shouldNotValidateInternalIdsWhenValidationDisabled() {
        arrangementWorkerWithoutValidation
            .validateInternalIds(new HashSet<>(internalItems), Sets.newHashSet("participantId"));
        verify(arrangementItemService, times(0)).validate(any(List.class), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenInternalIdsAreNotValidated() {
        doThrow(getBadRequestException(ERR_AG_089.getErrorMessage(),
            ERR_AG_089.getErrorCode()))
            .when(arrangementItemService).validate(any(List.class), any(List.class));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> arrangementWorker.validateInternalIds(new HashSet<>(internalItems),
                Sets.newHashSet("participantId")));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }

    @Test
    public void shouldConvertToInternalIdsSuccessfully() {
        when(arrangementItemService.mapExternalToInternalIds(any(Set.class), eq(null)))
            .thenReturn(externalInternalIdsMap);

        List<String> resultInternalItemsIds = arrangementWorker
            .convertToInternalIdsAndValidate(new HashSet<>(externalItems), Sets.newHashSet("participantId"), null);

        assertEquals(internalItems.size(), resultInternalItemsIds.size());
    }

    @Test
    public void shouldConvertToInternalIdsSuccessfullyWhenValidationDisabled() {
        when(arrangementItemService.mapExternalToInternalIds(any(Set.class), eq(null)))
            .thenReturn(externalInternalIdsMap);

        List<String> resultInternalItemsIds = arrangementWorkerWithoutValidation
            .convertToInternalIdsAndValidate(new HashSet<>(externalItems), Sets.newHashSet("participantId"), null);

        assertEquals(internalItems.size(), resultInternalItemsIds.size());
    }

    @Test
    public void shouldThrowExceptionWhenFailedToConvertToInternalIds() {
        when(arrangementItemService.mapExternalToInternalIds(any(Set.class), eq(null)))
            .thenReturn(new HashMap());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> arrangementWorker
                .convertToInternalIdsAndValidate(new HashSet<>(externalItems),
                    Sets.newHashSet("participantId"), null));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }

    @Test
    public void shouldThrowExceptionWhenValidationFailed() {
        when(arrangementItemService.mapExternalToInternalIds(any(Set.class), eq(null)))
            .thenReturn(externalInternalIdsMap);
        doThrow(getBadRequestException(ERR_AG_089.getErrorMessage(),
            ERR_AG_089.getErrorCode()))
            .when(arrangementItemService).validate(any(List.class), any(List.class));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> arrangementWorker
                .convertToInternalIdsAndValidate(new HashSet<>(externalItems),
                    Sets.newHashSet("participantId"), null));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode())));
    }
}
