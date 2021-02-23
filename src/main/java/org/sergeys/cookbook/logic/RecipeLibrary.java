package org.sergeys.cookbook.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public final class RecipeLibrary {

    private static class LazyHolder {
        static final RecipeLibrary INSTANCE = new RecipeLibrary();
    }

    public static RecipeLibrary getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static final Logger log = LoggerFactory.getLogger(RecipeLibrary.class);

    // word - tag, prefix - tag
    private final HashMap<String, String> fullwords = new HashMap<>();
    private final HashMap<String, String> prefixes = new HashMap<>();

    private RecipeLibrary(){

        // TODO support suggestions for other languages
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        getClass().getResourceAsStream("/tagsuggestions_ru.txt"), StandardCharsets.UTF_8))){

            String s;

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

        } catch (IOException e) {
            log.error("failed to parse tag suggestions", e);
        }
    }

    public void validate(){

//        try {
//            Thread.sleep(1400);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }


        try {
            final Database db = new Database();
            final List<Recipe> recipes = db.getAllRecipes();
            db.close();

            final ExecutorService executor = Executors.newCachedThreadPool();

            Instant start = Instant.now();

            recipes.parallelStream().forEach(r -> {

                final File f = new File(r.getUnpackedFilename());
                if(f.exists()){
                    //log.debug("already unpacked {}", f.getAbsolutePath());
                }
                else {
                    Task<Void> task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            log.debug("unpacking {}", f.getAbsolutePath());

                            final File temp = File.createTempFile("cookbook", ".jar");
                            temp.deleteOnExit();

                            final Database db = new Database();

                            db.extractRecipeFile(r.getHash(), temp);
                            db.close();

                            Util.unpackJar(temp, r.getUnpackedDir());

                            temp.delete();
                            return null;
                        }
                    };

                    executor.execute(task);
                }
            });

            log.debug("======================== shutdown validate ========================");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.MINUTES);
            log.debug("======================== done validate ========================");

            Instant finish = Instant.now();
            log.debug("=== validation time: {}", Duration.between(start, finish));

        } catch (Exception e) {
            log.error("", e);
        }
    }

    public List<String> suggestTags(String phrase){

        final HashSet<String> tags = new HashSet<>();
        final String[] words = phrase.split("[\\n\\r\\t\\p{Space}\\p{Punct}]");

        for(String word: words){
            final String wordLc = word.trim().toLowerCase();

            if(fullwords.containsKey(wordLc)){
                tags.add(fullwords.get(wordLc));
            }
            prefixes.entrySet().forEach(e -> {
                if(wordLc.startsWith(e.getKey())) {
                    tags.add(e.getValue());
                }
            });
        }

        return List.copyOf(tags);
    }
}
