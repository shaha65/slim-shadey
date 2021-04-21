/*
    Copyright (c) 2018 Avi Shah, Sudhir Nayak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but without any warranty. See the GNU General Public License for more 
    details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package edu.tcnj.biology.slimshadey.editor.basicshade;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.sequence.Alphabet;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import edu.tcnj.biology.slimshadey.editor.EditorTab;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * The <tt>BasicShadeRemote</tt> creates a popup window with options that, when
 * modified, will dynamically shade the underlying
 * <tt>VisualMultipleSequenceAlignment</tt> according to the BasicShade
 * protocol.
 *
 * @author Avi Shah
 */
public class BasicShadeRemote {

    private Stage stage;
    private Scene scene;

    private EditorTab originator;

    private VBox root;

    private CheckBox dynamicShading;
    private HBox dynamicShadingBox;

    private Label frequencyLabelName;
    private Slider frequencySlider;
    private Label frequencyLabel;
    private HBox frequencyBox;
    double frequency;
    final static double DEFAULT_FREQUENCY = 0.5;

    private HBox colorSimilarBox;
    private Label colorSimilarLabel;
    private ColorPicker cpSimilar;

    private HBox colorIdentityBox;
    private Label colorIdentityLabel;
    private ColorPicker cpIdentity;

    private HBox colorDiffBox;
    private Label colorDiffLabel;
    private ColorPicker cpDiff;

    private CheckBox inverseShadingBox;

    private VisualMultipleSequenceAlignment vmsa;

