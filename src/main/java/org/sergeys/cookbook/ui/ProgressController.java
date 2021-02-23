package org.sergeys.cookbook.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ProgressController {

    private static final Logger log = LoggerFactory.getLogger(ProgressController.class);

    @FXML private StackPane paneProgress;
    @FXML private AnchorPane paneGlass;

    @FXML private Label lblMessage;
    @FXML private Button btnCancel;
    @FXML private ProgressBar progressBar;

    public void onButtonCancel(ActionEvent e){
        log.debug("cancel");

        Parent p = paneProgress.getParent();
        Node main = p.lookup("#mainBorderPane");
        main.setEffect(null);

        FadeTransition ft = new FadeTransition(Duration.millis(500), paneGlass);
        ft.setFromValue(0.5);
        ft.setToValue(0);

        ft.setOnFinished(ev -> {
            paneProgress.setVisible(false);
        });
        ft.play();
    }
}

