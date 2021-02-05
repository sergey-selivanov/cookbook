package org.sergeys.cookbook.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public class ImportTask extends Task<ImportTask.ImportResult>
{
    //public enum Status { Unknown, InProgress, Complete, AlreadyExist, Failed };
    public enum ImportResult { Success, AlreadyExist, Failure };

    private final Logger log = LoggerFactory.getLogger(ImportTask.class);
    private final File htmlFile;

    public ImportTask(final File htmlFile) {
        this.htmlFile = htmlFile;
    }

    private void removeElements(Document doc, String tag) {
        Elements elts = doc.getElementsByTag(tag);
        //log.debug("found " + elts.size() + " element(s) of '" + tag + "' for removal");
        log.debug("found {} element(s) of '{}' for removal", elts.size(), tag);
        elts.remove();
    }

    private final Pattern FILENAME_IN_URL = Pattern.compile("((http:|https:)[^\\?]*/)|(\\?.*)");

    private void copyReferencedFiles(Document doc, String tag, String attribute,
            String srcMainDirname, String targetMainDirname, String targetSubdirname) {

        String targetDirname = targetMainDirname + File.separator + targetSubdirname;
        File targetDir = new File(targetDirname);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
            log.debug("created {}", targetDir);
        }

        Elements elements = doc.getElementsByTag(tag);
        elements.parallelStream().forEach(element -> {
            // TODO review logic when attribute is not 'ref'
            String ref = element.attr(attribute); // expected relative path with subdir, e.g ./subdir/file.ext

//            if(ref.startsWith("http:") || ref.startsWith("https:") || ref.isEmpty()) {
//                log.debug("skipping element " + tag + ", " + attribute + ": " + ref); //TODO download remote files?
//                return;
//            }

            if(ref.isEmpty()) {
                log.debug("skipping empty ref element {}, {}: {}", tag, attribute, ref);
                return;
            }


            // ignore case sensitivity
            if(ref.startsWith("http:") || ref.startsWith("https:")){
                // remote file

                String[] tokens = FILENAME_IN_URL.split(ref);
                if(tokens.length != 2) {
                    log.error("Failed to parse filename from {}", ref);
                }
                else {
                    String filename = tokens[1];

                    if(
                        filename.endsWith(".js")	// TODO check with regexp
                        || filename.endsWith(".css")
                        || filename.endsWith(".jpg")
                        || filename.endsWith(".png")
                        || filename.endsWith(".ico")

                        //|| filename.endsWith(".html")
                        || filename.endsWith(".json")
                    ) {

                        Path dest = FileSystems.getDefault().getPath(targetDirname, filename);

                        try {
                            if(!dest.toFile().exists()) {

                                log.debug(">>>> SKIPPED download {}", ref);
/*
                                HttpClient client = HttpClient.newBuilder()
                                        .build();
                                HttpRequest req = HttpRequest.newBuilder()
                                        .GET()
                                        .uri(URI.create(ref))
                                        .build();

                                // TODO SSLHandshakeException, ConnectException
                                HttpResponse<byte[]> resp = client.send(req, BodyHandlers.ofByteArray());
                                //log.debug("got " + resp.body().length + " bytes");

                                // save file

//                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest.toFile()));
//                                bos.write(resp.body());
//                                bos.close();

                                try(BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW))){
                                    bos.write(resp.body());
                                }
                                catch(FileAlreadyExistsException ex) {
                                    log.debug("already exists: {}", ex.getMessage());
                                }

                                log.debug("downloaded " + dest);
*/
                                element.attr(attribute, "./" + targetSubdirname + "/" + filename);
                                return;
                            }
                            else {
                                log.warn("don't download, already exists: {}", dest);
                            }

                        }
//                        catch (IOException | InterruptedException e1) {
//                            log.error("failed", e1);
//                            return;
//                        }
                        finally {}
                    }
                    else {
                        log.debug("skipping element {}, {}: {}, looks like not a file", tag, attribute, ref);
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
                    log.error("File {} does not exist", src);
                    return;
                }

                Path dest = FileSystems.getDefault().getPath(targetDirname, src.toFile().getName());

                if(Files.notExists(dest, LinkOption.NOFOLLOW_LINKS)) { // still filealreadyexists exception

                //if(!dest.toFile().exists()) { // still filealreadyexists exception? + used by another process? see /cookbook/samples/TyqYxuUgdA3E9b.html
                //if(!dest.toFile().isFile()) { // no exception?

                    // TODO try through channels? looks like older method
                    // https://stackoverflow.com/questions/20471374/copying-files-with-file-locks-in-java
                    // https://www.journaldev.com/861/java-copy-file

                    // https://stackoverflow.com/questions/1605332/java-nio-filechannel-versus-fileoutputstream-performance-usefulness

                    // https://bugs.openjdk.java.net/browse/JDK-8114830
                    // "Files.copy clearly specifies that it is not an atomic operation. So yes, it is possible to get interference from other entities that are creating/deleting/moving files at the same time."

                    //Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

                    // TODO FileSystemException
                    // java.nio.file.FileSystemException: I:\home\food2\??????? ??????? - ??????? ?? ?????? ???????_files\440.png -> j:\Temp\cookbook18270422026351628819\dc25a923a7a41db84fe20f507d7fc00a9980aaf37936afff8750c070cf9c2e83\440.png: The process cannot access the file because it is being used by another process
                    // at sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:92) ~[?:?]
                    Files.copy(src, dest);
                }
                else {
                    log.debug("{} already exists, skipped", dest.getFileName());
                }

                // update reference

                element.attr(attribute, "./" + targetSubdirname + "/" + dest.toFile().getName());
            }
            catch(FileAlreadyExistsException ex) {
                log.debug("already exists: {}", ex.getMessage());
            }
            catch(InvalidPathException | IOException ex) {
                log.error("failed", ex);
                return;
            }

        });
    }


    @Override
    protected ImportResult call() throws Exception {
        log.debug("import {}", htmlFile);

        Database db = new Database();

        // calculate and verify hash

        String hash = Util.getFileHash(htmlFile);
        log.debug("hash: {}", hash);
        if(db.isRecipeExists(hash)) {
            db.close();
            log.debug("already exist");
            return ImportResult.AlreadyExist;
        }

        // parse document

        Document doc = Jsoup.parse(htmlFile, null);
        log.debug("charset: {}", doc.charset());

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

        log.debug("temp dir: {}", tempDir);

        String baseOutputDir = tempDir.toString();

        String targetMainFile = baseOutputDir + File.separator + newName + ".html";
        String targetTxtFile = baseOutputDir + File.separator + newName + ".txt";

        copyReferencedFiles(doc, "img", "src",
                htmlFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);
        copyReferencedFiles(doc, "link", "href",	// TODO review what is this for
                htmlFile.getParentFile().getAbsolutePath(), baseOutputDir, newName);

        // extract and save plaintext (e.g. for db fulltext search)

        String plainText = doc.body().text();

        //BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile), doc.charset()));
//        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetTxtFile), StandardCharsets.UTF_8));
//        wr.write(plainText);
//        wr.close();
        try(BufferedWriter wr = Files.newBufferedWriter(Path.of(targetTxtFile), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)){
            wr.write(plainText);
        }

        // save html
//        wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetMainFile), doc.charset()));
//        wr.write(doc.toString());
//        wr.close();
        try(BufferedWriter wr = Files.newBufferedWriter(Path.of(targetMainFile), doc.charset(), StandardOpenOption.CREATE_NEW)){
            wr.write(doc.toString());
        }

        String title = "<missing title>";
        Elements elements = doc.getElementsByTag("title");
        if(elements.size() > 0) {
            title = elements.get(0).text();
            //log.debug("title: [" + title + "]");
            if(title.isBlank()) {
                title = "<empty title>";
            }
        }

        // pack all to a single file
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        Util.packJar(baseOutputDir, newName);

        // put to database
        //File jarfile = new File(baseOutputDir + File.separator + newName + ".jar");
        Path jarfile = Path.of(baseOutputDir, newName + ".jar");
        try {
            db.addRecipe(hash, jarfile, title, htmlFile.getAbsolutePath());
            List<String> suggestedTags = RecipeLibrary.getInstance().suggestTags(title);
            db.updateRecipeTags(hash, suggestedTags);
            db.close();
        } catch (Exception ex) {
            log.error("", ex);
            return ImportResult.Failure;
        }

        Util.deleteRecursively(tempDir.toFile());

        return ImportResult.Success;
    }

}
