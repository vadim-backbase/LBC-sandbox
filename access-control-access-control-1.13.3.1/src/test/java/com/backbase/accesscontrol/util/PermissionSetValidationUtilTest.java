package com.backbase.accesscontrol.util;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_102;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mapstruct.ap.internal.util.Collections.asSet;

import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class PermissionSetValidationUtilTest {

    @InjectMocks
    private PermissionSetValidationUtil permissionSetValidationUtil;

    @Test
    public void shouldThrowBadRequestWhenTwoEmptyIdentifiers() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers())
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionSetValidationUtil
            .validateUserApsIdentifiers(itemPut.getRegularUserAps(), itemPut.getAdminUserAps()));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenTwoIdentifiersPresent() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(1))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(Sets.newHashSet(new BigDecimal(2)))
                .withNameIdentifiers(asSet("APS name")));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> permissionSetValidationUtil
            .validateUserApsIdentifiers(itemPut.getRegularUserAps(), itemPut.getAdminUserAps()));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode()));
    }

}