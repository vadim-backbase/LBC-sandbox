package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_103;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetDataGroupByIdTest {

    @Mock
    private DataGroupService dataGroupService;

    @Mock
    private ValidationConfig validationConfig;

    @Spy
    private DataGroupMapper dataGroupMapper = Mappers.getMapper(DataGroupMapper.class);

    @InjectMocks
    private GetDataGroupById getDataGroupById;

    @Test
    public void shouldCallGetDataGroupByIdPAndPService() {
        DataGroup dataGroup = new DataGroup()
            .withId("id")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withDataItemType("ARRANGEMENTS");

        when(dataGroupService.getById(eq("id"))).thenReturn(dataGroup);

        doNothing().when(validationConfig).validateIfDataGroupTypeIsAllowed("ARRANGEMENTS");

        when(dataGroupService.getByIdWithExtendedData(eq("id")))
            .thenReturn(dataGroup.withDataItemIds(newHashSet("1", "2")));

        InternalRequest<DataGroupByIdGetResponseBody> dataGroupById =
            getDataGroupById.getDataGroupById(getInternalRequest(null), "id");

        assertEquals("id", dataGroupById.getData().getId());
    }

    @Test
    public void shouldThrowBadRequestOnGetDataGroupByIdPAndPServiceWithInvalidType() {
        DataGroup dataGroup = new DataGroup()
            .withId("id")
            .withName("name")
            .withDescription("description")
            .withServiceAgreementId("service-agreement-id")
            .withDataItemType("CUSTOMERS");

        when(dataGroupService.getById(eq("id"))).thenReturn(dataGroup);

        doThrow(getBadRequestException(
            ERR_AG_103.getErrorMessage(),
            ERR_AG_103.getErrorCode()))
            .when(validationConfig).validateIfDataGroupTypeIsAllowed("CUSTOMERS");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> getDataGroupById.getDataGroupById(getInternalRequest(null), "id"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_103.getErrorMessage(), ERR_AG_103.getErrorCode())));
    }

}