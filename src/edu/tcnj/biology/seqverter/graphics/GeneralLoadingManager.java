/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.graphics;

import java.util.Random;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class GeneralLoadingManager {

    public GeneralLoadingManager(String message) {
        this.message = DEFAULT_MESSAGE;
        if (message != null && !message.isEmpty()) {
            this.message = message;
        }
        initComponents();
    }

    private Stage loadingStage = null;
    private Scene loadingScene;
    private VBox loadingRoot;
    private ProgressBar loadingProgressBar;
    private HBox loadingText;
    private final String DEFAULT_MESSAGE = "Loading...";
    private String message;

    public void showLoading(boolean newColor) {
        if (newColor) {
            Random r = new Random();
            for (Node n : loadingText.getChildren()) {
                if (n instanceof Text) {
                    setFill((Label) n);
                }
            }
        }
        loadingStage.show();
    }

    private String decideForegroundColor(double[] rgb) {
        //https://codepen.io/znak/pen/aOvMOd

        for (int i = 0; i < rgb.length; i++) {
            if (rgb[i] < 0.03928) {
                rgb[i] = rgb[i] / 12.92;
            } else {
                rgb[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
            }
        }
        //luminance calculation
        double L = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
        //black if luminance exceeds threshold, white if does not
        return (L > 0.179 ? "#000000" : "#FFFFFF");
    }

    private void setFill(Label tx) {
        double[] rgb = {Math.random(),
            Math.random(),
            Math.random()};
        float base = 255f / 4f * 3f;
        tx.setStyle(" -fx-font: 54 Arial; -fx-font-weight: bold;"
                + "-fx-background-color: rgb(" + Math.round(base + Math.floor(rgb[0] * 255f) / 4f)
                + "," + Math.round(base + Math.floor(rgb[1] * 255f) / 4f) + ", " + Math.round(base + Math.floor(rgb[2] * 255f) / 4f) + ");"
                + "-fx-text-fill: #000000");
    }

    private void initComponents() {

        loadingProgressBar = new ProgressBar(-1);
        loadingText = new HBox();
        Random r = new Random();
        for (int k = 0; k < message.length(); k++) {
            Label currentText = new Label(String.valueOf(message.charAt(k)));
            setFill(currentText);
            loadingText.getChildren().add(currentText);
        }
        loadingRoot = new VBox(10);
        //loadingRoot.setPadding(new Insets(15));
        loadingRoot.getChildren().addAll(loadingText);

        loadingScene = new Scene(loadingRoot);

        loadingStage = new Stage();
loadingStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        //loadingStage.setTitle("Loading");
        loadingStage.setScene(loadingScene);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        //loadingStage.initStyle(StageStyle.UTILITY);
        loadingStage.setResizable(false);
    }

    public void exitLoading() {
        if (loadingStage != null) {
            loadingStage.hide();
        }
    }

}
