/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.matrixshade;

import edu.tcnj.biology.seqverter.graphics.GeneralLoadingManager;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.sequence.Matrix;
import edu.tcnj.biology.slimshadey.editor.EditorTab;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class MatrixShadeRemote {

    private VisualMultipleSequenceAlignment vmsa;

    private boolean successfulInitiation = false;

    private GeneralLoadingManager resource_glm = new GeneralLoadingManager("Building shading matrices...");

    public MatrixShadeRemote(EditorTab et) {
        this.vmsa = et.getVMSA();

        resource_glm.showLoading(true);
        Platform.runLater(() -> {
            successfulInitiation = buildShadingMatrices(vmsa.getAlphabet().matrices);
            if (successfulInitiation) {
                buildUI();
                shade();
            }
            Platform.runLater(() -> {
                resource_glm.exitLoading();
            });
        });

    }
    private Stage stage;
    private Scene scene;
    private VBox root;

    private HBox shadingMatrixBox;
    private ComboBox<ShadingMatrix> shadingMatrixChooser;
    private Label shadingMatrixLabel;

    private VBox sequenceContainer0;
    private RadioButton toSequence;
    private HBox sequenceContainer1;
    private ComboBox<String> sequenceChooser;
    private Button sortToSequence;
    private Label sequenceLabel;
    private CheckBox shadeMasterSequence;

    private RadioButton toSimpleConsensus;

    private VBox frequencyContainer0;
    private Label frequencyLabelName;
    private Slider frequencySlider;
    private Label frequencyLabel;
    private HBox frequencyContainer1;
    private double frequency;
    private final double DEFAULT_FREQUENCY = 0.5;

    private VBox cpHolder;
    private HBox cpMatchHolder;
    private ColorPicker cpMatch;
    private Label cpMatchLabel;
    private HBox cpMisMatchHolder;
    private ColorPicker cpMisMatch;
    private Label cpMisMatchLabel;

    private double[] DEFAULT_MATCH_RGB = new double[]{0d, 0d, 0d};
    private double[] DEFAULT_MISMATCH_RGB = new double[]{255d, 255d, 255d};

    private final Font DEFAULT_FONT_TEXT = Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14);

    private void populateComboBox() {
        sequenceChooser.getItems().removeAll(sequenceChooser.getItems());
        for (int k = 0; k < this.vmsa.getSequenceNamesBox().getChildren().size(); k++) {
            sequenceChooser.getItems().add(this.vmsa.getSequenceName(k));
        }
        if (!sequenceChooser.getItems().isEmpty()) {
            sequenceChooser.setValue(sequenceChooser.getItems().get(0));
        }
    }

    private void buildUI() {

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("Matrix shading");

        shadingMatrixBox = new HBox(5);
        shadingMatrixLabel = new Label("Substitution matrix: ");
        shadingMatrixLabel.setFont(DEFAULT_FONT_TEXT);
        shadingMatrixChooser = new ComboBox();
        shadingMatrixChooser.getItems().addAll(matrices);
        int index = 0;
        for (ShadingMatrix sm : shadingMatrixChooser.getItems()) {
            if (sm.name.equals("BLOSUM62")) {
                index = shadingMatrixChooser.getItems().indexOf(sm);
                break;
            }
        }
        shadingMatrixChooser.setValue(shadingMatrixChooser.getItems().get(index));
        shadingMatrixChooser.valueProperty().addListener(e -> {
            shade();
        });
        shadingMatrixBox.getChildren().addAll(shadingMatrixLabel, shadingMatrixChooser);

        sequenceContainer0 = new VBox(10);
        
        shadeMasterSequence = new CheckBox("Shade master sequence");
        shadeMasterSequence.setFont(DEFAULT_FONT_TEXT);
        shadeMasterSequence.setSelected(true);
        shadeMasterSequence.selectedProperty().addListener(e -> {
            if (stage.isShowing() && stage.isFocused()) {
                shade();
            }
        });

        toSequence = new RadioButton("Shade to a sequence");
        toSequence.setFont(DEFAULT_FONT_TEXT);
        toSequence.selectedProperty().addListener(e -> {
            shadeMasterSequence.setDisable(!toSequence.isSelected());
        });

        sequenceContainer1 = new HBox(5);
        toSequence.setSelected(true);
        sequenceChooser = new ComboBox<>();
        sequenceChooser.setMaxWidth(220);
        populateComboBox();
        this.vmsa.getSequenceNamesBox().getChildren().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable e) {
                populateComboBox();
            }
        });
        sequenceChooser.valueProperty().addListener(e -> {
            if (stage.isShowing() && stage.isFocused()) {
                shade();
            }
        });
        sequenceLabel = new Label("Sequence to shade to: ");
        sequenceLabel.setFont(DEFAULT_FONT_TEXT);
        
        sortToSequence = new Button("Sort by this sequence");
        sortToSequence.setOnAction(e -> {
            vmsa.sortBySequence_popup(vmsa.getIndexOfSequence(sequenceChooser.getValue()), shadingMatrixChooser.getValue().toString());
        });
        sortToSequence.setFont(DEFAULT_FONT_TEXT);

        sequenceContainer1.getChildren().addAll(sequenceLabel, sequenceChooser);
        sequenceContainer0.getChildren().addAll(toSequence, sequenceContainer1, sortToSequence, shadeMasterSequence);

        toSimpleConsensus = new RadioButton("Shade to simple majority");
        toSimpleConsensus.setFont(DEFAULT_FONT_TEXT);
        toSimpleConsensus.setSelected(false);
        frequencyLabelName = new Label("Shading frequency threshold:");
        frequencyLabelName.setFont(DEFAULT_FONT_TEXT);
        frequencySlider = new Slider(0.5, 1, frequency);
        frequency = DEFAULT_FREQUENCY;
        frequencyLabel = new Label(String.valueOf(DEFAULT_FREQUENCY));
        frequencyLabel.setFont(DEFAULT_FONT_TEXT);
        frequencySlider.setShowTickMarks(true);
        frequencySlider.setMajorTickUnit(0.1);
        frequencySlider.setMinorTickCount(1);

        //frequencySlider.setSnapToTicks(true);
        //frequencySlider.valueProperty().addListener(frequencySliderUpdater);
        frequencySlider.valueProperty().addListener(e -> updatedSlider());

        frequencyContainer1 = new HBox(10);
        frequencyContainer1.getChildren().addAll(frequencyLabelName, frequencySlider, frequencyLabel);

        frequencyContainer0 = new VBox(10);
        frequencyContainer0.getChildren().addAll(toSimpleConsensus, frequencyContainer1);

        cpMatchHolder = new HBox(5);
        cpMatch = new ColorPicker();
        cpMatch.setValue(Color.web(rgbToHex_255(this.DEFAULT_MATCH_RGB)));
        cpMatch.setOnAction(e -> {
            updateColoring();
        });
        cpMatchLabel = new Label("(Match color)");
        cpMatchLabel.setFont(DEFAULT_FONT_TEXT);
        cpMatchHolder.getChildren().addAll(cpMatch, cpMatchLabel);

        cpMisMatchHolder = new HBox(5);
        cpMisMatch = new ColorPicker();
        cpMisMatch.setValue(Color.web(rgbToHex_255(this.DEFAULT_MISMATCH_RGB)));
        cpMisMatch.setOnAction(e -> {
            updateColoring();
        });
        cpMisMatchLabel = new Label("(Mismatch color)");
        cpMisMatchLabel.setFont(DEFAULT_FONT_TEXT);
        cpMisMatchHolder.getChildren().addAll(cpMisMatch, cpMisMatchLabel);

        cpHolder = new VBox(10);
        cpHolder.getChildren().addAll(cpMatchHolder, cpMisMatchHolder);
        this.vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpMatch, cpMisMatch}));

        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(toSequence, toSimpleConsensus);
        sequenceChooser.setDisable(false);
        sequenceLabel.setDisable(false);
        sortToSequence.setDisable(false);
        frequencyLabelName.setDisable(true);
        frequencySlider.setDisable(true);
        frequencyLabel.setDisable(true);
        tg.selectedToggleProperty().addListener(e -> {
            if (toSequence.isSelected()) {
                sequenceChooser.setDisable(false);
                sequenceLabel.setDisable(false);
                sortToSequence.setDisable(false);
                frequencyLabelName.setDisable(true);
                frequencySlider.setDisable(true);
                frequencyLabel.setDisable(true);
            } else if (toSimpleConsensus.isSelected()) {
                sequenceChooser.setDisable(true);
                sequenceLabel.setDisable(true);
                sortToSequence.setDisable(true);
                frequencyLabelName.setDisable(false);
                frequencySlider.setDisable(false);
                frequencyLabel.setDisable(false);
            }
            shade();
        });

        root = new VBox(10);
        root.setPadding(new Insets(12));
        root.getChildren().addAll(shadingMatrixBox, sequenceContainer0, frequencyContainer0, cpHolder);

        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        stage.setWidth(stage.getWidth() + 50);
    }

    private void updateColoring() {
        Color colorMatch = cpMatch.getValue();
        double[] rgbMatch = new double[]{colorMatch.getRed() * 255d, colorMatch.getGreen() * 255d, colorMatch.getBlue() * 255d};
        Color colorMisMatch = cpMisMatch.getValue();
        double[] rgbMisMatch = new double[]{colorMisMatch.getRed() * 255d, colorMisMatch.getGreen() * 255d, colorMisMatch.getBlue() * 255d};
        //System.out.println(Arrays.toString(rgbMatch));
        for (ShadingMatrix sm : shadingMatrixChooser.getItems()) {
            sm.setShadingValues(rgbMatch, rgbMisMatch, vmsa.luminanceThreshold());
        }
        shade();
    }

    private void shade() {
        vmsa.clearShading();

        int index = 0;
        if (toSequence.isSelected()) {
            index = vmsa.getIndexOfSequence(sequenceChooser.getValue());
        } else if (toSimpleConsensus.isSelected()) {
            index = -1;
        }

        List<Integer> indices = new ArrayList<>();
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            indices.add(k);
        }

        if (index != -1) {
            indices.remove(index);
        }

        for (int k = 0; k < vmsa.sequenceLength(); k++) {
            Character toUse = null;
            if (index != -1) {
                toUse = vmsa.getVBC(index, k).getChar();
            } else {
                char[] columnchars = new char[vmsa.sequenceNumber()];
                for (int h = 0; h < vmsa.sequenceNumber(); h++) {
                    columnchars[h] = vmsa.getVBC(h, k).getChar();
                }
                toUse = getMajorityCharacter(columnchars);
            }
            if (toUse != null) {
                for (int seqindex : indices) {
                    VisualBioChar vbcToShade = vmsa.getVBC(seqindex, k);
                    String[] hex = this.shadingMatrixChooser.getValue().getHex(toUse, vbcToShade.getChar());
                    //System.out.println(Arrays.toString(hex) + " " + luminance(hex[0]) + " " + Arrays.toString(this.shadingMatrixChooser.getValue().getRGB(toUse, vbcToShade.getChar())));
                    vbcToShade.setCurrentStyle(vmsa.generateFont(hex[0], hex[1], false, false), hex[0], hex[1]);
                }
                if (index != -1 && shadeMasterSequence.isSelected()) {
                    VisualBioChar vbcToShade = vmsa.getVBC(index, k);
                    String[] hex = this.shadingMatrixChooser.getValue().getHex(toUse, vbcToShade.getChar());
                    //System.out.println(Arrays.toString(hex) + " " + luminance(hex[0]) + " " + Arrays.toString(this.shadingMatrixChooser.getValue().getRGB(toUse, vbcToShade.getChar())));
                    vbcToShade.setCurrentStyle(vmsa.generateFont(hex[0], hex[1], false, false), hex[0], hex[1]);

                }
            }
        }

    }

    private double luminance(String hex) {
        double[] rgb = this.hexToRGB_255(hex);
        double[] rgbi = new double[]{rgb[0] / 255d, rgb[1] / 255d, rgb[2] / 255d};
        for (int i = 0; i < rgb.length; i++) {
            if (rgbi[i] < 0.03928) {
                rgbi[i] = rgbi[i] / 12.92;
            } else {
                rgbi[i] = Math.pow((rgbi[i] + 0.055) / 1.055, 2.4);
            }
        }
        //luminance calculation
        double L = 0.2126 * rgbi[0] + 0.7152 * rgbi[1] + 0.0722 * rgbi[2];
        //System.err.println("luminance " + L);
        //black if luminance exceeds threshold, white if does not
        return L;
    }

    private Character getMajorityCharacter(char[] column) {

        HashMap<Character, Double> frequencyMap = new HashMap<>();
        for (char c : column) {
            frequencyMap.put(c, 0d);
        }

        for (char c : column) {
            frequencyMap.put(c, frequencyMap.get(c) + 1d);
        }

        double maxfreq = 0d;
        Character maxfreqchar = null;
        for (char c : column) {
            frequencyMap.put(c, frequencyMap.get(c) / column.length);
            if (frequencyMap.get(c) > maxfreq) {
                maxfreq = frequencyMap.get(c);
                maxfreqchar = c;
            }
        }
        if (maxfreq == 1) {
            return maxfreqchar;
        } else if (maxfreq <= frequency) {
            return null;
        } else {
            return maxfreqchar;
        }

    }

    private void updatedSlider() {
        double freq = frequencySlider.getValue();

        int toMod = ((int) (freq * 100.0));

        boolean greaterThanTwo = toMod % 5 >= 3;

        /*if (toMod % 5 >= 3) {
                        frequencySlider.setValue(((double) (((int) toMod / 5) + 1)) * 0.05);
                    } else if (toMod % 5 < 3) {
                        frequencySlider.setValue(((double) (((int) toMod / 5) + 0)) * 0.05);
        }*/
        frequencySlider.setValue(((double) (((int) toMod / 5) + (greaterThanTwo ? 1 : 0))) * 0.05);

        double newFreq = Double.parseDouble(String.valueOf((double) Math.floor(frequencySlider.getValue() * 100)).substring(0, 2)) / 100.0;

        //needed for 100% match (cannot be exceeded)
        if (frequencySlider.getValue() == 1) {
            newFreq = 1.0;
        }

        if (newFreq != frequency) {

            frequency = newFreq;

            frequencyLabel.setText(String.valueOf(frequency));

            shade();
        }

    }

    List<ShadingMatrix> matrices;

    //should only be called once, in constructor
    private boolean buildShadingMatrices(List<Matrix> matricesToConvert) {
        if (matricesToConvert == null || matricesToConvert.isEmpty()) {
            return false;
        }
        matrices = new ArrayList<>();
        for (Matrix m : matricesToConvert) {
            matrices.add(new ShadingMatrix(m.name, m.ordering, m.matrix, vmsa.luminanceThreshold()));
        }
        return true;
    }

    private double[] hexToRGB_255(String hex) {
        /*        System.out.println(Integer.valueOf(hex.substring(1, 3), 16) + " " +
            Integer.valueOf(hex.substring(3, 5), 16) + " " +
            Integer.valueOf(hex.substring(5, 7), 16));*/
        return new double[]{((double) Integer.valueOf(hex.substring(1, 3), 16)),
            ((double) Integer.valueOf(hex.substring(3, 5), 16)),
            ((double) Integer.valueOf(hex.substring(5, 7), 16))};
    }

    private String rgbToHex_255(double[] rgb) {
        return String.format("#%02X%02X%02X",
                (int) (rgb[0]),
                (int) (rgb[1]),
                (int) (rgb[2]));
    }

    public void hide() {
        if (this.successfulInitiation) {
            stage.hide();
        }
    }

    public void unHide(boolean paint) {
        if (this.successfulInitiation) {
            populateComboBox();
            stage.show();
            if (paint) {
                shade();
                //vmsa.clearShading();
            }
        }
    }

    //is this remote actually open?
    public boolean isShowing() {
        if (this.successfulInitiation) {
            return this.stage.isShowing();
        }
        return false;
    }
}
