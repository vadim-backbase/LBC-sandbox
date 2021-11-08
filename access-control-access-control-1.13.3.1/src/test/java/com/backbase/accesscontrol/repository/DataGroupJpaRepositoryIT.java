package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.Application;

import com.backbase.accesscontrol.domain.*;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class DataGroupJpaRepositoryIT {

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Autowired
    private DataGroupItemJpaRepository dataGroupItemJpaRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    private PersistenceUtil jpaUtil;

    @PersistenceContext
    private EntityManager entityManager;


    private ServiceAgreement serviceAgreement1;
    private ServiceAgreement serviceAgreement2;
    private DataGroup dataGroup1ServiceAgreement1;
    private DataGroup dataGroup2ServiceAgreement1;
    private DataGroup dataGroup4ServiceAgreement1;
    private DataGroup dataGroup2ServiceAgreement2;
    private String dataGroup1ServiceAgreement1Name;
    private String dataGroup2ServiceAgreement1Name;
    private String dataGroup4ServiceAgreement1Name;
    private String dataGroup2ServiceAgreement2Name;

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        jpaUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        LegalEntity legalEntity1 = createLegalEntity(null, "le1name", "legalEntity1", null, LegalEntityType.BANK);
        LegalEntity legalEntity2 = createLegalEntity(null, "le2name", "legalEntity2", null, LegalEntityType.BANK);

        legalEntityJpaRepository.save(legalEntity1);
        legalEntityJpaRepository.save(legalEntity2);
        legalEntityJpaRepository.flush();

        serviceAgreement1 = createServiceAgreement("sa1", "exid1", "desc1", legalEntity1, null, null);
        serviceAgreement2 = createServiceAgreement("sa2", "exid2", "desc2", legalEntity2, null, null);

        serviceAgreementJpaRepository.save(serviceAgreement1);
        serviceAgreementJpaRepository.save(serviceAgreement2);

        dataGroup1ServiceAgreement1Name = "dataGroup1ServiceAgreement1";
        dataGroup2ServiceAgreement1Name = "dataGroup2ServiceAgreement1";
        dataGroup4ServiceAgreement1Name = "dataGroup4ServiceAgreement1";
        dataGroup2ServiceAgreement2Name = "dataGroup2ServiceAgreement1";
        dataGroup1ServiceAgreement1 = createDataGroup(dataGroup1ServiceAgreement1Name, "ARRANGEMENTS",
            "arrangment1",
            serviceAgreement1);
        dataGroup2ServiceAgreement1 = createDataGroup(dataGroup2ServiceAgreement1Name, "CONTACTS", "arrangment2",
            serviceAgreement1);
        dataGroup4ServiceAgreement1 = createDataGroup(dataGroup4ServiceAgreement1Name, "CONTACTS", "arrangment4",
            serviceAgreement1);
        dataGroup2ServiceAgreement2 = createDataGroup(dataGroup2ServiceAgreement2Name, "CONTACTS", "arrangment2",
            serviceAgreement2);

        dataGroup1ServiceAgreement1.setDataItemIds(Collections.singleton("item1"));
        dataGroup2ServiceAgreement2.setDataItemIds(Sets.newHashSet("item3", "item2"));

        dataGroupJpaRepository.save(this.dataGroup1ServiceAgreement1);
        dataGroupJpaRepository.save(this.dataGroup2ServiceAgreement1);
        dataGroupJpaRepository.save(dataGroup2ServiceAgreement2);
        dataGroupJpaRepository.save(this.dataGroup4ServiceAgreement1);
    }

    @Test
    public void shouldFindByIdWithNamedEntityGraph() {
        String itemId = "ARR - 01";
        String type = "ARRANGEMENTS";

        LegalEntity legalEntity = createLegalEntity(null, "Backbase", "EX1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement3 = createServiceAgreement("sa3", "exid3", "desc3", legalEntity, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setName("Data group 1");
        dataGroup.setDescription("Data group desc");
        dataGroup.setDataItemType(type);
        dataGroup.setDataItemIds(Collections.singleton(itemId));
        dataGroup.setServiceAgreement(serviceAgreement3);

        dataGroupJpaRepository.save(dataGroup);

        Optional<DataGroup> returnedById = dataGroupJpaRepository.findById(dataGroup.getId(), DATA_GROUP_EXTENDED);

        assertTrue(returnedById.isPresent());
        assertEquals(dataGroup.getName(), returnedById.get().getName());
        assertEquals(dataGroup.getDescription(), returnedById.get().getDescription());
        assertEquals(dataGroup.getId(), returnedById.get().getId());
        assertEquals(itemId, dataGroup.getDataItemIds().iterator().next());
        assertEquals(type, returnedById.get().getDataItemType());
    }

    @Test
    public void shouldFindByIdWithoutNamedEntityGraph() {
        String itemId = "ARR - 01";
        String type = "ARRANGEMENTS";

        DataGroup dataGroup = new DataGroup();
        dataGroup.setName("Data group 1");
        dataGroup.setDescription("Data group desc");
        dataGroup.setDataItemType(type);
        dataGroup.setServiceAgreement(serviceAgreement1);
        dataGroup.setDataItemIds(Collections.singleton(itemId));

        dataGroupJpaRepository.save(dataGroup);

        Optional<DataGroup> returnedById = dataGroupJpaRepository.findById(dataGroup.getId(), null);

        assertFalse(jpaUtil.isLoaded(returnedById.get(), "dataGroupItems"));
        assertFalse(jpaUtil.isLoaded(returnedById.get(), "serviceAgreement"));

    }

    @Test
    public void shouldGetAllDataGroupsFromAListOfId() {

        LegalEntity legalEntity3 = createLegalEntity(null, "le3name", "legalEntity3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity3);

        ServiceAgreement serviceAgreement3 = createServiceAgreement("sa3", "exid3", "desc3", legalEntity3, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        DataGroup dataGroup1 = createDataGroup("dataGroup5", "ARRANGEMENTS", "arrangment5", serviceAgreement3);
        dataGroup1.setDataItemIds(Collections.singleton("ITEM 1"));
        DataGroup dataGroup2 = createDataGroup("dataGroup6", "CONTACTS", "arrangment6", serviceAgreement3);
        DataGroup dataGroup3 = createDataGroup("dataGroup7", "CONTACTS", "arrangment7", serviceAgreement3);
        dataGroup3.setDataItemIds(Collections.singleton("ITEM 2"));
        DataGroup dataGroup4 = createDataGroup("dataGroup8", "CONTACTS", "arrangment8", serviceAgreement3);

        dataGroupJpaRepository.save(dataGroup1);
        dataGroupJpaRepository.save(dataGroup2);
        dataGroupJpaRepository.save(dataGroup3);
        dataGroupJpaRepository.save(dataGroup4);


        List<String> ids = new ArrayList<>();
        ids.add(dataGroup1.getId());
        ids.add(dataGroup3.getId());

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findAllDataGroupsWithIdsIn(ids, GraphConstants.DATA_GROUP_EXTENDED);

        assertEquals(2, dataGroups.size());
        assertArray(dataGroups, dataGroup1);
        assertArray(dataGroups, dataGroup3);
    }


    @Test
    public void shouldGetAllDataGroupsFromAListOfExternalServiceAgreementIds() {

        LegalEntity legalEntity3 = createLegalEntity(null, "le3name", "legalEntity3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity3);

        ServiceAgreement serviceAgreement3 = createServiceAgreement("sa3", "exid3", "desc3", legalEntity3, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        DataGroup dataGroup1 = createDataGroup("dataGroup5", "ARRANGEMENTS", "arrangment5", serviceAgreement3);
        dataGroup1.setDataItemIds(Collections.singleton("ITEM 1"));
        DataGroup dataGroup2 = createDataGroup("dataGroup6", "CONTACTS", "arrangment6", serviceAgreement3);
        DataGroup dataGroup3 = createDataGroup("dataGroup7", "CONTACTS", "arrangment7", serviceAgreement3);
        dataGroup3.setDataItemIds(Collections.singleton("ITEM 2"));
        DataGroup dataGroup4 = createDataGroup("dataGroup8", "CONTACTS", "arrangment8", serviceAgreement3);

        dataGroupJpaRepository.save(dataGroup1);
        dataGroupJpaRepository.save(dataGroup2);
        dataGroupJpaRepository.save(dataGroup3);
        dataGroupJpaRepository.save(dataGroup4);

        Set<String> ids = new HashSet<>();
        ids.add(serviceAgreement1.getExternalId());
        ids.add(serviceAgreement2.getExternalId());
        ids.add(serviceAgreement3.getExternalId());

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findAllDataGroupsWithExternalServiceAgreementIdsIn(ids, GraphConstants.DATA_GROUP_EXTENDED);

        assertEquals(8, dataGroups.size());
        for (DataGroup dataGroup : dataGroups) {
            assertFalse(dataGroup.getServiceAgreement().getExternalId().isEmpty());
        }
    }

    private void assertArray(List<DataGroup> dataGroups, DataGroup dataGroup11) {
        Optional<DataGroup> dataGroupToAssert = dataGroups.stream()
            .filter(dataGroup -> dataGroup.getId().equals(dataGroup11.getId())).findFirst();
        assertTrue(dataGroupToAssert.isPresent());
        assertEquals(dataGroup11.getName(), dataGroupToAssert.get().getName());
        assertEquals(dataGroup11.getDescription(), dataGroupToAssert.get().getDescription());
        assertFalse(dataGroupToAssert.get().getDataItemIds().isEmpty());
        assertNotNull(dataGroupToAssert.get().getDataItemIds().iterator().next());
    }

    @Test
    public void shouldSaveDataGroup() {
        String itemId = "ARR - 01";
        String type = "ARRANGEMENTS";

        LegalEntity legalEntity = createLegalEntity(null, "Backbase", "EX1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setName("Data group 1");
        dataGroup.setDescription("Data group desc");
        dataGroup.setServiceAgreement(serviceAgreement1);
        dataGroup.setServiceAgreementId(serviceAgreement1.getId());
        dataGroup.setDataItemType(type);
        dataGroup.setDataItemIds(Collections.singleton(itemId));

        DataGroup returnedDataGroup = dataGroupJpaRepository.save(dataGroup);

        assertEquals(dataGroup.getName(), returnedDataGroup.getName());
        assertEquals(dataGroup.getDescription(), returnedDataGroup.getDescription());
        assertEquals(dataGroup.getId(), returnedDataGroup.getId());
        String dataItem = returnedDataGroup.getDataItemIds().iterator().next();
        assertEquals(itemId, dataItem);
        assertEquals(type, dataGroup.getDataItemType());
    }


    @Test
    public void shouldDeleteDataGroup() {
        String itemId = "ARR - 01";
        String type = "ARRANGEMENTS";

        LegalEntity legalEntity = createLegalEntity(null, "Backbase", "EX1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        ServiceAgreement serviceAgreement3 = createServiceAgreement("sa3", "exid3", "desc3", legalEntity, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement3);

        DataGroup dataGroup = new DataGroup();
        dataGroup.setName("Data group 1");
        dataGroup.setDescription("Data group desc");
        dataGroup.setDataItemType(type);
        dataGroup.setDataItemIds(Collections.singleton(itemId));
        dataGroup.setServiceAgreement(serviceAgreement3);

        dataGroup = dataGroupJpaRepository.save(dataGroup);

        dataGroupJpaRepository.delete(dataGroup);

        Optional<DataGroup> returnedById = dataGroupJpaRepository.findById(dataGroup.getId(), DATA_GROUP_EXTENDED);
        List<DataGroupItem> dataGroupItems = dataGroupItemJpaRepository.findAllByDataGroupId(dataGroup.getId());

        assertFalse(returnedById.isPresent());
        assertTrue(dataGroupItems.isEmpty());
    }

    @Test
    public void shouldFindDistinctByNameAndServiceAgreementId() {
        List<DataGroup> dataGroupList = dataGroupJpaRepository.findDistinctByNameAndServiceAgreementId(
            "dataGroup1ServiceAgreement1",
            serviceAgreement1.getId()
        );
        assertEquals(1, dataGroupList.size());
        assertEquals("dataGroup1ServiceAgreement1", dataGroupList.get(0).getName());
        assertEquals(serviceAgreement1.getId(), dataGroupList.get(0).getServiceAgreement().getId());
    }

    @Test
    public void shouldFindDistinctByServiceAgreementIdAndDataItemType() {
        List<DataGroup> dataGroupList = dataGroupJpaRepository.findByServiceAgreementIdAndDataItemType(
            serviceAgreement1.getId(),
            "ARRANGEMENTS", DATA_GROUP_EXTENDED
        );

        assertEquals(1, dataGroupList.size());
        assertEquals("dataGroup1ServiceAgreement1", dataGroupList.get(0).getName());
        assertEquals(serviceAgreement1.getId(), dataGroupList.get(0).getServiceAgreement().getId());
        assertEquals("arrangment1", dataGroupList.get(0).getDescription());
    }

    @Test
    public void shouldFindDistinctByServiceAgreementId() {
        List<DataGroup> dataGroupList = dataGroupJpaRepository.findByServiceAgreementId(
            serviceAgreement1.getId(), DATA_GROUP_EXTENDED
        );
        assertEquals(3, dataGroupList.size());
    }

    @Test
    public void shouldFindDistinctByNameAndServiceAgreementIdAndIdNot() {
        List<DataGroup> dataGroupList = dataGroupJpaRepository.findDistinctByNameAndServiceAgreementIdAndIdNot(
            dataGroup1ServiceAgreement1.getName(),
            dataGroup1ServiceAgreement1.getServiceAgreement().getId(),
            dataGroup2ServiceAgreement1.getId()
        );
        assertNotNull(dataGroupList);
        DataGroup dataGroup = dataGroupList.get(0);
        assertEquals(dataGroup1ServiceAgreement1.getId(), dataGroup.getId());
    }

    @Test
    public void shouldFindDataGroupsByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn() {
        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(serviceAgreement1.getId(),
                asList(dataGroup1ServiceAgreement1.getName(), dataGroup2ServiceAgreement1.getName()),
                asList(dataGroup2ServiceAgreement2.getId(), dataGroup4ServiceAgreement1.getId()));

        assertEquals(3, dataGroups.size());
        assertTrue(dataGroups.containsAll(
            asList(dataGroup1ServiceAgreement1, dataGroup2ServiceAgreement1, dataGroup4ServiceAgreement1)));
        assertFalse(dataGroups.contains(Collections.singletonList(dataGroup2ServiceAgreement2)));
    }

    @Test
    public void shouldFindDataGroupsByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdInWithNonExistingNameAndNonExistingId() {
        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(serviceAgreement1.getId(),
                asList(dataGroup1ServiceAgreement1.getName(), dataGroup2ServiceAgreement1.getName(),
                    "Non-existing-name"),
                asList(dataGroup2ServiceAgreement2.getId(), dataGroup4ServiceAgreement1.getId(), "Non-existing-id"));

        assertEquals(3, dataGroups.size());
        assertTrue(dataGroups.containsAll(
            asList(dataGroup1ServiceAgreement1, dataGroup2ServiceAgreement1, dataGroup4ServiceAgreement1)));
        assertFalse(dataGroups.contains(Collections.singletonList(dataGroup2ServiceAgreement2)));
    }

    @Test
    public void shouldFindDataGroupsByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdInWithDuplicateNameAndDuplicateId() {
        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(serviceAgreement1.getId(),
                asList(dataGroup1ServiceAgreement1.getName(), dataGroup2ServiceAgreement1.getName(),
                    "Non-existing-name"),
                asList(dataGroup2ServiceAgreement2.getId(), dataGroup4ServiceAgreement1.getId(), "Non-existing-id"));

        assertEquals(3, dataGroups.size());
        assertTrue(dataGroups.containsAll(
            asList(dataGroup1ServiceAgreement1, dataGroup2ServiceAgreement1, dataGroup4ServiceAgreement1)));
        assertFalse(dataGroups.contains(Collections.singletonList(dataGroup2ServiceAgreement2)));
    }

    @Test
    public void shouldFindDataGroupsByLegalEntityExternalId() {
        LegalEntity legalEntity = createLegalEntity(null, "EX1", "Backbase", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        LegalEntity otherLegalEntity = createLegalEntity(null, "EX2", "Backbase1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(otherLegalEntity);
        ServiceAgreement serviceAgreementWithParticipant = new ServiceAgreement()
                .withName("name3")
                .withDescription("description")
                .withCreatorLegalEntity(otherLegalEntity)
                .withExternalId("ext-id")
                .withMaster(true);
        Participant participant = new Participant()
                .withShareUsers(false)
                .withShareAccounts(true)
                .withLegalEntity(legalEntity);
        participant.addAdmin("user-id");
        serviceAgreementWithParticipant.addParticipant(participant);

        serviceAgreementJpaRepository.save(serviceAgreementWithParticipant);

        ServiceAgreement otherServiceAgreementWithParticipant = new ServiceAgreement()
                .withName("name4")
                .withDescription("description")
                .withCreatorLegalEntity(otherLegalEntity)
                .withExternalId("ext-id2")
                .withMaster(false);
        Participant otherParticipant = new Participant()
                .withShareUsers(true)
                .withShareAccounts(false)
                .withLegalEntity(legalEntity);
        otherParticipant.addAdmin("user-id2");
        otherServiceAgreementWithParticipant.addParticipant(otherParticipant);

        serviceAgreementJpaRepository.save(otherServiceAgreementWithParticipant);

        DataGroup otherdataGroupServiceAgreement = createDataGroup("otherDataGroupServiceAgreement",
                "ARRANGEMENTS",
                "arrangment10",
                serviceAgreementWithParticipant);
        dataGroupJpaRepository.save(otherdataGroupServiceAgreement);

        DataGroup otherUsersdataGroupServiceAgreement = createDataGroup("otherUsersDataGroupServiceAgreement",
                "ARRANGEMENTS",
                "arrangment20",
                otherServiceAgreementWithParticipant);
        dataGroupJpaRepository.save(otherUsersdataGroupServiceAgreement);

        List<DataGroup> dataGroups = dataGroupJpaRepository
                .findAllDataGroupsByServiceAgreementAndDataItem("ARRANGEMENTS", null,
                        null, null, null, "Backbase",
                    SharesEnum.ACCOUNTS);

        assertEquals(1, dataGroups.size());
        assertEquals("name3", dataGroups.get(0).getServiceAgreement().getName());
    }

    @Test
    public void shouldFindDataGroupsByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdInWithEmptyIdsList() {
        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(serviceAgreement1.getId(),
                asList(dataGroup1ServiceAgreement1.getName(), dataGroup2ServiceAgreement1.getName(),
                    "Non-existing-name"),
                new ArrayList<>());

        assertEquals(2, dataGroups.size());
        assertTrue(dataGroups.containsAll(asList(dataGroup1ServiceAgreement1, dataGroup2ServiceAgreement1)));
    }

    @Test
    public void shouldFindDataGroupsByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdInWithIdAndNameForTheSameDataGroup() {
        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(serviceAgreement1.getId(),
                Collections.singletonList(dataGroup1ServiceAgreement1.getName()),
                Collections.singletonList(dataGroup1ServiceAgreement1.getId()));

        assertEquals(1, dataGroups.size());
        assertEquals(dataGroup1ServiceAgreement1.getId(), dataGroups.get(0).getId());
        assertEquals(dataGroup1ServiceAgreement1.getName(), dataGroups.get(0).getName());
    }

    @Test
    public void shouldDataGroupSearchByServiceAgrementName() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("ARRANGEMENTS", null,
                "sa1", null, null, null,  SharesEnum.ACCOUNTS);

        assertEquals(1, result.size());
        assertEquals(dataGroup1ServiceAgreement1.getId(), result.get(0).getId());
    }

    @Test
    public void shouldDataGroupSearchByServiceAgrementExternalId() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("CONTACTS",
                null, null, "exid2", null, null,  SharesEnum.ACCOUNTS);

        assertEquals(1, result.size());
        assertEquals(dataGroup2ServiceAgreement2.getId(), result.get(0).getId());
    }


    @Test
    public void shouldDataGroupSearchByServiceAgrementId() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("CONTACTS", serviceAgreement2.getId(),
                null, null, null, null,  SharesEnum.ACCOUNTS);

        assertEquals(1, result.size());
        assertEquals(dataGroup2ServiceAgreement2.getId(), result.get(0).getId());
    }

    @Test
    public void shouldDataGroupSearchByDataItem() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("ARRANGEMENTS", null,
                null, null, "item1", null,  SharesEnum.ACCOUNTS);

        assertEquals(1, result.size());
        assertEquals(dataGroup1ServiceAgreement1.getId(), result.get(0).getId());
    }

    @Test
    public void shouldDataGroupSearchByServiceAgreementAndDataItem() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("ARRANGEMENTS", serviceAgreement1.getId(),
                null, null, "item1", null,  SharesEnum.ACCOUNTS);

        assertEquals(1, result.size());
        assertEquals(dataGroup1ServiceAgreement1.getId(), result.get(0).getId());
    }

    @Test
    public void shouldDataGroupSearchReturnEmptyList() {

        List<DataGroup> result = dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem("ARRANGEMENTS", serviceAgreement1.getId(),
                null, null, "item11", null,  SharesEnum.ACCOUNTS);

        assertEquals(0, result.size());
    }
}
