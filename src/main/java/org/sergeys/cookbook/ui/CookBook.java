package org.sergeys.cookbook.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.kordamp.bootstrapfx.BootstrapFX;
import org.sergeys.cookbook.logic.Database;
import org.sergeys.cookbook.logic.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CookBook extends Application
{
    //private static final Logger log = LoggerFactory.getLogger(CookBook.class); // not initialized yet
    private static Logger log;

    private Stage primaryStage;

    public static void main(String[] args) {
        SettingsManager.getInstance(); // this initializes System.setProperty("log4j.log.file"

        log = LoggerFactory.getLogger(CookBook.class);

        Properties version = SettingsManager.getInstance().getVersion();

        log.info("=================================================================");
        log.info("CookBook {}", version.getProperty("version", "unknown version"));
        log.info("rev      {} {}", version.getProperty("git.commit", "unknown"), version.getProperty("git.date", ""));
        log.info("built    {}", version.getProperty("build.date", " at unknown date"));
        log.info("built by {}", version.getProperty("build.host", "- host unknown"));
        log.info("build number {}", version.getProperty("hudson.build.number", "unknown"));
        log.info("{} {} {}", System.getProperty("java.vm.name"), System.getProperty("java.runtime.version"), System.getProperty("java.home"));
        log.info("-----------------------------------------------------------------");

        try {
            Database.validate();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // TODO show something
        }

        launch(args);
    }

    private void startTransparent() throws IOException {
        Pane root = new StackPane();

        URL location = getClass().getResource("/fxml/MainScene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);

        Pane mainPane = (Pane)fxmlLoader.load();
        final MainController mainController = (MainController)fxmlLoader.getController();

        //fxmlLoader.setLocation(getClass().getResource("/fxml/ProgressPane.fxml"));
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ProgressPane.fxml"));
        Pane progressPane = (Pane)fxmlLoader.load();

        root.getChildren().addAll(mainPane, progressPane);
        progressPane.setVisible(false);

        Scene scene = new Scene(root);
        //scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css"); // https://github.com/kordamp/bootstrapfx
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        primaryStage.setScene(scene);

        primaryStage.setTitle("CookBook");
        primaryStage.getIcons().add(new Image(CookBook.class.getResourceAsStream("/images/amor.png")));

        primaryStage.setX(SettingsManager.getInstance().getSettings().getWindowPosition().getX());
        primaryStage.setY(SettingsManager.getInstance().getSettings().getWindowPosition().getY());
        primaryStage.setWidth(SettingsManager.getInstance().getSettings().getWindowPosition().getWidth());
        primaryStage.setHeight(SettingsManager.getInstance().getSettings().getWindowPosition().getHeight());

        primaryStage.show();

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                mainController.setDivider();
            }});
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.debug("start");
        this.primaryStage = stage;
        startTransparent();
    }

    @Override
    public void init() throws Exception {
        super.init();
        log.debug("init");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        log.debug("stop");
    }
}
