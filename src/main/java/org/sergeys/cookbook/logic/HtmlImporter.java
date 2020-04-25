package org.sergeys.cookbook.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;

import org.apache.xml.serialize.HTMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.slf4j.Logger;
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * Must be used in javafx application thread
 *
 * @author sergeys
 *
 */
@SuppressWarnings("deprecation")
public class HtmlImporter {

    private final Logger log = LoggerFactory.getLogger(HtmlImporter.class);

    private File originalFile;
    private Document doc;
    private String destinationDir;
    private String hash;

    private WebEngine importEngine;

    public enum Status { Unknown, InProgress, Complete, AlreadyExist, Failed };

    private SimpleObjectProperty<Status> status = new SimpleObjectProperty<Status>();

    public HtmlImporter(){
        status.set(Status.Unknown);

        importEngine = new WebEngine();
        //importEngine.documentProperty().addListener(docListener);
        importEngine.getLoadWorker().stateProperty().addListener(workerListener);
        importEngine.getLoadWorker().exceptionProperty().addListener(exceptionListener);

//        importEngine.getLoadWorker().messageProperty().addListener(new ChangeListener<String>() {
//
//            @Override
//            public void changed(ObservableValue<? extends String> observable,
//                    String oldValue, String newValue) {

//                System.out.println("== " + newValue);
//            }
//        });
    }

    public HtmlImporter(ChangeListener<Status> importListener){
        this();
        status.addListener(importListener);
    }

    public SimpleObjectProperty<Status> statusProperty(){
        return status;
    }

    private void removeElements(Document document, String tag){
        NodeList nodes = document.getElementsByTagName(tag);

        //Settings.getLogger().debug("found " + nodes.getLength() + " node(s) of '" + tag + "' for removal");

        while(nodes.getLength() > 0){
            org.w3c.dom.Node n = nodes.item(0);
            n.getParentNode().removeChild(n);
            nodes = document.getElementsByTagName(tag);
        }
    }

//    private ChangeListener<Document> docListener = new ChangeListener<Document>(){
//        @Override
//        public void changed(
//                ObservableValue<? extends Document> observable,
//                Document oldValue, Document newValue) {
//
////            System.out.println("location " + importEngine.getLocation());
//            if(newValue != null){
////                System.out.println("document not null");
////                Document doc = importEngine.getDocument();
////                try {
////                    setDocument(doc);
////                } catch (IOException e) {
////
////                    e.printStackTrace();
////                }
//            }
//            else{
////                System.out.println("document changed but is null");
//            }
//        }};

