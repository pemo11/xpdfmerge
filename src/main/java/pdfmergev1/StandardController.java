package pdfmergev1;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class StandardController {
    private Stage currentStage;

    // @FXML
    // private Label welcomeText;

    public StandardController() {
        // Hier passiert im Moment noch nichts
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

}