package com.backbase.accesscontrol.business.datagroup.strategy;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_104;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.ContactsService;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContactWorkerTest {

    private static final String DATA_ITEM_TYPE = "PAYEES";

    private ContactWorker contactWorker;
    @Mock
    private ContactsService contactsService;

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

        contactWorker = new ContactWorker(contactsService);
    }

    @Test
    public void shouldReturnPayeesAsType() {
        String type = contactWorker.getType();
        assertEquals(DATA_ITEM_TYPE, type);
    }

    @Test
    public void shouldNotSupportParticipants() {
        boolean supportParticipants = contactWorker.isValidatingAgainstParticipants();
        assertFalse(supportParticipants);
    }

    @Test
    public void shouldConvertToInternalIdsSuccessfully() {
        String saId = "saId";
        when(contactsService.convertExternalContactIds(any(Set.class), eq(saId)))
            .thenReturn(externalInternalIdsMap);

        List<String> resultInternalItemsIds = contactWorker
            .convertToInternalIdsAndValidate(new HashSet<>(externalItems), emptySet(), saId);

        assertEquals(internalItems.size(), resultInternalItemsIds.size());
    }

    @Test
    public void shouldThrowBadRequestOnValidationOnConvertToInternalIds() {
        String saId = "saId";

        Map<String, List<String>> invalidMap = new HashMap<String, List<String>>() {{
            put("externalItem1", asList("internalItem1", "internalItem2"));
        }};
        Set<String> externalIds = new HashSet<>();
        externalIds.add("externalItem1");
        externalIds.add("externalItem2");
        when(contactsService.convertExternalContactIds(eq(externalIds), eq(saId)))
            .thenReturn(invalidMap);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> contactWorker
                .convertToInternalIdsAndValidate(externalIds, emptySet(), saId));
        assertEquals(ERR_AG_104.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenServiceAgreementIdIsNull() {

        when(contactsService
            .convertExternalContactIds(any(), eq(null)))
            .thenThrow(getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode()));

        assertThat(Assert.assertThrows(BadRequestException.class,
            () -> contactWorker
                .convertToInternalIdsAndValidate(Sets.newHashSet("ext1"), Sets.newHashSet("part1"), null)),
            new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode()));
    }
}