    private ChangeListener<State> workerListener = new ChangeListener<State>() {
        public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
            if (newState == State.SUCCEEDED) {
//                System.out.println("document load SUCCEEDED");

                Document doc = importEngine.getDocument();
                if(doc != null){
                    try {
                        setDocument(doc);
                    } catch (IOException e) {
                        //Settings.getLogger().error("", e);
                    }
                }
                else{
                    //Settings.getLogger().debug("worker succeeded but document is null");
                }
            }
            else if (newState == State.CANCELLED){
                //Settings.getLogger().debug("document load CANCELLED: " + importEngine.getLocation());
            }
//            else{
////                System.out.println("document load: " + newState);
//            }
        }
    };

    private ChangeListener<Throwable> exceptionListener = new ChangeListener<Throwable>() {

        @Override
        public void changed(
                ObservableValue<? extends Throwable> observable,
                Throwable oldValue, Throwable newValue) {
            //Settings.getLogger().error("import engine load worker got exception", newValue);
        }
    };

    public void importFile(final File htmlFile){

        status.set(Status.InProgress);

        originalFile = htmlFile;

        try {
            hash = getFileHash(originalFile);
        } catch (NoSuchAlgorithmException | IOException e2) {
            //Settings.getLogger().error("", e2);
            status.set(Status.Failed);
            return;
        }

        try {
            if(Database.getInstance().isRecipeExists(hash)){
                //Settings.getLogger().info("recipe with this hash already exist in database");
                status.set(Status.AlreadyExist);
                return;
            }
        } catch (Exception e2) {
            //Settings.getLogger().error("", e2);
            status.set(Status.Failed);
            return;
        }

        destinationDir = Settings.getSettingsDirPath() + File.separator + Settings.RECIPES_SUBDIR;
        File dir = new File(destinationDir);
        if(!dir.exists()){
            dir.mkdirs();
        }

        importEngine.load(htmlFile.toURI().toString());
    }

    /**
     * assume to work in temp dir
     *
     * @param document
     * @param tag
     * @param attribute
     * @param relativeSubdir
     * @param absTargetDir
     */
    private void fixReferences(Document document, String tag, String attribute, String relativeSubdir, String absTargetDir){
        // copy referenced files and fix references

        // collect referenced files
        NodeList nodes = document.getElementsByTagName(tag);

        //Settings.getLogger().debug("found " + nodes.getLength() + " node(s) of '" + tag + "'");

        for(int i = 0; i < nodes.getLength(); i++){
            org.w3c.dom.Node attr = nodes.item(i).getAttributes().getNamedItem(attribute);
            if(attr != null){
                // copy file and modify link
                if(attr.getNodeValue().startsWith("http:") ||
                    attr.getNodeValue().startsWith("https:") ||
                    attr.getNodeValue().startsWith("//") ||
                    attr.getNodeValue().isEmpty()){
                    // TODO: fetch remote files
//                    System.out.println(tag + ": skip url '" + attr.getNodeValue() + "'");
                    //Settings.getLogger().debug(tag + ": skip url '" + attr.getNodeValue() + "'");
                    continue;
                }

                String srcName;
                try {
                    srcName = URLDecoder.decode(attr.getNodeValue(), "UTF-8");
                } catch (UnsupportedEncodingException | DOMException e1) {
                    //Settings.getLogger().error("", e1);
                    continue;
                }

                // assume saved files keep other files in their relative subdir
                Path src = FileSystems.getDefault().getPath(
                        originalFile.getParentFile().getAbsolutePath(), srcName);
                Path dest = FileSystems.getDefault().getPath(absTargetDir, relativeSubdir, src.toFile().getName());
                if(src.toFile().exists()){
                    try {
                        if(!dest.getParent().toFile().exists()){
                            dest.getParent().toFile().mkdirs();
                            //dest.getParent().toFile().deleteOnExit();
                        }

                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        attr.setTextContent(relativeSubdir + "/" + src.getFileName());

//                        Settings.getLogger().debug("fixed '" + srcName + "' to '" +
//                                relativeSubdir + "/" + src.getFileName() +
//                                "'");

                    } catch (IOException e) {
                        //Settings.getLogger().error("failed to copy on '" + attr.getNodeValue() + "'", e);
                    }
                    //attr.setTextContent(hash);
                }
                else{
                    //Settings.getLogger().debug("nonexistent path " + src);
                }
            }
        }
    }

    private void addJarEntry(String baseDir, File source, JarOutputStream target) throws IOException
    {
      BufferedInputStream in = null;
      try
      {
        if (source.isDirectory())
        {
          String name = source.getPath().replace("\\", "/");
          name = name.substring(baseDir.length());
          if (!name.isEmpty())
          {
            if (!name.endsWith("/")){
              name += "/";
            }
            JarEntry entry = new JarEntry(name);
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            target.closeEntry();
          }
          for (File nestedFile: source.listFiles()){
            addJarEntry(baseDir, nestedFile, target);
          }
          return;
        }

        String name = source.getPath().replace("\\", "/");
        name = name.substring(baseDir.length());
        JarEntry entry = new JarEntry(name);
        entry.setTime(source.lastModified());
        target.putNextEntry(entry);
        in = new BufferedInputStream(new FileInputStream(source));

        byte[] buffer = new byte[1024];
        while (true)
        {
          int count = in.read(buffer);
          if (count == -1){
            break;
          }
          target.write(buffer, 0, count);
        }
        target.closeEntry();
      }
      finally
      {
        if (in != null){
          in.close();
        }
      }
    }

    // TODO move to util
    private void packJar(String dir, String subdir){
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream target;
        try {
            String jarname = FileSystems.getDefault().getPath(dir, subdir + ".jar").toString();
            target = new JarOutputStream(new FileOutputStream(jarname), manifest);

            Path path = FileSystems.getDefault().getPath(dir, subdir + ".html");
            addJarEntry(dir, path.toFile(), target);

            path = FileSystems.getDefault().getPath(dir, subdir + ".txt");
            addJarEntry(dir, path.toFile(), target);

            DirectoryStream<Path> subfiles = Files.newDirectoryStream(FileSystems.getDefault().getPath(dir, subdir));
            for(Path entry: subfiles) {
                addJarEntry(dir, entry.toFile(), target);
            }
            subfiles.close();
            target.close();
        } catch (IOException e) {
            //Settings.getLogger().error("", e);
        }
    }

    private void setDocument(Document document) throws IOException {

        doc = document;

        String encoding = doc.getInputEncoding();
        //Settings.getLogger().debug("doc encoding " + encoding);

        // remove garbage
        removeElements(doc, "script");
        removeElements(doc, "noscript");

        Path tempDir;

        tempDir = Files.createTempDirectory("cookbook");
        tempDir.toFile().deleteOnExit();

        fixReferences(doc, "img", "src", hash, tempDir.toString());
        fixReferences(doc, "link", "href", hash, tempDir.toString());

        // extract plaintext for db fulltext search
        NodeList nodes = doc.getElementsByTagName("body");
        if(nodes.getLength() < 1){
            //Settings.getLogger().debug("body not found");
            status.set(Status.Failed);
            return;
        }

        String bodytext = nodes.item(0).getTextContent();
        Path p = FileSystems.getDefault().getPath(tempDir.toString(), hash + ".txt");
        try {
            //FileWriter wr = new FileWriter(p.toFile());
            //OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(p.toFile()), StandardCharsets.UTF_8);
            OutputStreamWriter wr = new OutputStreamWriter(new FileOutputStream(p.toFile()), Charset.defaultCharset());
            wr.write(bodytext);
            wr.close();
        } catch (IOException e) {
            //Settings.getLogger().error("", e);
            status.set(Status.Failed);
        }


        // TODO broken encoding for 1251 file
        // http://weblogs.java.net/blog/fabriziogiudici/archive/2012/02/12/xslt-xhtml-jdk6-jdk7-madness
        OutputFormat of = new OutputFormat(doc);
        //Settings.getLogger().debug("orig outputformat encoding " + of.getEncoding());
        of.setEncoding(encoding);
        HTMLSerializer sr = new HTMLSerializer(of);
        try {
            p = FileSystems.getDefault().getPath(tempDir.toString(), hash + ".html");
            FileOutputStream fos = new FileOutputStream(p.toFile());
            sr.setOutputByteStream(fos);
            sr.serialize(doc);
            fos.close();
//            System.out.println("file written");
        } catch (IOException e1) {
            //Settings.getLogger().error("", e1);
            status.set(Status.Failed);
        }

        String title = "unknown";
        nodes = doc.getElementsByTagName("title");
        if(nodes.getLength() > 0){
            title = nodes.item(0).getTextContent();
        }

        // pack all to a single file
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        packJar(tempDir.toString(), hash);

        // put to database
        File jarfile = new File(tempDir.toString() + File.separator + hash + ".jar");
        try {
            Database.getInstance().addRecipe(hash, jarfile, title, originalFile.getAbsolutePath());
            List<String> suggestedTags = RecipeLibrary.getInstance().suggestTags(title);
            Database.getInstance().updateRecipeTags(hash, suggestedTags);
        } catch (Exception e) {
            //Settings.getLogger().error("", e);
            status.set(Status.Failed);
        }

        Util.deleteRecursively(tempDir.toFile());

        status.set(Status.Complete);
    }

    public String getHash(){
        return hash;
    }

    private String getFileHash(File file) throws IOException, NoSuchAlgorithmException
    {
        // http://www.mkyong.com/java/java-sha-hashing-example/

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);

        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        }

        fis.close();
        byte[] mdbytes = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
//            String s = Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1);

            //sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            sb.append(String.format("%02x", mdbytes[i]));
        }

//        System.out.println("Hex format : " + sb.toString());
        return sb.toString();

//       //convert the byte to hex format method 2
//        StringBuffer hexString = new StringBuffer();
//        for(int i = 0; i < mdbytes.length; i++) {
//          hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
//        }
//
//        System.out.println("Hex format : " + hexString.toString());
//        return hexString.toString();
    }
}
