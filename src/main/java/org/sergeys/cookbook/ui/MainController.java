package org.sergeys.cookbook.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.sergeys.cookbook.logic.Database;
import org.sergeys.cookbook.logic.Recipe;
import org.sergeys.cookbook.logic.RecipeLibrary;
import org.sergeys.cookbook.logic.Settings;
import org.sergeys.cookbook.logic.Tag;
import org.sergeys.cookbook.ui.RecipeTreeValue.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {

    Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private BorderPane mainBorderPane;
    @FXML private SplitPane splitPane;

    @FXML private MenuItem menuItemImport;
    @FXML private MenuItem menuItemExit;
    @FXML private MenuItem menuItemViewLog;
    @FXML private MenuItem menuItemAbout;

    //@FXML private TextField title;
    //@FXML private TextArea tags;

    @FXML private TreeView<RecipeTreeValue> tree;
    @FXML private WebView webview;

    public void initialize(){
        //System.out.println("init");
        log.debug("init");


        // TODO call in background
        RecipeLibrary.getInstance().validate();

//        buttonSave.setVisible(false);
//        buttonRevert.setVisible(false);

        TreeItem<RecipeTreeValue> treeRoot = new TreeItem<RecipeTreeValue>();
        tree.setShowRoot(false);
        tree.setRoot(treeRoot);
        treeRoot.setExpanded(true);

        tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tree.getSelectionModel().selectedItemProperty().addListener(treeListener);

        buildTree();

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
        System.out.println(">" + pos);
        splitPane.setDividerPositions(pos);

    }

    public void onMenuItemExit(ActionEvent e){
        Settings.getLogger().info("application exit");

        try {

            double pos = splitPane.getDividerPositions()[0];
            System.out.println("<" + pos);

            Settings.getInstance().setWinDividerPosition(pos);

            System.out.println(Settings.getInstance().getWinDividerPosition());

            Stage myStage = (Stage) mainBorderPane.getScene().getWindow();

            Settings.getInstance().getWinPosition().setSize(myStage.getX(), myStage.getY());
            Settings.getInstance().getWinSize().setSize(myStage.getWidth(), myStage.getHeight());

            Settings.save();
        } catch (FileNotFoundException e1) {
            Settings.getLogger().info("cannot save settings", e1);
        }

        Platform.exit();
    }

    public void onMenuItemImport(ActionEvent e){

    }
    public void onMenuItemViewLog(ActionEvent e){
    }

    public void onMenuItemAbout(ActionEvent e){
    }


    private Image tagIcon;
//  private Image recipeIcon;
  private Image favIcon;

  private void buildTree(){

      Settings.getLogger().debug("buildtree");

      tree.getRoot().getChildren().clear();

      if(tagIcon == null){
          try{
          tagIcon = new Image(getClass().getResourceAsStream("/images/folder_yellow.png"));
          //recipeIcon = new Image(getClass().getResourceAsStream("/images/free_icon.png"));
          favIcon = new Image(getClass().getResourceAsStream("/images/metacontact_online.png"));
          }
          catch(Exception ex){
              Settings.getLogger().error("", ex);
          }
      }

      try {
          ArrayList<Tag> tags = Database.getInstance().getRootTags();
          for(Tag t: tags){

              TreeItem<RecipeTreeValue> item;
              if(t.getVal().equals("favorites")){
                  item = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(t), new ImageView(favIcon));
              }
              else{
                  item = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(t), new ImageView(tagIcon));
              }

              if(t.getSpecialid() == Tag.SPECIAL_OTHER){
                  List<Recipe> recipes = Database.getInstance().getRecipesWithoutTags();
                  for(Recipe r: recipes){
                      //TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r), new ImageView(recipeIcon));
                      TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
                      item.getChildren().add(ritem);
                  }
                  tree.getRoot().getChildren().add(item);
              }
              else{
                  if(buildSubtree(item, t)){
                      tree.getRoot().getChildren().add(item);
                  }
              }
          }

      } catch (Exception e) {
          log.error("boom", e);
      }
  }

  private boolean buildSubtree(TreeItem<RecipeTreeValue> item, Tag tag){
      //Settings.getLogger().debug("buildsubtree");

      boolean hasChildren = false;

      try {

          List<Tag> tags = Database.getInstance().getChildrenTags(tag.getVal());
          for(Tag t: tags){
              TreeItem<RecipeTreeValue> titem = new TreeItem<RecipeTreeValue>();
              titem.setValue(new RecipeTreeValue(t));
              item.getChildren().add(titem);
              buildSubtree(titem, t);

              hasChildren = true;
          }

          List<Recipe> recipes = Database.getInstance().getRecipesByTag(tag.getVal());
          for(Recipe r: recipes){
              //TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r), new ImageView(recipeIcon));
              TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
              item.getChildren().add(ritem);

              hasChildren = true;
          }

      } catch (Exception e) {
          Settings.getLogger().error("", e);
      }

      return hasChildren;
  }


    private Recipe currentRecipe;

    private void setRecipe(Recipe recipe){
        log.debug("setrecipe");

        currentRecipe = recipe;

//        String filename = Settings.getSettingsDirPath() + File.separator + Settings.RECIPES_SUBDIR +
//                File.separator + recipe.getHash() + ".html";
//        webview.getEngine().load(new File(filename).toURI().toString());

        webview.getEngine().load(new File(currentRecipe.getUnpackedFilename()).toURI().toString());

        setTitle(recipe);
    }

    private void setTitle(Recipe recipe){
//        title.setText(recipe.getTitle());

        try {
            List<String> t = Database.getInstance().getRecipeTags(recipe.getHash());
            StringBuilder sb = new StringBuilder();
            for(String s: t){
                if(sb.length() > 0){
                    sb.append(", ");
                }
                sb.append(s);
            }
//            tags.setText(sb.toString());
        } catch (Exception e) {
            log.error("", e);
        }

    }

}
