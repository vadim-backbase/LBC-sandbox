package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_104;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.dataitems.CustomerItemService;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
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
public class CustomerWorkerTest {

    private static final String DATA_ITEM_TYPE = "CUSTOMERS";

    @Mock
    private CustomerItemService customerItemService;

    private CustomerWorker customerWorker;

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

        customerWorker = new CustomerWorker(customerItemService);
    }

    @Test
    public void shouldReturnArrangementAsType() {
        String type = customerWorker.getType();
        assertEquals(DATA_ITEM_TYPE, type);
    }

    @Test
    public void shouldSupportParticipants() {
        boolean supportParticipants = customerWorker.isValidatingAgainstParticipants();
        assertFalse(supportParticipants);
    }

    @Test
    public void shouldConvertToInternalIdsSuccessfully() {
        when(customerItemService.mapExternalToInternalIds(any(Set.class), anyString()))
            .thenReturn(externalInternalIdsMap);

        List<String> resultInternalItemsIds = customerWorker
            .convertToInternalIdsAndValidate(new HashSet<>(externalItems), emptySet(), "saId");

        assertEquals(internalItems.size(), resultInternalItemsIds.size());
    }

    @Test
    public void shouldThrowExceptionWhenFailedToConvertToInternalIds() {
        when(customerItemService.mapExternalToInternalIds(any(Set.class), anyString()))
            .thenReturn(new HashMap());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> customerWorker
                .convertToInternalIdsAndValidate(new HashSet<>(externalItems), emptySet(), "saId"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_104.getErrorMessage(), ERR_AG_104.getErrorCode())));
    }

}
