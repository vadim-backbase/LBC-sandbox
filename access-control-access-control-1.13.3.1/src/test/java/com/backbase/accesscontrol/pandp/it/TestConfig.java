package com.backbase.accesscontrol.pandp.it;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.repository.ApplicableFunctionPrivilegeJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupDetailsJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalDataGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextAssignFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.repository.AssignablePermissionSetJpaRepository;
import com.backbase.accesscontrol.repository.BusinessFunctionJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import com.backbase.accesscontrol.repository.ParticipantJpaRepository;
import com.backbase.accesscontrol.repository.PrivilegeJpaRepository;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserAssignedFunctionGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {Application.class})
@TestPropertySource(properties = {"backbase.communication.inbound=HTTP"})
public abstract class TestConfig {

    protected static final String TEST_SERVICE_TOKEN
        = "Bearer eyJraWQiOiJlNjJXTTRyamlOMUpcL0N3M0d3ZXBEbURNTklhWm9uOHJnW"
        + "kN0YXVLd1Y1TT0iLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJteS1zZXJ2aWNlIiwi"
        + "c2NvcGUiOlsiYXBpOnNlcnZpY2UiXSwiZXhwIjoyMTQ3NDgzNjQ3LCJpYXQiOjE0"
        + "ODQ4MjAxOTZ9.dOTROxf5p7fH4d00d4Ugl9HNY2zxpWpem38bn_J-ceg";

    protected MockMvc mockMvc;
    @Autowired
    protected PlatformTransactionManager transactionManager;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected DataGroupJpaRepository dataGroupJpaRepository;
    @Autowired
    protected DataGroupItemJpaRepository dataGroupItemJpaRepository;
    @Autowired
    protected ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    protected LegalEntityJpaRepository legalEntityJpaRepository;
    @Autowired
    protected ApprovalUserContextAssignFunctionGroupJpaRepository approvalUserContextAssignFunctionGroupJpaRepository;
    @Autowired
    protected ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    @Autowired
    protected BusinessFunctionJpaRepository businessFunctionJpaRepository;
    @Autowired
    protected PrivilegeJpaRepository privilegeJpaRepository;
    @Autowired
    protected ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository;
    @Autowired
    protected FunctionGroupJpaRepository functionGroupJpaRepository;
    @Autowired
    protected UserAssignedFunctionGroupJpaRepository userAssignedFunctionGroupJpaRepository;
    @Autowired
    protected UserAssignedCombinationRepository userAssignedCombinationRepository;
    @Autowired
    protected UserContextJpaRepository userContextJpaRepository;
    @Autowired
    protected ParticipantJpaRepository participantJpaRepository;
    @Autowired
    protected ApprovalDataGroupDetailsJpaRepository approvalDataGroupDetailsJpaRepository;
    @Autowired
    protected ApprovalDataGroupJpaRepository approvalDataGroupJpaRepository;
    @Autowired
    protected AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    @Autowired
    protected AssignablePermissionSetJpaRepository assignablePermissionSetJpaRepository;
    @Autowired
    protected BusinessFunctionCache businessFunctionCache;
    @Autowired
    protected ApprovalFunctionGroupJpaRepository approvalFunctionGroupJpaRepository;
    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment environment;

    @Autowired
    protected RepositoryCleaner repositoryCleaner;

