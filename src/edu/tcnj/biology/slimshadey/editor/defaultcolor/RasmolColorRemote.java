/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.defaultcolor;

import edu.tcnj.biology.seqverter.sequence.Alphabet;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.EditorTab;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class RasmolColorRemote {

    private VisualMultipleSequenceAlignment vmsa;

    private Stage stage;
    private Scene scene;
    private VBox root;

    private HashMap<Character, String> charHexMap;

    private final String spacer = "     ";

    public RasmolColorRemote(EditorTab originator) {
        this.vmsa = originator.getVMSA();
        this.charHexMap = this.vmsa.getAlphabet().defaultColorMapping;

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("Default coloring: " + originator.getText());
        stage.setAlwaysOnTop(true);
        
        root = new VBox();

        Alphabet alpha = this.vmsa.getAlphabet();
        VBox characterBox = new VBox();
        for (char c : alpha.keySet()) {
            String defaultColorHex = this.charHexMap.getOrDefault(c, "#FFFFFF");

            HBox labelAndColorpick = new HBox();
            Label lb = new Label(String.valueOf(c) + spacer + defaultColorHex + spacer);
            lb.setStyle(this.vmsa.generateFont(defaultColorHex, false, false));
            ColorPicker cp = new ColorPicker();
            cp.setValue(Color.web(defaultColorHex));
            cp.valueProperty().addListener(e -> {
                String hexColor = this.vmsa.hexifyColorFX(cp.getValue());
                this.charHexMap.replace(c, hexColor);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        lb.setText(String.valueOf(c) + spacer + hexColor + spacer);
                        lb.setStyle(vmsa.generateFont(hexColor, false, false));
                    }
                });
                paint();
            });
            Button resetDefaultColor = new Button("Reset default color");
            resetDefaultColor.setOnAction(e -> {
                cp.setValue(Color.web(defaultColorHex));
            });
            labelAndColorpick.getChildren().addAll(lb, cp, resetDefaultColor);
            characterBox.getChildren().add(labelAndColorpick);
        }

        root.getChildren().add(characterBox);
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        vmsa.clearShading();
        paint();
    }

    TimerTask task = null;
    Timer timer = new Timer();
    long delayTimeUnit = 10;

    private void paint() {
        if (task != null) {
            task.cancel();
            timer = new Timer();
        }
        task = new TimerTask() {
            @Override
            public void run() {
                paintOperation();
            }
        };
        //increase delay for larger input alignments
        timer.schedule(task, delayTimeUnit * vmsa.sequenceNumber());
    }

    private void paintOperation() {
        int seqLen = vmsa.sequenceLength();
        int seqNum = vmsa.sequenceNumber();

        for (int i = 0; i < seqLen; i++) {
            for (int k = 0; k < seqNum; k++) {
                VisualBioChar currentVBC = vmsa.getVBC(k, i);
                String backHexColor = charHexMap.getOrDefault(currentVBC.getChar(), "FFFFFF");
                System.out.print(currentVBC.getChar() + " " + backHexColor + " ");
                vmsa.hexToRGB(backHexColor);
                currentVBC.setCurrentStyle(vmsa.generateFont(backHexColor), backHexColor, vmsa.decideForeGroundColor(backHexColor));
            }
        }
    }

    public void hide() {
        stage.hide();
    }

    public void unHide(boolean paint) {
        stage.show();
        if (paint) {
            //vmsa.clearShading();
            paint();
        }
    }
    
     public boolean isShowing() {
        return this.stage.isShowing();
    }
    

}
