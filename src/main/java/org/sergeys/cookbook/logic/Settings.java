package org.sergeys.cookbook.logic;

//import java.awt.Dimension;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
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

    public static final String SETTINGS_PATH = ".CookBook3-devel-tmp";
    public static final String SETTINGS_FILE = "settings.xml";
    public static final String LOG_FILE = "log.txt";
    public static final String RECIPES_SUBDIR = "recipes";

    private static String settingsDirPath;
    private static String settingsFilePath;
    private static String recipeLibraryPath;

    private Properties resources = new Properties();
    //private Dimension winPosition = new Dimension();	// replace with something different from awt
    //private Dimension winSize = new Dimension();

    private WindowPosition windowPosition = new WindowPosition();

    private double winDividerPosition = 0;
    private String lastFilechooserLocation = "";
    private Date savedVersion = new Date(0);

    private static Settings instance = new Settings();
    //private static ExecutorService executor = Executors.newCachedThreadPool();
    //private static ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private static Logger log;

    static{
        //settingsDirPath = System.getProperty("user.home") + File.separator + SETTINGS_PATH;
        settingsDirPath = "i:/tmp" + File.separator + SETTINGS_PATH;
        settingsFilePath = settingsDirPath + File.separator + SETTINGS_FILE;
        recipeLibraryPath = settingsDirPath + File.separator + RECIPES_SUBDIR;

        File dir = new File(settingsDirPath);
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
        System.setProperty("log4j.log.file", settingsDirPath + File.separator + LOG_FILE);

        // slf4j logging
        log = LoggerFactory.getLogger("cookbook");

        load();
    }

    // Singleton must have private constructor
    // public constructor required for XML serialization, do not use it
    public Settings(){
    }

    public static synchronized Settings getInstance(){
        return instance;
    }

    public static String getSettingsDirPath() {
        return settingsDirPath;
    }

    public static void setSettingsDirPath(String settingsDirPath) {
        Settings.settingsDirPath = settingsDirPath;
    }

    public static String getRecipeLibraryPath() {
        return recipeLibraryPath;
    }

    public static void save() throws FileNotFoundException{

        instance.savedVersion = instance.getCurrentVersion();

        XMLEncoder e;

        synchronized (instance) {
            e = new XMLEncoder(
                    new BufferedOutputStream(
                        new FileOutputStream(settingsFilePath)));
            e.writeObject(instance);
            e.close();
        }
    }

    /**
     * Replaces instance
     */
    public static void load() {

        if(new File(settingsFilePath).exists()){

            FileInputStream is = null;
            try {
                is = new FileInputStream(settingsFilePath);
            } catch (FileNotFoundException e) {
                log.error("", e);
            }

            XMLDecoder decoder = new XMLDecoder(is);
            instance = (Settings)decoder.readObject();
            decoder.close();
        }
        else{
            instance.setDefaults();
        }

        InputStream is = Settings.class.getResourceAsStream("/settings.properties");
        try {
            instance.resources.load(is);
        } catch (Exception e) {
            log.error("failed to load properties, exit", e);
            Platform.exit();
        }
        finally{
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
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
    private static void extractResource(String filename, boolean overwrite){

        String targetfile = settingsDirPath + File.separator + filename;

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

    private void setDefaults() {
        windowPosition.setValues(50.0, 50.0, 800.0, 600.0);
    }

    public Date getCurrentVersion(){

        String ver = resources.getProperty("version", "");

        String[] tokens = ver.split("-");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Integer.valueOf(tokens[0]),
                Integer.valueOf(tokens[1]) - 1,    // month is 0 bazed
                Integer.valueOf(tokens[2]),
                Integer.valueOf(tokens[3]),
                Integer.valueOf(tokens[4]));
        Date date = cal.getTime();

        return date;
    }

    public Date getSavedVersion() {
        return savedVersion;
    }

    // required for xml serializer to work
    public void setSavedVersion(Date savedVersion) {
        this.savedVersion = savedVersion;
    }

    public Properties getResources(){
        return resources;
    }

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
