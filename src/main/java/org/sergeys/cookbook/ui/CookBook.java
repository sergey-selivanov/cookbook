package org.sergeys.cookbook.ui;

import java.io.IOException;
import java.util.Properties;

import org.kordamp.bootstrapfx.BootstrapFX;
import org.sergeys.cookbook.logic.Database;
import org.sergeys.cookbook.logic.RecipeLibrary;
import org.sergeys.cookbook.logic.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CookBook extends Application
{
    public interface InitCompletionHandler {
        void complete();
    }

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

        launch(args);
    }

    private void startTransparent() throws IOException {
        final Pane root = new StackPane();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainScene.fxml"));

        final Pane mainPane = (Pane)fxmlLoader.load();
        final MainController mainController = (MainController)fxmlLoader.getController();

        //fxmlLoader.setLocation(getClass().getResource("/fxml/ProgressPane.fxml"));
        fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ProgressPane.fxml"));
        final Pane progressPane = (Pane)fxmlLoader.load();

        root.getChildren().addAll(mainPane, progressPane);
        progressPane.setVisible(false);

        final Scene scene = new Scene(root);
        //scene.getStylesheets().add("org/kordamp/bootstrapfx/bootstrapfx.css"); // https://github.com/kordamp/bootstrapfx
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        primaryStage = new Stage(StageStyle.DECORATED);

        primaryStage.setScene(scene);

        primaryStage.setTitle("CookBook");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/amor.png")));

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
    public void start(final Stage initStage) throws Exception {
        log.debug("start");

        //this.primaryStage = initStage;
        //startTransparent();

        final Task<Boolean> initTask = new Task<>() {

            @Override
            protected Boolean call() {

                try {
                    updateProgress(0, 2);

                    updateMessage("Validating database...");
                    Database.validate();
                    updateProgress(1, 2);

                    updateMessage("Validating files...");
                    RecipeLibrary.getInstance().validate();
                    updateProgress(2, 2);

                    updateMessage("Done");
                }
                catch(Exception ex) {
                    log.error("failed", ex);
                    updateMessage(ex.getMessage());
                    return false;
                }

                return true;
            }
        };

        showSplash(
                initStage,
                initTask,
                () -> showMainStage(initTask.valueProperty())
        );

        new Thread(initTask).start();
    }

    // preparing-raw-barbeque-chicken-cooking.jpg
    private static final int SPLASH_WIDTH = 626;
    private static final int SPLASH_HEIGHT = 214;

    private void showSplash(
            final Stage initStage,
            Task<?> task,
            InitCompletionHandler initCompletionHandler
    ) {
        /*
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadProgress.progressProperty().unbind();
                loadProgress.setProgress(1);
                initStage.toFront();
                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
                fadeSplash.setFromValue(1.0);
                fadeSplash.setToValue(0.0);
                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
                fadeSplash.play();

                initCompletionHandler.complete();
            } // todo add code to gracefully handle other task states.
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        splashScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show();
        */

        try {
            final Pane splashPane = (Pane)new FXMLLoader(getClass().getResource("/fxml/Splash.fxml")).load();

            ((Label)splashPane.lookup("#lblMessage")).textProperty().bind(task.messageProperty());
            final ProgressBar progressBar = ((ProgressBar)splashPane.lookup("#progressBar"));
            progressBar.progressProperty().bind(task.progressProperty());

            task.stateProperty().addListener((observableValue, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(1);
                    initStage.toFront();
//	                FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
//	                fadeSplash.setFromValue(1.0);
//	                fadeSplash.setToValue(0.0);
//	                fadeSplash.setOnFinished(actionEvent -> initStage.hide());
//	                fadeSplash.play();

                    splashPane.setVisible(false);

                    initCompletionHandler.complete();
                } // todo add code to gracefully handle other task states.
            });

            final Scene splashScene = new Scene(splashPane, Color.TRANSPARENT);
//            splashScene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            final Rectangle2D bounds = Screen.getPrimary().getBounds();
            initStage.setScene(splashScene);
            initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
            initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
            initStage.initStyle(StageStyle.TRANSPARENT);
            initStage.setAlwaysOnTop(true);
            initStage.show();
        } catch (IOException ex) {
            log.error("failed", ex);
        }
    }

    private void showMainStage(ReadOnlyObjectProperty<Boolean> success) {
        try {
            startTransparent();
        } catch (IOException e) {
            log.error("", e);
        }
    }

//    private Pane splashLayout;
//    private ProgressBar loadProgress;
//    private Label progressText;
//
//    private static final int SPLASH_WIDTH = 676;
//    private static final int SPLASH_HEIGHT = 227;

    @Override
    public void init() throws Exception {
        super.init();
        log.debug("init");

        /*
        ImageView splash = new ImageView(new Image(getClass().getResourceAsStream("/images/splash.png")));

        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
        //progressText = new Label("Will find friends for peanuts . . .");
        progressText = new Label("");
        splashLayout = new VBox();
        //splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        splashLayout.getChildren().addAll(splash, progressText, loadProgress);
        progressText.setAlignment(Pos.CENTER);

//        splashLayout.setStyle(
//                "-fx-padding: 5; " +
//                "-fx-background-color: cornsilk; " +
//                "-fx-border-width:5; " +
//                "-fx-border-color: " +
//                    "linear-gradient(" +
//                        "to bottom, " +
//                        "chocolate, " +
//                        "derive(chocolate, 50%)" +
//                    ");"
//        );

        //splashLayout.setStyle("-fx-padding: 5; -fx-border-width:5;");
        splashLayout.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        splashLayout.setEffect(new DropShadow());
        */
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        log.debug("stop");
    }
}
