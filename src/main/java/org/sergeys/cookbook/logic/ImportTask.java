package org.sergeys.cookbook.logic;

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
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public class ImportTask extends Task<ImportTask.Status>
{
    public enum Status { Unknown, InProgress, Complete, AlreadyExist, Failed };

    private final Logger log = LoggerFactory.getLogger(ImportTask.class);
    private final File htmlFile;

    public ImportTask(final File htmlFile) {
        this.htmlFile = htmlFile;
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


    @Override
    protected Status call() throws Exception {
        log.debug("import " + htmlFile);

        Database db = new Database();

        // calculate and verify hash

        String hash = Util.getFileHash(htmlFile);
        log.debug("hash: " + hash);
        if(db.isRecipeExists(hash)) {
            db.close();
            return Status.AlreadyExist;
        }

        // parse document

        Document doc = Jsoup.parse(htmlFile, null);
        log.debug("charset: " + doc.charset());

        // looks broken if leave scripts:
        // https://ru-kitchen.ru/TyqYxuUgdA3E9b
        removeElements(doc, "script");
        removeElements(doc, "noscript");
        removeElements(doc, "noindex");

        // adjust relative references and copy referenced files

        String newName = hash;

        Path tempDir;

        tempDir = Files.createTempDirectory("cookbook");
        tempDir.toFile().deleteOnExit();

        log.debug("temp dir: " + tempDir);

        String baseOutputDir = tempDir.toString();

        String targetMainFile = baseOutputDir + File.separator + newName + ".html";
        String targetTxtFile = baseOutputDir + File.separator + newName + ".txt";

        copyReferencedFiles(doc, "img", "src",
                htmlFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);
        copyReferencedFiles(doc, "link", "href",
                htmlFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);

        // extract and save plaintext (e.g. for db fulltext search)

        String plainText = doc.body().text();

        // TODO: see vk file, saves as not utf-8, convert to utf8
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile), doc.charset()));
        //BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile))); // utf8
        wr.write(plainText);
        wr.close();

        // save html
        wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetMainFile), doc.charset()));
        wr.write(doc.toString());
        wr.close();


        String title = "<missing title>";
        Elements elts = doc.getElementsByTag("title");
        if(elts.size() > 0) {
            title = elts.get(0).text();
            //log.debug("title: [" + title + "]");
            if(title.isBlank()) {
                title = "<empty title>";
            }
        }

        // pack all to a single file
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        Util.packJar(baseOutputDir, newName);

        // put to database
        File jarfile = new File(baseOutputDir + File.separator + newName + ".jar");
        try {
            db.addRecipe(hash, jarfile, title, htmlFile.getAbsolutePath());
            List<String> suggestedTags = RecipeLibrary.getInstance().suggestTags(title);
            db.updateRecipeTags(hash, suggestedTags);
            db.close();
        } catch (Exception ex) {
            log.error("", ex);
            return Status.Failed;
        }

        Util.deleteRecursively(tempDir.toFile());

        return Status.Complete;
    }

}