    protected Privilege privView;
    protected Privilege privCreate;
    protected BusinessFunction bf1002;
    protected BusinessFunction bf1003;
    protected BusinessFunction bf1007;
    protected BusinessFunction bf1009;
    protected BusinessFunction bf1010;
    protected BusinessFunction bf1011;
    protected BusinessFunction bf1019;
    protected BusinessFunction bf1020;
    protected BusinessFunction bf1028;
    protected ApplicableFunctionPrivilege apfBf1002View;
    protected ApplicableFunctionPrivilege apfBf1003View;
    protected ApplicableFunctionPrivilege apfBf1003Edit;
    protected ApplicableFunctionPrivilege apfBf1007View;
    protected ApplicableFunctionPrivilege apfBf1007Create;
    protected ApplicableFunctionPrivilege apfBf1007Edit;
    protected ApplicableFunctionPrivilege apfBf1009View;
    protected ApplicableFunctionPrivilege apfBf1009Create;
    protected ApplicableFunctionPrivilege apfBf1009Edit;
    protected ApplicableFunctionPrivilege apfBf1010View;
    protected ApplicableFunctionPrivilege apfBf1011View;
    protected ApplicableFunctionPrivilege apfBf1011Create;
    protected ApplicableFunctionPrivilege apfBf1011Edit;
    protected ApplicableFunctionPrivilege apfBf1011Delete;
    protected ApplicableFunctionPrivilege apfBf1019View;
    protected ApplicableFunctionPrivilege apfBf1019Create;
    protected ApplicableFunctionPrivilege apfBf1019Edit;
    protected ApplicableFunctionPrivilege apfBf1019Delete;
    protected ApplicableFunctionPrivilege apfBf1019Approve;
    protected ApplicableFunctionPrivilege apfBf1020View;
    protected ApplicableFunctionPrivilege apfBf1020Create;
    protected ApplicableFunctionPrivilege apfBf1020Edit;
    protected ApplicableFunctionPrivilege apfBf1020Delete;
    protected ApplicableFunctionPrivilege apfBf1020Approve;
    protected ApplicableFunctionPrivilege apfBf1028View;
    protected ApplicableFunctionPrivilege apfBf1028Create;
    protected ApplicableFunctionPrivilege apfBf1028Edit;
    protected ApplicableFunctionPrivilege apfBf1028Delete;
    protected ApplicableFunctionPrivilege apfBf1028Approve;
    protected AssignablePermissionSet apsAdmin1;
    protected AssignablePermissionSet apsAdmin2;
    protected AssignablePermissionSet apsUser1;
    protected AssignablePermissionSet apsUser2;
    protected AssignablePermissionSet randomAps;
    protected FunctionGroup systemFG;
    protected FunctionGroup defaultFG1;
    protected FunctionGroup defaultFg2;
    protected LegalEntity legalEntity;
    protected Set<FunctionGroup> functionGroups;
    protected ServiceAgreement serviceAgreement;
    protected AssignablePermissionSet assignablePermissionSetRegular;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUpRestTemplate() {

        if (Arrays.asList(this.environment.getActiveProfiles()).contains("h2")) {
            h2Init();
        }

        tearDown();
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build();
        setUpInitialData();
    }

    private void setUpInitialData() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            // you should put all business functions, privileges and applicable function privileges here

            apfBf1002View = businessFunctionCache.getByFunctionIdAndPrivilege("1002", "view");
            bf1002 = apfBf1002View.getBusinessFunction();
            privView = apfBf1002View.getPrivilege();
            apfBf1003View = businessFunctionCache.getByFunctionIdAndPrivilege("1003", "view");
            apfBf1003Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1003", "edit");
            bf1003 = apfBf1003View.getBusinessFunction();

            apfBf1007View = businessFunctionCache.getByFunctionIdAndPrivilege("1007", "view");
            apfBf1007Create = businessFunctionCache.getByFunctionIdAndPrivilege("1007", "create");
            apfBf1007Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1007", "edit");
            bf1007 = apfBf1007Create.getBusinessFunction();
            privCreate = apfBf1007Create.getPrivilege();

            apfBf1009View = businessFunctionCache.getByFunctionIdAndPrivilege("1009", "view");
            apfBf1009Create = businessFunctionCache.getByFunctionIdAndPrivilege("1009", "create");
            apfBf1009Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1009", "edit");
            bf1009 = apfBf1009Create.getBusinessFunction();

            apfBf1010View = businessFunctionCache.getByFunctionIdAndPrivilege("1010", "view");
            bf1010 = apfBf1010View.getBusinessFunction();

