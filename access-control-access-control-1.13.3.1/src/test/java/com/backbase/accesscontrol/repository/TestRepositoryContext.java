package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.Application;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public abstract class TestRepositoryContext {

    @Autowired
    protected RepositoryCleaner repositoryCleaner;

    @Before
    public void cleanDB() {
        
        repositoryCleaner.clean();
    }
}
