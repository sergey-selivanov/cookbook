package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ErrorCode;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.ValidateOutput;
import org.flywaydb.core.api.output.ValidateResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sergeys.cookbook.logic.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DbTest {

    static String dataDir;

    static Logger log;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {


        dataDir = Settings.getDataDirPath();

        log = LoggerFactory.getLogger(DbTest.class);

    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }



    @Test
    void testFlyway() {

        //String dir = "i:/tmp";
        //System.setProperty("log4j.log.file", dataDir + "/test-log.txt");

        String url = String.format("jdbc:h2:%s/%s;TRACE_LEVEL_FILE=4", dataDir, "cookbook").replace('\\', '/');
        log.info(url);

        try {
            //Connection connection = DriverManager.getConnection(url, "sa", "sa");

            // TODO use DataSource?
            FluentConfiguration config = Flyway.configure()
                    .dataSource(url, "sa", "sa")
                    .installedBy("junit test")
                    //.locations("filesystem:/D:/git/cookbook/src/main/resources/db/migration")
                    //.locations("classpath:db/migration")

                    ;

            Flyway flyway = config.load();

// TODO with classpath location, fails under eclipse when project is modular
// FlywayException: Unable to obtain inputstream for resource: db/migration/V2__createdb.sql

            ValidateResult validateResult = flyway.validateWithResult();
            if(!validateResult.validationSuccessful) {
                log.error(validateResult.errorDetails.errorMessage);

                if(!validateResult.invalidMigrations.isEmpty()) {
                    ValidateOutput validateOutput = validateResult.invalidMigrations.get(0);
                    if(validateOutput.version.equals("1.0.0")
                            && validateOutput.errorDetails.errorCode == ErrorCode.RESOLVED_VERSIONED_MIGRATION_NOT_APPLIED) {

                        log.debug("first migration was not applied");


                        Connection connection = DriverManager.getConnection(url, "sa", "sa");
                        Statement st = connection.createStatement();
                        ResultSet rs = st.executeQuery("show tables");

                        Set<String> tables = new HashSet<>();

                        while(rs.next()){
                            log.debug(rs.getString("table_name"));
                            tables.add(rs.getString("table_name"));
                        }
                        st.close();
                        connection.close();

                        if(tables.isEmpty()) {
                            log.info("schema is empty, migrate");
                            MigrateResult result = flyway.migrate(); // will throw on error
                        }
                        else {

                            //tables.containsAll()
                            tables.removeAll(Set.of("PROPERTIES", "RECIPES", "RECIPETAGS", "TAGS"));

                            if(tables.isEmpty()) {
                                log.info("looks good, baseline");
                                flyway = config.baselineVersion("1.0.0").load();
                                //flyway = config.baselineVersion("0.0.1").load();
                                flyway.baseline();
                            }
                            else {
                                log.error("found extra tables");
                            }
                        }
                    }
                }

                fail(validateResult.errorDetails.errorMessage);
            }

            //MigrateResult result = flyway.migrate();

        } catch (Exception e) {

            e.printStackTrace();
            fail(e);
        }



    }

}
