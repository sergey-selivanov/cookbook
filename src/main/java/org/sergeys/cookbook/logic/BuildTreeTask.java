package org.sergeys.cookbook.logic;

import java.sql.SQLException;
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

        final ObservableList<TreeItem<RecipeTreeValue>> result = FXCollections.observableArrayList();

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
            final List<Tag> tags = db.getRootTags();

            for(Tag t: tags){

                final TreeItem<RecipeTreeValue> item =
                        new TreeItem<RecipeTreeValue>(new RecipeTreeValue(t), new ImageView(
                                "favorites".equals(t.getVal()) ? favIcon : tagIcon
                        ));

                if(t.getSpecialid() == Tag.SPECIAL_OTHER){
                    final List<Recipe> recipes = db.getRecipesWithoutTags();
                    for(Recipe r: recipes){
                        final TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
                        item.getChildren().add(ritem);
                    }
                    treeItemsList.add(item);
                }
                else{
                    if(buildSubtree(item, t)){
                        treeItemsList.add(item);
                    }
                }
            }

        } catch (Exception e) {
            log.error("boom", e);
        }
    }

    private boolean buildSubtree(TreeItem<RecipeTreeValue> item, Tag tag){

        boolean hasChildren = false;

        try {

            final List<Tag> tags = db.getChildrenTags(tag.getVal());
            for(Tag t: tags){
                final TreeItem<RecipeTreeValue> titem = new TreeItem<RecipeTreeValue>();
                titem.setValue(new RecipeTreeValue(t));
                item.getChildren().add(titem);
                buildSubtree(titem, t);

                hasChildren = true;
            }

            final List<Recipe> recipes = db.getRecipesByTag(tag.getVal());
            for(Recipe r: recipes){
                //TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r), new ImageView(recipeIcon));
                final TreeItem<RecipeTreeValue> ritem = new TreeItem<RecipeTreeValue>(new RecipeTreeValue(r));
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
