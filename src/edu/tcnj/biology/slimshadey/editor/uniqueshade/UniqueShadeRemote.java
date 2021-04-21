/*
    Copyright (c) 2018 Avi Shah, Anudeep Deevi, Sudhir Nayak

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
package edu.tcnj.biology.slimshadey.editor.uniqueshade;

import edu.tcnj.biology.seqverter.sequence.Sequence;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.EditorTab;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
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
 *
 * @author Avi Shah
 */
public class UniqueShadeRemote {

    private VisualMultipleSequenceAlignment vmsa;

    private Stage stage;
    private Scene scene;

    private EditorTab originator;

    private VBox root;

    private HBox countBox;
    private Label countLabelMessage;
    private Slider countSlider;
    private Label countLabel;
    private int count;

    private HBox colorDiffBox;
    private Label colorDiffLabel;
    private ColorPicker cpDiff;

    private CheckBox shadeGapsCheckBox;

    private static final int DEFAULT_COUNT = 1;

    /**
     * A <tt>UniqueShadeRemote</tt> is directly related to its originating
     * <tt>EditorTab</tt>. Users are able to highlight unique residues on the
     * <tt>VisualMultipleSequenceAlignment</tt> on the originating tab by
     * launching the tab's <tt>UniqueShadeRemote</tt> and changing the shading
     * settings.
     *
     * User's define the maximum allowable count for shading via a slider. The
     * slider is at most half the number of sequences (floored). Thus, residues
     * that comprise more than half a column will never be shaded.
     *
     * E.g. If a column has the following composition: A, A, A, D, N - and the
     * maximum allowable threshold is set to 1, then D and N will be shaded.
     * Note that floor(5/2) = 2, so A will never be shaded by UniqueShade.
     *
     * @param originator the <tt>EditorTab</tt> with which this remote is
     * associated
     */
    public UniqueShadeRemote(EditorTab originator) {
        this.vmsa = originator.getVMSA();
        this.originator = originator;

        stage = new Stage();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("UniqueShade remote - ".concat(originator.getText()));
        stage.setAlwaysOnTop(true);

        countLabelMessage = new Label("Maximum allowed count: ");
        countLabelMessage.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        countSlider = new Slider(0, Math.floor((double) this.vmsa.sequenceNumber() / 2), DEFAULT_COUNT);
        countSlider.setMajorTickUnit(1);
        countSlider.setMinorTickCount(0);
        countSlider.setShowTickMarks(true);

        count = DEFAULT_COUNT;
        countLabel = new Label(String.valueOf(DEFAULT_COUNT));
        countLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));

        countSlider.valueProperty().addListener(e -> updatedSlider());
        countBox = new HBox();
        countBox.getChildren().addAll(countLabelMessage, countSlider, countLabel);

        colorDiffLabel = new Label("(Unique residue shading)");
        colorDiffLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        cpDiff = new ColorPicker();
        cpDiff.setValue(Color.web(backHex));
        cpDiff.valueProperty().addListener(e -> {
            this.backHex = vmsa.hexifyColorFX(cpDiff.getValue());
            uniqueShade();
        });
        colorDiffBox = new HBox(5);
        colorDiffBox.getChildren().addAll(cpDiff, colorDiffLabel);
        vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpDiff}));

        shadeGapsCheckBox = new CheckBox("shade gaps");
        shadeGapsCheckBox.setSelected(true);
        shadeGapsCheckBox.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        shadeGapsCheckBox.setOnAction(e -> {
            uniqueShade();
        });

        root = new VBox(8);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.getChildren().addAll(countBox, colorDiffBox, shadeGapsCheckBox);

        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

        stage.setWidth(root.getWidth() + 50);

        //vmsa.clearShading();
        this.uniqueShade();
    }

    private String backHex = "#000000";
    private String foreHex;

    /**
     * The UniqueShade protocol is defined below.
     *
     * First, the count of each residue in each column is found. Then, based on
     * the maximum allowable threshold for shading, a set of instructions is
     * stored in an <tt>ArrayList</tt>, which is read back and used to paint the
     * originating <tt>EditorTab</tt>'s
     * <tt>VisualMultipleSequenceALignment</tt>.
     *
     */
    private void uniqueShade() {
        foreHex = vmsa.decideForeGroundColor(backHex);
        int seqNum = this.vmsa.sequenceNumber();
        int seqLen = this.vmsa.sequenceLength();
        int[][] countVectors = this.getCounts(seqNum, seqLen);

        ArrayList<ArrayList<Character>> uniqueShadeInstructions = new ArrayList<>(countVectors.length);

        //generate shading instructions by using reverse adapter
        for (int[] countVector : countVectors) {
            ArrayList<Character> charsToShade = new ArrayList<>();
            for (int k = 0; k < countVector.length; k++) {
                int count = countVector[k];
                if (count <= this.count) {
                    /*this index in the counts vector is at or below the maximum
                    allowable threshold, and so the corresponding char from the 
                    Alphabet is retrieved bia the reverse adapter*/
                    charsToShade.add(this.reverseAdapter.get(k));
                }
            }
            uniqueShadeInstructions.add(charsToShade);
        }

        //execute paint operations as per instructions list
        for (int i = 0; i < uniqueShadeInstructions.size(); i++) {
            ArrayList<Character> chars = uniqueShadeInstructions.get(i);
            for (int k = 0; k < seqNum; k++) {
                VisualBioChar vbc = this.vmsa.getVBC(k, i);
                for (char c : chars) {
                    if (vbc.getChar() == c) { //if the character at a VBC matches a selected char
                        this.paint(vbc, true);
                        break;
                    } else {
                        this.paint(vbc, false);
                    }
                }
            }
        }
    }

    /**
     * Paints the passed <tt>VisualBioChar</tt> according to a command to either
     * paint or leave the element blank (really paint it to default).
     *
     * @param vbc the <tt>VisualBioChar</tt> to paint
     * @param paint if <tt>true</tt> paint the <tt>VisualBioChar</tt> vbc
     */
    private void paint(VisualBioChar vbc, boolean paint) {
        if (paint) {
            if (vbc.getChar() != '-' || (vbc.getChar() == '-' && shadeGapsCheckBox.isSelected())) {
                //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("black"));
                vbc.setCurrentStyle(this.vmsa.generateFont(backHex, foreHex, false, false), backHex, foreHex);
            }
        } else {
            vbc.setCurrentStyle(this.vmsa.generateFont("#FFFFFF", "#000000", false, false), "#FFFFFF", "#000000");
            //vbc.getStyleClass().set(1, "biochar_basicShade_".concat("white"));
        }
    }

    private int[][] getCounts(int seqNum, int seqLen) { //generate intermediate dataholder

        //populate adapter maps
        this.populateUniqueAdapters();

        //create return vector, dimensions large enough to 
        int[][] ret = new int[seqLen][this.vmsa.getAlphabet().size() + 1];

        for (int i = 0; i < seqLen; i++) {
            /*the counts vector below is the size of the alphabet, and corresponds 
            to a single column. for every instance of a character in the column, 
            the corresponding position in the counts vector is incremented. this is done,
            so the stored counts can be compared to the maximum allowable threshold 
            later to determine the characters to shade*/
            int[] currentCountsVector = new int[this.vmsa.getAlphabet().size() + 1];
            for (int k = 0; k < seqNum; k++) {
                Character c = this.vmsa.getVBC(k, i).getChar();
                currentCountsVector[this.forwardAdapter.get(c)]++;
                currentCountsVector[currentCountsVector.length - 1]++;
            }
            ret[i] = currentCountsVector;
        }
        return ret;
    }

    private HashMap<Character, Integer> forwardAdapter;
    private HashMap<Integer, Character> reverseAdapter;

    private void populateUniqueAdapters() {

        forwardAdapter = new HashMap<>();
        reverseAdapter = new HashMap<>();

        int i = 0;
        for (Character ch : this.vmsa.getAlphabet().keySet()) {
            this.forwardAdapter.put(ch, i);
            this.reverseAdapter.put(i, ch);
            i++;
        }
    }

    /*NOTE: snapToTick() doesn't work, had to hardcode*/
    private void updatedSlider() {
        int count = (int) Math.rint(countSlider.getValue());
        countLabel.setText(String.valueOf(count));
        countSlider.setValue(count);
        if (count != this.count) {
            //only update and shade if the slider reached a new rounded value
            this.count = count;
            this.uniqueShade();
        }
    }

    public void unHide(boolean paint) {
        stage.show();
        if (paint) {
            //vmsa.clearShading();
            uniqueShade();
        }
    }

    public void hide() {
        stage.hide();
    }
}
