package com.backbase.accesscontrol.util.helpers;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepositoryCleaner {

    private static final Logger log = LoggerFactory.getLogger(RepositoryCleaner.class);

    private static List<String> commandList = new ArrayList<>();

    @Autowired
    private DataSource dataSource;
    @Value("${backbase.db.cleanup}")
    private String truncateScript;

    @PostConstruct
    public void init()
    {
        if (commandList.isEmpty()) {
            File file = new File("src/test/resources/"+truncateScript);
            try (Stream<String> stream = Files.lines(Paths.get(file.toPath().toUri()))) {
                stream.forEach(commandList::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clean() {
        log.info("Cleaning tables:" + truncateScript);

        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();) {
            for (String line : commandList) {
                stmt.addBatch(line);
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            System.err.println("Possible order error continue with order cleaner :" + e.getMessage());
            cleanWithOrder();
        }
    }


    private void cleanWithOrder(){

        List<String> list = new ArrayList<>(commandList);
        List<String> done = new ArrayList<>();
        int count = -1;
        int retry = 0;
        while  (count != list.size()){
            count = list.size();
            for(String command: list) {
                try {
                    executeOneCommand(command);
                    done.add(command);
                    retry=0;
                } catch (SQLException e) {
                    System.err.println(command+" "+e.getMessage());
                }
            }
            list.removeAll(done);
            if (retry<3 && count == list.size() && list.size()>0){
                retry++;
                count = -1;
            }
        }

        System.err.println("Not removed");
        for(String command: list){
            System.err.println(command);
        }

        System.err.println("Final list");
        for(String command: done){
            System.err.println(command);
        }
        if (list.isEmpty()) {
            commandList = done;
        }
        fail("Update list");

    }

    private void executeOneCommand(String command) throws SQLException {
        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();) {
            stmt.addBatch(command);
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

}
