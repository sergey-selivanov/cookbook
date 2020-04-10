package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

    private void removeElements(Document doc, String tag) {
        Elements elts = doc.getElementsByTag(tag);
        log.debug("found " + elts.size() + " element(s) of '" + tag + "' for removal");
        elts.remove();
    }

    private void copyReferencedFiles(Document doc, String tag, String attribute,
            String srcMainDirname, String targetMainDirname, String targetSubdirname) {

        String targetDirname = targetMainDirname + File.separator + targetSubdirname;
        File targetDir = new File(targetDirname);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
            log.debug("created " + targetDir);
        }

        Elements elements = doc.getElementsByTag(tag);
        elements.parallelStream().forEach(e -> {
            String ref = e.attr(attribute); // expected relative path with subdir, e.g ./subdir/file.ext

//            if(ref.startsWith("http:") || ref.startsWith("https:") || ref.isEmpty()) {
//                log.debug("skipping element " + tag + ", " + attribute + ": " + ref); //TODO download remote files?
//                return;
//            }

            if(ref.isEmpty()) {
                log.debug("skipping empty ref element " + tag + ", " + attribute + ": " + ref);
                return;
            }


            // ignore case sensitivity
            if(ref.startsWith("http:") || ref.startsWith("https:")){
                // remote file
                if(
                    ref.endsWith(".js")
                    || ref.endsWith(".css")
                    || ref.endsWith(".jpg")
                    || ref.endsWith(".ico")
                    // TODO parse supposed filename between last / and ?
                ) {
                    log.debug("download " + ref);
                    HttpClient client = HttpClient.newBuilder()
                            .build();
                    HttpRequest req = HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(ref))
                            .build();
                    try {
                        HttpResponse<byte[]> resp = client.send(req, BodyHandlers.ofByteArray());
                        log.debug("got " + resp.body().length + " bytes");
                    } catch (IOException | InterruptedException e1) {
                        log.error("failed", e1);
                        return;
                    }
                }
                else {
                    log.debug("skipping element " + tag + ", " + attribute + ": " + ref + ", looks like not a file");
                }
            }


            // restore e.g cyrillic text encoded as %D0%BF%D0%B0%D1
            ref= URLDecoder.decode(ref, StandardCharsets.UTF_8);


//            String fileName = ref.substring(ref.lastIndexOf("/") + 1); // TODO does any browser saves with \ separator?
//            log.debug(fileName + " -- " + ref);
            log.debug(ref);

            // copy file

            // assume saved files keep other files in their relative subdir
            Path src = FileSystems.getDefault().getPath(srcMainDirname, ref);
            if(!src.toFile().exists()) {
                log.error("File " + src + " does not exist");
                return;
            }
            Path dest = FileSystems.getDefault().getPath(targetDirname, src.toFile().getName());
            try {
                if(!dest.toFile().exists()) {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING); // still filealreadyexists exception?
                }
                else {
                    //log.debug(dest + " already exists");
                }
            } catch (IOException e1) {
                log.error("failed", e1);
            }

            // update reference

            e.attr(attribute, "./" + targetSubdirname + "/" + dest.toFile().getName());

        });
    }

    @Test
    void testJsoup() {
        log.info("jsoup test");

        //File inFile = new File("d:/git/cookbook/samples/Буженина в фольге - Все проходит....html");
        //File inFile = new File("d:/git/cookbook/samples/паста милано.htm");
        File inFile = new File("/home/sergeys/tmp/Суд да дело  dok_zlo — ЖЖ.html");
        //File inFile = new File("/home/sergeys/tmp/TyqYxuUgdA3E9b.html");


        try {
            Document doc = Jsoup.parse(inFile, null);

            log.debug("charset: " + doc.charset());

            // remove junk

            // looks broken if leave scripts:
            // https://ru-kitchen.ru/TyqYxuUgdA3E9b
            removeElements(doc, "script");
            removeElements(doc, "noscript");
            removeElements(doc, "noindex");


            // adjust relative references and copy referenced files

            String newName = "sample-output2";
            String baseOutputDir = Settings.getSettingsDirPath();

            //String targetSubdirName = baseOutputDir + File.separator + newName;
            String targetMainFile = baseOutputDir + File.separator + newName + ".html";

            copyReferencedFiles(doc, "img", "src",
                    inFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);
            copyReferencedFiles(doc, "link", "href",
                    inFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);

            // write modified main file

            //FileWriter fw = new FileWriter(Settings.getSettingsDirPath() + File.separator +  "jsoup-out3.html");
//            FileWriter fw = new FileWriter(targetMainFile);
//            fw.write(doc.toString());
//            fw.close();

            //OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(targetMainFile), Charset.defaultCharset());
            OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(targetMainFile), doc.charset());
            wr.write(doc.toString());
            wr.close();

        } catch (IOException e) {
            log.error("failed", e);
        }
    }

}
