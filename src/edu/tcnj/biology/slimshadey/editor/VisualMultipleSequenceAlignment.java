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
package edu.tcnj.biology.slimshadey.editor;

import edu.tcnj.biology.seqloco_x.SeqLoco_X;
import edu.tcnj.biology.seqverter.converter.FileFormat;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.graphics.GeneralLoadingManager;
import edu.tcnj.biology.seqverter.sequence.Alphabet;
import edu.tcnj.biology.seqverter.sequence.Matrix;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;

/**
 *
 * @author Avi Shah
 */
public class VisualMultipleSequenceAlignment {

    private EditorTab origin;

    private int sequenceLength;
    private int sequenceNumber;

    private VBox /*ObservableList<HBox>*/ annotationRows;
    private Label NUMBERING = new Label("Numbering");
    private VBox /*ObservableList<HBox>*/ visualSequences;

    private VBox /*ObservableList<Label>*/ annotationLabels;
    private HBox numberingRow;
    private VBox /*ObservableList<Label>*/ sequenceNames;

    private Label CONSENSUS = new Label("Consensus");
    private HBox consensusRow;

    private SequenceHolder sequenceHolder;

    private int numberAnnotationRows = 4;
    private boolean numberingActive = true;

    private int firstIndex = 1;

    public AnchorPane parent;
    public ScrollPane scrollParent;

    private VBox left;
    private VBox right;

    public int numberingSpacing = 10;

    private final static double GLOBAL_RECTANGLE_OPACITY = 0.26;

    private final SeqLoco_X seqLoco_controller;