            apfBf1011View = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "view");
            apfBf1011Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "edit");
            apfBf1011Create = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "create");
            apfBf1011Delete = businessFunctionCache.getByFunctionIdAndPrivilege("1011", "delete");
            bf1011 = apfBf1011View.getBusinessFunction();

            apfBf1019View = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "view");
            apfBf1019Create = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "create");
            apfBf1019Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "edit");
            apfBf1019Delete = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "delete");
            apfBf1019Approve = businessFunctionCache.getByFunctionIdAndPrivilege("1019", "approve");
            bf1019 = apfBf1019View.getBusinessFunction();

            apfBf1020View = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view");
            apfBf1020Create = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "create");
            apfBf1020Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "edit");
            apfBf1020Delete = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "delete");
            apfBf1020Approve = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "approve");
            bf1020 = apfBf1020View.getBusinessFunction();

            apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
            apfBf1028Create = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "create");
            apfBf1028Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "edit");
            apfBf1028Delete = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "delete");
            apfBf1028Approve = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "approve");
            bf1028 = apfBf1028View.getBusinessFunction();

            AssignablePermissionSet assignablePermissionSetAdmin = assignablePermissionSetJpaRepository
                .findFirstByType(AssignablePermissionType.ADMIN_USER_DEFAULT.getValue()).get();

            assignablePermissionSetRegular = assignablePermissionSetJpaRepository
                .findFirstByType(AssignablePermissionType.REGULAR_USER_DEFAULT.getValue()).get();
            AssignablePermissionSet assignablePermissionSet3 = new AssignablePermissionSet();
            assignablePermissionSet3.setName("Invalid");
            assignablePermissionSet3.setType(AssignablePermissionType.CUSTOM);
            assignablePermissionSet3.setDescription("desc");

            Set<String> privs3 = new HashSet<>();
            privs3.add(apfBf1028View.getId());
            privs3.add(apfBf1028Create.getId());
            assignablePermissionSet3.setPermissions(privs3);

            assignablePermissionSetJpaRepository.save(assignablePermissionSet3);

            return true;
        });

    }


    protected AssignablePermissionSet createAssignablePermissionSet(
        String name,
        AssignablePermissionType assignablePermissionType,
        String description,
        String... afpIds) {

        AssignablePermissionSet assignablePermissionSet = new AssignablePermissionSet();
        assignablePermissionSet.setName(name);
        assignablePermissionSet.setType(assignablePermissionType);
        assignablePermissionSet.setDescription(description);

        assignablePermissionSet.setPermissions(new HashSet<>(Arrays.asList(afpIds)));

        return assignablePermissionSet;
    }

    public void tearDown() {
        repositoryCleaner.clean();
    }


    public String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    private void h2Init() {

        String dropTableServiceAgreementAps = "drop table if exists service_agreement_aps";
        String createTableServiceAgreementAps = "create table service_agreement_aps (\n"
            + "       assignable_permission_set_id bigint not null,\n"
            + "        type integer not null,\n"
            + "        service_agreement_id varchar(255) not null,\n"
            + "        primary key (service_agreement_id, assignable_permission_set_id, type)\n"
            + "    )";

        String dropTableExistsSequenceTable = "drop table if exists sequence_table";
        String createTableExistsSequenceTable = "create table sequence_table  (\n"
            + "  sequence_name VARCHAR(90) NOT NULL,\n"
            + "  next_val      BIGINT);";

        String dropTableAssignablePermissionSetItem = "drop table if exists assignable_permission_set_item";
        String createTableAssignablePermissionSetItem = "CREATE TABLE assignable_permission_set_item\n"
            + "            (\n"
            + "                assignable_permission_set_id        BIGINT        NOT NULL,\n"
            + "                function_privilege_id               VARCHAR(36)   NOT NULL,\n"
            + "                PRIMARY KEY (assignable_permission_set_id, function_privilege_id)\n"
            + "            );";

        executeSqlCommands(dropTableServiceAgreementAps, createTableServiceAgreementAps);
        executeSqlCommands(dropTableExistsSequenceTable, createTableExistsSequenceTable);
        executeSqlCommands(dropTableAssignablePermissionSetItem, createTableAssignablePermissionSetItem);
    }

    private void executeSqlCommands(String dropTableUserAps, String createTableUserAps) {
        try (Connection con = dataSource.getConnection();
            Statement stmt = con.createStatement()) {
            stmt.execute(dropTableUserAps);
            stmt.execute(createTableUserAps);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FunctionGroup createFunctionGroup(String name, String description,
        ServiceAgreement serviceAgreement, List<ApplicableFunctionPrivilege> applicableFunctionPrivileges) {
        return functionGroupJpaRepository.save(
            FunctionGroupUtil
                .getFunctionGroup(null, name, description, new HashSet<>(), FunctionGroupType.DEFAULT,
                    serviceAgreement)
        );
    }

}
