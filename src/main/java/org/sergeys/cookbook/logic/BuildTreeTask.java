package org.sergeys.cookbook.logic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sergeys.cookbook.ui.RecipeTreeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BuildTreeTask extends Task<ObservableList<TreeItem<RecipeTreeValue>>>{

    private final static Image tagIcon;
    private final static Image favIcon;

    static {
        tagIcon = new Image(BuildTreeTask.class.getResourceAsStream("/images/folder_yellow.png"));
        favIcon = new Image(BuildTreeTask.class.getResourceAsStream("/images/metacontact_online.png"));
    }


    private final Logger log = LoggerFactory.getLogger(BuildTreeTask.class);

    private final Database db = new Database();

    public BuildTreeTask() {

    }

    @Override
    protected ObservableList<TreeItem<RecipeTreeValue>> call() throws Exception {

        ObservableList<TreeItem<RecipeTreeValue>> result = FXCollections.observableArrayList();

        buildTree(result);

        return result;
    }

    @Override
    protected void updateProgress(long workDone, long max) {

        log.debug("updateProgress");
        super.updateProgress(workDone, max);
    }

    private void buildTree(List<TreeItem<RecipeTreeValue>> treeItemsList){

        log.debug("buildtree");


        try {
            ArrayList<Tag> tags = db.getRootTags();
            for(Tag t: tags){

                TreeItem<RecipeTreeValue> item;
                if(t.getVal().equals("favorites")){
                    item = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(t), new ImageView(favIcon));
                }
                else{
                    item = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(t), new ImageView(tagIcon));
                }

                if(t.getSpecialid() == Tag.SPECIAL_OTHER){
                    List<Recipe> recipes = db.getRecipesWithoutTags();
                    for(Recipe r: recipes){
                        //TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r), new ImageView(recipeIcon));
                        TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
                        item.getChildren().add(ritem);
                    }
                    //tree.getRoot().getChildren().add(item);
                    treeItemsList.add(item);
                }
                else{
                    if(buildSubtree(item, t)){
                        //tree.getRoot().getChildren().add(item);
                        treeItemsList.add(item);
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

            List<Tag> tags = db.getChildrenTags(tag.getVal());
            for(Tag t: tags){
                TreeItem<RecipeTreeValue> titem = new TreeItem<RecipeTreeValue>();
                titem.setValue(new RecipeTreeValue(t));
                item.getChildren().add(titem);
                buildSubtree(titem, t);

                hasChildren = true;
            }

            List<Recipe> recipes = db.getRecipesByTag(tag.getVal());
            for(Recipe r: recipes){
                //TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r), new ImageView(recipeIcon));
                TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
                item.getChildren().add(ritem);

                hasChildren = true;
            }

        } catch (Exception e) {
            log.error("", e);
        }

        return hasChildren;
    }

    @Override
    protected void done() {
        try {
            db.close();
        } catch (SQLException ex) {
            log.error("", ex);
        }
        super.done();
    }


}