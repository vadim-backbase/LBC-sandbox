package com.backbase.accesscontrol.mappers;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubEntitiesGetResponseBodyMapperTest {

    private SubEntitiesGetResponseBodyMapper mapper = Mappers.getMapper(SubEntitiesGetResponseBodyMapper.class);

    @Test
    public void shouldMapSubEntitiesPostResponseBodyToRecordsDto() {
        SubEntitiesPostResponseBody postResponseBody01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        postResponseBody01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody postResponse02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        postResponse02.withAddition("address1", "address02-01");
        postResponse02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody postResponse03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        postResponse03.withAddition("address1", "address03-01");
        postResponse03.withAddition("phone", "88888888");

        List<SubEntitiesGetResponseBody> subEntitiesGetResponseBodies = mapper
            .toGetResponses(asList(postResponseBody01, postResponse02, postResponse03));

        assertThat(subEntitiesGetResponseBodies, hasSize(3));
        assertThat(subEntitiesGetResponseBodies, contains(
            allOf(
                hasProperty("id", is(postResponseBody01.getId())),
                hasProperty("externalId", is(postResponseBody01.getExternalId())),
                hasProperty("name", is(postResponseBody01.getName())),
                hasProperty("type", is(postResponseBody01.getType())),
                hasProperty("additions", is(postResponseBody01.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(postResponse02.getId())),
                hasProperty("externalId", is(postResponse02.getExternalId())),
                hasProperty("name", is(postResponse02.getName())),
                hasProperty("type", is(postResponse02.getType())),
                hasProperty("additions", is(postResponse02.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(postResponse03.getId())),
                hasProperty("externalId", is(postResponse03.getExternalId())),
                hasProperty("name", is(postResponse03.getName())),
                hasProperty("type", is(postResponse03.getType())),
                hasProperty("additions", is(postResponse03.getAdditions()))
            )
        ));
    }
}