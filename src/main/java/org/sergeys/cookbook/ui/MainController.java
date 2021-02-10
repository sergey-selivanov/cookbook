package org.sergeys.cookbook.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.sergeys.cookbook.logic.BuildTreeTask;
import org.sergeys.cookbook.logic.Database;
import org.sergeys.cookbook.logic.ImportTask;
import org.sergeys.cookbook.logic.MassImportTask;
import org.sergeys.cookbook.logic.Recipe;
import org.sergeys.cookbook.logic.RecipeLibrary;
import org.sergeys.cookbook.logic.SettingsManager;
import org.sergeys.cookbook.ui.RecipeTreeValue.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainController {

    private final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane mainBorderPane;
    @FXML private SplitPane splitPane;

    @FXML private MenuItem menuItemImport;
    @FXML private MenuItem menuItemExit;
    @FXML private MenuItem menuItemViewLog;
    @FXML private MenuItem menuItemAbout;

    @FXML private TextField title;
    @FXML private TextArea tags;

    @FXML private TreeView<RecipeTreeValue> tree;
    @FXML private WebView webview;

    private final FileChooser fc = new FileChooser();

    private final ExecutorService singleExecutor = Executors.newSingleThreadExecutor();

    private final Database db = new Database();

    // called by convention
    public void initialize(){
        log.debug("init");

        // TODO call in background
        try {
            //Database db = new Database();
//            db.upgradeOrCreateIfNeeded();
            //db.close();

            // TODO maybe in other place, while fx ui loads?
            //Database.validate();

        } catch (Exception ex) {
            log.error("", ex);
        }

        // TODO call in background
        //RecipeLibrary.getInstance().validate();


        final TreeItem<RecipeTreeValue> treeRoot = new TreeItem<RecipeTreeValue>();
        tree.setShowRoot(false);
        tree.setRoot(treeRoot);
        treeRoot.setExpanded(true);

        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree.getSelectionModel().selectedItemProperty().addListener(treeListener);

        rebuildTree();
    }


    private void rebuildTree(){
        final BuildTreeTask treeBuilder = new BuildTreeTask();
        treeBuilder.progressProperty().addListener(new ChangeListener<>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // TODO Auto-generated method stub
                log.debug("progress: {}", newValue.doubleValue());
            }
        });

        treeBuilder.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {

                ObservableList<TreeItem<RecipeTreeValue>> children;
                try {
                    children = treeBuilder.get();
                    tree.getRoot().getChildren().clear();
                    tree.getRoot().getChildren().addAll(children);

                    log.debug("tree built");
                } catch (InterruptedException | ExecutionException ex) {
                    log.error("failed", ex);
                }

            }
        });

        singleExecutor.execute(treeBuilder);
    }

    private ChangeListener<TreeItem<RecipeTreeValue>> treeListener = new ChangeListener<TreeItem<RecipeTreeValue>>(){

        @Override
        public void changed(
                ObservableValue<? extends TreeItem<RecipeTreeValue>> observable,
                TreeItem<RecipeTreeValue> oldValue,
                TreeItem<RecipeTreeValue> newValue) {

            if(newValue != null){
                if(newValue.getValue().getType() == Type.Recipe){
                    setRecipe(newValue.getValue().getRecipe());
                }
            }
        }};


    public void setDivider(){
        double pos = SettingsManager.getInstance().getSettings().getWinDividerPosition();

        if(pos == 0) {	// first launch
            pos = 0.2;
        }

        splitPane.setDividerPositions(pos);
    }

    public void onMenuItemExit(ActionEvent e){
        log.info("application exit");

        try {	// TODO do in different correct place

            final double pos = splitPane.getDividerPositions()[0];

            SettingsManager.getInstance().getSettings().setWinDividerPosition(pos);

            final Stage myStage = (Stage) mainBorderPane.getScene().getWindow();

            SettingsManager.getInstance().getSettings().getWindowPosition().setValues(myStage.getX(), myStage.getY(),
                    myStage.getWidth(), myStage.getHeight());

            SettingsManager.getInstance().saveSettings();

            // shutdown executors
            this.singleExecutor.shutdown();
            this.singleExecutor.awaitTermination(3, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException ex) {
            log.error("error on exit", ex);
        }

        Platform.exit();
    }


    public void onMenuImport(ActionEvent e){
        doImport();
    }

    public void onMenuMassImport(ActionEvent e){
        doMassImport();
    }

    public void onMenuItemViewLog(ActionEvent e){
    }


    private void showProgress() {

        mainBorderPane.setEffect(new BoxBlur());

        final Parent p = mainBorderPane.getParent();
        final Node progress = p.lookup("#paneProgress");
        final Node glass = p.lookup("#paneGlass");

        final FadeTransition ft = new FadeTransition(Duration.millis(500), glass);
        ft.setFromValue(0);
        ft.setToValue(0.5);

        progress.setVisible(true);
        ft.play();

    }

    private void hideProgress() {

        mainBorderPane.setEffect(null);

        final Parent p = mainBorderPane.getParent();
        final Node progress = p.lookup("#paneProgress");
        final Node glass = p.lookup("#paneGlass");

        final FadeTransition ft = new FadeTransition(Duration.millis(500), glass);
        ft.setFromValue(0.5);
        ft.setToValue(0);

        ft.setOnFinished(ev -> {
            progress.setVisible(false);
        });
        ft.play();
    }

    public void onMenuItemAbout(ActionEvent e){
        log.debug("about");
        //showProgress();

        try {
            Pane aboutPane = (Pane)new FXMLLoader(getClass().getResource("/fxml/About.fxml")).load();
            aboutPane.setVisible(true);

            Stage stage = new Stage();
            Scene scene = new Scene(aboutPane);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException ex) {
            log.error("failed", ex);
        }

    }

    private void setRecipe(Recipe recipe){
//        log.debug("setrecipe");

        final String url = new File(recipe.getUnpackedFilename()).toURI().toString();
        //log.debug("load: {}", url);
        webview.getEngine().load(url);

        setTitle(recipe);
    }

    private void setTitle(Recipe recipe){
        title.setText(recipe.getTitle());

        final List<String> recipeTags = db.getRecipeTags(recipe.getHash());
        tags.setText(String.join(", ", recipeTags));
    }


    private void doImport(){

        final File prev = new File(SettingsManager.getInstance().getSettings().getLastFilechooserLocation());
        if(prev.exists()){
            fc.setInitialDirectory(prev);
        }

        final File file = fc.showOpenDialog(mainBorderPane.getScene().getWindow());
        if(file != null){
            SettingsManager.getInstance().getSettings().setLastFilechooserLocation(file.getParent());

            final ImportTask importTask = new ImportTask(file);

            importTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent event) {
                    try {
                        final ImportTask.ImportResult status = importTask.get();
                        log.debug("status: {}", status);
                        if(status == ImportTask.ImportResult.AlreadyExist){
                            log.debug("already exist");
                        }
                        else{
                            RecipeLibrary.getInstance().validate();
                            rebuildTree();
                        }

                    } catch (InterruptedException | ExecutionException ex) {
                        log.error("", ex);
                    }
                }
            });
            importTask.progressProperty().addListener(new ChangeListener<>() {

                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    log.debug("import progress: {}", newValue);
                }
            });

            singleExecutor.execute(importTask);
        }
    }

    private final DoubleProperty progress = new SimpleDoubleProperty();

    public DoubleProperty progressProperty() {
        return progress;
    }

    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double val) {
        progress.set(val);
    }

  private void doMassImport(){

      final DirectoryChooser dc = new DirectoryChooser();

      final File prev = new File(SettingsManager.getInstance().getSettings().getLastFilechooserLocation());
      if(prev.exists()){
          dc.setInitialDirectory(prev.getParentFile());
      }

      final File dir = dc.showDialog(mainBorderPane.getScene().getWindow());

      if(dir == null){
          return;
      }

      SettingsManager.getInstance().getSettings().setLastFilechooserLocation(dir.toString());

      final MassImportTask massImportTask = new MassImportTask(dir);
      massImportTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    final MassImportTask.MassImportResult status = massImportTask.get();
                    //log.debug("status: {}", status);
//                    if(status != ImportTask.Status.Complete){
//                        log.debug("failed: {}", status);
//                    }
//                    else{
//                        // TODO reset progress
//                        RecipeLibrary.getInstance().validate();
//                        rebuildTree();
//                    }
                    log.debug("processed: {}, alreadyExist: {}, imported: {}, failed: {}",
                            status.getProcessed(), status.getAlreadyExist(),
                            status.getImported(), status.getFailed());

                    RecipeLibrary.getInstance().validate();
                    rebuildTree();

                } catch (InterruptedException | ExecutionException ex) {
                    log.error("", ex);
                }

                hideProgress();
            }
      });

      massImportTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
          @Override
          public void handle(WorkerStateEvent event) {
              log.error("failed: {}", event.getSource().getException());
              hideProgress();
          }
      });

      massImportTask.progressProperty().addListener(new ChangeListener<>() {

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            log.debug("import progress: {}", newValue);
        }
      });


      final Parent p = mainBorderPane.getParent();
      final ProgressBar progressBar = (ProgressBar)p.lookup("#progressBar");
      progressBar.progressProperty().bind(massImportTask.progressProperty()); // TODO allowed? or bind via our member field?

      final Label message = (Label)p.lookup("#lblMessage");
      message.setText("Import files from " + dir);

      showProgress();
      singleExecutor.execute(massImportTask);
  }

}
