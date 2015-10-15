package org.sergeys.cookbook.ui;

import java.io.FileNotFoundException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.sergeys.cookbook.logic.Settings;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private SplitPane splitPane;


    @FXML private MenuItem menuItemImport;
    @FXML private MenuItem menuItemExit;
    @FXML private MenuItem menuItemViewLog;
    @FXML private MenuItem menuItemAbout;


    public void initialize(){
        System.out.println("init");
    }

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



}
