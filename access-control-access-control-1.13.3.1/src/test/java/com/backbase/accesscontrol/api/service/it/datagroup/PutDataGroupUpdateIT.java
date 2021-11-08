package com.backbase.accesscontrol.api.service.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_085;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationSingleDataGroupPutRequestBody;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class PutDataGroupUpdateIT extends TestDbWireMock {

    private String url = "/accessgroups/data-groups/";

    private LegalEntity legalEntity;
    private ServiceAgreement serviceAgreement;
    private DataGroup dataGroup1;
    private DataGroup dataGroup2;

    @Before
    public void setUp() throws Exception {
        legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        dataGroup1 = DataGroupUtil.createDataGroup("dg-name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setDataItemIds(Collections.singleton("item1"));
        dataGroupJpaRepository.save(dataGroup1);
        dataGroup2 = DataGroupUtil.createDataGroup("dg-name-2", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup2.setDataItemIds(Sets.newHashSet("item3", "item2"));
        dataGroupJpaRepository.save(dataGroup2);
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyUpdateDataGroupsByIdIdentifier() throws IOException {
        PresentationSingleDataGroupPutRequestBody updateBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(dataGroup1.getId())
            )
            .withName("Updated name")
            .withDescription("Updated description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                Collections.singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(getUuid())
                )
            );

        executeRequest(url, updateBody, HttpMethod.PUT);

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup1.getId())));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyUpdateDataGroupsByNameIdentifier() throws Exception {
        PresentationSingleDataGroupPutRequestBody updateBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withNameIdentifier(
                        new NameIdentifier()
                            .withName(dataGroup2.getName())
                            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
                    )
            )
            .withName("Updated name")
            .withDescription("Updated description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                Collections.singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(getUuid())
                )
            );

        executeRequest(url, updateBody, HttpMethod.PUT);

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup2.getId())));
    }

    @Test
    public void shouldThrowNotFoundExceptionForInvalidIdIdentifier() {
        PresentationSingleDataGroupPutRequestBody updateBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(getUuid())
            )
            .withName("Updated name")
            .withDescription("Updated description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                Collections.singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(getUuid())
                )
            );

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeRequest(url, updateBody, HttpMethod.PUT));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_085.getErrorMessage(), ERR_ACC_085.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundExceptionForInvalidNameIdentifier() {
        PresentationSingleDataGroupPutRequestBody updateBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withNameIdentifier(
                        new NameIdentifier()
                            .withName("random-name")
                            .withExternalServiceAgreementId("random-ex-id")
                    )
            )
            .withName("Updated name")
            .withDescription("Updated description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                Collections.singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(getUuid())
                )
            );

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeRequest(url, updateBody, HttpMethod.PUT));
        assertThat(exception, new NotFoundErrorMatcher(ERR_ACC_085.getErrorMessage(), ERR_ACC_085.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionForDuplicateName() {
        PresentationSingleDataGroupPutRequestBody updateBody = new PresentationSingleDataGroupPutRequestBody()
            .withDataGroupIdentifier(
                new PresentationIdentifier()
                    .withIdIdentifier(dataGroup1.getId())
            )
            .withName(dataGroup2.getName())
            .withDescription("Updated description")
            .withType("ARRANGEMENTS")
            .withDataItems(
                Collections.singletonList(
                    new PresentationItemIdentifier()
                        .withInternalIdIdentifier(getUuid())
                )
            );

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeRequest(url, updateBody, HttpMethod.PUT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }

}
