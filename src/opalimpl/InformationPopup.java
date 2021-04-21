/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opalimpl;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 *
 * @author Avi Shah
 */
public class InformationPopup {

    private TextFlow textFlow;

    private Stage stage;
    private VBox root;
    private Scene scene;

    public InformationPopup(Label about, String id, String command, String description) {

        about.setCursor(Cursor.HAND);

        about.setOnMouseEntered(e -> {
            about.setTextFill(Color.BLUE);
            about.setUnderline(true);
        });

        about.setOnMouseExited(e -> {
            about.setTextFill(Color.BLACK);
            about.setUnderline(false);
        });

        about.setOnMouseClicked(e -> {
            display(true);
        });

        Text text1 = new Text(command + System.lineSeparator());
        text1.setFill(Color.BLACK);
        text1.setFont(Font.font("Arial", FontWeight.BLACK, 24));
        Text text2 = new Text(description);
        text2.setFill(Color.DARKBLUE);
        text2.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        textFlow = new TextFlow(text1, text2);

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("About command: " + command);
        root = new VBox();
        root.getChildren().add(textFlow);
        root.setPadding(new Insets(15));
        scene = new Scene(root, 600, 400);
        stage.setScene(scene);

    }

    private void display(boolean display) {
        if (display) {
            stage.show();
            stage.requestFocus();
        } else {
            stage.hide();
        }
    }

}
