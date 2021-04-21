/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade;

import edu.tcnj.biology.slimshadey.editor.EditorTab;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositeShadeRemote {

    private boolean agree = false;

    private VisualMultipleSequenceAlignment vmsa;

    private DatabaseSearcher_ProSiteDB_Mar2020 dbs;

    private Stage stage;
    private Scene scene;
    private VBox root;

    private HashMap<Character, String> charHexMap;

    private EditorTab originator;

    public PrositeShadeRemote(EditorTab originator) {
        this.vmsa = originator.getVMSA();
        this.originator = originator;

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("Prosite pattern based shading: " + originator.getText());
        stage.setAlwaysOnTop(true);

        root = getRoot();
        scene = new Scene(root);
        stage.setScene(scene);

        if (originator.THE_PROGRAM.isPrositeAgreed()) {
            agree = true;
            show_agreed(false);
        } else {
            show();
        }

    }

    private RadioButton runOnlineScan;
    private CheckBox filterLowComplexity;
    private CheckBox includeProfiles;
    private CheckBox highSensitivity;
    private Button resetDefaultOptions;

    private RadioButton runLocalScan;

    private final ToggleGroup scanTypeTG = new ToggleGroup();

    private CheckBox useProfile; // we do not have profiles yet
    private HBox runScanBox;
    private Button runScan;

    private VBox getRoot() {
        VBox vb = new VBox(20);
        //vb.setAlignment(Pos.CENTER);

        runOnlineScan = new RadioButton("Run scan using ExPASy's ScanProsite implementation");
        runOnlineScan.setSelected(true);
        runOnlineScan.setToggleGroup(scanTypeTG);

        filterLowComplexity = new CheckBox("Filter low complexity patterns");
        filterLowComplexity.setSelected(true);

        includeProfiles = new CheckBox("Include ProSite profiles in search");
        includeProfiles.setSelected(true);

        highSensitivity = new CheckBox("Run scan at high sensitivity (allow weak matches)");
        highSensitivity.setSelected(false);

        resetDefaultOptions = new Button("Reset default options");
        resetDefaultOptions.setOnAction(e -> {
            filterLowComplexity.setSelected(true);
            includeProfiles.setSelected(true);
            highSensitivity.setSelected(false);
        });

        runLocalScan = new RadioButton("Run scan locally with patterns");
        runLocalScan.setSelected(false);
        runLocalScan.setToggleGroup(scanTypeTG);
        runLocalScan.selectedProperty().addListener(e -> {
            if (runLocalScan.isSelected()) {
                filterLowComplexity.setDisable(true);
                includeProfiles.setDisable(true);
                highSensitivity.setDisable(true);
                resetDefaultOptions.setDisable(true);
            }
            if (!runLocalScan.isSelected()) {
                filterLowComplexity.setDisable(false);
                includeProfiles.setDisable(false);
                highSensitivity.setDisable(false);
                resetDefaultOptions.setDisable(false);
            }

        });

        runScanBox = new HBox();
        HBox.setHgrow(runScanBox, Priority.ALWAYS);
        runScan = new Button("Run Scan");
        runScan.setOnAction(e -> {
            if (runLocalScan.isSelected()) {
                new LocalScanRunner(vmsa, dbs, true);
            } else if (runOnlineScan.isSelected()) {
                new OnlineScanRunner(vmsa,
                        highSensitivity.isSelected(),
                        includeProfiles.isSelected(),
                        filterLowComplexity.isSelected()
                );
            }
            stage.close();
        });
        runScan.setPrefWidth(100);
        runScanBox.getChildren().add(runScan);
        runScanBox.setAlignment(Pos.CENTER);
        stage.setOnShowing(e -> runScanBox.setPrefWidth(stage.getWidth()));

        int spaceWidth = 20; //px
        vb.getChildren().addAll(runOnlineScan,
                getSpacerBox(filterLowComplexity, spaceWidth, 0),
                getSpacerBox(includeProfiles, spaceWidth, 0),
                getSpacerBox(highSensitivity, spaceWidth, 0),
                getSpacerBox(resetDefaultOptions, spaceWidth, 0),
                runLocalScan, runScanBox);
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
        new PrositeAgreement(this, originator.THE_PROGRAM);
    }

    //actually show the window, once user sees citation information
    protected void show_agreed(boolean fromAgreement) {
        if (fromAgreement) {
            agree = true;
            originator.THE_PROGRAM.prositeAgreementAccepted();
        }
        if (agree) {
            if (dbs == null && originator.THE_PROGRAM.dbs == null) {

                dbs = new DatabaseSearcher_ProSiteDB_Mar2020(true);
                originator.THE_PROGRAM.dbs = dbs;
                //sjls.window.close();
            } else {
                dbs = originator.THE_PROGRAM.dbs;
            }
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
