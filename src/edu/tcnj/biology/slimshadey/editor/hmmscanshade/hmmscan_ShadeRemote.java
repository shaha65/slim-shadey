/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import edu.tcnj.biology.slimshadey.editor.EditorTab;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_ShadeRemote {

    private boolean agree = false;

    private VisualMultipleSequenceAlignment vmsa;

    private Stage stage;
    private Scene scene;
    private VBox root;

    private HashMap<Character, String> charHexMap;

    private EditorTab originator;

    public hmmscan_ShadeRemote(EditorTab originator) {
        this.vmsa = originator.getVMSA();
        this.originator = originator;

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("Prosite pattern based shading: " + originator.getText());
        stage.setAlwaysOnTop(true);

        root = getRoot();
        scene = new Scene(root);
        stage.setScene(scene);

        if (originator.THE_PROGRAM.isHmmerAgreed()) {
            agree = true;
            show_agreed(false);
        } else {
            show();
        }

    }

    private Button runScan;

    private VBox getRoot() {
        VBox vb = new VBox(20);
        //vb.setAlignment(Pos.CENTER);

        runScan = new Button("Run Scan");
        runScan.setOnAction(e -> {
            new hmmscan_Runner(vmsa);
            Platform.runLater(() -> {
                stage.close();
            });
        });

        stage.setOnShown(e -> {
            runScan.fire();
        });

        int spaceWidth = 20; //px
        vb.getChildren().addAll(runScan);
        VBox vb2 = new VBox();
        vb2.setPadding(new Insets(20, 20, 20, 20));
        vb2.getChildren().add(vb);
        return vb2;
    }

    private HBox getSpacerBox(Node n, double fixed_w, double fixed_h) {
        HBox ret = new HBox();
        Region r = new Region();
        r.setMaxSize(fixed_w, fixed_h);
        r.setMinSize(fixed_w, fixed_h);
        ret.getChildren().addAll(r, n);
        return ret;
    }

    public void show() {
        new hmmer_Agreement(this, originator.THE_PROGRAM);
    }

    //actually show the window, once user sees citation information
    protected void show_agreed(boolean fromAgreement) {
        if (fromAgreement) {
            agree = true;
            originator.THE_PROGRAM.hmmerAgreementAccepted();
        }
        if (agree) {
            this.stage.show();
        } else {
            this.show();
        }
    }

    public void hide() {
        stage.hide();
    }

    public void unHide(boolean paint) {
        this.show_agreed(false); //assume user agreed this session
        if (paint) {
            //vmsa.clearShading();
            //reshade
        }
    }

}
