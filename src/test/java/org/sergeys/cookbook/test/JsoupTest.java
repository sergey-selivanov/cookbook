package org.sergeys.cookbook.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    void testPatterns() {
        String patternStr = "((http:|https:)[^\\?]*/)|(\\?.*)";
        //String patternStr = "images";
        String url = "http://vk.com/images/faviconnew.ico?blabla=123/123";

        Pattern pattern = Pattern.compile(patternStr);

        Matcher matcher = pattern.matcher(url);
        while(matcher.find()) {
            log.debug("== " + matcher.group());
        }

        // When there is a positive-width match at the beginning of the input sequence then an empty leading substring is included
        // Trailing empty strings will be discarded
        pattern.splitAsStream(url).forEachOrdered(str -> {
            log.debug("-- " + str);
        });

        String[] tokens = pattern.split(url);
        log.debug(tokens[1]);
    }



    private void removeElements(Document doc, String tag) {
        Elements elts = doc.getElementsByTag(tag);
        log.debug("found " + elts.size() + " element(s) of '" + tag + "' for removal");
        elts.remove();
    }

    private final Pattern FILENAME_IN_URL = Pattern.compile("((http:|https:)[^\\?]*/)|(\\?.*)");

    private void copyReferencedFiles(Document doc, String tag, String attribute,
            String srcMainDirname, String targetMainDirname, String targetSubdirname) {

        String targetDirname = targetMainDirname + File.separator + targetSubdirname;
        File targetDir = new File(targetDirname);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
            log.debug("created " + targetDir);
        }

        Elements elements = doc.getElementsByTag(tag);
        elements.parallelStream().forEach(element -> {
            String ref = element.attr(attribute); // expected relative path with subdir, e.g ./subdir/file.ext

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

                String[] tokens = FILENAME_IN_URL.split(ref);
                if(tokens.length != 2) {
                    log.error("Failed to parse filename from " + ref);
                }
                else {
                    String filename = tokens[1];

                    if(
                        filename.endsWith(".js")	// TODO check with regexp
                        || filename.endsWith(".css")
                        || filename.endsWith(".jpg")
                        || filename.endsWith(".png")
                        || filename.endsWith(".ico")
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

                            // save file
                            Path dest = FileSystems.getDefault().getPath(targetDirname, filename);

                            if(!dest.toFile().exists()) {
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest.toFile()));
                                bos.write(resp.body());
                                bos.close();
                                log.debug("downloaded " + dest);

                                element.attr(attribute, "./" + targetSubdirname + "/" + filename);
                                return;
                            }
                            else {
                                log.warn("already exists: " + dest);
                            }

                        } catch (IOException | InterruptedException e1) {
                            log.error("failed", e1);
                            return;
                        }
                    }
                    else {
                        log.debug("skipping element " + tag + ", " + attribute + ": " + ref + ", looks like not a file");
                        return;
                    }
                }
            }


            // restore e.g cyrillic text encoded as %D0%BF%D0%B0%D1
            ref = URLDecoder.decode(ref, StandardCharsets.UTF_8);


//            String fileName = ref.substring(ref.lastIndexOf("/") + 1); // TODO does any browser saves with \ separator?
//            log.debug(fileName + " -- " + ref);
            log.debug(ref);
            if(ref.startsWith("http://") || ref.startsWith("https://")) {
                // not a local file
                return;
            }

            // copy file

            try {
                // assume saved files keep other files in their relative subdir
                Path src = FileSystems.getDefault().getPath(srcMainDirname, ref);
                if(!src.toFile().exists()) {
                    log.error("File " + src + " does not exist");
                    return;
                }
                Path dest = FileSystems.getDefault().getPath(targetDirname, src.toFile().getName());

                if(!dest.toFile().exists()) {
                    // still filealreadyexists exception? + used by another process?
                    // TODO try through channels
                    // https://stackoverflow.com/questions/20471374/copying-files-with-file-locks-in-java
                    // https://www.journaldev.com/861/java-copy-file
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
//                else {
//                    //log.debug(dest + " already exists");
//                }

                // update reference

                element.attr(attribute, "./" + targetSubdirname + "/" + dest.toFile().getName());
            }
            catch(InvalidPathException | IOException ex) {
                log.error("failed", ex);
                return;
            }

        });
    }

    @Test
    void testJsoup() {
        log.info("jsoup test");

        String newName = "sample-output3";

        //File inFile = new File("d:/git/cookbook/samples/Буженина в фольге - Все проходит....html");
        //File inFile = new File("d:/git/cookbook/samples/паста милано.htm");
        //File inFile = new File("d:/git/cookbook/samples/Суд да дело  dok_zlo — ЖЖ.html");
        File inFile = new File("d:/git/cookbook/samples/TyqYxuUgdA3E9b.html");
        //File inFile = new File("/home/sergeys/tmp/Суд да дело  dok_zlo — ЖЖ.html");
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

            //String newName = "sample-output1";
            String baseOutputDir = Settings.getSettingsDirPath();

            //String targetSubdirName = baseOutputDir + File.separator + newName;
            String targetMainFile = baseOutputDir + File.separator + newName + ".html";
            String targetTxtFile = baseOutputDir + File.separator + newName + ".txt";

            copyReferencedFiles(doc, "img", "src",
                    inFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);
            copyReferencedFiles(doc, "link", "href",
                    inFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);


            // extract and save plaintext (e.g. for db fulltext search)

            String plainText = doc.body().text();
            //BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile), doc.charset()));
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile))); // utf8
            wr.write(plainText);
            wr.close();


            // write modified main file

            //OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(targetMainFile), Charset.defaultCharset());
            //OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(targetMainFile), doc.charset());
            wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetMainFile), doc.charset()));
            wr.write(doc.toString());
            wr.close();

        } catch (IOException e) {
            log.error("failed", e);
        }
    }

}
