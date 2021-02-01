package org.sergeys.cookbook.logic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
            final File[] files = file.listFiles();
            for(File f: files){
                deleteRecursively(f);
            }
        }

        if(!file.delete()){
            log.info("not deleted {}", file);
            file.deleteOnExit();
        }
    }

    private static void addJarEntry(final String baseDir, final File source, final JarOutputStream target) throws IOException
    {
      //BufferedInputStream in = null;
//      try
//      {
        if (source.isDirectory())
        {
          String name = source.getPath().replace("\\", "/");
          name = name.substring(baseDir.length());
          if (!name.isEmpty())
          {
            if (!name.endsWith("/")){
              name += "/";
            }
            final JarEntry entry = new JarEntry(name);
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
        final JarEntry entry = new JarEntry(name);
        entry.setTime(source.lastModified());
        target.putNextEntry(entry);

        /*
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
        */
        try(InputStream is = Files.newInputStream(source.toPath(), StandardOpenOption.READ)){
            // TODO inputstream is not buffered, performance?
            is.transferTo(target);
        }

        target.closeEntry();
//      }
//      finally
//      {
//        if (in != null){
//          in.close();
//        }
//      }
    }



    public static void packJar(final String dir, final String subdir){
        // http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        //JarOutputStream target;
        try {
            final String jarname = FileSystems.getDefault().getPath(dir, subdir + ".jar").toString();
            //target = new JarOutputStream(new FileOutputStream(jarname), manifest);

            try(JarOutputStream jos =
                    new JarOutputStream(Files.newOutputStream(Path.of(jarname), StandardOpenOption.CREATE_NEW),
                            manifest)){

                // TODO this is app-specific, pack all files instead

                Path path = FileSystems.getDefault().getPath(dir, subdir + ".html");
                addJarEntry(dir, path.toFile(), jos);

                path = FileSystems.getDefault().getPath(dir, subdir + ".txt");
                addJarEntry(dir, path.toFile(), jos);

                try(DirectoryStream<Path> subfiles = Files.newDirectoryStream(FileSystems.getDefault().getPath(dir, subdir))){
                    for(Path entry: subfiles) {
                        addJarEntry(dir, entry.toFile(), jos);
                    }

                }
            }

        } catch (IOException e) {
            log.error("", e);
        }
    }


    public static void unpackJar(File jar, String targetDir){
        try {
            //JarInputStream jis = new JarInputStream(new FileInputStream(jar));
            try(JarInputStream jis = new JarInputStream(Files.newInputStream(jar.toPath(), StandardOpenOption.READ))) {
                JarEntry je;
                while((je = jis.getNextJarEntry()) != null){	// manifest not included
    //				System.out.println("entry " + je.getName());

                    final File f = new File(targetDir + File.separator + je.getName().substring(1)); // name starts with slash
                    f.getParentFile().mkdirs();

                    try(OutputStream os = Files.newOutputStream(f.toPath(), StandardOpenOption.CREATE_NEW)){
                        jis.transferTo(os);
                    }
                }

            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    public static String getFileHash(File file) throws IOException, NoSuchAlgorithmException
    {
        // http://www.mkyong.com/java/java-sha-hashing-example/

        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        //FileInputStream fis = new FileInputStream(file);
        try(InputStream fis = Files.newInputStream(file.toPath(), StandardOpenOption.READ)){

            //byte[] dataBytes = new byte[1024];
            final byte[] dataBytes = new byte[10240];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
              md.update(dataBytes, 0, nread);
            }
        }

        final byte[] mdbytes = md.digest();

        //convert the byte to hex format method 1
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(String.format("%02x", mdbytes[i]));
        }

        return sb.toString();
    }
}