    /**
     *
     * @param originator
     */
    public BasicShadeRemote(EditorTab originator) {
        this.vmsa = originator.getVMSA();
        this.originator = originator;
        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("BasicShade remote - ".concat(originator.getText()));

        dynamicShading = new CheckBox("Shade dynamically?");
        dynamicShadingBox = new HBox();
        dynamicShadingBox.getChildren().add(dynamicShading);

        frequencyLabelName = new Label("Shading frequency threshold:");
        frequencyLabelName.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        frequencySlider = new Slider(0.5, 1, frequency);
        frequency = DEFAULT_FREQUENCY;
        frequencyLabel = new Label(String.valueOf(DEFAULT_FREQUENCY));
        frequencyLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        frequencySlider.setShowTickMarks(true);
        frequencySlider.setMajorTickUnit(0.1);
        frequencySlider.setMinorTickCount(1);

        //frequencySlider.setSnapToTicks(true);
        //frequencySlider.valueProperty().addListener(frequencySliderUpdater);
        frequencySlider.valueProperty().addListener(e -> updatedSlider());

        frequencyBox = new HBox(10);
        frequencyBox.getChildren().addAll(frequencyLabelName, frequencySlider, frequencyLabel);

        inverseShadingBox = new CheckBox("Inverse shading");
        inverseShadingBox.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        inverseShadingBox.setSelected(false);
        inverseShadingBox.setOnAction(e -> {
            this.basicShade(frequency, inverseShadingBox.isSelected());
        });

        colorIdentityLabel = new Label("(Shading level 1)");
        colorIdentityLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        cpIdentity = new ColorPicker();
        cpIdentity.setValue(Color.web(backHexIdentity));
        cpIdentity.valueProperty().addListener(e -> {
            this.backHexIdentity = vmsa.hexifyColorFX(cpIdentity.getValue());
            basicShade(frequency, inverseShadingBox.isSelected());
        });
        colorIdentityBox = new HBox(5);
        colorIdentityBox.getChildren().addAll(cpIdentity, colorIdentityLabel);

        colorSimilarLabel = new Label("(Shading level 2)");
        colorSimilarLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        cpSimilar = new ColorPicker();
        cpSimilar.setValue(Color.web(backHexSimilar));
        cpSimilar.valueProperty().addListener(e -> {
            this.backHexSimilar = vmsa.hexifyColorFX(cpSimilar.getValue());
            basicShade(frequency, inverseShadingBox.isSelected());
        });
        colorSimilarBox = new HBox(5);
        colorSimilarBox.getChildren().addAll(cpSimilar, colorSimilarLabel);

        colorDiffLabel = new Label("(Shading level 3)");
        colorDiffLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        cpDiff = new ColorPicker();
        cpDiff.setValue(Color.web(backHexDiff));
        cpDiff.valueProperty().addListener(e -> {
            this.backHexDiff = vmsa.hexifyColorFX(cpDiff.getValue());
            basicShade(frequency, inverseShadingBox.isSelected());
        });
        colorDiffBox = new HBox(5);
        colorDiffBox.getChildren().addAll(cpDiff, colorDiffLabel);

        vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpIdentity, cpSimilar, cpDiff}));
        
        root = new VBox(15);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.getChildren().addAll(
                //dynamicShadingBox, 
                frequencyBox, inverseShadingBox,
                colorIdentityBox, colorSimilarBox, colorDiffBox);

        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);

        stage.setWidth(root.getWidth() + 50);

        //shadeToLabel.impl_processCSS(true);
        //shadeToOptions.setPrefWidth(root.getWidth() - this.getLabelLength(shadeToLabel) - shadeToOptionsBox.getSpacing() * 3);
        root.widthProperty().addListener(e -> {
            //shadeToOptions.setPrefWidth(root.getWidth() - this.getLabelLength(shadeToLabel) - shadeToOptionsBox.getSpacing() * 3);
        });

        //vmsa.clearShading();
        this.basicShade(DEFAULT_FREQUENCY, inverseShadingBox.isSelected());
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

            basicShade(frequency, inverseShadingBox.isSelected());
        }

    }

    //basic shade functionality
    //private BasicShadeSimilarityRules basicRules;
    private HashMap<Character, String> categoryRulesMap;
    private HashMap<Character, Integer> alphabetAdapterFor;
    private HashMap<Integer, Character> alphabetAdapterRev;
    private HashMap<String, Integer> basicAdapterFor;
    private HashMap<Integer, String> basicAdapterRev;

    /**
     * This method shades the alignment within the <tt>EditorTab</tt>
     *
     *
     * @param consensusTo
     * @param frequency
     */
    public void basicShade(double frequency, boolean inverse) {
        System.out.println("*****" + String.valueOf(inverse));

        //basicRules = vmsa.getAlphabet().bssr;
        //categoryRulesMap = basicRules.getMap();
        categoryRulesMap = vmsa.getAlphabet().categories;
        for (char c : vmsa.getAlphabet().keySet()) {
            System.out.println(c + " " + categoryRulesMap.get(c));
        }

        frequencyAdapterFromAlphabet(this.vmsa.getAlphabet());
        frequencyAdapterFromBasicRules(categoryRulesMap);
        int seqLen = this.vmsa.sequenceLength();
        int seqNum = this.vmsa.sequenceNumber();
        System.out.println(seqLen + " " + seqNum);

        char[] charVec1 = new char[seqLen];
        String[] stringVec1 = new String[seqLen];
        for (int i = 0; i < seqLen; i++) {
            int[] vec1 = new int[alphabetAdapterFor.keySet().size() + 1];
            int[] vec2 = new int[basicAdapterFor.values().size() + 1];
            for (int k = 0; k < seqNum; k++) {
                char currentCharacter = ((VisualBioChar) this.vmsa.getSequence(k).getChildren().get(i)).getChar();

                int adapterValue = alphabetAdapterFor.getOrDefault(currentCharacter, -1);
                if (adapterValue != -1) {
                    vec1[adapterValue]++;
                }
                vec1[vec1.length - 1]++;

                int adapterValue2 = basicAdapterFor.getOrDefault((categoryRulesMap.get(currentCharacter)), -1);
                if (adapterValue2 != -1) {
                    vec2[adapterValue2]++;
                }
                vec2[vec2.length - 1]++;
            }

            int thresholdIndex1 = vec1.length;
            int thresholdIndex2 = vec2.length;

            loop_A: //find an element whose frequency exceeds or is equal to the set threshold
            for (int k = 0; k < vec1.length; k++) {
                //System.out.println(vec1[k]/vec1[vec1.length - 1]);
                double vecFreq = (double) ((double) vec1[k]) / ((double) vec1[vec1.length - 1]);
                if ((vecFreq > frequency)
                        || (vecFreq == 1.0) && vec1[vec1.length - 1] > 2) {
                    thresholdIndex1 = k;
                    break loop_A;
                }
            }
            // '$' represents NO THRESHOLD CHARACTER
            char thresholdChar1 = alphabetAdapterRev.getOrDefault(thresholdIndex1, '\u0000');
            charVec1[i] = thresholdChar1;
            System.out.println(i);
            System.out.print(Arrays.toString(vec1));
            System.out.println(thresholdIndex1 + " " + thresholdChar1);

            /*
                char letter = 0;
                int counter = 0;
                Iterator iter = this.vmsa.getAlphabet().keySet().iterator();
                loop_C:
                while (iter.hasNext()) {
                    letter = (char) iter.next();
                    if (counter == thresholdIndex1) {
                        break loop_C;
                    }
                    counter++;
                }
                charVec1[i] = letter;
             */
            loop_B: //find an element whose frequency exceeds or is equal to the set threshold
            for (int k = 0; k < vec2.length; k++) {
                //System.out.println(vec1[k]/vec1[vec1.length - 1]);
                double vecFreq = (double) ((double) vec2[k]) / ((double) vec2[vec2.length - 1]);
                if (vecFreq > frequency
                        || (vecFreq == 1.0) && vec2[vec2.length - 1] > 2) {
                    thresholdIndex2 = k;
                    // System.out.println(Arrays.toString(vec2));
                    break loop_B;
                }
            }

            String thresholdString2 = basicAdapterRev.getOrDefault(thresholdIndex2, null);
            stringVec1[i] = thresholdString2;

            System.out.print(Arrays.toString(vec2));
            System.out.println(thresholdIndex2 + " " + thresholdString2);
            /*
                Iterator iter2 = categoryRulesMap.values().iterator();
                String value2 = null;
                int counter2 = 0;
                loop_D:
                while (iter2.hasNext()) {
                    value2 = (String) iter2.next();
                    if (counter2 == thresholdIndex2) {
                        break loop_D;
                    }
                    counter2++;
                }
                //System.out.println("      - + " + value2);
                stringVec1[i] = value2;
             */
        }
        //System.out.println(sequenceLength + " " + stringVec1.length);

        shadeProtocol_basic(charVec1, stringVec1, inverse);

    }

    private void shadeProtocol_basic(char[] priority1, String[] priority2, boolean inverse) {
        setColors();
        long startTime = System.nanoTime();

        //catches all four levels of dissimilarity (branched ifs below), 0 -> white, 1 -> grey, 2 -> black
        for (int i = 0; i < vmsa.sequenceLength(); i++) {
            if (!(priority1[i] == '\u0000')) {
                String ref1 = categoryRulesMap.getOrDefault(priority1[i], "");
                for (int k = 0; k < vmsa.sequenceNumber(); k++) {
                    VisualBioChar currentVBC = (VisualBioChar) this.vmsa.getSequence(k).getChildren().get(i);
                    if (currentVBC.getChar() == priority1[i]) {
                        //shade black
                        basicShadePaintOperation(currentVBC, 2, inverse);
                    } else if (categoryRulesMap.getOrDefault(currentVBC.getChar(), "-").equals(ref1)) {
                        //shade grey
                        basicShadePaintOperation(currentVBC, 1, inverse);
                    } else {
                        if (currentVBC.getChar() != '-') {
                            basicShadePaintOperation(currentVBC, 0, inverse);
                        }
                    }
                }
            } else if (!(priority2[i] == null)) {
                for (int k = 0; k < this.vmsa.sequenceNumber(); k++) {
                    //System.out.println(sequenceHolder.get(k).get(i).get());

                    VisualBioChar currentVBC = (VisualBioChar) this.vmsa.getSequence(k).getChildren().get(i);
                    //System.out.println(categoryRulesMap.getOrDefault(currentVBC.getChar(), "-") + " " + priority2[i] + " " + i + " " + k);
                    if (categoryRulesMap.getOrDefault(currentVBC.getChar(), "-").equals(priority2[i])) {
                        //shade grey
                        //System.out.println(k + " " + i);
                        basicShadePaintOperation(currentVBC, 1, inverse);
                        //System.out.println("***************************");
                    } else if (currentVBC.getChar() != '-') {
                        //shade white
                        basicShadePaintOperation(currentVBC, 0, inverse);
                    }
                }
            }

        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
        System.out.println("duration: " + duration / 1000000);
    }

    private String backHexSimilar = "#B6B6B6";
    private String backHexIdentity = "#000000";
    private String backHexDiff = "#FFFFFF";

    private String foreHexIdentity;
    private String foreHexSimilar;
    private String foreHexDiff;

    private void setColors() {
        foreHexIdentity = vmsa.decideForeGroundColor(backHexIdentity);
        foreHexSimilar = vmsa.decideForeGroundColor(backHexSimilar);
        foreHexDiff = vmsa.decideForeGroundColor(backHexDiff);
    }

    private void basicShadePaintOperation(VisualBioChar vbc, int color, boolean invert) {

        //0 = white, 1 = grey, 2 = white
        if (color != -1) {
            if (invert) {
                color = 2 - color;
            }

            if (color == 0) {
                vbc.setCurrentStyle(this.vmsa.generateFont(backHexDiff, foreHexDiff, false, false), backHexDiff, foreHexDiff);
                //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("white"));
            } else if (color == 1) {
                vbc.setCurrentStyle(this.vmsa.generateFont(backHexSimilar, foreHexSimilar, false, false),
                        backHexSimilar, foreHexSimilar);
                //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("grey"));

            } else if (color == 2) {
                vbc.setCurrentStyle(this.vmsa.generateFont(backHexIdentity, foreHexIdentity, false, false), backHexIdentity, foreHexIdentity);
                //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("black"));
            }

        }
    }

    private void frequencyAdapterFromBasicRules(HashMap<Character, String> categoryRules) {
        basicAdapterFor = new HashMap<>();
        basicAdapterRev = new HashMap<>();

        List<String> categories = new ArrayList<String>();
        for (String val : categoryRules.values()) {
            boolean repeat = false;
            repeat_checker:
            for (String strcat : categories) {
                if (strcat.equals(val)) {
                    repeat = true;
                    break repeat_checker;
                }
            }
            if (!repeat) {
                categories.add(val);
            }
        }

        int i = 0;
        for (String ss : categories) {
            basicAdapterFor.put(ss, i);
            basicAdapterRev.put(i, ss);
            System.out.println(ss + " " + i + " " + basicAdapterFor.get(ss) + " " + basicAdapterRev.get(i));

            i++;
        }

    }

    private void frequencyAdapterFromAlphabet(Alphabet a) {
        alphabetAdapterFor = new HashMap<>();
        alphabetAdapterRev = new HashMap<>();

        int i = 0;
        for (Character cc : a.keySet()) {
            if (cc != '-' && !a.get(cc).equals("Gap")) { //exclude gap characters
                alphabetAdapterFor.put(cc, i);
                alphabetAdapterRev.put(i, cc);

                System.out.println(cc + " " + i + " " + alphabetAdapterFor.get(cc) + " " + alphabetAdapterRev.get(i));
                i++;
            }
        }

    }

    /*
    private double getLabelLength(Label label) {
        return Toolkit.getToolkit().getFontLoader().computeStringWidth(label.getText(), label.getFont());
    }
     */
    public void hide() {
        stage.hide();
    }

    public void unHide(boolean paint) {
        stage.show();
        if (paint) {
            //vmsa.clearShading();
            basicShade(frequency, inverseShadingBox.isSelected());
        }
    }

    //is this remote actually open?
    public boolean isShowing() {
        return this.stage.isShowing();
    }

}
