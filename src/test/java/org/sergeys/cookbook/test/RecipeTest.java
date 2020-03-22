package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sergeys.cookbook.logic.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RecipeTest {

    static {
        try {
            // TODO note that temp file is not deleted
            System.setProperty("log4j.log.file", File.createTempFile("junittest", "test").getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    static Recipe recipe;

    // uses main log4j2.xml, rolling file logger
    // TODO how to use different log4j2.xml, or no config file at all for console out only?
    static final Logger log = LoggerFactory.getLogger(RecipeTest.class);

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        recipe = new Recipe();
        recipe.setHash("test");
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

    @Disabled
    @Test
    void testGetHash() {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetHash() {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetTitle() {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetTitle() {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testGetId() {
        fail("Not yet implemented");
    }

    @Disabled
    @Test
    void testSetId() {
        fail("Not yet implemented");
    }

    @Test
    void testGetUnpackedDir() {
        log.info(recipe.getUnpackedDir());
    }

    @Test
    void testGetUnpackedFilename() {
        log.info(recipe.getUnpackedFilename());
    }

}
