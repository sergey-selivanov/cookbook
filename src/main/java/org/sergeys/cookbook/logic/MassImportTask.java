package org.sergeys.cookbook.logic;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.sergeys.cookbook.logic.ImportTask.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class MassImportTask extends Task<ImportTask.Status>
{
    private static final String HTML_FILES_GLOB = "glob:*.{html,htm}"; // case insensitive
    private final Logger log = LoggerFactory.getLogger(MassImportTask.class);

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Path directory;

    public MassImportTask(File directory){
        this.directory = directory.toPath();
    }

    @Override
    protected ImportTask.Status call() throws Exception {

//        try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory, HTML_FILES_GLOB)){ // case insensitive(?)
//
//        	PathMatcher matcher = directory.getFileSystem().getPathMatcher(HTML_FILES_GLOB);
//
//            long count = Files.list(directory)
//            		.filter(p -> matcher.matches(p))
//            		.count();
//            log.debug("count: " + count);
//
//            stream.forEach(path -> {
//                log.debug("= " + path.getFileName());
//            });
//        }

        try {
            PathMatcher matcher = directory.getFileSystem().getPathMatcher(HTML_FILES_GLOB);

            final long total;

            try(Stream<Path> stream = Files.list(directory)){
                total = stream
                    .filter(p -> matcher.matches(p.getFileName()))
                    .count();
            }

            AtomicLong processed = new AtomicLong();

            try(Stream<Path> stream = Files.list(directory)){

                // TODO count files??

                //long count = stream.count();
                //log.debug("count: " + count);

                stream
                    .filter(p -> matcher.matches(p.getFileName()))
                    .forEach(p -> {
                        log.debug("= {}", p.getFileName());

                        ImportTask importTask = new ImportTask(p.toFile());
                        importTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                            @Override
                            public void handle(WorkerStateEvent event) {
                                log.debug("=== done");
                                //processed.incrementAndGet();
                                updateProgress(processed.incrementAndGet(), total);
                            }
                        });

                        executor.execute(importTask);
                    });

            }

        }
        catch(Exception ex) {
            log.error("", ex);

        }

        log.debug("======================== shutdown mass import ========================");
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        log.debug("======================== done mass import ========================");

        return Status.Complete;
    }

//    @Override
//    protected void done() {
//
//        try {
//            executor.shutdown();
//            executor.awaitTermination(3, TimeUnit.SECONDS);
//            log.debug("shutdown");
//        } catch (InterruptedException ex) {
//            log.error("", ex);
//        }
//        super.done();
//    }


}
