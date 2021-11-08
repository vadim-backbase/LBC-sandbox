package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_089;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountArrangementsLegalEntities;
import com.backbase.dbs.arrangement.api.client.v2.model.AccountPresentationArrangementLegalEntityIds;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "backbase.data-group.validation.enabled=true",
    "backbase.approval.validation.enabled=true"}
)
public class PutDataGroupByIdApprovalOnValidateIT extends TestDbWireMock {

    private static final String PERSISTENCE_ARRANGEMENTS = "/service-api/v2/arrangements/legalentities";

    private String url = "/accessgroups/data-groups/";

    private DataGroup dataGroup;

    @Before
    public void setUp() {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);


        dataGroup = DataGroupUtil
            .createDataGroup("dg-name", "ARRANGEMENTS", "description", rootMsa);

        dataGroup = dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    public void shouldFailValidationRoute() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Arrays.asList("1","2"));

        AccountArrangementsLegalEntities arrangementIdsBody = new AccountArrangementsLegalEntities()
            .arrangementsLegalEntities(
                singletonList(new AccountPresentationArrangementLegalEntityIds().arrangementId("1")));

        addStubGet(
            new UrlBuilder(PERSISTENCE_ARRANGEMENTS)
                .addQueryParameter("arrangementIds", "1")
                .addQueryParameter("arrangementIds", "2")
                .addQueryParameter("legalEntityIds", rootLegalEntity.getId())
                .build(),
            arrangementIdsBody,
            200
        );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(),
                HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_089.getErrorMessage(), ERR_AG_089.getErrorCode()));
    }

    @Test
    public void shouldFailIfCheckHierarchyFails() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId("76579bcba5e14a698bea4679575a1104")
            .withType("ARRANGEMENTS")
            .withItems(singletonList("item"));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(),
                HttpMethod.PUT, updateBody, "user2", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()));
    }
}