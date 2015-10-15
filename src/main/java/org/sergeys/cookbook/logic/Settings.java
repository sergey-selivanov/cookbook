package org.sergeys.cookbook.logic;

import java.awt.Dimension;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {

    public static final String SETTINGS_PATH = ".CookBook3-devel";
    public static final String SETTINGS_FILE = "settings.xml";
    public static final String LOG_FILE = "log.txt";
    public static final String RECIPES_SUBDIR = "recipes";

    private static String settingsDirPath;
    private static String settingsFilePath;
    private static String recipeLibraryPath;

    private Properties resources = new Properties();
    private Dimension winPosition = new Dimension();
    private Dimension winSize = new Dimension();
    private double winDividerPosition = 0;
    private String lastFilechooserLocation = "";
    private Date savedVersion = new Date(0);

    private static Settings instance = new Settings();
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private static Logger log;

    static{
        settingsDirPath = System.getProperty("user.home") + File.separator + SETTINGS_PATH;
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
                Settings.getLogger().error("", e);
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
            Settings.getLogger().error("failed to load properties, exit", e);
            Platform.exit();
        }
        finally{
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                Settings.getLogger().error("", e);
            }
        }

    }

    public static Logger getLogger(){
        return log;
    }

    public static ExecutorService getExecutor(){
        return executor;
    }

    public static ExecutorService getSingleExecutor(){
        return singleExecutor;
    }

    public static void shutdown(){
        singleExecutor.shutdown();
        executor.shutdown();
    }

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
        winSize.setSize(800.0, 600.0);
        winPosition.setSize(50.0, 50.0);
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

    public Dimension getWinPosition() {
        return winPosition;
    }

    public void setWinPosition(Dimension winPosition) {
        this.winPosition = winPosition;
    }

    public Dimension getWinSize() {
        return winSize;
    }

    public void setWinSize(Dimension winSize) {
        this.winSize = winSize;
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
}
