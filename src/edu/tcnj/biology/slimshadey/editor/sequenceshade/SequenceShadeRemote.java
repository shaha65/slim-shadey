/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.sequenceshade;

import edu.tcnj.biology.slimshadey.editor.EditorTab;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class SequenceShadeRemote {

    private Stage stage;
    private Scene scene;

    private EditorTab originator;
    private VisualMultipleSequenceAlignment vmsa;

    private VBox vroot;

    private HBox shadeToBox;
    private Label shadeToLabel;

    //private Button sequenceButton;
    private ComboBox<String> sequenceComboBox;
    private Button sortBySeqButton;

    private HBox colorMasterBox;
    private Label colorMasterSequenceLabel;
    private ColorPicker cpMasterSequence;

    private HBox colorSimilarBox;
    private Label colorSimilarLabel;
    private ColorPicker cpSimilar;

    private HBox colorIdentityBox;
    private Label colorIdentityLabel;
    private ColorPicker cpIdentity;

    private HBox colorDiffBox;
    private Label colorDiffLabel;
    private ColorPicker cpDiff;

    private String currentName = null;
    private int currentIndex = 0;

    private final Font DEFAULT_FONT_TEXT = Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14);

    public SequenceShadeRemote(EditorTab originator) {
        this.originator = originator;
        this.vmsa = originator.getVMSA();
        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("SequenceShade remote - ".concat(originator.getText()));

        shadeToLabel = new Label("(Master sequence)");
        //sequenceButton = new Button(((Label) vmsa.getSequenceNamesBox().getChildren().get(0)).getText());
        //setSizePermanent(sequenceButton);
        shadeToLabel.setFont(DEFAULT_FONT_TEXT);

        //sequenceButton.setOnAction((ActionEvent e) -> {
        //    chooseSequenceUI();
        //});
        sequenceComboBox = new ComboBox<>();
        setSizePermanent(sequenceComboBox);
        populateComboBox();
        currentName = sequenceComboBox.getItems().get(0);
        sequenceComboBox.valueProperty().addListener(e -> {
            if (stage.isShowing() && stage.isFocused()) {
                currentName = sequenceComboBox.getValue();
                currentIndex = vmsa.getIndexOfSequence(currentName);
                shade(currentIndex);
            }
        });

        this.vmsa.getSequenceNamesBox().getChildren().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                populateComboBox();
            }
        });

        shadeToBox = new HBox(5);
        //shadeToBox.setAlignment(Pos.CENTER);
        //shadeToBox.getChildren().addAll(sequenceButton, shadeToLabel);

        sortBySeqButton = new Button("Sort by this sequence");
        sortBySeqButton.setOnAction(e -> {
            vmsa.sortBySequence_popup(vmsa.getIndexOfSequence(sequenceComboBox.getValue()), null);
        });
        sortBySeqButton.setFont(DEFAULT_FONT_TEXT);
        shadeToBox.getChildren().addAll(sequenceComboBox, shadeToLabel);
        colorMasterSequenceLabel = new Label("(Master sequence shading)");
        colorMasterSequenceLabel.setFont(DEFAULT_FONT_TEXT);
        cpMasterSequence = new ColorPicker();
        setSizePermanent(cpMasterSequence);
        cpMasterSequence.setValue(Color.web(backHexIdentity));
        cpMasterSequence.valueProperty().addListener(e -> {
            shadeMasterSequence();
        });
        vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpMasterSequence}));
        colorMasterBox = new HBox(5);
        //colorBox.setAlignment(Pos.CENTER);
        colorMasterBox.getChildren().addAll(cpMasterSequence, colorMasterSequenceLabel);

        colorIdentityLabel = new Label("(Identity shading color)");
        colorIdentityLabel.setFont(DEFAULT_FONT_TEXT);
        cpIdentity = new ColorPicker();
        setSizePermanent(cpIdentity);
        cpIdentity.setValue(Color.web(backHexIdentity));
        cpIdentity.valueProperty().addListener(e -> {
            this.backHexIdentity = vmsa.hexifyColorFX(cpIdentity.getValue());
            shade(currentIndex);
        });
        colorIdentityBox = new HBox(5);
        colorIdentityBox.getChildren().addAll(cpIdentity, colorIdentityLabel);

        colorSimilarLabel = new Label("(Similar residue shading color)");
        colorSimilarLabel.setFont(DEFAULT_FONT_TEXT);
        cpSimilar = new ColorPicker();
        setSizePermanent(cpSimilar);
        cpSimilar.setValue(Color.web(backHexSimilar));
        cpSimilar.valueProperty().addListener(e -> {
            this.backHexSimilar = vmsa.hexifyColorFX(cpSimilar.getValue());
            shade(currentIndex);
        });
        colorSimilarBox = new HBox(5);
        colorSimilarBox.getChildren().addAll(cpSimilar, colorSimilarLabel);

        colorDiffLabel = new Label("(Different residue shading color)");
        colorDiffLabel.setFont(DEFAULT_FONT_TEXT);
        cpDiff = new ColorPicker();
        setSizePermanent(cpDiff);
        cpDiff.setValue(Color.web(backHexDiff));
        cpDiff.valueProperty().addListener(e -> {
            this.backHexDiff = vmsa.hexifyColorFX(cpDiff.getValue());
            shade(currentIndex);
        });
        colorDiffBox = new HBox(5);
        colorDiffBox.getChildren().addAll(cpDiff, colorDiffLabel);

        vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpIdentity, cpSimilar, cpDiff}));

        vroot = new VBox(5);
        //vroot.setAlignment(Pos.CENTER);
        vroot.setPadding(new Insets(15));
        vroot.getChildren().addAll(shadeToBox, sortBySeqButton, colorMasterBox, colorIdentityBox, colorSimilarBox, colorDiffBox);

        scene = new Scene(vroot);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.show();
        //chooseSequenceUI();
        shade(currentIndex);
    }

    private void populateComboBox() {
        sequenceComboBox.getItems().removeAll(sequenceComboBox.getItems());
        for (int k = 0; k < this.vmsa.getSequenceNamesBox().getChildren().size(); k++) {
            sequenceComboBox.getItems().add(this.vmsa.getSequenceName(k));
        }
        if (!sequenceComboBox.getItems().isEmpty()) {
            sequenceComboBox.setValue(sequenceComboBox.getItems().get(0));
        }
    }

    private void shadeMasterSequence() {
        for (int position = 0; position < vmsa.sequenceLength(); position++) {
            String backHex = vmsa.hexifyColorFX(cpMasterSequence.getValue());
            String foreHex = vmsa.decideForeGroundColor(backHex);
            ((VisualBioChar) vmsa.getVBC(currentIndex, position)).setCurrentStyle(vmsa.generateFont(backHex, foreHex, false, false), backHex, foreHex);
        }
    }

    private String backHexSimilar = "#B6B6B6";
    private String backHexIdentity = "#000000";
    private String backHexDiff = "#FFFFFF";

    private List<VisualBioChar> similarVBCs;
    private List<VisualBioChar> identVBCs;

    private int PERM_WIDTH = 150;

    private void setSizePermanent(Control c) {
        c.setMinWidth(PERM_WIDTH);
        c.setMaxWidth(PERM_WIDTH);
    }

    public void chooseSequenceUI() {
        Stage chooseSeqStage = new Stage();
chooseSeqStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));

        VBox chooseSeqRoot = new VBox(10);
        chooseSeqRoot.setPadding(new Insets(15));

        ToggleGroup sequencetg = new ToggleGroup();
        VBox sequences = new VBox(5);
        int currentIndexIter = 0;
        boolean selectionSet = false;
        for (Node n : vmsa.getSequenceNamesBox().getChildren()) {
            RadioButton seqrb = new RadioButton(((Label) n).getText());
            sequences.getChildren().add(seqrb);
            sequencetg.getToggles().add(seqrb);
            if (currentIndex == currentIndexIter) {
                seqrb.setSelected(true);
                selectionSet = true;
            }
            currentIndexIter++;
        }
        if (!selectionSet) {
            ((RadioButton) sequences.getChildren().get(0)).setSelected(true);
        }

        Button executeShade = new Button("Shade");
        executeShade.setOnAction((ActionEvent event) -> {
            RadioButton selectedToggle = (RadioButton) sequencetg.getSelectedToggle();
            String seqName = selectedToggle.getText();
            currentIndex = vmsa.getIndexOfSequence(seqName);
            //sequenceButton.setText(seqName);
            Platform.runLater(() -> {
                shade(currentIndex);
            });
            chooseSeqStage.close();
        });

        chooseSeqRoot.getChildren().addAll(sequences, executeShade);
        Scene chooseSeqScene = new Scene(chooseSeqRoot);
        chooseSeqStage.setScene(chooseSeqScene);
        chooseSeqStage.initModality(Modality.APPLICATION_MODAL);
        chooseSeqStage.setAlwaysOnTop(true);
        chooseSeqStage.showAndWait();
    }

    public void shade(int index) {
        if (index >= vmsa.sequenceNumber() || index < 0) {
            index = 0;
        }
        List<Integer> traverseIndices = new ArrayList<>();
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            traverseIndices.add(k);
        }
        traverseIndices.remove(index);

        HBox comparingSequence = vmsa.getSequence(index);
        int seqLen = vmsa.sequenceLength();

        HashMap<Character, String> categoryRulesMap = vmsa.getAlphabet().categories;
        shadeMasterSequence();
        for (int position = 0; position < seqLen; position++) {
            char comparing = ((VisualBioChar) comparingSequence.getChildren().get(position)).getChar();
            if (comparing != '-') {
                String category = categoryRulesMap.get(comparing);
                //shadeVBC(vmsa.getVBC(index, position), Color.WHITE);
                for (int seqIndex : traverseIndices) {
                    VisualBioChar currentVBC = vmsa.getVBC(seqIndex, position);
                    if (currentVBC.getChar() != '-') {
                        if (currentVBC.getChar() == comparing) {
                            //shade black for perfect match
                            shadeVBC(currentVBC, backHexIdentity);
                        } else if (categoryRulesMap.getOrDefault(currentVBC.getChar(), "").equals(category)) {
                            //shade grey for being in same category
                            shadeVBC(currentVBC, backHexSimilar);
                        } else {
                            //shade white for being dissimilar
                            shadeVBC(currentVBC, backHexDiff);
                        }
                    } else {
                        shadeVBC(currentVBC, "#FFFFFF");
                    }
                }
            } else {
                for (int seqIndex : traverseIndices) {
                    VisualBioChar currentVBC = vmsa.getVBC(seqIndex, position);
                    shadeVBC(currentVBC, "#FFFFFF");
                }
            }
        }
    }

    public void hide() {
        stage.hide();
    }

    public void determineCurrentIndex() {

        boolean found = false;
        if (currentName != null) {
            for (int i = 0; i < vmsa.sequenceNumber(); i++) {
                if (vmsa.getSequenceName(i).equals(currentName)) {
                    sequenceComboBox.setValue(currentName);
                    currentIndex = i;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            sequenceComboBox.setValue(vmsa.getSequenceName(0));
            currentIndex = 0;
        }
    }

    public void unHide(boolean paint) {
        populateComboBox();
        determineCurrentIndex();
        stage.show();
        if (paint) {
            //vmsa.clearShading();
            //chooseSequenceUI();
            shade(currentIndex);
        }
    }

    private void shadeVBC(VisualBioChar vbc, String backHex) {
        String foreHex = vmsa.decideForeGroundColor(backHex);
        vbc.setCurrentStyle(this.vmsa.generateFont(backHex, foreHex, false, false), backHex, foreHex);
        //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("white"));

    }

    //is this remote actually open?
    public boolean isShowing() {
        return this.stage.isShowing();
    }
}
