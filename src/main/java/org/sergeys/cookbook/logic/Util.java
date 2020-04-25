package org.sergeys.cookbook.logic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static void deleteRecursively(File file){
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(File f: files){
                deleteRecursively(f);
            }
        }

        if(!file.delete()){
            log.info("not deleted " + file);
            file.deleteOnExit();
        }
    }

    private static void addJarEntry(String baseDir, File source, JarOutputStream target) throws IOException
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



    public static void packJar(String dir, String subdir){
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        JarOutputStream target;
        try {
            String jarname = FileSystems.getDefault().getPath(dir, subdir + ".jar").toString();
            target = new JarOutputStream(new FileOutputStream(jarname), manifest);

            // TODO this is app-specific, pack all files instead

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
            log.error("", e);
        }
    }


    public static void unpackJar(File jar, String targetDir){
        try {
            JarInputStream jis = new JarInputStream(new FileInputStream(jar));
            JarEntry je;
            while((je = jis.getNextJarEntry()) != null){	// manifest not included
//				System.out.println("entry " + je.getName());

                File f = new File(targetDir + File.separator + je.getName().substring(1)); // name starts with slash
                f.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(f);

                byte[] buf = new byte[20480];
                int count;

                while((count = jis.read(buf)) > 0){
//					System.out.println("read bytes " + count);
                    fos.write(buf, 0, count);
                }

                fos.close();
            }

            jis.close();
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public static String getFileHash(File file) throws IOException, NoSuchAlgorithmException
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
            sb.append(String.format("%02x", mdbytes[i]));
        }

        return sb.toString();
    }
}
