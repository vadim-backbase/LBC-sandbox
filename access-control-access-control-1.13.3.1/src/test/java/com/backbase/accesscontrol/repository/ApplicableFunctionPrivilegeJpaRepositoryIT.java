package com.backbase.accesscontrol.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ApplicableFunctionPrivilegeJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @Autowired
    private BusinessFunctionCache businessFunctionCache;


    @Autowired
    private EntityManager entityManager;


    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void shouldPersistAndFind() {

        ApplicableFunctionPrivilege applicableFunctionPrivilege = businessFunctionCache
            .getByFunctionIdAndPrivilege("1011", "view");

        ApplicableFunctionPrivilege applicableFunctionPrivilegeList = applicableFunctionPrivilegeJpaRepository
            .findById(applicableFunctionPrivilege.getId()).orElse(null);

        assertThat(applicableFunctionPrivilegeList, is(applicableFunctionPrivilege));


    }
}