package org.sergeys.cookbook.logic;

import java.io.File;

public class Recipe {

    private long id;
    private String hash;
    private String title;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public String getUnpackedDir(){
    	return Settings.getRecipeLibraryPath() + File.separator + hash.charAt(0);
    }
    
    public String getUnpackedFilename(){
    	return getUnpackedDir() + File.separator + hash + ".html";
    }
}
