package org.sergeys.cookbook.logic;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsManager {

    private static final String DATA_SUBDIR = ".CookBook";
    private static final String SETTINGS_FILE = "settings.xml";
    private static final String LOG_FILE = "log.txt";
    private static final String RECIPES_SUBDIR = "recipes";

    private final Path dataDirPath;
    private final Path settingsFilePath;
    private final Path recipeSubdirPath;

    private final Properties options = new Properties();
    private final Properties version = new Properties();

    private Settings settings;

    private final Logger log;

    // https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom#Example_Java_Implementation
    private static class LazyHolder {
        static final SettingsManager INSTANCE = new SettingsManager();
    }

    public static SettingsManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private SettingsManager() {

        try(InputStream is = getClass().getResourceAsStream("/options.properties")) {
            options.load(is);
            options.forEach((k, v) -> {
                // "String replaceAll method removes backslashes in replacement string"
                //instance.options.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", System.getProperty("user.home")));
                options.replace(k, v.toString().replace("{{HOME_DIR}}", System.getProperty("user.home")));
            });
        }
        catch(IOException e) {
            throw new UncheckedIOException("failed to load properties", e);
        }

        dataDirPath = Path.of(options.getProperty("data.directory.base"), DATA_SUBDIR);
        settingsFilePath = Path.of(options.getProperty("data.directory.base"), DATA_SUBDIR, SETTINGS_FILE);
        recipeSubdirPath = Path.of(options.getProperty("data.directory.base"), DATA_SUBDIR, RECIPES_SUBDIR);

        try {
            //Files.createDirectories(dataDirPath);
            Files.createDirectories(recipeSubdirPath);
        } catch (IOException e) {
            String msg = "failed to create directories: " + recipeSubdirPath;
            throw new UncheckedIOException(msg, e);
        }

        // setup log4j
        //extractResource("log4j.properties", false);
        //System.setProperty("log4j.configuration", new File(settingsDirPath + File.separator + "log4j.properties").toURI().toString());

        System.setProperty("log4j.log.file", Path.of(options.getProperty("data.directory.base"), DATA_SUBDIR, LOG_FILE).toString());


        log = LoggerFactory.getLogger(getClass());
        log.debug("data dir: {}", dataDirPath);

        // load settings
        if(Files.exists(settingsFilePath)) {
            try(XMLDecoder decoder =
                    new XMLDecoder(
                            Files.newInputStream(settingsFilePath, StandardOpenOption.READ))){

                settings = (Settings)decoder.readObject();

            }
            //catch (IOException ex) { // other unchecked exceptions
            catch (Exception ex) {
                log.error("failed to load settings from {}", settingsFilePath);
                log.error("failed to load settings, set defaults; reason:", ex);

                settings = new Settings();
                settings.getWindowPosition().setValues(50.0, 50.0, 800.0, 600.0);
            }
        }
        else {
            settings = new Settings();
            settings.getWindowPosition().setValues(50.0, 50.0, 800.0, 600.0);
        }

        try(InputStream is = getClass().getResourceAsStream("/version.properties")) {
            version.load(is);
        } catch (IOException e) {
            log.error("failed lo load version info");
            // well ok, can continue
        }
    }

    public Path getDataDirPath() {
        return dataDirPath;
    }

    public Path getRecipeSubdirPath() {
        return recipeSubdirPath;
    }

    public Properties getOptions() {
        return options;
    }

    public Properties getVersion() {
        return version;
    }

    public Settings getSettings() {
        return settings;
    }

    public void saveSettings() throws IOException{
        try(XMLEncoder e = new XMLEncoder(Files.newOutputStream(settingsFilePath, StandardOpenOption.CREATE))){
            e.writeObject(settings);
        }
    }


    /**
     * Extracts file to the settings directory
     *
     * @param filename
     * @param overwrite
     */
/*
    private static void extractResource(String filename, boolean overwrite){

        String targetfile = dataDirPath + File.separator + filename;

        try{

            if(!overwrite && new File(targetfile).exists()){
                return;
            }

            InputStream is = Settings.class.getResourceAsStream("/" + filename);
            if (is != null) {
                byte[] buf = new byte[20480];
                FileOutputStream fos = new FileOutputStream(targetfile);
                int count = 0;
                while ((count = is.read(buf)) > 0) {
                    fos.write(buf, 0, count);
                }
                fos.close();
                is.close();
            }
        }
        catch(IOException ex){
            if(log != null){
                log.error("Failed to extract data to " + targetfile, ex);
            }
            else{
                ex.printStackTrace(System.err);
            }
        }
    }
*/

//    public Date getCurrentVersion(){
//
//        String ver = resources.getProperty("version", "");
//
//        String[] tokens = ver.split("-");
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(0);
//        cal.set(Integer.valueOf(tokens[0]),
//                Integer.valueOf(tokens[1]) - 1,    // month is 0 bazed
//                Integer.valueOf(tokens[2]),
//                Integer.valueOf(tokens[3]),
//                Integer.valueOf(tokens[4]));
//        Date date = cal.getTime();
//
//        return date;
//    }

//    public Date getSavedVersion() {
//        return savedVersion;
//    }

//    // required for xml serializer to work
//    public void setSavedVersion(Date savedVersion) {
//        this.savedVersion = savedVersion;
//    }
//
//    public Properties getResources(){
//        return resources;
//    }


}
