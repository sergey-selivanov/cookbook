package org.sergeys.cookbook.logic;

import java.io.File;
import java.io.FilenameFilter;

import org.sergeys.cookbook.logic.HtmlImporter.Status;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

public class MassImportTask extends Task<Void> implements ChangeListener<HtmlImporter.Status>
{
    private File directory;
    private HtmlImporter importer;
    private boolean canContinue;

    private Object sync = new Object();

    public MassImportTask(File directory, HtmlImporter importer){
        this.directory = directory;
        this.importer = importer;
    }

    @Override
    protected Void call()  {
//throws Exception
        importer.statusProperty().addListener(this);

        String[] files = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File f = new File(dir.getAbsolutePath() + File.separator + filename);
                return f.isFile() && (filename.toLowerCase().endsWith(".html") || filename.toLowerCase().endsWith(".htm"));
            }
        });

        int count = 0;
        for(final String file: files){

            if (isCancelled()) {
                updateMessage("Cancelled");
                break;
            }

            updateProgress(count++, files.length);

            synchronized (sync) {
                canContinue = false;
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    try{
                        importer.importFile(new File(directory.getAbsolutePath() + File.separator + file));
                    }
                    catch(Exception ex){
                        Settings.getLogger().error("", ex);
                        synchronized (sync) {
                            canContinue = true;
                        }
                    }
                }
            });
            //importer.Import(new File(directory.getAbsolutePath() + File.separator + file));

            boolean cont = false;
            int waitcount = 0;
            while(!cont){
                synchronized (sync) {
                    cont = canContinue;
                }

                if(!cont){
                    
                    try{
                        Thread.sleep(500);
                    }
                    catch (InterruptedException interrupted) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            break;
                        }
                    }
                }

                waitcount++;

                if(waitcount > 30){
                    break;
                }
            }
        }

        return null;
    }

    @Override
    public void changed(ObservableValue<? extends Status> observable,
            Status oldValue, Status newValue) {

        //System.out.println("> status in task " + newValue);
        synchronized (sync) {
            if(newValue == Status.Complete ||
               newValue == Status.AlreadyExist ||
               newValue == Status.Failed){

                canContinue = true;
                //System.out.println("> can continue");
            }
            else{
                canContinue = false;
            }
        }

    }

}
