package com.backbase.accesscontrol.mappers;

import static com.backbase.accesscontrol.domain.enums.LegalEntityType.BANK;
import static com.backbase.accesscontrol.domain.enums.LegalEntityType.CUSTOMER;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class SubEntitiesPersistenceToRecordsDtoMapperTest {

    private SubEntitiesPersistenceToRecordsDtoMapper mapper = Mappers
        .getMapper(SubEntitiesPersistenceToRecordsDtoMapper.class);

    @Test
    public void shouldMapSubEntitiesPostResponseBodyToRecordsDto() {
        LegalEntity legalEntity01 = new LegalEntity();
        legalEntity01.setExternalId("external-01");
        legalEntity01.setId("id-01");
        legalEntity01.setName("name-01");
        legalEntity01.setType(CUSTOMER);
        legalEntity01.setAddition("phone", "123456");

        LegalEntity legalEntity02 = new LegalEntity();
        legalEntity02.setExternalId("external-02");
        legalEntity02.setId("id-02");
        legalEntity02.setName("name-02");
        legalEntity02.setType(CUSTOMER);
        legalEntity02.setAddition("address1", "address02-01");
        legalEntity02.setAddition("address2", "address02-02");

        LegalEntity legalEntity03 = new LegalEntity();
        legalEntity03.setExternalId("external-03");
        legalEntity03.setId("id-03");
        legalEntity03.setName("name-03");
        legalEntity03.setType(BANK);
        legalEntity03.setAddition("address1", "address03-01");
        legalEntity03.setAddition("phone", "88888888");

        Page<LegalEntity> legalEntities = new PageImpl<>(asList(legalEntity01, legalEntity02, legalEntity03));

        RecordsDto<SubEntitiesPostResponseBody> recordsDto = mapper.toPresentation(legalEntities);

        assertEquals(legalEntities.getTotalElements(), recordsDto.getTotalNumberOfRecords().longValue());
        assertThat(recordsDto.getRecords(), contains(
            allOf(
                hasProperty("id", is(legalEntity01.getId())),
                hasProperty("externalId", is(legalEntity01.getExternalId())),
                hasProperty("name", is(legalEntity01.getName())),
                hasProperty("type", is(LegalEntityType.fromValue(legalEntity01.getType().name()))),
                hasProperty("additions", is(legalEntity01.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity02.getId())),
                hasProperty("externalId", is(legalEntity02.getExternalId())),
                hasProperty("name", is(legalEntity02.getName())),
                hasProperty("type", is(LegalEntityType.fromValue(legalEntity02.getType().name()))),
                hasProperty("additions", is(legalEntity02.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity03.getId())),
                hasProperty("externalId", is(legalEntity03.getExternalId())),
                hasProperty("name", is(legalEntity03.getName())),
                hasProperty("type", is(LegalEntityType.fromValue(legalEntity03.getType().name()))),
                hasProperty("additions", is(legalEntity03.getAdditions()))
            )
        ));
    }
}
