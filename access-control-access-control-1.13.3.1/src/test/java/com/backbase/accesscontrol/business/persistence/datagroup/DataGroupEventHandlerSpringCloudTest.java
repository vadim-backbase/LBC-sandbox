package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.domain.*;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.repository.*;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.handler.EventHandler;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.*;
import com.google.common.collect.Sets;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataGroupEventHandlerSpringCloudTest.TestConfig.class})
@ContextConfiguration
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=" +
                "org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration")
@DirtiesContext
public class DataGroupEventHandlerSpringCloudTest {

    @Autowired
    private EventBus eventBus;

    @Autowired
    protected RepositoryCleaner repositoryCleaner;

    @Autowired
    private UserContextJpaRepository userContextJpaRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @Autowired
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;

    @Autowired
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @Autowired
    private BusinessFunctionCache businessFunctionCache;

    private UserContext userContext;

    private LegalEntity legalEntity;

    private FunctionGroup savedFunctionGroup;

    private DataGroup dataGroup;

    private ServiceAgreement serviceAgreement;

    private ApplicableFunctionPrivilege viewSa;

    private ApplicableFunctionPrivilege createSa;

    private ApplicableFunctionPrivilege viewPs;

    private RetryPolicy assertionFailedRetryPolicy = new RetryPolicy()
            .retryOn(AssertionError.class)
            .withMaxRetries(30)
            .withDelay(100, TimeUnit.MILLISECONDS);

    private static List<UserContextEvent> userContextEvents = new ArrayList<>();

    @Before
    public void setUp() {
        repositoryCleaner.clean();

        legalEntity = legalEntityJpaRepository
                .save(createLegalEntity(null, "le-ex-id", "le-name", null, LegalEntityType.BANK));

        // create SA
        serviceAgreement =
                createServiceAgreement("BB between self 1", "id.external.1", "desc", legalEntity, legalEntity.getId(),
                        legalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        viewSa = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        createSa = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "create");
        viewPs = businessFunctionCache.getByFunctionIdAndPrivilege("1006", "view");

        //save function group
        GroupedFunctionPrivilege viewSaWithLimit = getGroupedFunctionPrivilege(null, viewSa, null);
        GroupedFunctionPrivilege createSaWitLimit = getGroupedFunctionPrivilege(null, createSa, null);
        GroupedFunctionPrivilege viewPsWithLimit = getGroupedFunctionPrivilege(null, viewPs, null);
        savedFunctionGroup = functionGroupJpaRepository.save(
                getFunctionGroup(null, "function-group-name", "function-group-description",
                        getGroupedFunctionPrivileges(
                                viewSaWithLimit,
                                createSaWitLimit,
                                viewPsWithLimit
                        ),
                        FunctionGroupType.DEFAULT, serviceAgreement)
        );
        functionGroupJpaRepository.flush();

        // create data group
        dataGroup = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc",
                serviceAgreement);

        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
        dataGroupJpaRepository.save(dataGroup);
        dataGroupJpaRepository.flush();

        userContext = new UserContext("u1", serviceAgreement.getId());
        userContextJpaRepository.save(userContext);

        UserAssignedFunctionGroup userAssignedFunctionGroup =
                new UserAssignedFunctionGroup(savedFunctionGroup, userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);


        UserAssignedFunctionGroupCombination userAssignedFunctionGroupDataGroup
                = new UserAssignedFunctionGroupCombination(
                Sets.newHashSet(dataGroup.getId()),
                userAssignedFunctionGroup
        );
        userAssignedCombinationRepository.save(userAssignedFunctionGroupDataGroup);
    }

    @Test
    public void successHandleDataGroupEvent() {
        EnvelopedEvent<DataGroupEvent> dataGroupEnvelop = new EnvelopedEvent<>();
        DataGroupEvent dataGroupEvent = new DataGroupEvent().withAction(Action.UPDATE).withId(dataGroup.getId());
        dataGroupEnvelop.setEvent(dataGroupEvent);

        eventBus.emitEvent(dataGroupEnvelop);

        Failsafe.with(assertionFailedRetryPolicy).run(() -> {
            List<String> userIds = userContextEvents
                    .stream().map(userEvent -> userEvent.getUserId())
                    .collect(Collectors.toList());
            assertThat(userIds.size()).isEqualTo(1);
            assertThat(userIds.get(0)).isEqualTo("u1");
        });
    }

    @Configuration
    static class TestConfig {

        @Bean
        public UserContextEventHandler userContextEventHandler() {
            return new UserContextEventHandler();
        }
    }

    static class UserContextEventHandler implements EventHandler<UserContextEvent> {

        @Override
        public void handle(EnvelopedEvent<UserContextEvent> envelopedEvent) {
            UserContextEvent userContextEvent = envelopedEvent.getEvent();
            userContextEvents.add(userContextEvent);
        }
    }
}
