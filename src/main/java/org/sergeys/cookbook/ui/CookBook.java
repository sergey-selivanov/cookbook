package org.sergeys.cookbook.ui;

import java.net.URL;

import org.sergeys.cookbook.logic.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CookBook extends Application
{
    //private static final Logger log = LoggerFactory.getLogger(CookBook.class); // not initialized yet
    private static Logger log;

    private Stage primaryStage;

    static {
        Settings.getSettingsDirPath(); // this initializes System.setProperty("log4j.log.file"
    }

    public static void main(String[] args) {
        log = LoggerFactory.getLogger(CookBook.class);
        log.debug("main");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.debug("start");

        this.primaryStage = stage;

        URL location = getClass().getResource("/fxml/MainScene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(location);

        //Pane root = (Pane)fxmlLoader.load();
        Object o = fxmlLoader.load();
        Pane root = (Pane)o;


//        Panel panel = new Panel("bootstrap panel");
//        panel.getStyleClass().add("panel-primary");
//        panel.setBody(root);

        final MainController controller = (MainController)fxmlLoader.getController();
        Scene scene = new Scene(root);
        //Scene scene = new Scene(panel);
        scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css"); // https://github.com/kordamp/bootstrapfx
        primaryStage.setScene(scene);
        //primaryStage.sizeToScene();
//        controller.myInit(primaryStage);

        // http://stackoverflow.com/questions/15041332/javafx-splitpane-divider-position-inconsistent-behaviour

        primaryStage.setX(Settings.getInstance().getWinPosition().getWidth());
        primaryStage.setY(Settings.getInstance().getWinPosition().getHeight());
        primaryStage.setWidth(Settings.getInstance().getWinSize().getWidth());
        primaryStage.setHeight(Settings.getInstance().getWinSize().getHeight());

        primaryStage.show();

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                controller.setDivider();
            }});
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
