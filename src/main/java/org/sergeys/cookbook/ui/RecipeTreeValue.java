package org.sergeys.cookbook.ui;

import org.sergeys.cookbook.logic.Recipe;
import org.sergeys.cookbook.logic.Tag;

public class RecipeTreeValue {
    public enum Type { Tag, Recipe };

    private Type type;
    private Tag tag;
    private Recipe recipe;

    public RecipeTreeValue(Tag tag){
        setType(Type.Tag);
        this.setTag(tag);
    }

    public RecipeTreeValue(Recipe recipe){
        setType(Type.Recipe);
        this.setRecipe(recipe);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

	@Override
	public String toString() {
		
		if(type == Type.Recipe){
			return recipe.getTitle();
		}
		else{
			String t = tag.getVal();
			if(t.length() > 1){
				return t.substring(0, 1).toUpperCase() + t.substring(1);
			}
			else{
				return t;
			}			 
		}				 
	}
}
