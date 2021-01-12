package org.sergeys.cookbook.ui;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.sergeys.cookbook.logic.Settings;
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
import javafx.scene.Node;
import javafx.scene.Parent;
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
// TODO do flyway
log.error(">>> TODO upgradeOrCreateIfNeeded database here");
//            db.upgradeOrCreateIfNeeded();
            //db.close();
        } catch (Exception ex) {
            log.error("", ex);
        }

        // TODO call in background
        RecipeLibrary.getInstance().validate();


        TreeItem<RecipeTreeValue> treeRoot = new TreeItem<RecipeTreeValue>();
        tree.setShowRoot(false);
        tree.setRoot(treeRoot);
        treeRoot.setExpanded(true);

        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree.getSelectionModel().selectedItemProperty().addListener(treeListener);

        rebuildTree();
    }


    private void rebuildTree(){
        BuildTreeTask treeBuilder = new BuildTreeTask();
        treeBuilder.progressProperty().addListener(new ChangeListener<>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // TODO Auto-generated method stub
                log.debug("progress: " + newValue.doubleValue());
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
        double pos = Settings.getInstance().getWinDividerPosition();

        if(pos == 0) {	// first launch
            pos = 0.2;
        }

        splitPane.setDividerPositions(pos);
    }

    public void onMenuItemExit(ActionEvent e){
        log.info("application exit");

        try {	// TODO do in different correct place

            double pos = splitPane.getDividerPositions()[0];
//            System.out.println("<" + pos);

            Settings.getInstance().setWinDividerPosition(pos);

//            System.out.println(Settings.getInstance().getWinDividerPosition());

            Stage myStage = (Stage) mainBorderPane.getScene().getWindow();

//            Settings.getInstance().getWinPosition().setSize(myStage.getX(), myStage.getY());
//            Settings.getInstance().getWinSize().setSize(myStage.getWidth(), myStage.getHeight());

            Settings.getInstance().getWindowPosition().setValues(myStage.getX(), myStage.getY(),
                    myStage.getWidth(), myStage.getHeight());

            Settings.save();

            // shutdown executors
            this.singleExecutor.shutdown();
            this.singleExecutor.awaitTermination(3, TimeUnit.SECONDS);
            //RecipeLibrary.getInstance().shutdown();

        } catch (FileNotFoundException | InterruptedException ex) {
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

        Parent p = mainBorderPane.getParent();
        Node progress = p.lookup("#paneProgress");
        Node glass = p.lookup("#paneGlass");

        FadeTransition ft = new FadeTransition(Duration.millis(500), glass);
        ft.setFromValue(0);
        ft.setToValue(0.5);

        progress.setVisible(true);
        ft.play();

    }

    private void hideProgress() {

        mainBorderPane.setEffect(null);

        Parent p = mainBorderPane.getParent();
        Node progress = p.lookup("#paneProgress");
        Node glass = p.lookup("#paneGlass");

        FadeTransition ft = new FadeTransition(Duration.millis(500), glass);
        ft.setFromValue(0.5);
        ft.setToValue(0);

        ft.setOnFinished(ev -> {
            progress.setVisible(false);
        });
        ft.play();
    }

    public void onMenuItemAbout(ActionEvent e){
        log.debug("about");
        showProgress();
    }

    //private Recipe currentRecipe;

    private void setRecipe(Recipe recipe){
        log.debug("setrecipe");

        //currentRecipe = recipe;

//        String filename = Settings.getSettingsDirPath() + File.separator + Settings.RECIPES_SUBDIR +
//                File.separator + recipe.getHash() + ".html";
//        webview.getEngine().load(new File(filename).toURI().toString());

        //webview.getEngine().load(new File(currentRecipe.getUnpackedFilename()).toURI().toString());
        webview.getEngine().load(new File(recipe.getUnpackedFilename()).toURI().toString());

        setTitle(recipe);
    }

    private void setTitle(Recipe recipe){
        title.setText(recipe.getTitle());

        try {
            //List<String> t = Database.getInstance().getRecipeTags(recipe.getHash());
            List<String> t = db.getRecipeTags(recipe.getHash());
            StringBuilder sb = new StringBuilder();
            for(String s: t){
                if(sb.length() > 0){
                    sb.append(", ");
                }
                sb.append(s);
            }
            tags.setText(sb.toString());
        } catch (Exception e) {
            log.error("", e);
        }

    }


    private void doImport(){

        File prev = new File(Settings.getInstance().getLastFilechooserLocation());
        if(prev.exists()){
            fc.setInitialDirectory(prev);
        }

        final File file = fc.showOpenDialog(mainBorderPane.getScene().getWindow());
        if(file != null){
            Settings.getInstance().setLastFilechooserLocation(file.getParent());

            ImportTask importTask = new ImportTask(file);

            importTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @Override
                public void handle(WorkerStateEvent event) {
                    try {
                        ImportTask.Status status = importTask.get();
                        log.debug("status: " + status);
                        if(status == ImportTask.Status.AlreadyExist){
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
                    log.debug("import progress: " + newValue);
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

      DirectoryChooser dc = new DirectoryChooser();

      File prev = new File(Settings.getInstance().getLastFilechooserLocation());
      if(prev.exists()){
          dc.setInitialDirectory(prev.getParentFile());
      }

      final File dir = dc.showDialog(mainBorderPane.getScene().getWindow());

      if(dir == null){
          return;
      }

      Settings.getInstance().setLastFilechooserLocation(dir.toString());

      MassImportTask massImportTask = new MassImportTask(dir);
      massImportTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    ImportTask.Status status = massImportTask.get();
                    log.debug("status: " + status);
                    if(status != ImportTask.Status.Complete){
                        log.debug("failed: " + status);
                    }
                    else{
                        // TODO reset progress
                        RecipeLibrary.getInstance().validate();
                        rebuildTree();
                    }

                } catch (InterruptedException | ExecutionException ex) {
                    log.error("", ex);
                }

                hideProgress();

            }
      });

      massImportTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
          @Override
          public void handle(WorkerStateEvent event) {
              log.error("failed: " + event.getSource().getException());
              hideProgress();
          }
      });

      massImportTask.progressProperty().addListener(new ChangeListener<>() {

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            log.debug("import progress: " + newValue);
        }
      });


      Parent p = mainBorderPane.getParent();
      ProgressBar progressBar = (ProgressBar)p.lookup("#progressBar");
      progressBar.progressProperty().bind(massImportTask.progressProperty()); // TODO allowed? or bind via our member field?

      Label message = (Label)p.lookup("#lblMessage");
      message.setText("Import files from " + dir);

      showProgress();
      singleExecutor.execute(massImportTask);
  }

}
