package com.backbase.accesscontrol.business.datagroup;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.dataitems.ArrangementItemService;
import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemsValidationService;
import com.backbase.accesscontrol.dto.DataItemsValidatable;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidateDataGroupRouteProxyImplTest {

    public static final String SERVICE_AGREEMENT_ID = "service-agreement-id";
    @Mock
    private ArrangementItemService arrangementItemService;
    @Mock
    private ValidateDataGroupRouteProxyImpl validateDataGroup;

    private List<DataItemsValidationService> dataItemExternalIdConverterServices = new ArrayList<>();

    @Before
    public void setUp() {
        when(arrangementItemService.getType()).thenReturn("ARRANGEMENTS");
        dataItemExternalIdConverterServices.add(arrangementItemService);
        validateDataGroup = new ValidateDataGroupRouteProxyImpl(dataItemExternalIdConverterServices);
    }

    @Test
    public void shouldCallTheAppropriateValidation() {

        BadRequestException badRequestException = null;
        try {
            InternalRequest<DataItemsValidatable> arrangements = RequestUtils.getInternalRequest(
                new DataItemsValidatable("ARRANGEMENTS", Arrays.asList("1", "2"), SERVICE_AGREEMENT_ID));
            arrangements.setInternalRequestContext(new DefaultInternalRequestContext());
            validateDataGroup.validate(arrangements);
        } catch (BadRequestException e) {
            badRequestException = e;
        }

        assertEquals(null, badRequestException);
        verify(arrangementItemService, times(1))
            .validate(eq(Arrays.asList("1", "2")), eq(SERVICE_AGREEMENT_ID));

    }

}