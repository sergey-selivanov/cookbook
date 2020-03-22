package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sergeys.cookbook.logic.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

class JsoupTest {

    static {
        // load settings and setup logger
        Settings.getLogger().info("jsoup test setup");
    }

    static final Logger log = LoggerFactory.getLogger(JsoupTest.class);

    @BeforeAll
    static void setUpBeforeClass() throws Exception {

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
    void testJsoup() {
        log.info("jsoup test");

        File inFile = new File("d:/git/cookbook/samples/Буженина в фольге - Все проходит....html");
        try {
            Document doc = Jsoup.parse(inFile, null);


            // remove junk

            String tag = "script";
            Elements elts = doc.getElementsByTag(tag);
            log.debug("found " + elts.size() + " node(s) of '" + tag + "' for removal");
            elts.remove();

            tag = "noscript";
            elts = doc.getElementsByTag(tag);
            log.debug("found " + elts.size() + " node(s) of '" + tag + "' for removal");
            elts.remove();


            // adjust relative references and copy referenced files

            String newName = "sample-output";
            String baseOutputDir = Settings.getSettingsDirPath();

            tag = "img";
            elts = doc.getElementsByTag(tag);
            elts.parallelStream().forEach(e -> {
                log.debug(e.attr("src"));



                });

            FileWriter fw = new FileWriter(Settings.getSettingsDirPath() + File.separator +  "jsoup-out3.html");
            fw.write(doc.toString());
            fw.close();

        } catch (IOException e) {
            log.error("failed", e);
        }
    }

}
