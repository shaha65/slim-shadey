/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade;

import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.EditorTab;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import opalimpl.OpalRunner;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositeAgreement {

    private Stage stage;
    private Scene scene;
    private VBox root;

    private HBox logoBox;

    private HBox agreeLabel;

    private VBox citebox;

    private EditorInterface PARENT_PROGRAM;

    public PrositeAgreement(PrositeShadeRemote parent, EditorInterface PROGRAM) {
        this.PARENT_PROGRAM = PROGRAM;

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        root = new VBox();

        logoBox = new HBox();
        logoBox.setAlignment(Pos.CENTER);
        //ScanJam

        char[] letters = "ScanJam".toCharArray();
        Label[] labels_logo = new Label[letters.length];

        for (int k = 0; k < letters.length; k++) {
            Label lb = new Label(String.valueOf(letters[k]));

            double[] rgb = {Math.random(),
                Math.random(),
                Math.random()};
            lb.setStyle(" -fx-font: Arial; -fx-font-size: 150; "
                    + "-fx-font-style: italic; "
                    + "-fx-background-color: rgb(" + Math.round(Math.floor(rgb[0] * 255f))
                    + "," + Math.round(Math.floor(rgb[1] * 255f)) + ", " + Math.round(Math.floor(rgb[2] * 255f)) + ");"
                    + "-fx-text-fill: " + this.decideForegroundColor(rgb));
            labels_logo[k] = lb;
            logoBox.getChildren().add(labels_logo[k]);
        }

        citebox = this.getCite();

        agreeLabel = this.getAgreeLabel(parent);

        root.getChildren().addAll(logoBox, citebox, agreeLabel);

        scene = new Scene(root, 800, 600, true);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

    }

    private ScrollPane sp1;
    private ScrollPane sp2;
    private TextArea ta1;
    private TextArea ta2;

    private VBox getCite() {

        String helpString1 = "ScanJam (ScanProsite Java model)"
                + " scans sequences using the Prosite database. "
                + System.lineSeparator()
                + System.lineSeparator()
                + "If you publish results using"
                + " ScanJam, please cite the paper that describes ScanProsite:"
                + System.lineSeparator()
                + System.lineSeparator();

        String helpString2 = "Hulo, N., Sigrist, C.J., Le Saux, V.,"
                + " Langendijk???Genevaux, P.S., Bordoli, L., Gattiker,"
                + " A., De Castro, E., Bucher, P. and Bairoch, A., (2004)"
                + " Recent improvements to the PROSITE database. ";

        String helpString3 = "Nucleic Acids Research";
        String helpString4 = ", 32(suppl_1), pp.D134-D137." + System.lineSeparator() + System.lineSeparator();

        ta1 = new TextArea();
        ta2 = new TextArea();

        ta1.appendText(helpString1);
        ta2.appendText(helpString2 + helpString3 + helpString4);

        ta1.setEditable(false);
        ta2.setEditable(false);

        ta1.setWrapText(true);
        ta2.setWrapText(true);

        ta1.setStyle(" -fx-font: 24 Arial; -fx-font-weight: bold;");
        ta2.setStyle(" -fx-font: 24 Arial; -fx-font-weight: bold; -fx-text-fill: blue");

        sp1 = new ScrollPane(ta1);
        sp2 = new ScrollPane(ta2);

        sp1.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp1.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp2.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox ret = new VBox();
        ret.getChildren().addAll(sp1, sp2);

        ta1.prefWidthProperty().bind(ret.widthProperty());
        ta2.prefWidthProperty().bind(ret.widthProperty());

        return ret;
    }

    private final static String AGREE_STYLE = " -fx-font: 48 Arial; -fx-font-weight:bold; -fx-text-fill:";
    private final static String DEFAULT_STYLE = "black;";
    private final static String HOVER_STYLE = "blue;";
    private final static String CLICKED_STYLE = "green;";

    private boolean clicked = false;
    private boolean mouse_in = false;

    Label agree;

    private HBox getAgreeLabel(PrositeShadeRemote origin) {

        HBox agreeContainer = new HBox();
        Label agree = new Label("I agree");
        agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));
        agree.setCursor(Cursor.HAND);
        agreeContainer.setAlignment(Pos.CENTER);

        agree.setOnMouseEntered(e -> {
            mouse_in = true;
            System.out.println("mouse_in = true;");
            if (!clicked) {
                agree.setStyle(AGREE_STYLE.concat(HOVER_STYLE));
            }
        });

        agree.setOnMouseExited(e -> {
            mouse_in = false;
            System.out.println("mouse_in = false;");
            if (!clicked) {
                agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));
            }
        });

        agree.setOnMousePressed(e -> {
            clicked = true;
            System.out.println("clicked = true;");
            agree.setStyle(AGREE_STYLE.concat(CLICKED_STYLE));
        });

        agree.setOnMouseReleased(e -> {
            clicked = false;
            System.out.println("clicked = false;");
            if (mouse_in) {
                //moot since Pressed event (Above) kills this.stage
                agree.setStyle(AGREE_STYLE.concat(HOVER_STYLE));
            } else {
                agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));

            }
        });

        agree.setOnMouseClicked(e -> {
            agree.setText("Loading prosite database....");
            //update UI
            PauseTransition pause = new PauseTransition(Duration.millis(250));
            pause.setOnFinished(event -> {
                this.PARENT_PROGRAM.prositeAgreementAccepted();
                origin.show_agreed(true);

                stage.close();
            });
            pause.play();
        });

        agreeContainer.getChildren().add(agree);
        return agreeContainer;
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

}
