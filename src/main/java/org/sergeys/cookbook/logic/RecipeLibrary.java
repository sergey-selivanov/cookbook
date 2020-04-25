package org.sergeys.cookbook.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;


public final class RecipeLibrary {

    private final Logger log = LoggerFactory.getLogger(RecipeLibrary.class);

    private static Object instanceLock = new Object();
    private static RecipeLibrary instance;

    private Hashtable<String, String> fullwords = new Hashtable<String, String>();
    private Hashtable<String, String> prefixes = new Hashtable<String, String>();

    // singleton
    private RecipeLibrary(){
        // TODO error handling here
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/tagsuggestions_ru.txt"), StandardCharsets.UTF_8));
        String s;
        try {
            while((s = br.readLine()) != null){
                if(!s.isEmpty() && !s.startsWith("#")){
                    String[] arr = s.split("=");
                    String[] words = arr[1].split(",");
                    for(String word: words){
                        word = word.trim().toLowerCase();
                        if(word.endsWith("-")){
                            prefixes.put(word.substring(0, word.length() - 1), arr[0].trim().toLowerCase());
                        }
                        else{
                            fullwords.put(word, arr[0].trim().toLowerCase());
                        }

                    }
                }
            }
        } catch (Exception e) {
            log.error("failed to parse tag suggestions", e);
        }
    }

    public static RecipeLibrary getInstance() {
        synchronized (instanceLock) {
            if(instance == null){
                instance = new RecipeLibrary();
            }
        }

        return instance;
    }

    //ExecutorService executor;

    public void validate(){
        try {
            Database db = new Database();
            //ArrayList<Recipe> recipes = Database.getInstance().getAllRecipes();
            ArrayList<Recipe> recipes = db.getAllRecipes();
            db.close();

            ExecutorService executor = Executors.newCachedThreadPool();

            for(final Recipe r: recipes){

                //final File f = new File(Settings.getRecipeLibraryPath() + File.separator + r.getHash() + ".html");
                //final File dir = new File(r.getUnpackedDir());
                final File f = new File(r.getUnpackedFilename());
                if(!f.exists()){

                    Task<Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            log.debug("unpacking " + f.getAbsolutePath());

                            File temp = File.createTempFile("cookbook", ".jar");
                            temp.deleteOnExit();

                            Database db = new Database();

                            db.extractRecipeFile(r.getHash(), temp);
                            db.close();

                            Util.unpackJar(temp, r.getUnpackedDir());

                            temp.delete();
                            return null;
                        }
                    };

                    //singleExecutor.execute(task);
                    executor.execute(task);
                }
            }

            log.debug("======================== shutdown validate ========================");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
            log.debug("======================== done validate ========================");


        } catch (Exception e) {
            log.error("", e);
        }
    }

    public List<String> suggestTags(String phrase){

        Hashtable<String, String> tags = new Hashtable<>();

        String[] words = phrase.split("[\\n\\r\\t\\p{Space}\\p{Punct}]");
        for(String word: words){
            word = word.trim().toLowerCase();
            if(fullwords.containsKey(word)){
                tags.put(fullwords.get(word), "");
            }

            for(Enumeration<String> en = prefixes.keys(); en.hasMoreElements();){
                String prefix = en.nextElement();
                if(word.startsWith(prefix)){
                    tags.put(prefixes.get(prefix), "");
                }
            }
        }

        return Collections.list(tags.keys());
    }

//    public boolean shutdown() throws InterruptedException {
//        singleExecutor.shutdown();
//        return singleExecutor.awaitTermination(3, TimeUnit.SECONDS);
//    }
}
