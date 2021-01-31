package org.sergeys.cookbook.logic;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

public class Settings {

    public class WindowPosition{
        private double x;
        private double y;
        private double width;
        private double height;

        public WindowPosition() {}

        public void setValues(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

    }

    public static final String SETTINGS_PATH = ".CookBook";
    public static final String SETTINGS_FILE = "settings.xml";
    public static final String LOG_FILE = "log.txt";
    public static final String RECIPES_SUBDIR = "recipes";

    private static String dataDirPath;
    private static String settingsFilePath;
    private static String recipeLibraryPath;

    private Properties options = new Properties();
    private Properties version = new Properties();

    private WindowPosition windowPosition = new WindowPosition();

    private double winDividerPosition = 0;
    private String lastFilechooserLocation = "";

    private static Settings instance = new Settings();
    //private static ExecutorService executor = Executors.newCachedThreadPool();
    //private static ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private static Logger log;

    static{

        try(InputStream is = Settings.class.getResourceAsStream("/options.properties")) {
            instance.options.load(is);

            instance.options.forEach((k, v) -> {
                // "String replaceAll method removes backslashes in replacement string"
                //instance.options.replace(k, v.toString().replaceFirst("\\{\\{HOME_DIR\\}\\}", System.getProperty("user.home")));
                instance.options.replace(k, v.toString().replace("{{HOME_DIR}}", System.getProperty("user.home")));
            });

        }
        catch(Exception e) {
            //log.error("failed to load properties, exit", e);
            System.err.println("failed to load properties, exit");
            e.printStackTrace();
            Platform.exit();
        }

        //settingsDirPath = System.getProperty("user.home") + File.separator + SETTINGS_PATH;
        dataDirPath = instance.options.getProperty("data.directory.base") + File.separator + SETTINGS_PATH;

        settingsFilePath = dataDirPath + File.separator + SETTINGS_FILE;
        recipeLibraryPath = dataDirPath + File.separator + RECIPES_SUBDIR;

        File dir = new File(dataDirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        dir = new File(recipeLibraryPath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        // settings for log4j
        //extractResource("log4j.properties", false);
        //System.setProperty("log4j.configuration", new File(settingsDirPath + File.separator + "log4j.properties").toURI().toString());
        System.setProperty("log4j.log.file", dataDirPath + File.separator + LOG_FILE);

        // slf4j logging
        log = LoggerFactory.getLogger("cookbook");

        log.debug("data dir: " + dataDirPath);

        load();
    }

    // Singleton must have private constructor
    // public constructor required for XML serialization, do not use it
    public Settings(){
    }

    public static synchronized Settings getInstance(){
        return instance;
    }


    public static String getDataDirPath() {
        return dataDirPath;
    }

    public Properties getOptions() {
        return options;
    }

    public Properties getVersion() {
        return version;
    }

    public static String getRecipeLibraryPath() {
        return recipeLibraryPath;
    }

    public static void save() throws IOException{
        //XMLEncoder e;

        synchronized (instance) {
            /*
            e = new XMLEncoder(
                    new BufferedOutputStream(
                        new FileOutputStream(settingsFilePath)));
            e.writeObject(instance);
            e.close();
            */
            // TODO refactor all FileOutputStream(
            try(XMLEncoder e = new XMLEncoder(Files.newOutputStream(Path.of(settingsFilePath), StandardOpenOption.CREATE))){
                e.writeObject(instance);
            }
        }
    }

    /**
     * Replaces instance
     */
    public static void load() {

        //if(new File(settingsFilePath).exists()){
        if(Files.exists(Path.of(settingsFilePath))) {

            /*
            FileInputStream is = null;
            try {
                is = new FileInputStream(settingsFilePath);
            } catch (FileNotFoundException e) {
                log.error("", e);
            }

            XMLDecoder decoder = new XMLDecoder(is);
            instance = (Settings)decoder.readObject();
            decoder.close();
            */
            try(XMLDecoder decoder =
                    new XMLDecoder(
                            Files.newInputStream(Path.of(settingsFilePath), StandardOpenOption.READ))){

                instance = (Settings)decoder.readObject();

            } catch (IOException ex) {
                log.error("failed to load settings, will set defaults", ex);
                instance.setDefaults();
            }

        }
        else {
            instance.setDefaults();
        }

        //InputStream is = Settings.class.getResourceAsStream("/version.properties");
        try(InputStream is = Settings.class.getResourceAsStream("/version.properties")) {
            instance.version.load(is);
        }
        catch(Exception e) {
            log.error("failed to load properties, exit", e);
            Platform.exit();
        }

    }

    // TODO why static logger here and no individual loggers in classes??
//    public static Logger getLogger(){
//        return log;
//    }

//    public static ExecutorService getExecutor(){
//        return executor;
//    }
//
//    public static ExecutorService getSingleExecutor(){
//        return singleExecutor;
//    }
//
//    public static void shutdown(){
//        singleExecutor.shutdown();
//        executor.shutdown();
//    }

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
    private void setDefaults() {
        windowPosition.setValues(50.0, 50.0, 800.0, 600.0);
    }

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

    public double getWinDividerPosition() {
        return winDividerPosition;
    }

    public void setWinDividerPosition(double winDividerPosition) {
        this.winDividerPosition = winDividerPosition;
    }

    public String getLastFilechooserLocation() {
        return lastFilechooserLocation;
    }

    public void setLastFilechooserLocation(String lastFilechooserLocation) {
        this.lastFilechooserLocation = lastFilechooserLocation;
    }

    public WindowPosition getWindowPosition() {
        return windowPosition;
    }

    public void setWindowPosition(WindowPosition windowPosition) {
        this.windowPosition = windowPosition;
    }
}