    public VisualMultipleSequenceAlignment(SequenceHolder sequences,
            AnchorPane parent, ScrollPane scrollParent, EditorTab origin) {
        System.out.println(hexifyColorFX(Color.GRAY.brighter()));
        this.sequenceHolder = sequences;
        this.parent = parent;
        this.scrollParent = scrollParent;
        this.origin = origin;

        sequenceNumber = sequenceHolder.size();        //# of sequences in MSA
        sequenceLength = sequenceHolder.get(0).size(); //# of chars in each sequence

        // do check for many duplicates; if present, pause construction
        List<List<Sequence>> groups = new ArrayList<>();

        sequence_loop:
        for (Sequence seq : sequenceHolder) {
            boolean wasAddedToGroup = false;
            group_loop:
            for (List<Sequence> group : groups) {
                if (seq.seqString().equals(group.get(0).seqString())) {
                    group.add(seq);
                    wasAddedToGroup = true;
                    break group_loop;
                }
            }
            if (!wasAddedToGroup) {
                groups.add(new ArrayList(Arrays.asList(new Sequence[]{seq})));
            }
        }

        final boolean[] autoCollapse = new boolean[]{false};

        if (groups.size() < sequenceHolder.size()) {
            Stage autoCollapseStage = new Stage();
            autoCollapseStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            autoCollapseStage.setTitle("Collapse sequences?");
            String message = "You have repeated sequences in this alignment. "
                    + "Before displaying this alignment, you can choose to collapse "
                    + "repeated sequences into groups; doing so now would reduce "
                    + "the number of sequences displayed from "
                    + String.valueOf(sequenceHolder.size())
                    + " to "
                    + String.valueOf(groups.size())
                    + " and would save a roughly proportional amount of time "
                    + "and memory.";

            Text messageText = new Text(message);
            messageText.setWrappingWidth(550);
            messageText.setTextAlignment(TextAlignment.CENTER);

            HBox buttonBox = new HBox(10);
            Button showAllButton = new Button("Show all sequences");
            showAllButton.setOnAction(ev -> {
                autoCollapseStage.hide();
            });
            showAllButton.setMinWidth(150);
            Button autoCollapseButton = new Button("Collapse repeats");
            autoCollapseButton.setOnAction(ev -> {
                autoCollapse[0] = true;
                autoCollapseStage.hide();
            });
            autoCollapseButton.setMinWidth(150);

            buttonBox.getChildren().addAll(showAllButton, autoCollapseButton);
            buttonBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(buttonBox, Priority.ALWAYS);

            VBox autoCollapseRoot = new VBox(10);
            autoCollapseRoot.getChildren().addAll(messageText, buttonBox);
            autoCollapseRoot.setAlignment(Pos.CENTER);
            autoCollapseRoot.setPadding(new Insets(12, 12, 12, 12));

            Scene autoCollapseScene = new Scene(autoCollapseRoot);
            autoCollapseStage.setScene(autoCollapseScene);
            autoCollapseStage.setAlwaysOnTop(true);
            autoCollapseStage.showAndWait();
        }

        seqLoco_controller = new SeqLoco_X(this);
        /*
        the sequenceStr rows being initialized
         */
        annotationRows = new VBox();
        //visualRows = FXCollections.observableArrayList();
        for (int i = 0; i < numberAnnotationRows; i++) {
            HBox annotationRow = new HBox();
            boolean isanno = true;
            boolean isnumb = false;
            boolean isseq = false;
            boolean isgroup = false;
            boolean iscons = false;
            for (int k = 0; k < sequenceLength; k++) {
                VisualBioChar vbc = new VisualBioChar(isanno, isnumb, isseq, isgroup, iscons);
                vbc.setCurrentStyle(DEFAULT_STYLE, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
                this.setVBCBehavior(vbc);
                annotationRow.getChildren().add(vbc);
                vbc.index = k;
            }
            this.annotationRows.getChildren().add(annotationRow);
        }
        annotationLabels = new VBox();
        //rowNames = FXCollections.observableArrayList();
        //4 empty rows, sequenceStr rows
        for (int i = 0; i < numberAnnotationRows; i++) {
            Label temp = new Label("(Annotations_".concat(String.valueOf(i + 1)).concat(")"));
            temp.setStyle(DEFAULT_STYLE);
            //temp.getStyleClass().set(0, "biochar_label");            
            this.setLabelBehavior(temp, true, false, false, false, false);
            annotationLabels.getChildren().add(temp);
        }
        //sequenceNames.add(NUMBERING);

        /*
        Loading in the visual sequences, in their own VBox for both names and
        raw sequences (gapped). 
         */
        visualSequences = new VBox();
        //visualSequences = FXCollections.observableArrayList();
        for (int i = 0; i < sequenceNumber; i++) {
            HBox sequence = new HBox();
            Sequence seq = sequenceHolder.get(i);
            for (int k = 0; k < sequenceLength; k++) {
                boolean isanno = false;
                boolean isnumb = false;
                boolean isseq = true;
                boolean isgroup = false;
                boolean iscons = false;
                VisualBioChar vbc = new VisualBioChar(String.valueOf(seq.get(k).get()),
                        isanno, isnumb, isseq, isgroup, iscons);
                vbc.setCurrentStyle(DEFAULT_STYLE, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
                this.setVBCBehavior(vbc);
                vbc.index = k;
                sequence.getChildren().add(vbc);
            }
            this.visualSequences.getChildren().add(sequence);
        }
        sequenceNames = new VBox();
        //sequenceNames = FXCollections.observableArrayList();
        sequenceHolder.forEach((seq) -> {
            Label temp = new Label(seq.name());
            temp.setStyle(DEFAULT_STYLE);
            //temp.getStyleClass().set(0, "biochar_label");
            this.setLabelBehavior(temp, false, false, true, false, false);
            sequenceNames.getChildren().add(temp);
        });

        // set up all numbering
        numberingRow = new HBox();
        for (int k = 0; k < sequenceLength; k++) {
            boolean isanno = false;
            boolean isnumb = true;
            boolean isseq = false;
            boolean isgroup = false;
            boolean iscons = false;
            VisualBioChar vbc = new VisualBioChar(isanno, isnumb, isseq, isgroup, iscons);
            vbc.setCurrentStyle(DEFAULT_STYLE, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
            this.setVBCBehavior(vbc);
            numberingRow.getChildren().add(vbc);
            vbc.index = k;
        }
        numbering(numberingSpacing, true);
        NUMBERING.setStyle(DEFAULT_STYLE);
        //NUMBERING.getStyleClass().set(0, "biochar_label");
        this.setLabelBehavior(NUMBERING, false, true, false, false, false);
        //this.annotationRows.add(numberingRow);

        consensusRow = new HBox();
        for (int k = 0; k < sequenceLength; k++) {
            boolean isanno = false;
            boolean isnumb = false;
            boolean isseq = false;
            boolean isgroup = false;
            boolean iscons = true;
            VisualBioChar vbc = new VisualBioChar(isanno, isnumb, isseq, isgroup, iscons);
            vbc.setCurrentStyle(DEFAULT_STYLE, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
            this.setVBCBehavior(vbc);
            consensusRow.getChildren().add(vbc);
            vbc.index = k;
        }
        simpleConsensus(sequenceHolder.consensusSequence);

        CONSENSUS.setStyle(DEFAULT_STYLE);
        this.setLabelBehavior(CONSENSUS, false, false, false, false, true);

        left = new VBox();
        left.getChildren().addAll(annotationLabels);
        left.getChildren().add(NUMBERING);
        left.getChildren().addAll(sequenceNames);
        left.getChildren().add(CONSENSUS);

        right = new VBox();
        right.getChildren().addAll(annotationRows);
        right.getChildren().add(numberingRow);
        right.getChildren().addAll(visualSequences);
        right.getChildren().addAll(consensusRow);

        right.setOnMouseExited(e -> {
            hover_taskManager(null);
        });

        //collapseIdenticalSequences(autoCollapse[0]);
        this.autoCollapse = autoCollapse[0];
    }
    
    public boolean autoCollapse = false;
    
    
    //save identical sequence groups for de-collapsing sequences
    private List<List<List<Object>>> identicalSequenceGroups;
    private boolean isCollapsed = false;

    public boolean isCollapsed() {
        return this.isCollapsed;
    }

    public void collapseIdenticalSequences(boolean collapse) {

        this.getEditorTab().closeAllRemotes();
        if (collapse && !this.isCollapsed) {
            final boolean[] newTab = new boolean[]{false};
            Stage collapseOptStage = new Stage();
            collapseOptStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            collapseOptStage.setTitle("Collapse sequences");
            Text graphicWarningText2 = new Text("Would you like to open the"
                    + " collapsed sequences in a new tab? Doing so will allow you"
                    + " to rename sequence groups and reorder sequences. If you"
                    + " choose to collapse sequences within this tab, you will"
                    + " not be able to rename sequence groups or reorder"
                    + " sequences, unless you uncollapse.");
            graphicWarningText2.setWrappingWidth(550);
            graphicWarningText2.setTextAlignment(TextAlignment.CENTER);

            Button no1 = new Button("Within this tab");
            no1.setMinWidth(150);
            no1.setOnAction(e -> {
                newTab[0] = false;
                collapseOptStage.close();
            });

            Button no2 = new Button("In new tab");
            no2.setMinWidth(150);
            no2.setOnAction(eve -> {
                newTab[0] = true;
                collapseOptStage.close();
            });

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(buttonBox, Priority.ALWAYS);
            buttonBox.getChildren().addAll(no1, no2);

            VBox rootBox = new VBox(15);
            rootBox.setPadding(new Insets(10, 10, 10, 10));
            rootBox.setAlignment(Pos.CENTER);
            rootBox.getChildren().addAll(graphicWarningText2, buttonBox);

            Scene mwpScene = new Scene(rootBox);
            collapseOptStage.setScene(mwpScene);
            collapseOptStage.initModality(Modality.APPLICATION_MODAL);
            collapseOptStage.setAlwaysOnTop(true);
            collapseOptStage.setMaxWidth(600);
            collapseOptStage.showAndWait();

            //structure of object: (int index, String names...) : all info on seq needed
            List<List<List<Object>>> sequenceGroups = new ArrayList<>();
            List<List<Object>> group1 = new ArrayList<>();
            List<Object> object1 = new ArrayList<>();
            object1.add(0); //add first sequence to first group
            object1.add(((Label) sequenceNames.getChildren().get(0)).getText());
            group1.add(object1);
            sequenceGroups.add(group1);
            outer_loop:
            for (int i = 1; i < this.sequenceNumber(); i++) {
                inner_loop:
                for (int k = 0; k < sequenceGroups.size(); k++) {
                    if (this.visualSequencesSame(i, (int) sequenceGroups.get(k).get(0).get(0))) {
                        //this sequence belongs to the present group
                        List<Object> newObject = new ArrayList<>();
                        newObject.add(i);
                        newObject.add((((Label) sequenceNames.getChildren().get(i)).getText()));
                        // has index and name
                        sequenceGroups.get(k).add(newObject);
                        break inner_loop;
                    } else if (k == sequenceGroups.size() - 1) {
                        //this sequences belongs to a new group
                        List<List<Object>> newGroup = new ArrayList<>();
                        List<Object> newObject = new ArrayList<>();
                        newObject.add(i);
                        newObject.add((((Label) sequenceNames.getChildren().get(i)).getText()));
                        newGroup.add(newObject);
                        sequenceGroups.add(newGroup);
                        break inner_loop;
                    }
                }
            }
            // debugging grouper code
            /*
        for (List<Integer> group : sequenceGroups) {
            System.out.println("");
            for (Integer index : group) {
                System.out.print(index + "; ");
                System.out.println(((Label) this.getSequenceNamesBox().getChildren().get(index)).getText());
            }
        }
             */

            System.out.println(DEFAULT_STYLE);
            VBox collapsedSequenceNames = new VBox();
            VBox collapsedSequences = new VBox();
            for (int i = 0; i < sequenceGroups.size(); i++) {
                String labelstr = "Group".concat(String.valueOf(i)).concat("_");
                for (List<Object> infoObject : sequenceGroups.get(i)) {
                    labelstr = labelstr.concat(String.valueOf(infoObject.get(1)).concat("_"));
                }
                //labelstr = labelstr.concat("}");
                Label groupLabel = new Label(labelstr);
                groupLabel.setStyle(generateFont("#FFFFFF", "#000000", false, false));
                this.setLabelBehavior(groupLabel, false, false, false, true, false);
                collapsedSequenceNames.getChildren().add(groupLabel);
                System.out.println(i + " " + visualSequences.getChildren().size());
                HBox sequenceGroupi = (HBox) visualSequences.getChildren().get(((int) sequenceGroups.get(i).get(0).get(0)));
                HBox sequenceGroupiCopy = new HBox();
                for (int k = 0; k < sequenceGroupi.getChildren().size(); k++) {
                    VisualBioChar currentChar = ((VisualBioChar) sequenceGroupi.getChildren().get(k));
                    VisualBioChar vbc = currentChar.copy();
                    vbc.isgroup = true;
                    this.setVBCBehavior(vbc);
                    vbc.index = k;
                    sequenceGroupiCopy.getChildren().add(vbc);
                }
                collapsedSequences.getChildren().add(sequenceGroupiCopy);
            }

            left.getChildren().set(left.getChildren().indexOf(sequenceNames), collapsedSequenceNames);
            right.getChildren().set(right.getChildren().indexOf(visualSequences), collapsedSequences);

            sequenceNames = collapsedSequenceNames;
            visualSequences = collapsedSequences;

            identicalSequenceGroups = sequenceGroups;
            this.isCollapsed = true;
            if (newTab[0]) {
                try {
                    File tempShadey = File.createTempFile("temp_shadey", ".tmp");
                    this.getEditorTab().THE_PROGRAM.getShadeyProjectManager().setData_collapseOpts(this.getEditorTab(), tempShadey);
                    this.getEditorTab().THE_PROGRAM.read(tempShadey, FileFormat.slim, this.getEditorTab().getText().concat("_collapsed"), false, false);
                } catch (IOException ex) {
                    Logger.getLogger(VisualMultipleSequenceAlignment.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    this.collapseIdenticalSequences(false);
                }
            }
        } else if (!collapse && this.isCollapsed) { //i.e. un-collapse sequences

            VBox sequenceNames_temp = new VBox();
            VBox visualSequences_temp = new VBox();
            //sequenceNames = new VBox();
            //visualSequences = new VBox();

            for (int i = 0; i < identicalSequenceGroups.size(); i++) {
                for (int k = 0; k < identicalSequenceGroups.get(i).size(); k++) {
                    String name = String.valueOf(identicalSequenceGroups.get(i).get(k).get(1));
                    Label nameLabel = new Label(name);
                    nameLabel.setStyle(generateFont("#FFFFFF", "#000000", false, false));
                    this.setLabelBehavior(nameLabel, false, false, true, false, false);
                    sequenceNames_temp.getChildren().add(nameLabel);

                    HBox sequenceBoxTemp = new HBox();
                    for (int t = 0; t < ((HBox) visualSequences.getChildren().get(i)).getChildren().size(); t++) {
                        //HBox currentSeq = (HBox) collapsedSequences.getChildren().get(i);
                        VisualBioChar currentChar = ((VisualBioChar) ((HBox) visualSequences.getChildren().get(i)).getChildren().get(t));
                        VisualBioChar vbc = currentChar.copy();
                        vbc.isgroup = false;
                        this.setVBCBehavior(vbc);
                        vbc.index = t;
                        sequenceBoxTemp.getChildren().add(vbc);
                    }
                    visualSequences_temp.getChildren().add(sequenceBoxTemp);
                }
            }

            left.getChildren().set(left.getChildren().indexOf(sequenceNames), sequenceNames_temp);
            right.getChildren().set(right.getChildren().indexOf(visualSequences), visualSequences_temp);

            sequenceNames = sequenceNames_temp;
            visualSequences = visualSequences_temp;

            this.isCollapsed = false;
        }

    }

    private boolean visualSequencesSame(int seqIndex1, int seqIndex2) {
        for (int i = 0; i < this.sequenceLength(); i++) {
            if (this.getVBC(seqIndex1, i).getChar() != this.getVBC(seqIndex2, i).getChar()) {
                return false;
            }
        }
        return true;
    }

    // consensus methods
    public void simpleConsensus(Sequence consensus) {
        if (consensus != null) {
            int seqLen = this.sequenceLength();
            for (int i = 0; i < seqLen; i++) { //iterate over length of seqs
                ((VisualBioChar) consensusRow.getChildren().get(i)).setText(String.valueOf(consensus.charAtSequenceIndex(i).get()));
            }
        } else {
            int seqLen = this.sequenceLength();
            int seqNum = this.sequenceNumber();

            Alphabet alpha = this.getAlphabet();
            Set<Character> charSet = alpha.keySet();

            HashMap<Character, Integer> residueIncidenceCounter = new HashMap();

            for (int i = 0; i < seqLen; i++) { //iterate over length of seqs

                for (Character c : charSet) { //reset the incidence counter object
                    residueIncidenceCounter.put(c, 0);
                }

                for (int k = 0; k < seqNum; k++) { //traverse column at given seqIndex
                    char currentChar = this.getVBC(k, i).getChar();
                    if (charSet.contains(currentChar)) {
                        residueIncidenceCounter.put(currentChar, residueIncidenceCounter.get(currentChar) + 1);
                    }
                }

                ArrayList<Character> mostIncidences = new ArrayList();
                int maxIncidenceCounter = 0;
                for (Character c : charSet) {
                    if (residueIncidenceCounter.get(c) == maxIncidenceCounter) {
                        mostIncidences.add(c);
                    } else if (residueIncidenceCounter.get(c) > maxIncidenceCounter) {
                        maxIncidenceCounter = residueIncidenceCounter.get(c);
                        mostIncidences.clear();
                        mostIncidences.add(c);
                    }
                }

                double maxIncidenceRatio = ((double) maxIncidenceCounter) / ((double) seqNum);
                double oneOffRatio = (((double) seqNum) - 1.0) / ((double) seqNum);
                //this is where ties can be broken:
                if (mostIncidences.size() == 1 && maxIncidenceRatio >= oneOffRatio) {
                    ((VisualBioChar) consensusRow.getChildren().get(i)).setText(String.valueOf(mostIncidences.get(0)));
                } else if (mostIncidences.size() == 1 && maxIncidenceRatio < oneOffRatio) {
                    ((VisualBioChar) consensusRow.getChildren().get(i)).setText(String.valueOf(mostIncidences.get(0)).toLowerCase());
                }
                //System.out.print(i + ": " + ((VisualBioChar) consensusRow.getChildren().get(i)).getChar());
                for (char ch : mostIncidences) {
                    //System.out.print(ch + " ");
                }
                //System.out.println();
            }
        }
    }

    public void dotsAndIdentityConsensus() {
        int seqLen = this.sequenceLength();
        int seqNum = this.sequenceNumber();

        Alphabet alpha = this.getAlphabet();
        Set<Character> charSet = alpha.keySet();

        HashMap<Character, Integer> residueIncidenceCounter = new HashMap<>();
        HashMap<Character, Double> residueFrequencyCounter = new HashMap<>();

        for (int i = 0; i < seqLen; i++) { //iterate over length of seqs

            for (Character c : charSet) { //reset the incidence counter object
                residueIncidenceCounter.put(c, 0);
                residueFrequencyCounter.put(c, 0.0d);
            }

            for (int k = 0; k < seqNum; k++) { //traverse column at given seqIndex
                char currentChar = this.getVBC(k, i).getChar();
                residueIncidenceCounter.put(currentChar, residueIncidenceCounter.get(currentChar) + 1);
            }

            for (Character c : residueIncidenceCounter.keySet()) {
                residueFrequencyCounter.put(c, (double) (((double) residueIncidenceCounter.get(c)) / ((double) seqNum)));
            }

            double maxFreq = 0.0;
            Character identityChar = null;
            for (Character c : residueFrequencyCounter.keySet()) {
                if (residueFrequencyCounter.get(c) > maxFreq) {
                    maxFreq = residueFrequencyCounter.get(c);
                    if (maxFreq == 1.0) {
                        identityChar = c;
                        break;
                    }
                }
            }

            char toUse = ' ';
            if (identityChar != null) {
                toUse = identityChar;
            } else if (maxFreq >= 0.0 && maxFreq < 0.25) {
                toUse = ' ';
            } else if (maxFreq >= 0.25 && maxFreq < 0.5) {
                toUse = '.';
            } else if (maxFreq >= 0.5 && maxFreq < 0.75) {
                toUse = ':';
            } else if (maxFreq >= 0.75 && maxFreq < 1) {
                toUse = '*';
            }
            ((VisualBioChar) consensusRow.getChildren().get(i)).setText(String.valueOf(toUse));

        }
    }

    public void consensusClustalX() {
        Alphabet alpha = this.getAlphabet();

        int seqLen = this.sequenceLength();
        int seqNum = this.sequenceNumber();

        if (alpha.consensusAlphabetClustal != null && !alpha.consensusAlphabetClustal.isEmpty()) {
            HashMap<String, Integer> groupToCountMap = new HashMap<>();
            for (int i = 0; i < seqLen; i++) { //iterate over length of seqs

                for (List<String> l : alpha.consensusRulesClustal) {
                    groupToCountMap.put(l.get(2), 0);
                    //System.out.println(l.get(2));
                }

                for (int k = 0; k < seqNum; k++) { //traverse column at given seqIndex
                    char currentChar = this.getVBC(k, i).getChar();
                    for (String group : groupToCountMap.keySet()) {
                        if (group.contains(String.valueOf(currentChar))) {
                            groupToCountMap.put(group, groupToCountMap.get(group) + 1);
                        }
                    }
                }

                String consCar = " ";
                for (List<String> list : alpha.consensusRulesClustal) {
                    String currentGroup = list.get(2);
                    double groupFreq = (double) ((double) groupToCountMap.get(currentGroup)) / ((double) seqNum);
                    double necessaryFreq = Double.parseDouble(list.get(1));
                    if (groupFreq >= necessaryFreq) {
                        consCar = list.get(0);
                        break;
                    }
                }
                ((VisualBioChar) consensusRow.getChildren().get(i)).setText(consCar);
            }
        }
    }

    public void colorClustalX() {
        this.consensusClustalX();

        Alphabet alpha = this.getAlphabet();

        int seqLen = this.sequenceLength();
        int seqNum = this.sequenceNumber();

        if (alpha.consensusAlphabetClustal != null && !alpha.consensusAlphabetClustal.isEmpty()) {
            for (int i = 0; i < seqLen; i++) {
                //color column by column
                String consensusChar = String.valueOf(((VisualBioChar) this.consensusRow.getChildren().get(i)).getChar());
                //traverse columns
                for (int k = 0; k < seqNum; k++) { //traverse column at given seqIndex
                    VisualBioChar currentVBC = this.getVBC(k, i);
                    char currentChar = currentVBC.getChar();
                    String[] colorRules = alpha.consensusColorClustal.getOrDefault(currentChar, null);
                    if (colorRules != null && colorRules[1].contains(String.valueOf(consensusChar))) {
                        String hexColor = alpha.colorToHexClustal.get(colorRules[0]);
                        currentVBC.setCurrentStyle(this.generateFont(hexColor, false, false), hexColor, decideForeGroundColor(hexColor));
                    }
                }

            }
        }
    }

    public void calculateConsensus() {
        Stage consensusStage = new Stage();
        consensusStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        consensusStage.setTitle("Consensus options");

        VBox consensusOptionsRoot = new VBox(10);

        RadioButton simpleConsensus = new RadioButton("Simple (default)");
        simpleConsensus.setSelected(true);

        RadioButton dotsAndIdentity = new RadioButton("Dots and identity");
        dotsAndIdentity.setSelected(false);

        ToggleGroup toggles = new ToggleGroup();
        toggles.getToggles().addAll(simpleConsensus, dotsAndIdentity);

        VBox optionsBox = new VBox(5);
        optionsBox.getChildren().addAll(simpleConsensus, dotsAndIdentity);

        RadioButton consensusClustalX = new RadioButton("Clustal X consensus");
        consensusClustalX.setSelected(false);
        if (this.getAlphabet().consensusAlphabetClustal != null && !this.getAlphabet().consensusAlphabetClustal.isEmpty()) {
            toggles.getToggles().add(consensusClustalX);
            optionsBox.getChildren().add(consensusClustalX);
        }

        Button executeConsensus = new Button("Generate consensus");
        executeConsensus.setOnAction(event -> {
            for (Node n : consensusRow.getChildren()) {
                ((VisualBioChar) n).setText(" ");
            }
            if (simpleConsensus.isSelected()) {
                simpleConsensus(null);
            } else if (dotsAndIdentity.isSelected()) {
                dotsAndIdentityConsensus();
            } else if (consensusClustalX.isSelected()) {
                consensusClustalX();
            }
            consensusStage.close();
        });
        HBox executeConsensusBox = new HBox();
        executeConsensusBox.setPadding(new Insets(5));
        executeConsensusBox.getChildren().add(executeConsensus);
        executeConsensusBox.setAlignment(Pos.CENTER);

        Label consensusOptionsLabel = new Label("Consensus types:");
        consensusOptionsRoot.getChildren().addAll(consensusOptionsLabel, optionsBox, executeConsensusBox);
        consensusOptionsRoot.setPadding(new Insets(10, 10, 0, 10));
        Scene consensusOptionsScene = new Scene(consensusOptionsRoot);
        consensusStage.initModality(Modality.APPLICATION_MODAL);
        consensusStage.setAlwaysOnTop(true);
        consensusStage.setScene(consensusOptionsScene);
        consensusStage.show();
    }

    public void numbering(int spacing, boolean active) {
        for (Node n : numberingRow.getChildren()) {
            ((VisualBioChar) n).setText(" ");
        }
        if (active) {
            for (int k = 0; k < sequenceLength(); k++) {
                if ((k + 1) % spacing == 0) {
                    int trueIndex = k + this.firstIndex;
                    if (String.valueOf(trueIndex).length() < sequenceLength() - k - String.valueOf(trueIndex).length()) {
                        for (int l = 0; l < String.valueOf(trueIndex).length(); l++) {
                            if (numberingRow.getChildren().get(k + l) instanceof VisualBioChar) {
                                ((VisualBioChar) numberingRow.getChildren().get(k + l)).setText(String.valueOf(String.valueOf(trueIndex).charAt(l)));
                            }
                        }
                    }
                    System.out.println(k + this.firstIndex + " " + k + " " + this.firstIndex);
                }
            }
        } else {
            for (int k = 0; k < sequenceLength(); k++) {
                if (numberingRow.getChildren().get(k) instanceof VisualBioChar) {
                    ((VisualBioChar) numberingRow.getChildren().get(k)).setText(" ");
                }
            }
        }
    }

    private ContextMenu cmClick = null;
    private ContextMenu cmDragr = null;

    protected int[] getColumnIndices(VisualBioChar vbc) {
        Parent p = vbc.getParent();
        int gapCount = 0;
        for (int childIndex = 0; childIndex <= p.getChildrenUnmodifiable().indexOf(vbc); childIndex++) {
            if (((VisualBioChar) p.getChildrenUnmodifiable().get(childIndex)).getChar() == '-') {
                gapCount++;
            }
        }
        return new int[]{p.getChildrenUnmodifiable().indexOf(vbc) + 1, p.getChildrenUnmodifiable().indexOf(vbc) + 1 - gapCount};
    }

    private void setVBCBehavior(VisualBioChar vbc) {
        vbc.setCursor(Cursor.HAND);

        vbc.setOnMouseEntered(e -> {
            hover_taskManager(vbc);
            //this.getEditorTab().vbcEditor().releaseVBC();
            this.getEditorTab().vbcEditor().updateTempVBC(vbc);
        });

        vbc.setOnMouseClicked((MouseEvent me) -> {
            System.out.println(me.getButton());
            if (me.getButton().equals(MouseButton.PRIMARY) && !vbc.isnumb) {
                vbc.select(true, generateFont(Color.ORANGE, Color.BLACK, false, false));
                //this.select(vbc, true);

                resetContextMenus();

                cmClick = new ContextMenu();

                MenuItem edit = new MenuItem("Edit");
                edit.setOnAction(e -> {
                    cmClick.hide();
                    this.getEditorTab().vbcEditor().disable(false);
                    this.getEditorTab().vbcEditor().setCurrentVBC(vbc);
                    //vbcEditor.setCurrentVBC_show(vbc, true);
                });

                cmClick.getItems().addAll(edit);

                Bounds bounds = vbc.getBoundsInLocal();
                Bounds screenBounds = vbc.localToScreen(bounds);
                int x = (int) screenBounds.getMinX();
                int y = (int) screenBounds.getMinY();

                cmClick.show(vbc.getParent(), screenBounds.getMaxX(), screenBounds.getMaxY());

                edit.setStyle("");

                cmClick.setOnHidden(event -> {

                    vbc.select(false, null);
                    //this.select(vbc, false);
                    //this.removeSelection();
                });
            }
        });

        this.scrollTimeline = new Timeline();
        this.scrollTimeline.setCycleCount(Timeline.INDEFINITE);
        this.scrollTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(20), "Autoscroll", e -> {
                    dragScroll();
                }));

        vbc.setOnDragDetected(e -> {
            vbc.startFullDrag();
            //this.dragOrigin = vbc;
        });

        vbc.setOnMouseDragged((MouseEvent de) -> {

            if (de.getButton().equals(MouseButton.SECONDARY)) {

                this.scrollDirection = 0;
                Bounds bounds = this.scrollParent.getBoundsInLocal();

                Bounds screenBounds = this.scrollParent.localToScreen(bounds);
                if (de.getScreenX() < screenBounds.getMinX() + 10 /*10px*/) {
                    this.scrollDirection = -1.0 / ((double) this.sequenceLength());
                    this.scrollTimeline.play();
                } else if (de.getScreenX() > screenBounds.getMaxX() - 10 /*10 px leeway*/) {
                    this.scrollDirection = 1.0 / ((double) this.sequenceLength());
                    this.scrollTimeline.play();
                } else {
                    //update selection - within bounds
                    this.scrollTimeline.pause();
                }
            }
        });

        vbc.setOnMousePressed(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                this.singleColumnSelect(vbc.getBoundsInParent().getMinX(), vbc.getWidth());
                this.dragOrigin = vbc;
                this.isSingleSelect = true;
            }
        });

        vbc.setOnMouseReleased(e -> {
            this.scrollTimeline.pause();
            if (e.getButton().equals(MouseButton.SECONDARY) && dragRect != null) {

                resetContextMenus();

                int[] range = this.getCurrentSelectionRange();

                cmDragr = new ContextMenu();

                MenuItem insertLeft = new MenuItem("Insert to left");
                insertLeft.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    insertColumns(range[0]);
                });

                MenuItem insertRight = new MenuItem("Insert to right");
                insertRight.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    insertColumns(range[1] + 1);
                });

                MenuItem deleteColumn = new MenuItem("Delete selection");
                deleteColumn.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    deleteColumns(range[0], range[1], true);
                });

                MenuItem makeLogo = new MenuItem("Make sequence logo");
                makeLogo.setOnAction(event -> {

                    String[] seqs = new String[this.sequenceNumber()];

                    int loc1 = dragOrigin.getParent().getChildrenUnmodifiable().indexOf(dragOrigin);
                    int loc2 = loc1;
                    if (dragEnd != null) {
                        loc2 = dragEnd.getParent().getChildrenUnmodifiable().indexOf(dragEnd);
                    }

                    //System.out.println(loc1 + " " + loc2);
                    if (loc1 == loc2 || isSingleSelect) {
                        for (Node node : this.getSequencesBox().getChildren()) {
                            HBox seq = (HBox) node;
                            int currentIndex = this.getSequencesBox().getChildren().indexOf(node);
                            seqs[currentIndex] = String.valueOf(this.getVBC(currentIndex, loc1).getChar());
                        }
                    } else {
                        if (loc2 < loc1) {
                            int inter = loc1;
                            loc1 = loc2;
                            loc2 = inter;
                        }
                        for (Node node : this.getSequencesBox().getChildren()) {
                            int seqIndex = this.getSequencesBox().getChildren().indexOf(node);
                            String toAdd = "";
                            for (int k = loc1; k <= loc2; k++) {
                                toAdd = toAdd.concat(String.valueOf(this.getVBC(seqIndex, k).getChar()));
                            }
                            seqs[seqIndex] = toAdd;
                        }
                    }
                    //System.out.println(Arrays.deepToString(seqs));
                    seqLoco_controller.setupSeqLoco(seqs, this.getAlphabet().defaultColorMapping);

                });

                MenuItem addAnnotation = new MenuItem("Annotate selection");
                addAnnotation.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    annotateSeq(range[0], range[1]);
                });

                MenuItem editSequences = new MenuItem("Edit sequences");
                editSequences.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    editSeqs(range[0], range[1]);
                });

                MenuItem shadeSelection = new MenuItem("Shade manually");
                shadeSelection.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    shadeSelectionManually(range[0], range[1]);
                });

                MenuItem blockShade = new MenuItem("Block shading");
                blockShade.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    shadeSelectionBlocked(range[0], range[1]);
                });

                MenuItem exportSelection = new MenuItem("Export selection");
                exportSelection.setOnAction(event -> {
                    this.setWorkingColumnSelection(range[0], range[1]);
                    exportColumns(getCurrentSelectionRange());
                    this.removeWorkingColumnSelection();
                });

                cmDragr.getItems().addAll(insertLeft, insertRight, deleteColumn,
                        new SeparatorMenuItem(), makeLogo,
                        new SeparatorMenuItem(), addAnnotation,
                        new SeparatorMenuItem(), editSequences,
                        new SeparatorMenuItem(), shadeSelection, blockShade,
                        new SeparatorMenuItem(), exportSelection);

                Bounds bounds = dragRect.getBoundsInLocal();
                Bounds screenBounds = dragRect.localToScreen(bounds);
                int x = (int) screenBounds.getMinX();
                int y = (int) screenBounds.getMinY();

                cmDragr.setOnShown(ev -> {

                    //this.vbcEditor.hide();
                });

                cmDragr.show(dragRect, screenBounds.getMaxX(), screenBounds.getMaxY());

                cmDragr.setOnHidden(event -> {
                    this.removeSelection();
                });
            }
        });

        vbc.setOnMouseDragEntered(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                this.dragEnd = vbc;
                this.updateColumnSelectionDrag();
                this.isSingleSelect = false;
                System.out.println(vbc.index + " " + dragOrigin.index + " " + isSingleSelect);
            }
        });

    }

    private void insertColumns(int index) {
        Stage insertColStage = new Stage();
        insertColStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        insertColStage.setTitle("Insert columns");

        VBox insertColRoot = new VBox(10);

        HBox numColsBox = new HBox(5);
        TextField numCols = new TextField();
        numCols.setText("1");
        numCols.setPromptText("1");
        numCols.setMaxWidth(60);
        numCols.textProperty().addListener(event -> {
            if ((!numCols.getText().isEmpty() && !isInteger(numCols.getText())) || numCols.getText().length() > 2) {
                numCols.setText(numCols.getText().substring(0, numCols.getLength() - 1));
            }
        });
        Label numColsLabel0 = new Label("Insert ");
        Label numColsLabel1 = new Label(" columns");
        numColsBox.getChildren().addAll(numColsLabel0, numCols, numColsLabel1);
        numColsBox.setAlignment(Pos.CENTER);

        HBox executeBox = new HBox(5);
        Button execute = new Button("Insert");
        execute.setOnAction(e -> {
            int toAdd = 1;
            if (!numCols.getText().isEmpty()) {
                toAdd = Integer.parseInt(numCols.getText());
            }
            //boolean isanno, boolean isnumb, boolean isseq, boolean isgroup, boolean iscons
            boolean isgroup = isCollapsed;
            for (int j = 0; j < toAdd; j++) {
                for (Node n : annotationRows.getChildren()) {
                    VisualBioChar addanno = new VisualBioChar(" ", true, false, false, false, false);
                    this.setVBCBehavior(addanno);
                    addanno.setCurrentStyle(generateFont("#FFFFFF", "#000000", false, false), "#FFFFFF", "#000000");
                    ((HBox) n).getChildren().add(index, addanno);
                }
                for (Node n : visualSequences.getChildren()) {
                    VisualBioChar addseq = new VisualBioChar("-", false, false, true, isgroup, false);
                    this.setVBCBehavior(addseq);
                    addseq.setCurrentStyle(generateFont("#FFFFFF", "#000000", false, false), "#FFFFFF", "#000000");
                    ((HBox) n).getChildren().add(index, addseq);
                }
                VisualBioChar vbcnum = new VisualBioChar(" ", false, true, false, false, false);
                this.setVBCBehavior(vbcnum);
                vbcnum.setCurrentStyle(generateFont("#FFFFFF", "#000000", false, false), "#FFFFFF", "#000000");
                numberingRow.getChildren().add(index, vbcnum);
                VisualBioChar vbccons = new VisualBioChar(" ", false, false, false, false, true);
                this.setVBCBehavior(vbccons);
                vbccons.setCurrentStyle(generateFont("#FFFFFF", "#000000", false, false), "#FFFFFF", "#000000");
                consensusRow.getChildren().add(index, vbccons);
            }
            numbering(numberingSpacing, true);
            insertColStage.close();
        });
        execute.setMinWidth(140);
        executeBox.getChildren().add(execute);
        executeBox.setAlignment(Pos.CENTER);

        insertColRoot.getChildren().addAll(numColsBox, executeBox);
        insertColRoot.setPadding(new Insets(15));

        Scene insertColScene = new Scene(insertColRoot);
        insertColStage.setScene(insertColScene);
        insertColStage.setAlwaysOnTop(true);
        insertColStage.initModality(Modality.APPLICATION_MODAL);
        insertColStage.show();

        insertColStage.setOnHidden(e -> {
            this.removeWorkingColumnSelection();
        });

    }

    private int[] getCurrentSelectionRange() {
        int loc1 = dragOrigin.getParent().getChildrenUnmodifiable().indexOf(dragOrigin);
        int loc2 = loc1;
        if (dragEnd != null && dragEnd.getParent() != null) {
            loc2 = dragEnd.getParent().getChildrenUnmodifiable().indexOf(dragEnd);
        }
        if (isSingleSelect) {
            loc2 = loc1;
        }
        if (loc2 < loc1) {
            int inter = loc1;
            loc1 = loc2;
            loc2 = inter;
        }
        return new int[]{loc1, loc2};
    }

    private void shadeSelectionBlocked(int start, int end) {
        Stage manualShadeStage = new Stage();
        manualShadeStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        manualShadeStage.setTitle("Shade manually");
        ScrollPane manualShadeScrollRoot = new ScrollPane();

        GridPane manualShadeRoot = new GridPane();
        manualShadeRoot.setPadding(new Insets(12));
        //manualShadeRoot.alignmentProperty().set(Pos.CENTER);
        manualShadeRoot.setHgap(10);
        manualShadeRoot.setVgap(5);
        String backColor = "Shading";
        String foreColor = "Text fill";

        int rows = 0;
        Label back1 = new Label(backColor);
        back1.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore1 = new Label(foreColor);
        fore1.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back1, 1, rows);
        manualShadeRoot.add(fore1, 2, rows);
        Label annoLabel = new Label("(Annotations)");
        annoLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(annoLabel, 0, rows++);

        List<ColorPicker> toBind = new ArrayList<>();

        Button resetAnno = new Button("Reset");
        manualShadeRoot.add(resetAnno, 0, rows);

        ColorPicker cpbackAnno = new ColorPicker();
        manualShadeRoot.add(cpbackAnno, 1, rows);
        toBind.add(cpbackAnno);

        ColorPicker cpforeAnno = new ColorPicker();
        manualShadeRoot.add(cpforeAnno, 2, rows);
        toBind.add(cpforeAnno);

        HashMap<Integer, HashMap<Integer, String[]>> origColorsAnno = new HashMap<>();
        for (int k = 0; k < annotationRows.getChildren().size(); k++) {

            final int seqIndex = k;
            final HashMap<Integer, String[]> origColorsMap = new HashMap<>();
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                origColorsMap.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
            }
            origColorsAnno.put(k, origColorsMap);
        }

        resetAnno.setOnAction(e -> {

            for (int k = 0; k < annotationRows.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    vbc.setCurrentStyle(generateFont(origColorsAnno.get(k).get(j)[0], origColorsAnno.get(k).get(j)[1], false, false), origColorsAnno.get(k).get(j)[0], origColorsAnno.get(k).get(j)[1]);
                }
            }
            cpbackAnno.setValue(Color.web(origColorsAnno.get(0).get(start)[0]));
            cpforeAnno.setValue(Color.web(origColorsAnno.get(0).get(start)[1]));
        });

        VisualBioChar firstvbc = getAnnoVBC(0, start);
        cpbackAnno.setValue(Color.web(firstvbc.getBackHex()));
        cpforeAnno.setValue(Color.web(firstvbc.getForeHex()));

        cpbackAnno.setOnAction(e -> {
            for (int k = 0; k < annotationRows.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpbackAnno.getValue());
                    String foreHex = this.decideForeGroundColor(backHex);
                    cpforeAnno.setValue(Color.web(foreHex));
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            }
        });

        cpforeAnno.setOnAction(e -> {
            for (int k = 0; k < annotationRows.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpbackAnno.getValue());
                    String foreHex = hexifyColorFX(cpforeAnno.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            }
        });

        //Label lbname = new Label(((Label) annotationLabels.getChildren().get(k)).getText());
        //manualShadeRoot.add(lbname, 3, rows);
        rows++;

        Label back2 = new Label(backColor);
        back2.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore2 = new Label(foreColor);
        fore2.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back2, 1, rows);
        manualShadeRoot.add(fore2, 2, rows);
        Label seqLabel = new Label("(Sequences)");
        seqLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(seqLabel, 0, rows++);

        Button resetSeq = new Button("Reset");
        manualShadeRoot.add(resetSeq, 0, rows);

        ColorPicker cpbackSeq = new ColorPicker();
        manualShadeRoot.add(cpbackSeq, 1, rows);
        toBind.add(cpbackSeq);

        ColorPicker cpforeSeq = new ColorPicker();
        manualShadeRoot.add(cpforeSeq, 2, rows);
        toBind.add(cpforeSeq);

        HashMap<Integer, HashMap<Integer, String[]>> origColorsSeq = new HashMap<>();
        for (int k = 0; k < visualSequences.getChildren().size(); k++) {

            final int seqIndex = k;
            final HashMap<Integer, String[]> origColorsMap = new HashMap<>();
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = getVBC(seqIndex, j);
                origColorsMap.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
            }
            origColorsSeq.put(k, origColorsMap);
        }

        resetSeq.setOnAction(e -> {
            for (int k = 0; k < visualSequences.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    vbc.setCurrentStyle(generateFont(origColorsSeq.get(k).get(j)[0], origColorsSeq.get(k).get(j)[1], false, false), origColorsSeq.get(k).get(j)[0], origColorsSeq.get(k).get(j)[1]);
                }

            }
            cpbackSeq.setValue(Color.web(origColorsSeq.get(0).get(start)[0]));
            cpforeSeq.setValue(Color.web(origColorsSeq.get(0).get(start)[1]));
        });

        VisualBioChar firstvbcSeq = getVBC(0, start);
        cpbackSeq.setValue(Color.web(firstvbcSeq.getBackHex()));
        cpforeSeq.setValue(Color.web(firstvbcSeq.getForeHex()));

        cpbackSeq.setOnAction(e -> {
            for (int k = 0; k < visualSequences.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpbackSeq.getValue());
                    String foreHex = hexifyColorFX(cpforeSeq.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            }
        });

        cpforeSeq.setOnAction(e -> {
            for (int k = 0; k < visualSequences.getChildren().size(); k++) {

                final int seqIndex = k;
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpbackSeq.getValue());
                    String foreHex = hexifyColorFX(cpforeSeq.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            }
        });

        //Label lbname = new Label(((Label) sequenceNames.getChildren().get(k)).getText());
        //manualShadeRoot.add(lbname, 3, rows);
        rows++;

        bindColorPickers(toBind);

        Label back3 = new Label(backColor);
        back3.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore3 = new Label(foreColor);
        fore3.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back3, 1, rows);
        manualShadeRoot.add(fore3, 2, rows);
        Label consLabel = new Label("(Consensus)");
        consLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(consLabel, 0, rows++);

        Button reset = new Button("Reset");
        manualShadeRoot.add(reset, 0, rows);
        ColorPicker cpback = new ColorPicker();
        manualShadeRoot.add(cpback, 1, rows);

        ColorPicker cpfore = new ColorPicker();
        manualShadeRoot.add(cpfore, 2, rows);

        final HashMap<Integer, String[]> origColors = new HashMap<>();
        for (int j = start; j <= end; j++) {
            VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
            origColors.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
        }
        reset.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                vbc.setCurrentStyle(generateFont(origColors.get(j)[0], origColors.get(j)[1], false, false), origColors.get(j)[0], origColors.get(j)[1]);
            }
            cpback.setValue(Color.web(origColors.get(start)[0]));
            cpfore.setValue(Color.web(origColors.get(start)[1]));
        });
        VisualBioChar firstvbcCons = (VisualBioChar) consensusRow.getChildren().get(start);
        cpback.setValue(Color.web(firstvbcCons.getBackHex()));
        cpfore.setValue(Color.web(firstvbcCons.getForeHex()));

        cpback.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                String backHex = hexifyColorFX(cpback.getValue());
                String foreHex = hexifyColorFX(cpfore.getValue());
                vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
            }
        });

        cpfore.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                String backHex = hexifyColorFX(cpback.getValue());
                String foreHex = hexifyColorFX(cpfore.getValue());
                vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
            }
        });

        //Label lbname = new Label(CONSENSUS.getText());
        //manualShadeRoot.add(lbname, 3, rows);
        manualShadeScrollRoot.setContent(manualShadeRoot);
        Scene manualShadeScene = new Scene(manualShadeScrollRoot);
        manualShadeStage.setScene(manualShadeScene);
        manualShadeStage.setAlwaysOnTop(true);
        manualShadeStage.initModality(Modality.APPLICATION_MODAL);
        manualShadeStage.show();

        manualShadeStage.setOnHidden(e -> {
            this.removeWorkingColumnSelection();
        });
    }

    private void shadeSelectionManually(int start, int end) {
        Stage manualShadeStage = new Stage();
        manualShadeStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        manualShadeStage.setTitle("Shade manually");
        ScrollPane manualShadeScrollRoot = new ScrollPane();

        GridPane manualShadeRoot = new GridPane();
        manualShadeRoot.setPadding(new Insets(12));
        //manualShadeRoot.alignmentProperty().set(Pos.CENTER);
        manualShadeRoot.setHgap(10);
        manualShadeRoot.setVgap(5);
        String backColor = "Shading";
        String foreColor = "Text fill";

        int rows = 0;
        Label back1 = new Label(backColor);
        back1.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore1 = new Label(foreColor);
        fore1.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back1, 1, rows);
        manualShadeRoot.add(fore1, 2, rows);
        Label annoLabel = new Label("(Annotations)");
        annoLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(annoLabel, 3, rows);

        List<ColorPicker> toBind = new ArrayList<>();

        rows++;
        for (int k = 0; k < annotationRows.getChildren().size(); k++) {
            Button reset = new Button("Reset");
            manualShadeRoot.add(reset, 0, rows);

            ColorPicker cpback = new ColorPicker();
            manualShadeRoot.add(cpback, 1, rows);
            toBind.add(cpback);

            ColorPicker cpfore = new ColorPicker();
            manualShadeRoot.add(cpfore, 2, rows);
            toBind.add(cpfore);

            final int seqIndex = k;
            final HashMap<Integer, String[]> origColors = new HashMap<>();
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                origColors.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
            }

            reset.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    vbc.setCurrentStyle(generateFont(origColors.get(j)[0], origColors.get(j)[1], false, false), origColors.get(j)[0], origColors.get(j)[1]);
                }
                cpback.setValue(Color.web(origColors.get(start)[0]));
                cpfore.setValue(Color.web(origColors.get(start)[1]));
            });

            VisualBioChar firstvbc = getAnnoVBC(seqIndex, start);
            cpback.setValue(Color.web(firstvbc.getBackHex()));
            cpfore.setValue(Color.web(firstvbc.getForeHex()));

            cpback.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpback.getValue());
                    String foreHex = this.decideForeGroundColor(backHex);
                    cpfore.setValue(Color.web(foreHex));
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            });

            cpfore.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getAnnoVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpback.getValue());
                    String foreHex = hexifyColorFX(cpfore.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            });

            Label lbname = new Label(((Label) annotationLabels.getChildren().get(k)).getText());
            manualShadeRoot.add(lbname, 3, rows);
            rows++;
        }

        Label back2 = new Label(backColor);
        back2.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore2 = new Label(foreColor);
        fore2.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back2, 1, rows);
        manualShadeRoot.add(fore2, 2, rows);
        Label seqLabel = new Label("(Sequences)");
        seqLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(seqLabel, 3, rows++);

        for (int k = 0; k < visualSequences.getChildren().size(); k++) {
            Button reset = new Button("Reset");
            manualShadeRoot.add(reset, 0, rows);

            ColorPicker cpback = new ColorPicker();
            manualShadeRoot.add(cpback, 1, rows);
            toBind.add(cpback);

            ColorPicker cpfore = new ColorPicker();
            manualShadeRoot.add(cpfore, 2, rows);
            toBind.add(cpfore);

            final int seqIndex = k;
            final HashMap<Integer, String[]> origColors = new HashMap<>();
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = getVBC(seqIndex, j);
                origColors.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
            }
            reset.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    vbc.setCurrentStyle(generateFont(origColors.get(j)[0], origColors.get(j)[1], false, false), origColors.get(j)[0], origColors.get(j)[1]);
                }
                cpback.setValue(Color.web(origColors.get(start)[0]));
                cpfore.setValue(Color.web(origColors.get(start)[1]));
            });

            VisualBioChar firstvbc = getVBC(k, start);
            cpback.setValue(Color.web(firstvbc.getBackHex()));
            cpfore.setValue(Color.web(firstvbc.getForeHex()));

            cpback.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpback.getValue());
                    String foreHex = hexifyColorFX(cpfore.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            });

            cpfore.setOnAction(e -> {
                for (int j = start; j <= end; j++) {
                    VisualBioChar vbc = getVBC(seqIndex, j);
                    String backHex = hexifyColorFX(cpback.getValue());
                    String foreHex = hexifyColorFX(cpfore.getValue());
                    vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
                }
            });

            Label lbname = new Label(((Label) sequenceNames.getChildren().get(k)).getText());
            manualShadeRoot.add(lbname, 3, rows);
            rows++;
        }

        bindColorPickers(toBind);

        Label back3 = new Label(backColor);
        back3.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        Label fore3 = new Label(foreColor);
        fore3.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(back3, 1, rows);
        manualShadeRoot.add(fore3, 2, rows);
        Label consLabel = new Label("(Consensus)");
        consLabel.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        manualShadeRoot.add(consLabel, 3, rows++);

        Button reset = new Button("Reset");
        manualShadeRoot.add(reset, 0, rows);
        ColorPicker cpback = new ColorPicker();
        manualShadeRoot.add(cpback, 1, rows);

        ColorPicker cpfore = new ColorPicker();
        manualShadeRoot.add(cpfore, 2, rows);

        final HashMap<Integer, String[]> origColors = new HashMap<>();
        for (int j = start; j <= end; j++) {
            VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
            origColors.put(j, new String[]{vbc.getBackHex(), vbc.getForeHex()});
        }
        reset.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                vbc.setCurrentStyle(generateFont(origColors.get(j)[0], origColors.get(j)[1], false, false), origColors.get(j)[0], origColors.get(j)[1]);
            }
            cpback.setValue(Color.web(origColors.get(start)[0]));
            cpfore.setValue(Color.web(origColors.get(start)[1]));
        });
        VisualBioChar firstvbc = (VisualBioChar) consensusRow.getChildren().get(start);
        cpback.setValue(Color.web(firstvbc.getBackHex()));
        cpfore.setValue(Color.web(firstvbc.getForeHex()));

        cpback.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                String backHex = hexifyColorFX(cpback.getValue());
                String foreHex = hexifyColorFX(cpfore.getValue());
                vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
            }
        });

        cpfore.setOnAction(e -> {
            for (int j = start; j <= end; j++) {
                VisualBioChar vbc = (VisualBioChar) consensusRow.getChildren().get(j);
                String backHex = hexifyColorFX(cpback.getValue());
                String foreHex = hexifyColorFX(cpfore.getValue());
                vbc.setCurrentStyle(generateFont(backHex, foreHex, false, false), backHex, foreHex);
            }
        });

        Label lbname = new Label(CONSENSUS.getText());
        manualShadeRoot.add(lbname, 3, rows);
        manualShadeScrollRoot.setContent(manualShadeRoot);
        Scene manualShadeScene = new Scene(manualShadeScrollRoot);
        manualShadeStage.setScene(manualShadeScene);
        manualShadeStage.setAlwaysOnTop(true);
        manualShadeStage.initModality(Modality.APPLICATION_MODAL);
        manualShadeStage.show();

        manualShadeStage.setOnHidden(e -> {
            this.removeWorkingColumnSelection();
        });
    }

    private int exportct = 1;

    private void exportColumns(int[] range) {

        //copy annotations
        VBox annotationLabelsTemp = new VBox();
        for (int k = 0; k < annotationLabels.getChildren().size(); k++) {
            Label temp = new Label(((Label) annotationLabels.getChildren().get(k)).getText());
            temp.setStyle(DEFAULT_STYLE);
            //temp.getStyleClass().set(0, "biochar_label");            
            annotationLabelsTemp.getChildren().add(temp);
        }

        VBox annotationRowsTemp = new VBox();
        for (int k = 0; k < annotationRows.getChildren().size(); k++) {
            HBox currentAnnotationRow = (HBox) this.annotationRows.getChildren().get(k);
            HBox annotationRowTemp = new HBox();
            for (int i = range[0]; i <= range[1]; i++) {
                annotationRowTemp.getChildren().add(((VisualBioChar) currentAnnotationRow.getChildren().get(i)).copy());
            }
            annotationRowsTemp.getChildren().add(annotationRowTemp);
        }

        VBox sequenceNamesTemp = new VBox();
        for (int k = 0; k < sequenceNames.getChildren().size(); k++) {
            Label temp = new Label(((Label) sequenceNames.getChildren().get(k)).getText());
            temp.setStyle(DEFAULT_STYLE);
            //temp.getStyleClass().set(0, "biochar_label");            
            sequenceNamesTemp.getChildren().add(temp);
        }

        VBox visualSequencesTemp = new VBox();
        for (int k = 0; k < visualSequences.getChildren().size(); k++) {
            HBox currentVisualSequence = (HBox) this.visualSequences.getChildren().get(k);
            HBox visualSequenceTemp = new HBox();
            for (int i = range[0]; i <= range[1]; i++) {
                visualSequenceTemp.getChildren().add(((VisualBioChar) currentVisualSequence.getChildren().get(i)).copy());
            }
            visualSequencesTemp.getChildren().add(visualSequenceTemp);
        }

        HBox consensusTemp = new HBox();
        for (int i = range[0]; i <= range[1]; i++) {
            consensusTemp.getChildren().add(((VisualBioChar) consensusRow.getChildren().get(i)).copy());
        }

        VisualMultipleSequenceAlignment newvmsa = this.origin.THE_PROGRAM.tabFromSubset(annotationLabelsTemp,
                annotationRowsTemp, sequenceNamesTemp, visualSequencesTemp,
                consensusTemp, range,
                this.origin.getText().concat("_ex-").concat(String.valueOf(exportct++)),
                this.getAlphabet());
        newvmsa.updateStyleSettings(fontSize, luminanceThreshold);
    }

    protected VisualMultipleSequenceAlignment(VBox annotationLabelsTemp,
            VBox annotationRowsTemp, VBox sequenceNamesTemp, VBox visualSequencesTemp,
            HBox consensusTemp, AnchorPane parent, ScrollPane scrollParent, EditorTab origin,
            int[] range, Alphabet alpha) {
        this.parent = parent;
        this.scrollParent = scrollParent;
        this.origin = origin;
        //vbcEditor = new VBCEditor(this);
        seqLoco_controller = new SeqLoco_X(this);

        sequenceNumber = visualSequencesTemp.getChildren().size();        //# of sequences in MSA
        sequenceLength = ((HBox) visualSequencesTemp.getChildren().get(0)).getChildren().size(); //# of chars in each sequence

        this.sequenceHolder = new SequenceHolder();
        this.sequenceHolder.setAlphabet(alpha);
        this.annotationLabels = annotationLabelsTemp;
        for (Node lbanno : annotationLabels.getChildren()) {
            ((Label) lbanno).setStyle(generateFont("#FFFFFF", "#000000", false, false));
            this.setLabelBehavior((Label) lbanno, true, false, false, false, false);
        }

        this.annotationRows = annotationRowsTemp;
        for (Node hbx : annotationRows.getChildren()) {
            HBox currentAnnotationRow = (HBox) hbx;
            for (Node vbc : currentAnnotationRow.getChildren()) {
                this.setVBCBehavior((VisualBioChar) vbc);
            }
        }

        this.firstIndex = range[0] + 1;
        numberingRow = new HBox();
        for (int k = 0; k < sequenceLength; k++) {
            boolean isanno = false;
            boolean isnumb = true;
            boolean isseq = false;
            boolean isgroup = false;
            boolean iscons = false;
            VisualBioChar vbc = new VisualBioChar(isanno, isnumb, isseq, isgroup, iscons);
            vbc.setCurrentStyle(DEFAULT_STYLE, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
            this.setVBCBehavior(vbc);
            numberingRow.getChildren().add(vbc);
            vbc.index = k;
        }

        NUMBERING.setStyle(generateFont("#FFFFFF", "#000000", false, false));
        this.setLabelBehavior(NUMBERING, false, true, false, false, false);

        this.sequenceNames = sequenceNamesTemp;
        for (Node lbseq : sequenceNames.getChildren()) {
            ((Label) lbseq).setStyle(generateFont("#FFFFFF", "#000000", false, false));
            this.setLabelBehavior((Label) lbseq, false, false, true, false, false);
        }

        this.visualSequences = visualSequencesTemp;
        for (Node hbx : visualSequences.getChildren()) {
            HBox currentVisualSequence = (HBox) hbx;
            for (Node vbc : currentVisualSequence.getChildren()) {
                this.setVBCBehavior((VisualBioChar) vbc);
            }
        }

        this.updatedSequenceHolder(range[0]);

        this.consensusRow = consensusTemp;
        for (Node vbc : consensusRow.getChildren()) {
            this.setVBCBehavior((VisualBioChar) vbc);
        }
        CONSENSUS.setStyle(generateFont("#FFFFFF", "#000000", false, false));
        this.setLabelBehavior(CONSENSUS, false, false, false, false, true);

        left = new VBox();
        left.getChildren().addAll(annotationLabels);
        left.getChildren().add(NUMBERING);
        left.getChildren().addAll(sequenceNames);
        left.getChildren().add(CONSENSUS);

        right = new VBox();
        right.getChildren().addAll(annotationRows);
        right.getChildren().add(numberingRow);
        right.getChildren().addAll(visualSequences);
        right.getChildren().addAll(consensusRow);

        right.setOnMouseExited(e -> {
            hover_taskManager(null);
        });

        numbering(numberingSpacing, true);
    }

    public void updatedSequenceHolder(int startIndex) {
        Alphabet alpha = sequenceHolder.getAlphabet();
        this.sequenceHolder = new SequenceHolder();
        sequenceHolder.setAlphabet(alpha);
        for (int k = 0; k < this.sequenceNames.getChildren().size(); k++) {
            Sequence s = new Sequence();
            s.setName(((Label) this.sequenceNames.getChildren().get(k)).getText());
            HBox currentVisualSeq = (HBox) this.visualSequences.getChildren().get(k);
            StringBuilder sbseq = new StringBuilder();
            for (int j = 0; j < currentVisualSeq.getChildren().size(); j++) {
                VisualBioChar currentVBC = (VisualBioChar) currentVisualSeq.getChildren().get(j);
                sbseq.append(currentVBC.getChar());
            }
            s.setSequence(sbseq.toString(), startIndex);
            this.sequenceHolder.add(s);
        }
    }

    private void annotateSeq(int start, int end) {
        System.out.println(start + " " + end);
        int allowableLength = end - start + 1;
        Stage annotateStage = new Stage();
        annotateStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        annotateStage.setTitle("Add annotations");

        Button executeAnnotate = new Button("Annotate");

        VBox annotateCommands = new VBox(5);
        List<TextField> regularizeSize = new ArrayList<>();
        List<Label> regularizeSizeComplement = new ArrayList<>();
        List<Callable<Void>> callables = new ArrayList<>();
        for (int k = 0; k < annotationLabels.getChildren().size(); k++) {
            HBox annotateBox = new HBox(5);
            TextField annotation = new TextField();
            HBox.setHgrow(annotation, Priority.ALWAYS);
            annotation.setMinWidth(400);
            annotation.setStyle(DEFAULT_STYLE);
            String existingAnnotation = "";
            StringBuilder sb = new StringBuilder();
            for (int i = start; i <= end; i++) {
                sb.append(getAnnoVBC(k, i).getChar());
            }

            existingAnnotation = sb.toString();
            if (existingAnnotation.trim().isEmpty()) {
                existingAnnotation = "";
            }
            annotation.setText(existingAnnotation);
            Label annotateLabel = new Label(((Label) annotationLabels.getChildren().get(k)).getText());
            if (annotation.getText().isEmpty() || annotation.getText().trim().isEmpty()) {
                annotateLabel.setTextFill(Color.GREY);
            } else {
                annotateLabel.setTextFill(Color.BLACK);
            }
            annotation.textProperty().addListener(ev -> {
                if (annotation.getText().isEmpty() || annotation.getText().trim().isEmpty()) {
                    annotateLabel.setTextFill(Color.GREY);
                } else {
                    annotateLabel.setTextFill(Color.BLACK);
                }
                if (annotation.getText().length() > allowableLength) {
                    annotation.setText(annotation.getText().substring(0, annotation.getLength() - 1));
                }
            });
            annotateBox.getChildren().addAll(annotation, annotateLabel);
            annotateCommands.getChildren().add(annotateBox);
            final int index = k;
            Callable onExecute = new Callable() {
                @Override
                public Object call() throws Exception {
                    Platform.runLater(() -> {
                        //System.err.println(pattern);
                        annotate(index, start, end, annotation.getText());
                    });
                    return null;
                }
            };
            callables.add(onExecute);

            regularizeSize.add(annotation);
            regularizeSizeComplement.add(annotateLabel);
        }

        executeAnnotate.setOnAction(e -> {
            annotateStage.close();
            for (Callable c : callables) {
                try {
                    c.call();
                } catch (Exception ex) {
                    Logger.getLogger(VisualMultipleSequenceAlignment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        TextField commonChars = new TextField();
        String[] useful = new String[]{"➰", "⇐", "⇒", "α", "β", "γ", "δ", "ε", "π"};
        String commonCharsText = "Common characters: ".concat(useful[0]);
        for (int p = 1; p < useful.length; p++) {
            commonCharsText = commonCharsText.concat(", ").concat(useful[p]);
        }
        commonChars.setText(commonCharsText);
        commonChars.setStyle(DEFAULT_STYLE);
        HBox.setHgrow(commonChars, Priority.ALWAYS);
        VBox annotateRoot = new VBox(8);
        annotateRoot.setPadding(new Insets(10));
        //annotateRoot.setAlignment(Pos.CENTER);
        HBox executeAnnotateBox = new HBox();
        executeAnnotateBox.getChildren().add(executeAnnotate);
        executeAnnotateBox.setAlignment(Pos.CENTER);
        annotateRoot.getChildren().addAll(annotateCommands, commonChars, executeAnnotateBox);
        Scene annotateScene = new Scene(annotateRoot);
        annotateStage.setScene(annotateScene);
        annotateStage.initModality(Modality.APPLICATION_MODAL);
        annotateStage.setAlwaysOnTop(true);
        annotateStage.show();
        annotateStage.setOnHidden(e -> {
            this.removeWorkingColumnSelection();
        });

        commonChars.setPrefWidth(annotateStage.getWidth());

        final double maxLabWidth = Collections.max(regularizeSizeComplement, Comparator.comparing(l -> l.getWidth())).getWidth();
        annotateStage.widthProperty().addListener(e -> {
            double paneWidth = ((HBox) annotateCommands.getChildren().get(0)).getWidth() - 20;
            double prefWidth = paneWidth - maxLabWidth;
            for (TextField tf : regularizeSize) {
                tf.setPrefWidth(prefWidth);
            }
        });

        double paneWidth = ((HBox) annotateCommands.getChildren().get(0)).getWidth() - 20;
        double prefWidth = paneWidth - maxLabWidth;
        for (TextField tf : regularizeSize) {
            tf.setPrefWidth(prefWidth);
        }
    }

    private void editSeqs(int start, int end) {
        System.out.println(start + " " + end);
        int allowableLength = end - start + 1;
        Stage editSeqsStage = new Stage();
        editSeqsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        editSeqsStage.setTitle("Edit sequences");

        Button executeEdit = new Button("Edit sequences");

        VBox editTypeOptionsBox = new VBox(10);
        String interpretSpaceAsGapText = "Replace end spaces with gaps";
        RadioButton interpretSpaceAsGap = new RadioButton(interpretSpaceAsGapText);
        interpretSpaceAsGap.setSelected(true);
        String fillOutPattternText = "Repeat entered pattern if shorter than  selection length";
        RadioButton fillOutPattern = new RadioButton(fillOutPattternText);
        fillOutPattern.setSelected(false);
        editTypeOptionsBox.getChildren().addAll(interpretSpaceAsGap, fillOutPattern);
        editTypeOptionsBox.setPadding(new Insets(10));

        ToggleGroup editType = new ToggleGroup();
        editType.getToggles().addAll(interpretSpaceAsGap, fillOutPattern);

        List<TextField> regularizeSize = new ArrayList<>();
        List<Label> regularizeSizeComplement = new ArrayList<>();

        VBox editSeqCommands = new VBox(5);
        List<Callable<Void>> callables = new ArrayList<>();
        for (int k = 0; k < this.sequenceNames.getChildren().size(); k++) {
            HBox seqBox = new HBox(5);
            TextField sequenceStr = new TextField();
            regularizeSize.add(sequenceStr);
            //HBox.setHgrow(sequenceStr, Priority.ALWAYS);
            sequenceStr.setMinWidth(400);
            sequenceStr.setStyle(DEFAULT_STYLE);
            String existingSequence = "";
            StringBuilder sb = new StringBuilder();
            for (int i = start; i <= end; i++) {
                sb.append(((VisualBioChar) ((HBox) this.visualSequences.getChildren().get(k)).getChildren().get(i)).getChar());
            }

            existingSequence = sb.toString();
            if (existingSequence.trim().isEmpty()) {
                existingSequence = "";
            }
            sequenceStr.setText(existingSequence);
            Label seqNameLabel = new Label(((Label) sequenceNames.getChildren().get(k)).getText());

            if (sequenceStr.getText().isEmpty() || sequenceStr.getText().trim().replaceAll("-", "").isEmpty()) {
                seqNameLabel.setTextFill(Color.GREY);
            } else {
                seqNameLabel.setTextFill(Color.BLACK);
            }
            sequenceStr.textProperty().addListener(ev -> {
                if (sequenceStr.getText().isEmpty() || sequenceStr.getText().trim().replaceAll("-", "").isEmpty()) {
                    seqNameLabel.setTextFill(Color.GREY);
                } else {
                    seqNameLabel.setTextFill(Color.BLACK);
                }
                if (sequenceStr.getText().length() > allowableLength) {
                    sequenceStr.setText(sequenceStr.getText().substring(0, sequenceStr.getLength() - 1));
                }

            });
            seqBox.getChildren().addAll(sequenceStr, seqNameLabel);
            editSeqCommands.getChildren().add(seqBox);
            final int index = k;
            Callable onExecute = new Callable() {
                @Override
                public Object call() throws Exception {
                    Platform.runLater(() -> {
                        //System.err.println(pattern);
                        editseq(index, start, end, sequenceStr.getText(), interpretSpaceAsGap.isSelected());
                    });
                    return null;
                }
            };
            callables.add(onExecute);

            regularizeSize.add(sequenceStr);
            regularizeSizeComplement.add(seqNameLabel);
        }

        executeEdit.setOnAction(e -> {
            editSeqsStage.close();
            for (Callable c : callables) {
                try {
                    c.call();
                } catch (Exception ex) {
                    Logger.getLogger(VisualMultipleSequenceAlignment.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        ScrollPane annotateCommandsPane = new ScrollPane();
        annotateCommandsPane.setContent(editSeqCommands);

        HBox.setHgrow(annotateCommandsPane, Priority.ALWAYS);
        VBox.setVgrow(annotateCommandsPane, Priority.ALWAYS);

        VBox annotateRoot = new VBox(8);
        //annotateRoot.setAlignment(Pos.CENTER);
        HBox executeEditBox = new HBox();
        executeEditBox.setPadding(new Insets(10));
        executeEditBox.getChildren().add(executeEdit);
        executeEditBox.setAlignment(Pos.CENTER);
        annotateRoot.getChildren().addAll(annotateCommandsPane, editTypeOptionsBox, executeEditBox);
        Scene annotateScene = new Scene(annotateRoot);
        editSeqsStage.setScene(annotateScene);
        editSeqsStage.initModality(Modality.APPLICATION_MODAL);
        editSeqsStage.setAlwaysOnTop(true);
        editSeqsStage.show();

        editSeqsStage.setOnHidden(e -> {
            this.removeWorkingColumnSelection();
        });

        final double maxLabWidth = Collections.max(regularizeSizeComplement, Comparator.comparing(l -> l.getWidth())).getWidth();
        editSeqsStage.widthProperty().addListener(e -> {

            double paneWidth = editSeqsStage.getWidth() - 50;
            double prefWidth = paneWidth - maxLabWidth;
            for (TextField tf : regularizeSize) {
                tf.setPrefWidth(prefWidth);
            }
        });

        double paneWidth = annotateCommandsPane.getWidth() - 50;
        double prefWidth = paneWidth - maxLabWidth;
        for (TextField tf : regularizeSize) {
            tf.setPrefWidth(prefWidth);
        }

        annotateCommandsPane.fitToWidthProperty().set(true);
    }

    private void annotate(int index, int start, int end, String pattern) {
        int patternIter = 0;
        boolean empty = pattern.trim().isEmpty();
        for (int k = start; k <= end; k++) {
            //System.err.println(pattern.charAt(patternIter));
            String toUse = " ";
            if (!empty) {
                toUse = String.valueOf(pattern.charAt(patternIter));
            }
            getAnnoVBC(index, k).setText(toUse);
            patternIter++;
            if (patternIter >= pattern.length()) {
                patternIter = 0;
            }
        }
    }

    private void editseq(int index, int start, int end, String pattern, boolean endSpaceAsGap) {
        if (endSpaceAsGap) {
            int patternIter = 0;
            boolean empty = pattern.trim().isEmpty();
            for (int k = start; k <= end; k++) {
                //System.err.println(pattern.charAt(patternIter));
                String toUse = " ";
                if (!empty) {
                    if (patternIter >= pattern.length()) {
                        toUse = " ";
                    } else {
                        toUse = String.valueOf(pattern.charAt(patternIter));
                    }
                }
                if (toUse.equals(" ")) {
                    toUse = "-";
                }
                ((VisualBioChar) ((HBox) this.visualSequences.getChildren().get(index)).getChildren().get(k)).setText(toUse);
                patternIter++;

            }
        } else {
            int patternIter = 0;
            boolean empty = pattern.trim().isEmpty();
            for (int k = start; k <= end; k++) {
                //System.err.println(pattern.charAt(patternIter));
                String toUse = " ";
                if (!empty) {
                    toUse = String.valueOf(pattern.charAt(patternIter));
                }
                if (toUse.equals(" ")) {
                    toUse = "-";
                }
                ((VisualBioChar) ((HBox) this.visualSequences.getChildren().get(index)).getChildren().get(k)).setText(toUse);
                patternIter++;
                if (patternIter >= pattern.length()) {
                    patternIter = 0;
                }
            }
        }
    }

    private void resetContextMenus() {
        if (cmDragr != null) {
            cmDragr.hide();
        }
        if (cmClick != null) {
            cmClick.hide();
        }
        if (cmLabel != null) {
            cmLabel.hide();
        }
    }

    private void removeSelection() {
        if (this.dragRect != null) {
            this.parent.getChildren().remove(this.dragRect);
        }
    }

    private boolean isSingleSelect = true;
    private VisualBioChar dragOrigin = null;
    private VisualBioChar dragEnd = null;
    private Rectangle dragRect = null;
    private final static Color DRAGFILL = Color.LIME;

    private void updateColumnSelectionDrag() {
        this.removeSelection();

        Bounds bs_orig = this.dragOrigin.getBoundsInParent();
        Bounds bs_drag = this.dragEnd.getBoundsInParent();

        double minX_orig = bs_orig.getMinX();
        double minX_drag = bs_drag.getMinX();

        double yval = 0.0;
        double xval = 0.0;
        double width = 0.0;
        if (minX_drag > minX_orig) {
            xval = minX_orig;
            width = minX_drag - minX_orig + this.dragEnd.getWidth();
        } else {
            xval = minX_drag;
            width = minX_orig - minX_drag + this.dragEnd.getWidth();
        }
        double height = this.parent.getHeight();

        this.dragRect = new Rectangle();
        this.dragRect.setMouseTransparent(true);
        this.dragRect.setFill(DRAGFILL);
        this.dragRect.setOpacity(GLOBAL_RECTANGLE_OPACITY);
        this.dragRect.setY(yval);
        this.dragRect.setX(xval);
        this.dragRect.setWidth(width);
        this.dragRect.setHeight(height);
        this.parent.getChildren().add(dragRect);
    }

    private Rectangle workingColumnSelection;

    private void setWorkingColumnSelection(int start, int end) {
        this.removeWorkingColumnSelection();
        VisualBioChar first = (VisualBioChar) numberingRow.getChildren().get(start);
        VisualBioChar last = (VisualBioChar) numberingRow.getChildren().get(end);
        Bounds bs_orig = first.getBoundsInParent();
        Bounds bs_drag = last.getBoundsInParent();

        double minX_orig = bs_orig.getMinX();
        double minX_drag = bs_drag.getMinX();

        double yval = 0.0;
        double xval = 0.0;
        double width = 0.0;
        if (minX_drag > minX_orig) {
            xval = minX_orig;
            width = minX_drag - minX_orig + first.getWidth();
        } else {
            xval = minX_drag;
            width = minX_orig - minX_drag + last.getWidth();
        }
        double height = this.parent.getHeight();
        this.workingColumnSelection = new Rectangle();
        this.workingColumnSelection.setMouseTransparent(true);
        this.workingColumnSelection.setFill(Color.web("#D3D3D3"));
        this.workingColumnSelection.setOpacity(0.24);
        this.workingColumnSelection.setY(yval);
        this.workingColumnSelection.setX(xval);
        this.workingColumnSelection.setWidth(width);
        this.workingColumnSelection.setHeight(height);
        this.parent.getChildren().add(workingColumnSelection);
    }

    private void removeWorkingColumnSelection() {
        if (this.workingColumnSelection != null) {
            this.parent.getChildren().remove(this.workingColumnSelection);
        }
    }

    private Timeline scrollTimeline;
    private double scrollDirection = 0;

    private void dragScroll() {
        double newValue = this.scrollParent.getHvalue() + scrollDirection;
        newValue = Math.min(newValue, 1.0);
        newValue = Math.max(newValue, 0.0);
        this.scrollParent.setHvalue(newValue);
    }

    private VisualBioChar selectedVBC = null;
    private VisualBioChar hoverVBC;

    private Rectangle screenRectHover;

    private Timer timerRefresh;
    private TimerTask timerTaskRefresh;

    private long refreshDelay = 0; //milliseconds

    private boolean trackLiveHover = true;

    public boolean getLiveHover() {
        return trackLiveHover;
    }

    public void setLiveHover(boolean hover) {
        trackLiveHover = hover;
    }

    public long getRefreshDelay() {
        return refreshDelay;
    }

    public void setRefreshDelay(long delay) {
        refreshDelay = delay;
    }

    private void hover_taskManager(VisualBioChar vbc) {
        //<editor-fold defaultstate="collapsed" desc="orig code column hover (underline)">
        /*
        if (select) {
        int commonIndex = vbc.getParent().getChildrenUnmodifiable().indexOf(vbc);
        for (HBox hb : this.annotationRows) {
        if (hb.getChildren().get(commonIndex) instanceof VisualBioChar
        && !((VisualBioChar) hb.getChildren().get(commonIndex)).selected
        && !hb.getChildren().get(commonIndex).getStyle().contains(underlineProperty)) {
        hb.getChildren().get(commonIndex).setStyle(hb.getChildren().get(commonIndex).getStyle().concat(underlineProperty));
        }
        }
        } else if (!select) {
        int commonIndex = vbc.getParent().getChildrenUnmodifiable().indexOf(vbc);
        for (HBox hb : this.annotationRows) {
        if (hb.getChildren().get(commonIndex) instanceof VisualBioChar
        && !((VisualBioChar) hb.getChildren().get(commonIndex)).selected
        && hb.getChildren().get(commonIndex).getStyle().contains(underlineProperty)) {
        hb.getChildren().get(commonIndex).setStyle(hb.getChildren().get(commonIndex).getStyle().replace(underlineProperty, ""));
        }
        }
        }*/
        //</editor-fold>

        if (refreshDelay <= 10L) {
            hover(vbc);
        } else if (trackLiveHover) {

            if (timerRefresh != null) {
                timerRefresh.cancel();
                timerRefresh.purge();
            }
            if (timerTaskRefresh != null) {
                timerTaskRefresh.cancel();
            }

            timerRefresh = new Timer();

            timerTaskRefresh = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        hover(vbc);
                    });

                }
            };

            timerRefresh.schedule(timerTaskRefresh, refreshDelay);
            // character hover
        }
    }

    private void hover(VisualBioChar vbc) {

        if (vbc == null) {
            if (timerRefresh != null) {
                timerRefresh.cancel();
                timerRefresh.purge();
            }
            if (timerTaskRefresh != null) {
                timerTaskRefresh.cancel();
            }
        }

        if (selectedVBC == null) {
            if (hoverVBC != null && !hoverVBC.equals(selectedVBC)) {

                hoverVBC.tempStyle(null);
                hoverVBC = null;

            }
            if (vbc != null && !vbc.equals(selectedVBC)) {
                hoverVBC = vbc;
                if (trackLiveHover) {
                    hoverVBC.tempStyle(generateFont(Color.GREENYELLOW, Color.BLACK, true, false));
                }
            }
        }

        //column hover
        if (screenRectHover != null) {
            parent.getChildren().remove(screenRectHover);

        }
        if (trackLiveHover && vbc != null) {
            //double correction = vbc.getWidth() * 2; //ERROR correction: double VBC spacer
            double xRect = vbc.getBoundsInParent().getMinX() /*+ correction*/;

            screenRectHover = new Rectangle();
            screenRectHover.setMouseTransparent(true);
            screenRectHover.setFill(Color.ROYALBLUE);
            screenRectHover.setOpacity(GLOBAL_RECTANGLE_OPACITY);
            screenRectHover.setX(xRect);
            screenRectHover.setWidth(vbc.getWidth());
            screenRectHover.setHeight(parent.getHeight());
            parent.getChildren().add(screenRectHover);
        }
    }

    private void singleColumnSelect(double xvalrect, double width) {
        this.removeSelection();
        this.dragRect = new Rectangle();
        this.dragRect.setMouseTransparent(true);
        this.dragRect.setFill(DRAGFILL);
        this.dragRect.setOpacity(GLOBAL_RECTANGLE_OPACITY);
        this.dragRect.setX(xvalrect);
        this.dragRect.setWidth(width);
        this.dragRect.setHeight(this.parent.getHeight());
        this.parent.getChildren().add(this.dragRect);
    }

    public int absoluteVerticalIndex(VisualBioChar vbc) {
        int ret = 0;
        if (vbc.isanno) {
            System.out.println("anno");
            for (int k = 0; k < annotationNumber(); k++) {
                if (this.getAnnotationRow(k).getChildren().contains(vbc)) {
                    return k;
                }
            }
        } else if (vbc.isnumb) {
            System.out.println("numb");
            return this.annotationNumber();
        } else if (vbc.isseq) {
            System.out.println("seq");
            ret += this.annotationNumber() + 1;
            for (int k = 0; k < sequenceNumber(); k++) {
                if (this.getSequence(k).getChildren().contains(vbc)) {
                    return k + ret;
                }
            }
        } else if (vbc.iscons) {
            System.out.println("cons");
            return this.annotationNumber() + this.sequenceNumber() + 1;
        }
        return ret;
    }

    ContextMenu cmLabel;

    private void setLabelBehavior(Label label, boolean isanno, boolean isnumb, boolean isseq, boolean isgroup, boolean iscons) {
        label.setCursor(Cursor.HAND);
        label.setMnemonicParsing(false);
        label.setOnMouseEntered(e -> {
            label.setStyle(this.generateFont(Color.LIGHTBLUE, Color.BLACK, false, false));
            //label.getStyleClass().set(0, "biochar_label_hover0");
        });
        label.setOnMouseExited(e -> {
            label.setStyle(this.generateFont(Color.WHITE, Color.BLACK, false, false));
            //label.getStyleClass().set(0, "biochar_label");
        });
        label.setOnMouseClicked(e -> {
            resetContextMenus();

            cmLabel = new ContextMenu();

            if (isnumb) {
                MenuItem sessionSettings = new MenuItem("Change start index (via settings)");
                sessionSettings.setOnAction(event -> {
                    changeSettings();
                });
                cmLabel.getItems().addAll(sessionSettings);

                Bounds bounds = label.getBoundsInLocal();
                Bounds screenBounds = label.localToScreen(bounds);
                int x = (int) screenBounds.getMinX();
                int y = (int) screenBounds.getMinY();

                cmLabel.show(label.getParent(), e.getScreenX(), screenBounds.getMaxY());
            }

            if (isanno || isseq) {
                MenuItem edit = new MenuItem("Edit name");
                edit.setOnAction(ev -> {
                    Stage st = new Stage();
                    st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                    VBox root = new VBox();
                    st.setTitle("Edit name");

                    HBox tbox = new HBox();
                    Label notif = null;
                    if (isanno) {
                        notif = new Label("Annotation name: ");
                    } else {
                        notif = new Label("Sequence name: ");
                    }

                    TextField tf = new TextField();
                    tf.setText(label.getText());
                    tf.setPromptText("Sequence_".concat(String.valueOf(sequenceNames.getChildren().indexOf(label) + 1)));
                    tf.textProperty().addListener(eve -> {
                        if (tf.getText().contains(" ")) {
                            tf.setText(tf.getText().replaceAll(" ", ""));
                        }
                    });
                    HBox.setHgrow(tf, Priority.ALWAYS);
                    tbox.getChildren().addAll(notif, tf);
                    VBox.setVgrow(tbox, Priority.ALWAYS);
                    tbox.setAlignment(Pos.CENTER);
                    tbox.setPadding(new Insets(15));

                    HBox buttonbox = new HBox();
                    Button ok = new Button("Change name");
                    buttonbox.getChildren().add(ok);
                    root.getChildren().addAll(tbox, buttonbox);

                    buttonbox.setPadding(new Insets(15));
                    ok.setOnAction(eve -> {
                        String tftext = tf.getText();
                        String name = "";
                        if (tftext == null || tftext.isEmpty()) {
                            name = tf.getPromptText();
                        } else {
                            name = tftext;
                            for (Node node : sequenceNames.getChildren()) {
                                Label lb = null;
                                if (node instanceof Label) {
                                    lb = (Label) node;
                                    if (!lb.equals(label) && lb.getText().equals(tftext)) {
                                        name = name.concat("_RS").concat(String.valueOf((int) (Math.random() * 1000000f)));
                                        break;
                                    }
                                }
                            }
                        }
                        label.setText(name);
                        st.close();
                    });

                    Scene sc = new Scene(root, 500, 135, true);
                    st.setScene(sc);
                    st.show();

                    ok.prefWidthProperty().bind(st.widthProperty());
                    ok.requestFocus();
                });

                MenuItem moveup = new MenuItem("Move up");
                moveup.setOnAction(eve -> {
                    if (isseq) {
                        int index = sequenceNames.getChildren().indexOf(label);

                        if (index != 0) {
                            swapRow(index, index - 1, true, false);
                        }
                    } else if (isanno) {
                        int index = annotationLabels.getChildren().indexOf(label);

                        if (index != 0) {
                            swapRow(index, index - 1, false, true);
                        }
                    }
                });

                MenuItem movetop = new MenuItem("Move to top");
                movetop.setOnAction(eve -> {
                    if (isseq) {
                        int index = sequenceNames.getChildren().indexOf(label);

                        sequenceNames.getChildren().get(index).toBack();
                        visualSequences.getChildren().get(index).toBack();
                    } else if (isanno) {
                        int index = annotationLabels.getChildren().indexOf(label);

                        annotationLabels.getChildren().get(index).toBack();
                        annotationRows.getChildren().get(index).toBack();
                    }
                });

                MenuItem movebot = new MenuItem("Move to bottom");
                movebot.setOnAction(eve -> {
                    if (isseq) {
                        int index = sequenceNames.getChildren().indexOf(label);

                        sequenceNames.getChildren().get(index).toFront();
                        visualSequences.getChildren().get(index).toFront();
                    } else if (isanno) {
                        int index = annotationLabels.getChildren().indexOf(label);

                        annotationLabels.getChildren().get(index).toFront();
                        annotationRows.getChildren().get(index).toFront();
                    }
                });

                MenuItem deleteSequence = new MenuItem("Delete sequence");
                if (isanno) {
                    deleteSequence.setText("Delete annotation row");
                }
                deleteSequence.setOnAction(event -> {
                    if (isseq) {
                        deleteSequence(sequenceNames.getChildren().indexOf(label));
                    } else if (isanno) {
                        deleteAnnotationRow(annotationLabels.getChildren().indexOf(label));
                    }
                });

                //ADD FUNCTIONALITY DELETION OF SEQUENCE
                if (isanno) {
                    MenuItem addAnnoRow = new MenuItem("Add new annotation row");
                    addAnnoRow.setOnAction(event -> {
                        addAnnotationRow();
                    });
                    cmLabel.getItems().addAll(edit, new SeparatorMenuItem(),
                            moveup, movetop, movebot,
                            new SeparatorMenuItem(), deleteSequence);
                    cmLabel.getItems().addAll(new SeparatorMenuItem(), addAnnoRow);
                } else if (isseq) {
                    if (!getAlphabet().matrices.isEmpty()) {
                        MenuItem sortByMatrix = new MenuItem("Sort by similarity");
                        sortByMatrix.setOnAction(event -> {
                            sortBySequence_popup(sequenceNames.getChildren().indexOf(label), null);
                        });
                        cmLabel.getItems().addAll(edit, new SeparatorMenuItem(),
                                sortByMatrix, new SeparatorMenuItem(),
                                moveup, movetop, movebot,
                                new SeparatorMenuItem(), deleteSequence);
                    } else {
                        cmLabel.getItems().addAll(edit, new SeparatorMenuItem(),
                                moveup, movetop, movebot,
                                new SeparatorMenuItem(), deleteSequence);
                    }
                }

                Bounds bounds = label.getBoundsInLocal();
                Bounds screenBounds = label.localToScreen(bounds);
                int x = (int) screenBounds.getMinX();
                int y = (int) screenBounds.getMinY();

                cmLabel.show(label.getParent(), e.getScreenX(), screenBounds.getMaxY());

            }

            if (isgroup) {

            }

            if (iscons) {
                MenuItem recalculateConsensus = new MenuItem("Recalculate consensus");
                recalculateConsensus.setOnAction(event -> {
                    calculateConsensus();
                });
                cmLabel.getItems().addAll(recalculateConsensus);

                Bounds bounds = label.getBoundsInLocal();
                Bounds screenBounds = label.localToScreen(bounds);
                int x = (int) screenBounds.getMinX();
                int y = (int) screenBounds.getMinY();

                cmLabel.show(label.getParent(), e.getScreenX(), screenBounds.getMaxY());
            }

        });
    }

    private int annotationNumber = numberAnnotationRows + 1;

    public void addAnnotationRow() {
        HBox annotationRow = new HBox();
        boolean isanno = true;
        boolean isnumb = false;
        boolean isseq = false;
        boolean isgroup = false;
        boolean iscons = false;
        String style = generateFont(DEFAULT_BACKGROUND, DEFAULT_FOREGROUND, false, false);
        for (int k = 0; k < this.sequenceLength(); k++) {
            VisualBioChar vbc = new VisualBioChar(isanno, isnumb, isseq, isgroup, iscons);
            vbc.setCurrentStyle(style, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
            this.setVBCBehavior(vbc);
            annotationRow.getChildren().add(vbc);
            vbc.index = k;
        }
        this.annotationRows.getChildren().add(annotationRow);

        Label temp = new Label("(Annotations_".concat(String.valueOf(annotationNumber++)).concat(")"));
        temp.setStyle(style);
        //temp.getStyleClass().set(0, "biochar_label");            
        this.setLabelBehavior(temp, isanno, isnumb, isseq, isgroup, iscons);
        annotationLabels.getChildren().add(temp);
    }

    public HBox getSequence(int index) {
        return (HBox) visualSequences.getChildren().get(index);
        //return this.annotationRows.get(index + numberAnnotationRows + (numberingActive ? 1 : 0));
    }

    public void swapRow(int i, int k, boolean seq, boolean anno) {
        if (seq) {
            ObservableList<Node> names = FXCollections.observableArrayList(sequenceNames.getChildren());
            Collections.swap(names, i, k);
            sequenceNames.getChildren().setAll(names);
            ObservableList<Node> seqs = FXCollections.observableArrayList(visualSequences.getChildren());
            Collections.swap(seqs, i, k);
            visualSequences.getChildren().setAll(seqs);
        } else if (anno) {
            ObservableList<Node> names = FXCollections.observableArrayList(annotationLabels.getChildren());
            Collections.swap(names, i, k);
            annotationLabels.getChildren().setAll(names);
            ObservableList<Node> seqs = FXCollections.observableArrayList(annotationRows.getChildren());
            Collections.swap(seqs, i, k);
            annotationRows.getChildren().setAll(seqs);
        }
    }

    private String chosenMatrixName = "BLOSUM62";

    public void sortBySequence_popup(int seqIndex, String matrixName) {
        if (this.isCollapsed()) {
            this.confirm_popup("Cannot sort", "You have collapsed sequences in "
                    + "this tab. Sequences cannot be sorted.");
        } else {
            int rootwidth = 450;
            if (matrixName != null && !matrixName.isEmpty()) {
                chosenMatrixName = matrixName;
            }

            Stage performSortStage = new Stage();
            performSortStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            performSortStage.setTitle("Sort sequences");

            VBox performSortRoot = new VBox(5);
            performSortRoot.setPadding(new Insets(15, 15, 0, 15));
            performSortRoot.setAlignment(Pos.CENTER);

            Text performSortLabel = new Text("Continuing will sort the sequences "
                    + "in order of their score relative to " + getSequenceName(seqIndex)
                    + " using the substitution matrix selected below.\n");
            performSortLabel.setTextAlignment(TextAlignment.CENTER);
            performSortLabel.setWrappingWidth(rootwidth);

            HBox optionsBox = new HBox(5);
            HBox matrixComboBoxBox = new HBox();
            ComboBox<Matrix> matrixComboBox = new ComboBox<>();
            matrixComboBoxBox.getChildren().add(matrixComboBox);
            matrixComboBox.getItems().addAll(getAlphabet().matrices);
            matrixComboBox.setValue(matrixComboBox.getItems().get(0));
            for (Matrix m : matrixComboBox.getItems()) {
                if (m.toString().equals(chosenMatrixName)) {
                    matrixComboBox.setValue(m);
                    break;
                }
            }
            matrixComboBox.valueProperty().addListener(e -> {
                chosenMatrixName = matrixComboBox.toString();
            });

            matrixComboBox.setMinWidth(100);
            matrixComboBoxBox.setPadding(new Insets(0, 30, 0, 0));

            RadioButton descend = new RadioButton("Descending order");
            descend.setSelected(true);

            RadioButton ascend = new RadioButton("Ascending order");
            ascend.setSelected(false);

            ToggleGroup tgascend = new ToggleGroup();
            tgascend.getToggles().addAll(ascend, descend);
            optionsBox.setAlignment(Pos.CENTER);
            optionsBox.getChildren().addAll(matrixComboBoxBox, descend, ascend);

            Button yes = new Button("Continue");
            yes.setOnAction(e -> {
                sortBySequence(seqIndex, matrixComboBox.getValue(), descend.isSelected());
                performSortStage.close();
            });
            Button no = new Button("Cancel");
            no.setOnAction(e -> {
                performSortStage.close();
            });

            HBox buttons = new HBox(8);
            buttons.getChildren().addAll(no, yes);

            buttons.setPadding(new Insets(15));
            performSortStage.widthProperty().addListener(o -> {
                for (int i = 0; i < buttons.getChildren().size(); i++) {
                    if (buttons.getChildren().get(i) instanceof Button) {
                        ((Button) buttons.getChildren().get(i)).setPrefWidth(performSortStage.getWidth() / buttons.getChildren().size());
                    }
                }
            });

            performSortRoot.getChildren().addAll(performSortLabel, optionsBox, buttons);

            performSortRoot.setMaxWidth(rootwidth);

            Scene performSortScene = new Scene(performSortRoot);

            performSortStage.setScene(performSortScene);
            performSortStage.showAndWait();
            performSortRoot.setStyle("max-height: none;");
        }
    }

    public void sortBySequence(int seqIndex, Matrix m, boolean descend) {
        List<IndexScoreList> indexScoreList = new ArrayList<>();
        for (int k = 0; k < this.sequenceNumber(); k++) {
            indexScoreList.add(new IndexScoreList(k, 0));
        }
        //indexScoreList.remove(seqIndex);
        System.out.println("Master: " + getSequenceName(seqIndex));

        for (IndexScoreList irpair : indexScoreList) {
            int indexSubject = irpair.getIndex();
            double score = 0.0;
            for (int k = 0; k < sequenceLength(); k++) {
                score += m.getOrMin(getVBC(seqIndex, k).getChar(), getVBC(indexSubject, k).getChar());
            }
            irpair.setScore(score);
        }
        Collections.sort(indexScoreList, Comparator.comparing(IndexScoreList::getScore));
        if (descend) {
            Collections.reverse(indexScoreList);
        }
        HashMap<Integer, String> sortOrderByName = new HashMap<>();
        for (IndexScoreList isl : indexScoreList) {
            sortOrderByName.put(isl.getIndex(), getSequenceName(isl.getIndex()));
        }
        for (IndexScoreList isl : indexScoreList) {
            //System.out.println(getSequenceName(isl.getIndex()) + " " + isl.getScore());
            int index = getIndexOfSequence(sortOrderByName.get(isl.getIndex()));
            sequenceNames.getChildren().get(index).toFront();
            visualSequences.getChildren().get(index).toFront();
        }

    }

    public void moveSeqToTop(int k) {
        sequenceNames.getChildren().get(k).toBack();
        visualSequences.getChildren().get(k).toBack();
    }

    public VisualBioChar crawl(VisualBioChar location, KeyCode movement) {
        VisualBioChar vbcRet = location;
        switch (movement) {
            case LEFT:
                System.out.println("left");
                if (location.getParent().getChildrenUnmodifiable().indexOf(location) == 0) {
                    vbcRet = location;
                } else {
                    vbcRet = (VisualBioChar) location.getParent().getChildrenUnmodifiable().get(location.getParent().getChildrenUnmodifiable().indexOf(location) - 1);
                }
                break;
            case RIGHT:
                System.out.println("right");
                if (location.getParent().getChildrenUnmodifiable().indexOf(location) == location.getParent().getChildrenUnmodifiable().size() - 1) {
                    vbcRet = location;
                } else {
                    vbcRet = (VisualBioChar) location.getParent().getChildrenUnmodifiable().get(location.getParent().getChildrenUnmodifiable().indexOf(location) + 1);
                }
                break;
            case UP:
                System.out.println("up");
                if (location.isseq) {
                    if (((HBox) visualSequences.getChildren().get(0)).getChildren().contains(location)) {
                        vbcRet = (VisualBioChar) ((HBox) annotationRows.getChildren().get(annotationRows.getChildren().size() - 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    } else {
                        vbcRet = (VisualBioChar) ((HBox) location.getParent().getParent().getChildrenUnmodifiable().get(location.getParent().getParent().getChildrenUnmodifiable().indexOf(location.getParent()) - 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    }
                } else if (location.isanno) {
                    if (((HBox) annotationRows.getChildren().get(0)).getChildren().contains(location)) {
                        vbcRet = location;
                    } else {
                        vbcRet = (VisualBioChar) ((HBox) location.getParent().getParent().getChildrenUnmodifiable().get(location.getParent().getParent().getChildrenUnmodifiable().indexOf(location.getParent()) - 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    }
                } else if (location.iscons) {
                    vbcRet = (VisualBioChar) ((HBox) visualSequences.getChildren().get(visualSequences.getChildren().size() - 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                }
                break;
            case DOWN:
                System.out.println("down");
                if (location.isseq) {
                    if (((HBox) visualSequences.getChildren().get(visualSequences.getChildren().size() - 1)).getChildren().contains(location)) {
                        //vbcRet = location;
                        //enter consensus
                        vbcRet = (VisualBioChar) consensusRow.getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    } else {
                        vbcRet = (VisualBioChar) ((HBox) visualSequences.getChildren().get(visualSequences.getChildren().indexOf(location.getParent()) + 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    }
                } else if (location.isanno) {
                    if (((HBox) annotationRows.getChildren().get(annotationRows.getChildren().size() - 1)).getChildren().contains(location)) {
                        vbcRet = (VisualBioChar) ((HBox) visualSequences.getChildren().get(0)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    } else {
                        vbcRet = (VisualBioChar) ((HBox) annotationRows.getChildren().get(annotationRows.getChildren().indexOf(location.getParent()) + 1)).getChildren().get(location.getParent().getChildrenUnmodifiable().indexOf(location));
                    }
                } else if (location.iscons) {
                    vbcRet = location;
                }
                break;
            default:
                break;
        }
        return vbcRet;
    }

    public VisualBioChar getVBC(int seqIndex, int vbcIndex) {
        HBox seq = this.getSequence(seqIndex);
        VisualBioChar vbc = (VisualBioChar) seq.getChildren().get(vbcIndex);
        return vbc;
    }

    public VisualBioChar getAnnoVBC(int annoIndex, int vbcIndex) {
        HBox anno = this.getAnnotationRow(annoIndex);
        VisualBioChar vbc = (VisualBioChar) anno.getChildren().get(vbcIndex);
        return vbc;
    }

    public VisualBioChar getConsensusVBC(int vbcIndex) {
        return ((VisualBioChar) this.consensusRow.getChildren().get(vbcIndex));
    }

    public VBox getRows() {
        return right;
    }

    public VBox getNames() {
        return left;
    }

    public int sequenceNumber() {
        return this.visualSequences.getChildren().size();
    }

    public int sequenceLength() {
        return ((HBox) this.visualSequences.getChildren().get(0)).getChildren().size();
    }

    public VBox getSequencesBox() {
        return visualSequences;
    }

    public VBox getSequenceNamesBox() {
        return sequenceNames;
    }

    public VBox getAnnotationsBox() {
        return this.annotationRows;
    }

    public VBox getAnnotationNamesBox() {
        return this.annotationLabels;
    }

    public SequenceHolder getSequenceHolder() {
        this.updatedSequenceHolder(firstIndex);
        return this.sequenceHolder;
    }

    public Alphabet getAlphabet() {
        return this.sequenceHolder.getAlphabet();
    }

    public int getIndexOfSequence(String value) {
        int ret = -1;
        for (int k = 0; k < this.getSequenceNamesBox().getChildren().size(); k++) {
            if ((((Label) this.getSequenceNamesBox().getChildren().get(k)).getText()).equals(value)) {
                return k;
            }
        }
        return ret;
    }

    public String getSequenceName(int index) {
        return ((Label) this.sequenceNames.getChildren().get(index)).getText();
    }

    public HBox getAnnotationRow(int index) {
        return (HBox) this.annotationRows.getChildren().get(index);
    }

    public HBox getAnnotationRow(String name) {
        for (int k = 0; k < this.annotationLabels.getChildren().size(); k++) {
            if (name.equals(((Label) this.annotationLabels.getChildren().get(k)).getText())) {
                return this.getAnnotationRow(k);
            }
        }
        return null; //if no sequenceStr row matches the name given
    }

    public String getAnnotationName(int index) {
        return ((Label) this.annotationLabels.getChildren().get(index)).getText();
    }

    public int annotationNumber() {
        return this.annotationLabels.getChildren().size();
    }

    public HBox getConsensusRow() {
        return consensusRow;
    }

    /**
     * Clears all character cells (<tt>VisualBioChar</tt>s) of any shading or
     * coloring. Can be accessed publicly by remote controls to clear this
     * <tt>VisualMultipleSequenceALignment</tt> of prior styling.
     */
    public void clearShading() {
        String style = generateFont(DEFAULT_BACKGROUND, DEFAULT_FOREGROUND, false, false);
        for (int i = 0; i < this.sequenceNumber(); i++) {
            for (int k = 0; k < this.sequenceLength(); k++) {
                this.getVBC(i, k).setCurrentStyle(style, DEFAULT_BACKGROUND, DEFAULT_FOREGROUND);
            }
        }
    }

    public boolean clearShading_popup() {
        return origin.clearShading_popup();
    }

    public final int DEFAULT_FONTSIZE = 18;
    public final String DEFAULT_BACKGROUND = "#FFFFFF";
    public final String DEFAULT_FOREGROUND = "#000000";
    private final String DEFAULT_STYLE = "\n"
            + "    -fx-font-family: \"unifont\";\n"
            + "    -fx-font-size: " + String.valueOf(DEFAULT_FONTSIZE) + ";\n"
            + "    -fx-background-color:" + DEFAULT_BACKGROUND + ";\n"
            + "    -fx-text-fill: " + DEFAULT_FOREGROUND + ";\n"
            + "    -fx-underline: false ;\n"
            + "";

    private int fontSize = DEFAULT_FONTSIZE;

    public void updateStyleSettings(int fontSize, double luminance) {
        // all labels
        this.fontSize = fontSize;
        this.luminanceThreshold = luminance;

        for (int k = 0; k < annotationRows.getChildren().size(); k++) {
            Label annolb = ((Label) annotationLabels.getChildren().get(k));
            annolb.setStyle(generateFont("#FFFFFF", "#000000", false, false));
            HBox annorow = (HBox) annotationRows.getChildren().get(k);
            for (int j = 0; j < annorow.getChildren().size(); j++) {
                VisualBioChar annovbc = (VisualBioChar) annorow.getChildren().get(j);
                String backHex = annovbc.getBackHex();
                String foreHex = decideForeGroundColor(backHex);
                annovbc.setCurrentStyle(generateFont(backHex, foreHex, false, false),
                        backHex, foreHex);
            }
        }

        for (int k = 0; k < sequenceNames.getChildren().size(); k++) {
            Label seqlb = ((Label) sequenceNames.getChildren().get(k));
            seqlb.setStyle(generateFont("#FFFFFF", "#000000", false, false));
            HBox visseq = (HBox) visualSequences.getChildren().get(k);
            for (int j = 0; j < visseq.getChildren().size(); j++) {
                VisualBioChar currentvbc = (VisualBioChar) visseq.getChildren().get(j);
                String backHex = currentvbc.getBackHex();
                String foreHex = decideForeGroundColor(backHex);
                currentvbc.setCurrentStyle(generateFont(backHex, foreHex, false, false),
                        backHex, foreHex);
            }
        }

        //NUMBERING.setStyle("-fx-font-size:" + String.valueOf(fontSize) + ";");
        NUMBERING.setStyle(generateFont("#FFFFFF", "#000000", false, false));
        for (int j = 0; j < numberingRow.getChildren().size(); j++) {
            VisualBioChar currentvbc = (VisualBioChar) numberingRow.getChildren().get(j);
            String backHex = currentvbc.getBackHex();
            String foreHex = decideForeGroundColor(backHex);
            currentvbc.setCurrentStyle(generateFont(backHex, foreHex, false, false),
                    backHex, foreHex);
        }

        CONSENSUS.setStyle(generateFont("#FFFFFF", "#000000", false, false));
        for (int j = 0; j < consensusRow.getChildren().size(); j++) {
            VisualBioChar currentvbc = (VisualBioChar) consensusRow.getChildren().get(j);
            String backHex = currentvbc.getBackHex();
            String foreHex = decideForeGroundColor(backHex);
            currentvbc.setCurrentStyle(generateFont(backHex, foreHex, false, false),
                    backHex, foreHex);
        }

    }

    private String changeFontToAccommodateSize(String style, int size) {
        String[] components = style.split(";");
        for (int i = 0; i < components.length; i++) {
            if (components[i].contains("-fx-font-size")) {
                components[i] = "  -fx-font-size:" + String.valueOf(size);
            }
        }
        StringBuilder newStyle = new StringBuilder();
        for (String s : components) {
            newStyle.append(s).append(";\n");
        }
        return newStyle.toString();
    }

    public void changeSettings() {
        Stage settingsStage = new Stage();
        settingsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        settingsStage.setTitle("Settings for MSA");

        HBox firstIndexBox = new HBox(5);
        Label firstIndexLabel = new Label("First index");
        TextField firstIndexTextField = new TextField(String.valueOf(this.getFirstIndex()));
        firstIndexBox.getChildren().addAll(firstIndexTextField, firstIndexLabel);
        firstIndexTextField.setPromptText(String.valueOf(fontSize));
        firstIndexTextField.setPromptText(String.valueOf(this.getFirstIndex()).concat(" (Current Index)"));
        firstIndexTextField.textProperty().addListener(event -> {
            if ((!firstIndexTextField.getText().isEmpty() && !isInteger(firstIndexTextField.getText())) || firstIndexTextField.getText().length() > 5) {
                firstIndexTextField.setText(firstIndexTextField.getText().substring(0, firstIndexTextField.getLength() - 1));
            }
        });

        HBox fontSizeBox = new HBox(5);
        Label fontSizeLabel = new Label("Font size (px)");
        TextField fontSizeTextField = new TextField(String.valueOf(fontSize));
        fontSizeBox.getChildren().addAll(fontSizeTextField, fontSizeLabel);
        fontSizeTextField.setPromptText(String.valueOf(fontSize));
        fontSizeTextField.textProperty().addListener(e -> {
            if (!fontSizeTextField.getText().isEmpty()
                    && (!isInteger(fontSizeTextField.getText())
                    || Integer.parseInt(fontSizeTextField.getText()) > 120)) {
                fontSizeTextField.setText(fontSizeTextField.getText().substring(0, fontSizeTextField.getText().length() - 1));
            }
        });

        HBox luminanceBox = new HBox(5);
        Label luminanceLabel = new Label("Luminance threshold");
        TextField luminanceTextField = new TextField(String.valueOf(luminanceThreshold));
        luminanceBox.getChildren().addAll(luminanceTextField, luminanceLabel);
        luminanceTextField.setPromptText(String.valueOf(luminanceThreshold));
        luminanceTextField.textProperty().addListener(e -> {
            if (!luminanceTextField.getText().isEmpty()
                    && (!isDouble(luminanceTextField.getText())
                    || Double.parseDouble(luminanceTextField.getText()) > 99.999999
                    || Double.parseDouble(luminanceTextField.getText()) < 0)) {
                luminanceTextField.setText(luminanceTextField.getText().substring(0, luminanceTextField.getText().length() - 1));
            }
        });

        VBox repaintSettingsVBox = new VBox(10);
        CheckBox trackLivePosition = new CheckBox("Show selection on hover?");
        trackLivePosition.setSelected(trackLiveHover);
        HBox repaintSettingsBox = new HBox(5);
        Label repaintDelayLabel = new Label("Hover refresh delay (ms)");
        TextField repaintDelayTextField = new TextField(String.valueOf(refreshDelay));
        repaintSettingsBox.getChildren().addAll(repaintDelayTextField, repaintDelayLabel);
        repaintDelayTextField.setPromptText(String.valueOf(refreshDelay));
        repaintDelayLabel.setDisable(!trackLiveHover);
        repaintDelayTextField.setDisable(!trackLiveHover);
        trackLivePosition.selectedProperty().addListener(e -> {
            trackLiveHover = trackLivePosition.isSelected();
            repaintDelayLabel.setDisable(!trackLiveHover);
            repaintDelayTextField.setDisable(!trackLiveHover);
        });
        repaintDelayTextField.textProperty().addListener(e -> {
            if (!repaintDelayTextField.getText().isEmpty()
                    && (!isInteger(repaintDelayTextField.getText())
                    || Integer.parseInt(repaintDelayTextField.getText()) > 2000
                    || Integer.parseInt(repaintDelayTextField.getText()) < 0)) {
                repaintDelayTextField.setText(repaintDelayTextField.getText().substring(0, repaintDelayTextField.getText().length() - 1));
            }
        });

        repaintSettingsVBox.getChildren().addAll(trackLivePosition, repaintSettingsBox);
        repaintSettingsVBox.setPadding(new Insets(5, 0, 0, 0));

        HBox changeSettingsButtonBox = new HBox();
        HBox.setHgrow(changeSettingsButtonBox, Priority.ALWAYS);
        Button changeSettings = new Button("Set values");
        changeSettings.setMinWidth(165);
        changeSettings.setOnAction(e -> {
            luminanceThreshold = Double.parseDouble(luminanceTextField.getText());

            updateStyleSettings(Integer.parseInt(fontSizeTextField.getText()), Double.parseDouble(luminanceTextField.getText()));

            if (firstIndexTextField.getText().isEmpty()) {
                this.setFirstIndex(this.getFirstIndex());
            } else {
                this.setFirstIndex(Integer.parseInt(firstIndexTextField.getText()));
                this.numbering(this.numberingSpacing, true);
            }

            trackLiveHover = trackLivePosition.isSelected();
            if (!repaintDelayTextField.getText().isEmpty()) {
                refreshDelay = Integer.parseInt(repaintDelayTextField.getText());
            }

            settingsStage.close();
        });
        changeSettingsButtonBox.getChildren().add(changeSettings);
        changeSettingsButtonBox.setAlignment(Pos.CENTER);
        changeSettingsButtonBox.setPadding(new Insets(5, 0, 0, 0));

        VBox settingsRoot = new VBox(10);
        settingsRoot.setPadding(new Insets(10));
        settingsRoot.getChildren().addAll(firstIndexBox, fontSizeBox, luminanceBox,
                repaintSettingsVBox, changeSettingsButtonBox);

        Scene settingsScene = new Scene(settingsRoot);
        settingsStage.setScene(settingsScene);
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setAlwaysOnTop(true);
        settingsStage.showAndWait();
    }

    public boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public String generateFont(Color back) {
        return generateFont(back, false, false);
    }

    public String generateFont(String backHex) {
        return generateFont(backHex, false, false);
    }

    public String generateFont(Color back, boolean underline, boolean bold) {
        //need to decide of foreground is white or black
        System.out.println(back.getRed() + " " + back.getGreen() + " " + back.getBlue());
        return generateFont(hexifyColorFX(back),
                decideForegroundColor(new double[]{back.getRed(), back.getGreen(), back.getBlue()}),
                underline, bold);
    }

    public String generateFont(String backHex, boolean underline, boolean bold) {
        return generateFont(backHex, decideForegroundColor(hexToRGB(backHex)), underline, bold);
    }

    private double luminanceThreshold = 0.179;

    public String decideForeGroundColor(String backHex) {
        return decideForegroundColor(hexToRGB(backHex));
    }

    public String decideForegroundColor(double[] rgb) {
        //https://codepen.io/znak/pen/aOvMOd
        double[] rgbc = rgb;
        if (rgb[0] > 1d || rgb[1] > 1d || rgb[2] > 1) {
            rgbc = new double[]{rgb[0] / 255d, rgb[1] / 255d, rgb[2] / 255d};
        }
        for (int i = 0; i < rgb.length; i++) {
            if (rgbc[i] < 0.03928) {
                rgbc[i] = rgbc[i] / 12.92;
            } else {
                rgbc[i] = Math.pow((rgbc[i] + 0.055) / 1.055, 2.4);
            }
        }
        //luminance calculation
        double L = 0.2126 * rgbc[0] + 0.7152 * rgbc[1] + 0.0722 * rgbc[2];
        //System.err.println("luminance " + L);
        //black if luminance exceeds threshold, white if does not
        return (L > luminanceThreshold ? "#000000" : "#FFFFFF");
    }

    public String generateFont(Color back, Color fore, boolean underline, boolean bold) {
        return generateFont(hexifyColorFX(back), hexifyColorFX(fore), underline, bold);
    }

    public String generateFont(String backColor, String foreColor, boolean underline, boolean bold) {
        StringBuilder str = new StringBuilder("");
        String backgroundHex = DEFAULT_BACKGROUND;
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        if (backColor != null) {
            backgroundHex = backColor;
        }
        String foregroundHex = DEFAULT_FOREGROUND;
        if (foreColor != null) {
            foregroundHex = foreColor;
        }
        if (fontSize == -1) {
            fontSize = DEFAULT_FONTSIZE;
        }

        str.append("    -fx-font-family: \"unifont\";\n"); //everything unifont - always
        str.append("    -fx-font-size: ").append(String.valueOf(fontSize)).append(";\n");
        str.append("    -fx-background-color: ").append(backgroundHex).append(" ;\n");
        str.append("    -fx-text-fill: ").append(foregroundHex).append(" ;\n");
        str.append("    -fx-underline: ").append(underline ? "true" : "false").append(" ;\n");
        str.append("    -fx-font-weight: ").append(bold ? "bold" : "normal").append(" ;\n");

        return str.toString();
    }

    public String hexifyColorFX(Color c) {
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255f),
                (int) (c.getGreen() * 255f),
                (int) (c.getBlue() * 255f));
    }

    //sample input: "#FFFFFF"   (white)
    public double[] hexToRGB(String hex) {
        /*        System.out.println(Integer.valueOf(hex.substring(1, 3), 16) + " " +
            Integer.valueOf(hex.substring(3, 5), 16) + " " +
            Integer.valueOf(hex.substring(5, 7), 16));*/
        return new double[]{((double) Integer.valueOf(hex.substring(1, 3), 16)) / 255d,
            ((double) Integer.valueOf(hex.substring(3, 5), 16)) / 255d,
            ((double) Integer.valueOf(hex.substring(5, 7), 16)) / 255d};
    }

    public boolean mwpRetboolIgnore = false;
    private boolean mwpRetboolAction = false;

    public boolean[] modificationWarningPopup(String warningText) {
        //structure [ignore boolean] [action boolean]
        boolean[] ret = new boolean[]{false, false};
        mwpRetboolIgnore = false;
        mwpRetboolAction = false;

        if (warningText == null || warningText.isEmpty()) {
            warningText = "The action you are about to perform may change "
                    + "the information content of the alignment. You may "
                    + "need to realign your sequences.";
        }

        Stage mwpStage = new Stage();
        mwpStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        mwpStage.setTitle("Alignment edit warning");

        Text graphicWarningText = new Text(warningText);
        graphicWarningText.setWrappingWidth(550);
        graphicWarningText.setTextAlignment(TextAlignment.CENTER);
        CheckBox dontShowAgain = new CheckBox("Don't show warnings like this again");
        Button continueAction = new Button("Continue");

        continueAction.setOnAction(e -> {
            if (dontShowAgain.isSelected()) {
                mwpRetboolIgnore = true;
            }

            mwpRetboolAction = true;
            mwpStage.close();
        });

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(10, 10, 10, 10));
        rootBox.setAlignment(Pos.CENTER);
        rootBox.getChildren().addAll(graphicWarningText, dontShowAgain, continueAction);

        Scene mwpScene = new Scene(rootBox);
        mwpStage.setScene(mwpScene);
        mwpStage.initModality(Modality.APPLICATION_MODAL);
        mwpStage.setAlwaysOnTop(true);
        mwpStage.setMaxWidth(600);
        mwpStage.showAndWait();

        ret[0] = mwpRetboolIgnore;
        ret[1] = mwpRetboolAction;
        return ret;
    }

    public DatabaseSearcher_ProSiteDB_Mar2020 dbs = null;

    public String getDefaultFont() {
        return this.DEFAULT_STYLE;
    }

    public void setFirstIndex(int k) {
        this.firstIndex = k;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public EditorTab getEditorTab() {
        return this.origin;
    }

    public int fontSize() {
        return fontSize;
    }

    public double luminanceThreshold() {
        return luminanceThreshold;
    }

    private boolean ignoreColumnDeletionWarning = false;
    private boolean continueWithColumnDeletion = false;

    private void deleteColumns(int start, int end, boolean warn) {
        if (!ignoreColumnDeletionWarning && warn) {
            String warning = "You are about to delete columns from "
                    + "your alignment, which may change the information content."
                    + " NOTE: This cannot be undone.";
            if (start == end) {
                warning = "You are about to delete a column from "
                        + "your alignment, which may change the information "
                        + "content. NOTE: This cannot be undone.";
            }
            boolean[] userInput = this.modificationWarningPopup(warning);
            ignoreColumnDeletionWarning = userInput[0];
            continueWithColumnDeletion = userInput[1];
        }
        this.removeWorkingColumnSelection();
        if (continueWithColumnDeletion || !warn) {
            System.out.println("deleting");
            for (int k = end; k >= start; k--) {
                for (Node n : annotationRows.getChildren()) {
                    ((HBox) n).getChildren().remove(k);
                }
                for (Node n : visualSequences.getChildren()) {
                    ((HBox) n).getChildren().remove(k);
                }
                numberingRow.getChildren().remove(k);
                consensusRow.getChildren().remove(k);
            }
            numbering(numberingSpacing, true);
        }
    }

    private boolean ignoreSequenceDeletionWarning = false;
    private boolean continueWithSequenceDeletion = false;

    private void deleteSequence(int index) {
        if (!ignoreSequenceDeletionWarning) {
            String warning = "You are about to delete an entire sequence from "
                    + "your alignment, which may change the information content."
                    + " NOTE: This cannot be undone.";

            boolean[] userInput = this.modificationWarningPopup(warning);
            ignoreSequenceDeletionWarning = userInput[0];
            continueWithSequenceDeletion = userInput[1];
        }
        if (continueWithSequenceDeletion) {
            sequenceNames.getChildren().remove(index);
            visualSequences.getChildren().remove(index);
        }
        //do all gap column checks
        checkForAllGapColumns(0, sequenceLength() - 1, false);
    }

    protected void checkForAllGapColumns(int start, int end, boolean notifyIfOK) {
        List<Integer> allGapIndices = new ArrayList<>();
        for (int k = start; k <= end; k++) {
            boolean isAllGaps = true;
            column_check_loop:
            for (int j = 0; j < sequenceNumber(); j++) {
                if (getVBC(j, k).getChar() != '-') {
                    isAllGaps = false;
                    break column_check_loop;
                }
            }
            if (isAllGaps) {
                allGapIndices.add(k);
            }
        }
        if (allGapIndices.size() > 0) {
            //System.out.println("columsn with all gaps exist!");
            Stage allGapsStage = new Stage();
            allGapsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            allGapsStage.setTitle("Clean up");
            Text graphicWarningText = new Text("Your your multiple sequence alignment has "
                    + "columns with only gap characters. Would you like to delete "
                    + "these columns now?");
            if (allGapIndices.size() == 1) {
                graphicWarningText = new Text("Your your multiple sequence alignment has a "
                        + "column with only gap characters. Would you like to delete "
                        + "this column now?");
            }
            graphicWarningText.setWrappingWidth(550);
            graphicWarningText.setTextAlignment(TextAlignment.CENTER);
            Button no = new Button("Cancel");
            no.setMinWidth(150);
            no.setOnAction(e -> {
                allGapsStage.close();
            });
            Button yes = new Button("Continue");
            yes.setMinWidth(150);
            yes.setOnAction(e -> {
                Collections.reverse(allGapIndices);
                for (int j : allGapIndices) {
                    deleteColumns(j, j, false);
                }
                allGapsStage.close();
                confirm_popup("Gap-only columns removed",
                        "All columns in your alignment now "
                        + "have at least one non-gap character.");
            });
            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(buttonBox, Priority.ALWAYS);
            buttonBox.getChildren().addAll(no, yes);

            VBox rootBox = new VBox(15);
            rootBox.setPadding(new Insets(10, 10, 10, 10));
            rootBox.setAlignment(Pos.CENTER);
            rootBox.getChildren().addAll(graphicWarningText, buttonBox);

            Scene mwpScene = new Scene(rootBox);
            allGapsStage.setScene(mwpScene);
            allGapsStage.initModality(Modality.APPLICATION_MODAL);
            allGapsStage.setAlwaysOnTop(true);
            allGapsStage.setMaxWidth(600);
            allGapsStage.show();
            yes.requestFocus();
        } else if (notifyIfOK) {
            //System.out.println("columsn with all gaps exist!");
            Stage allGapsStage = new Stage();
            allGapsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            allGapsStage.setTitle("Clean up");
            Text graphicWarningText = new Text("All columns in your alignment "
                    + "have at least one non-gap character.");
            graphicWarningText.setWrappingWidth(550);
            graphicWarningText.setTextAlignment(TextAlignment.CENTER);
            Button no = new Button("Confirm");
            no.setMinWidth(150);
            no.setOnAction(e -> {
                allGapsStage.close();
            });

            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(buttonBox, Priority.ALWAYS);
            buttonBox.getChildren().addAll(no);

            VBox rootBox = new VBox(15);
            rootBox.setPadding(new Insets(10, 10, 10, 10));
            rootBox.setAlignment(Pos.CENTER);
            rootBox.getChildren().addAll(graphicWarningText, buttonBox);

            Scene mwpScene = new Scene(rootBox);
            allGapsStage.setScene(mwpScene);
            allGapsStage.initModality(Modality.APPLICATION_MODAL);
            allGapsStage.setAlwaysOnTop(true);
            allGapsStage.setMaxWidth(600);
            allGapsStage.show();
            no.requestFocus();
        }
    }

    public void deleteAllColumnsWithGaps() {
        Stage allGapsStage = new Stage();
        allGapsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        allGapsStage.setTitle("Delete columns with gaps");
        Text graphicWarningText = new Text("This will delete all columns "
                + "in this alignment that have a gap character, and this action "
                + "cannot be "
                + "reversed. This option is useful "
                + "for creating input files for some "
                + "phylogenetic inference programs, which do not use or "
                + "disregard columns with gap characters. "
                + "Would you like to continue?\n");

        graphicWarningText.setWrappingWidth(550);
        graphicWarningText.setTextAlignment(TextAlignment.CENTER);
        Button no = new Button("Cancel");
        no.setMinWidth(150);
        no.setOnAction(e -> {
            allGapsStage.close();
        });
        Button yes = new Button("Continue");
        yes.setMinWidth(150);
        yes.setOnAction(e -> {
            GeneralLoadingManager glm_del = new GeneralLoadingManager("Deleting columns with gaps...");
            Platform.runLater(() -> {
                glm_del.showLoading(true);
                List<Integer> allGapIndices = new ArrayList<>();
                for (int k = 0; k < this.sequenceLength(); k++) {
                    boolean hasGaps = false;
                    column_check_loop:
                    for (int j = 0; j < sequenceNumber(); j++) {
                        if (getVBC(j, k).getChar() == '-') {
                            hasGaps = true;
                            break column_check_loop;
                        }
                    }
                    if (hasGaps) {
                        allGapIndices.add(k);
                    }
                }
                Collections.reverse(allGapIndices);
                for (int j : allGapIndices) {
                    deleteColumns(j, j, false);
                }
                allGapsStage.close();
                Platform.runLater(() -> {
                    //System.out.println("");
                    glm_del.exitLoading();
                    confirm_popup("Gap-only columns removed",
                            "All columns in your alignment now "
                            + "have at least one non-gap character.");
                });
            });

        });
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        buttonBox.getChildren().addAll(no, yes);

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(10, 10, 10, 10));
        rootBox.setAlignment(Pos.CENTER);
        rootBox.getChildren().addAll(graphicWarningText, buttonBox);

        Scene mwpScene = new Scene(rootBox);
        allGapsStage.setScene(mwpScene);
        allGapsStage.initModality(Modality.APPLICATION_MODAL);
        //allGapsStage.setAlwaysOnTop(true);
        allGapsStage.setMaxWidth(600);
        allGapsStage.show();
        yes.requestFocus();
    }

    private boolean ignoreAnnotationDeletionWarning = false;
    private boolean continueWithAnnotationDeletion = false;

    private void deleteAnnotationRow(int index) {
        if (!ignoreAnnotationDeletionWarning) {
            String warning = "You are about to delete an entire annotation row from "
                    + "your alignment."
                    + " NOTE: This cannot be undone.";

            boolean[] userInput = this.modificationWarningPopup(warning);
            ignoreAnnotationDeletionWarning = userInput[0];
            continueWithAnnotationDeletion = userInput[1];
        }
        if (continueWithAnnotationDeletion) {
            annotationLabels.getChildren().remove(index);
            annotationRows.getChildren().remove(index);
        }
    }

    public ObservableList<Color> getUserCustomColors() {
        return this.getEditorTab().THE_PROGRAM.recentColors;
    }

    private void addUserCustomColor(Color c) {
        if (!this.getEditorTab().THE_PROGRAM.recentColors.contains(c)) {
            this.getEditorTab().THE_PROGRAM.recentColors.add(c);
        }

    }

    Timer timer = new Timer();
    TimerTask task = null;
    long delayTime = 150;

    public void bindColorPickers(List<ColorPicker> lcp) {
        for (ColorPicker cp : lcp) {
            cp.valueProperty().addListener(e -> {
                if (task != null) {
                    task.cancel();
                    timer = new Timer();
                }

                task = new TimerTask() {
                    @Override
                    public void run() {
                        addUserCustomColor(cp.getValue());
                        //System.err.println("tried it");
                    }
                };

                timer.schedule(task, delayTime);
            });
            this.getUserCustomColors().addListener((Observable e) -> {
                Platform.runLater(() -> {
                    cp.getCustomColors().setAll(getUserCustomColors());
                });
            });
        }
    }

    private void confirm_popup(String title, String message) {
        Stage allGapsStage2 = new Stage();
        allGapsStage2.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        allGapsStage2.setTitle(title);
        Text graphicWarningText2 = new Text(message);
        graphicWarningText2.setWrappingWidth(550);
        graphicWarningText2.setTextAlignment(TextAlignment.CENTER);
        Button no2 = new Button("Confirm");
        no2.setMinWidth(150);
        no2.setOnAction(eve -> {
            allGapsStage2.close();
        });

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        buttonBox.getChildren().addAll(no2);

        VBox rootBox = new VBox(15);
        rootBox.setPadding(new Insets(10, 10, 10, 10));
        rootBox.setAlignment(Pos.CENTER);
        rootBox.getChildren().addAll(graphicWarningText2, buttonBox);

        Scene mwpScene = new Scene(rootBox);
        allGapsStage2.setScene(mwpScene);
        allGapsStage2.initModality(Modality.APPLICATION_MODAL);
        allGapsStage2.setAlwaysOnTop(true);
        allGapsStage2.setMaxWidth(600);
        allGapsStage2.show();
        no2.requestFocus();
    }

}

class IndexScoreList extends ArrayList<Number> {

    public IndexScoreList(int selfIndex, double score) {
        super(2);
        add(selfIndex);
        add(score);
    }

    public int getIndex() {
        return (int) get(0);
    }

    public void setIndex(int k) {
        set(0, k);
    }

    public double getScore() {
        return (double) get(1);
    }

    public void setScore(double d) {
        set(1, d);
    }
}
