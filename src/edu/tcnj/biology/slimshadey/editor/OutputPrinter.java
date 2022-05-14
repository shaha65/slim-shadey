/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import edu.tcnj.biology.seqverter.graphics.GeneralLoadingManager;
import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class OutputPrinter {

    private EditorTab parent;
    private VisualMultipleSequenceAlignment vmsa;

    public OutputPrinter(EditorTab ei) {
        this.parent = ei;
        this.vmsa = this.parent.getVMSA();
    }

    private final int DEFAULT_NUMBERING_SPACING = 5;
    private final int DEFAULT_COLUMNS_PER_ROW = 60;
    private final int DEFAULT_MAX_NAMES_CHARS = 30;
    private final int DEFAULT_OUTPUT_FONTSIZE = 18;

    private final boolean SHOW_ANNOTATIONS_NAMES = true;
    private final boolean SHOW_DEFAULT_ANNOTATIONS_NAMES = false;
    private final boolean OMIT_EMPTY_SUBSECTIONS = true;

    private final int SETTINGS_SPACER_INTERVAL_PX = 35;

    public void makeOutput() {
        Stage outputSettingsStage = new Stage();
        outputSettingsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        outputSettingsStage.setTitle("Printout settings");

        VBox outputSettingsRoot = new VBox(5);
        outputSettingsRoot.setPadding(new Insets(10));
        outputSettingsRoot.setAlignment(Pos.CENTER);
        VBox outputSettingsAndAnnotationsBox = new VBox(5);

        VBox outputSettingsBox = new VBox(5);
        outputSettingsBox.setPadding(new Insets(10));

        //CheckBox shading = new CheckBox("Show shading");
        //shading.setSelected(true);
        VBox numberingContainer = new VBox(5);
        CheckBox numbering = new CheckBox("Show numbering");
        numbering.setSelected(true);

        HBox numberingLeft_space = new HBox();
        CheckBox numberingLeft = new CheckBox("Left");
        numberingLeft.setSelected(true);
        numberingLeft_space.getChildren().addAll(this.getSpacer(SETTINGS_SPACER_INTERVAL_PX, 0), numberingLeft);
        HBox numberingRight_space = new HBox();
        CheckBox numberingRight = new CheckBox("Right");
        numberingRight.setSelected(false);
        numberingRight_space.getChildren().addAll(this.getSpacer(SETTINGS_SPACER_INTERVAL_PX, 0), numberingRight);

        HBox numberingTop_space = new HBox();
        CheckBox numberingTop = new CheckBox("Top");
        numberingTop.setSelected(true);
        numberingTop_space.getChildren().addAll(this.getSpacer(SETTINGS_SPACER_INTERVAL_PX, 0), numberingTop);

        HBox numberingTopIntervalContainer = new HBox();
        Label numberingSpacingTopLabel = new Label("Every:    ");
        TextField numberingSpacingTopField = new TextField();
        numberingSpacingTopField.setMaxWidth(60);
        numberingSpacingTopField.setText(String.valueOf(DEFAULT_NUMBERING_SPACING));
        numberingSpacingTopField.setPromptText(String.valueOf(DEFAULT_NUMBERING_SPACING));
        numberingTopIntervalContainer.getChildren().addAll(this.getSpacer(2 * SETTINGS_SPACER_INTERVAL_PX, 0), numberingSpacingTopLabel, numberingSpacingTopField);
        numberingContainer.getChildren().addAll(numbering, numberingLeft_space,
                numberingRight_space, numberingTop_space, numberingTopIntervalContainer);

        HBox columnsPerRowContainer = new HBox();
        TextField columnsPerRowField = new TextField();
        columnsPerRowField.setMaxWidth(60);
        columnsPerRowField.setText(String.valueOf(DEFAULT_COLUMNS_PER_ROW));
        columnsPerRowField.setPromptText(String.valueOf(DEFAULT_COLUMNS_PER_ROW));
        Label columnsPerRowLabel = new Label("   columns per row");
        columnsPerRowContainer.getChildren().addAll(columnsPerRowField, columnsPerRowLabel);

        HBox maxNameCharsContainer = new HBox();
        TextField maxNameCharsField = new TextField();
        maxNameCharsField.setMaxWidth(60);
        maxNameCharsField.setText(String.valueOf(DEFAULT_MAX_NAMES_CHARS));
        maxNameCharsField.setPromptText(String.valueOf(DEFAULT_MAX_NAMES_CHARS));
        Label maxNameCharsLabel = new Label("   characters allowed in name");
        maxNameCharsContainer.getChildren().addAll(maxNameCharsField, maxNameCharsLabel);

        HBox fontSizeContainer = new HBox();
        TextField fontSizeField = new TextField();
        fontSizeField.setMaxWidth(60);
        fontSizeField.setText(String.valueOf(DEFAULT_OUTPUT_FONTSIZE));
        fontSizeField.setPromptText(String.valueOf(DEFAULT_OUTPUT_FONTSIZE));
        Label fontSizeLabel = new Label("   output font size");
        fontSizeContainer.getChildren().addAll(fontSizeField, fontSizeLabel);

        VBox annotationsCheckBoxContainer = new VBox(5);
        annotationsCheckBoxContainer.setPadding(new Insets(5));
        CheckBox masterAnnotationCheckBox = new CheckBox("Show all (nonempty) annotations");
        masterAnnotationCheckBox.setSelected(true);
        annotationsCheckBoxContainer.getChildren().add(masterAnnotationCheckBox);
        List<CheckBox> annoCheckBoxes = new ArrayList<>();
        HashMap<String, Integer> nameToIndexMapAnnotations = new HashMap<>();
        for (int k = 0; k < parent.getVMSA().annotationNumber(); k++) {
            String annostr = parent.getVMSA().getAnnotationName(k);
            HBox annoContainer = new HBox();
            CheckBox cb = new CheckBox();
            cb.setMnemonicParsing(false);
            cb.setText(annostr);
            nameToIndexMapAnnotations.put(annostr, k);
            annoContainer.getChildren().addAll(this.getSpacer(SETTINGS_SPACER_INTERVAL_PX, 0), cb);
            annotationsCheckBoxContainer.getChildren().add(annoContainer);
            annoCheckBoxes.add(cb);
            cb.setSelected(!this.annotationRowIsEmpty(this.parent.getVMSA(), annostr));
        }

        CheckBox showAnnotationsNames = new CheckBox("Show annotation names?");
        VBox showAnnotationsNamesBox = new VBox();
        showAnnotationsNames.setSelected(SHOW_ANNOTATIONS_NAMES);
        showAnnotationsNamesBox.getChildren().add(showAnnotationsNames);
        showAnnotationsNamesBox.setPadding(new Insets(5, 0, 0, 0));
        annotationsCheckBoxContainer.getChildren().add(showAnnotationsNamesBox);

        //Label spaceLabel1 = new Label(" ");
        CheckBox showDefaultAnnotationsNames = new CheckBox("Show default annotation names?");
        showDefaultAnnotationsNames.setSelected(SHOW_DEFAULT_ANNOTATIONS_NAMES);
        HBox showDefaultAnnotationsNamesBox = new HBox();
        showDefaultAnnotationsNamesBox.getChildren().addAll(this.getSpacer(SETTINGS_SPACER_INTERVAL_PX, 0), showDefaultAnnotationsNames);
        annotationsCheckBoxContainer.getChildren().add(showDefaultAnnotationsNamesBox);

        CheckBox omitEmptyAnnotations = new CheckBox("Omit empty sub-rows?");
        omitEmptyAnnotations.setSelected(OMIT_EMPTY_SUBSECTIONS);
        HBox omitEmptyAnnotationsBox = new HBox();
        omitEmptyAnnotationsBox.getChildren().addAll(omitEmptyAnnotations);
        omitEmptyAnnotationsBox.setPadding(new Insets(5, 0, 0, 0));
        annotationsCheckBoxContainer.getChildren().add(omitEmptyAnnotationsBox);

        HBox showConsensusBoxContainer = new HBox(5);
        CheckBox showConsensusBox = new CheckBox("Show consensus");
        showConsensusBox.setSelected(true);
        showConsensusBoxContainer.getChildren().add(showConsensusBox);
        showConsensusBoxContainer.setPadding(new Insets(5));

        Button execute = new Button("Generate print preview");

        //encode component behaviors
        numbering.setOnAction(e -> {
            if (numbering.isSelected()) {

                numberingLeft.setDisable(false);
                numberingRight.setDisable(false);
                numberingTop.setDisable(false);

                numberingLeft.setSelected(true);
                numberingRight.setSelected(true);
                numberingTop.setSelected(true);
            } else {
                numberingLeft.setSelected(false);
                numberingRight.setSelected(false);
                numberingTop.setSelected(false);

                numberingLeft.setDisable(true);
                numberingRight.setDisable(true);
                numberingTop.setDisable(true);
            }
        });

        numberingTop.selectedProperty().addListener(e -> {
            if (numberingTop.isSelected()) {
                numberingSpacingTopField.disableProperty().set(false);
            } else {
                numberingSpacingTopField.disableProperty().set(true);
            }
        });

        numberingSpacingTopField.textProperty().addListener(e -> {
            boolean empty = numberingSpacingTopField.getText().isEmpty();
            if ((!empty && !isInteger(numberingSpacingTopField.getText()))
                    || numberingSpacingTopField.getText().length() > 2
                    || (!empty && Integer.parseInt(columnsPerRowField.getText()) < 1)) {
                numberingSpacingTopField.setText(numberingSpacingTopField.getText().substring(0, numberingSpacingTopField.getLength() - 1));
            }
        });

        columnsPerRowField.textProperty().addListener(e -> {
            boolean empty = columnsPerRowField.getText().isEmpty();
            if ((!empty && !isInteger(columnsPerRowField.getText())) || columnsPerRowField.getText().length() > 3
                    || (!empty && Integer.parseInt(columnsPerRowField.getText()) < 1)
                    || (!empty && Integer.parseInt(columnsPerRowField.getText()) > 500)) {
                columnsPerRowField.setText(columnsPerRowField.getText().substring(0, columnsPerRowField.getLength() - 1));
            }
        });

        maxNameCharsField.textProperty().addListener(e -> {
            boolean empty = maxNameCharsField.getText().isEmpty();
            if ((!empty && !isInteger(maxNameCharsField.getText())) || maxNameCharsField.getText().length() > 2
                    || (!empty && Integer.parseInt(maxNameCharsField.getText()) < 1)
                    || (!empty && Integer.parseInt(maxNameCharsField.getText()) > 35)) {
                maxNameCharsField.setText(maxNameCharsField.getText().substring(0, maxNameCharsField.getLength() - 1));
            }
        });

        fontSizeField.textProperty().addListener(e -> {
            boolean empty = fontSizeField.getText().isEmpty();
            if ((!empty && !isInteger(fontSizeField.getText())) || fontSizeField.getText().length() > 2
                    || (!empty && Integer.parseInt(fontSizeField.getText()) < 1)) {
                fontSizeField.setText(fontSizeField.getText().substring(0, fontSizeField.getLength() - 1));
            }
        });

        masterAnnotationCheckBox.selectedProperty().addListener((Observable e) -> {
            if (masterAnnotationCheckBox.isSelected()) {
                for (CheckBox cb : annoCheckBoxes) {
                    cb.setSelected(!this.annotationRowIsEmpty(this.parent.getVMSA(), cb.getText()));
                }
            } else {
                for (CheckBox cb : annoCheckBoxes) {
                    cb.setSelected(false);
                }
            }
        });

        execute.setOnAction(e -> {

            GeneralLoadingManager glm_graphic = new GeneralLoadingManager("Loading print preview...");
            glm_graphic.showLoading(true);
            //establish settings
            showNumbering = false;
            if (numbering.isSelected()) {
                showNumbering = true;
                numbering_left = false;
                numbering_right = false;
                numbering_top = false;
                numberingSpacing_top = DEFAULT_NUMBERING_SPACING;
                if (numberingLeft.isSelected()) {
                    numbering_left = true;
                }
                if (numberingRight.isSelected()) {
                    numbering_right = true;
                }
                if (numberingTop.isSelected()) {
                    numbering_top = true;
                    numberingSpacing_top = Integer.parseInt(numberingSpacingTopField.getText());
                }
            }

            printAnnotationsNames = showAnnotationsNames.isSelected();
            printDefaultAnnotationsNames = showAnnotationsNames.isSelected() && showDefaultAnnotationsNames.isSelected();
            omitEmptySubsections = omitEmptyAnnotations.isSelected();

            annotationIndexShowMap = new HashMap<>();
            for (int p = 0; p < vmsa.annotationNumber(); p++) {
                annotationIndexShowMap.put(p, false);
            }
            if (masterAnnotationCheckBox.isSelected()) {
                for (CheckBox cb : annoCheckBoxes) {
                    if (cb.isSelected()) {
                        for (int p = 0; p < vmsa.annotationNumber(); p++) {
                            if (vmsa.getAnnotationName(p).equals(cb.getText())) {
                                annotationIndexShowMap.put(p, true);
                            }
                        }
                    }
                }
            }

            showConsensus = false;
            if (showConsensusBox.isSelected()) {
                showConsensus = true;
            }

            columnsPerRow = DEFAULT_COLUMNS_PER_ROW;
            if (!columnsPerRowField.getText().isEmpty()) {
                columnsPerRow = Integer.parseInt(columnsPerRowField.getText());
            }

            maxNamesChars_ui = DEFAULT_MAX_NAMES_CHARS;
            if (!maxNameCharsField.getText().isEmpty()) {
                maxNamesChars_ui = Integer.parseInt(maxNameCharsField.getText());
            }

            outputFontSize = DEFAULT_OUTPUT_FONTSIZE;
            if (!fontSizeField.getText().isEmpty()) {
                outputFontSize = Integer.parseInt(fontSizeField.getText());
            }

            //generate preview
            Platform.runLater(() -> {
                this.showOutputWindow();
                outputSettingsStage.close();
                Platform.runLater(() -> {
                    glm_graphic.exitLoading();
                });
            });

        });
        HBox executeBox = new HBox();
        executeBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(executeBox, Priority.ALWAYS);
        executeBox.getChildren().add(execute);
        executeBox.setPadding(new Insets(10));

        outputSettingsBox.getChildren().addAll(/*shading,*/numberingContainer,
                maxNameCharsContainer, fontSizeContainer, columnsPerRowContainer);
        outputSettingsAndAnnotationsBox.getChildren().addAll(outputSettingsBox,
                annotationsCheckBoxContainer, showConsensusBoxContainer);
        outputSettingsRoot.getChildren().addAll(outputSettingsAndAnnotationsBox, executeBox);
        Scene outputSettingsScene = new Scene(outputSettingsRoot);
        outputSettingsStage.setScene(outputSettingsScene);
        outputSettingsStage.initModality(Modality.APPLICATION_MODAL);
        outputSettingsStage.setResizable(false);
        outputSettingsStage.show();

        //outputSettingsBox.setMinWidth(outputSettingsBox.getWidth() * 1.35);
    }

    private boolean annotationRowIsEmpty(VisualMultipleSequenceAlignment vmsa, String annoName) {
        for (Node n : vmsa.getAnnotationRow(annoName).getChildren()) {
            VisualBioChar vbc = (VisualBioChar) n;
            if (vbc.getChar() != ' ') {
                return false;
            }
        }
        return true;
    }

    private boolean showNumbering = false;
    private boolean numbering_left = false;
    private boolean numbering_right = false;
    private boolean numbering_top = false;
    private int numberingSpacing_top = DEFAULT_NUMBERING_SPACING;

    private HashMap<Integer, Boolean> annotationIndexShowMap;

    private boolean showConsensus = false;

    private int columnsPerRow = DEFAULT_COLUMNS_PER_ROW;

    private int maxNamesChars_ui = DEFAULT_MAX_NAMES_CHARS;

    private int outputFontSize = DEFAULT_OUTPUT_FONTSIZE;

    private boolean printAnnotationsNames = SHOW_ANNOTATIONS_NAMES;
    private boolean printDefaultAnnotationsNames = SHOW_DEFAULT_ANNOTATIONS_NAMES;
    private boolean omitEmptySubsections;

    private void showOutputWindow() {
        Stage outStage = new Stage();
        outStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        ScrollPane scrollRoot = new ScrollPane();
        VBox root = new VBox();
        VBox outputBox = makeOutput_printout();
        outputBox.setStyle("-fx-background-color: white;");

        MenuBar mb = new MenuBar();
        Menu file = new Menu("File");
        mb.getMenus().add(file);
        MenuItem save = new MenuItem("Save as PNG");
        file.getItems().add(save);

        save.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save alignment as PNG");
            fileChooser.setInitialFileName(vmsa.getEditorTab().getText());

            File f = fileChooser.showSaveDialog(outStage);
            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) outputBox.getWidth(), (int) outputBox.getHeight());
                    outputBox.snapshot(null, writableImage);

                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", f);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        scrollRoot.setContent(outputBox);
        root.getChildren().addAll(mb, scrollRoot);
        Scene outScene = new Scene(root);
        outStage.setScene(outScene);
        outStage.show();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        outStage.setHeight(screenBounds.getHeight() * 0.85);
        outStage.setX((screenBounds.getWidth() - outStage.getWidth()) / 2);
        outStage.setY((screenBounds.getHeight() - outStage.getHeight()) / 2);
        
    }

    private VBox makeOutput_printout() {
        int maxNameChars = 0;
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            String name = vmsa.getAnnotationName(k);
            maxNameChars = Math.min(name.length(), maxNamesChars_ui);
        }
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            String name = vmsa.getSequenceName(k);
            maxNameChars = Math.min(name.length(), maxNamesChars_ui);
        }

        HashMap<Integer, String> annoIndexToPrintStr = new HashMap<>();
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            String name = vmsa.getAnnotationName(k);
            StringBuilder sb = new StringBuilder();

            boolean emptyChars = false;
            if (printAnnotationsNames) {
                if (printDefaultAnnotationsNames) {

                } else {
                    // check if the name matches defualt annotation pattern
                    if (name.matches("[(]Annotations_\\d+[)]")) {
                        emptyChars = true;
                    }
                }
            } else {
                emptyChars = true;
            }

            for (int j = 0; j < maxNameChars; j++) {
                try {
                    sb.append(emptyChars ? ' ' : name.charAt(j));
                } catch (StringIndexOutOfBoundsException sioobe) {
                    sb.append(' ');
                }
            }
            annoIndexToPrintStr.put(k, sb.toString());
            //System.out.println(sb.toString());
        }

        HashMap<Integer, String> seqsIndexToPrintStr = new HashMap<>();
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            String name = vmsa.getSequenceName(k);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < maxNameChars; j++) {
                try {
                    sb.append(name.charAt(j));
                } catch (StringIndexOutOfBoundsException sioobe) {
                    sb.append(' ');
                }
            }
            seqsIndexToPrintStr.put(k, sb.toString());
            //System.out.println(sb.toString());
        }

        StringBuilder sb = new StringBuilder();
        String consensusStrTemp = "Consensus";
        for (int j = 0; j < maxNameChars; j++) {
            try {
                sb.append(consensusStrTemp.charAt(j));
            } catch (StringIndexOutOfBoundsException sioobe) {
                sb.append(' ');
            }
        }
        String consensusPrintStr = sb.toString();
        //System.out.println(consensusPrintStr);

        //do one set of rows at a time
        int setsOfRows = (int) Math.round(Math.ceil(((double) vmsa.sequenceLength()) / ((double) columnsPerRow)));
        System.out.println(setsOfRows);
        HashMap<Integer, List<List<VisualBioChar>>> annoOrganized = new HashMap<>();
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            annoOrganized.put(k, new ArrayList<List<VisualBioChar>>());
            organizeSeq(vmsa.getAnnotationRow(k), columnsPerRow, annoOrganized.get(k));
            System.out.println(annoOrganized.get(k).size());
        }
        HashMap<Integer, List<List<VisualBioChar>>> seqsOrganized = new HashMap<>();
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            seqsOrganized.put(k, new ArrayList<List<VisualBioChar>>());
            organizeSeq(vmsa.getSequence(k), columnsPerRow, seqsOrganized.get(k));
            System.out.println(seqsOrganized.get(k).size());
        }
        List<List<VisualBioChar>> consOrganized = new ArrayList<>();
        organizeSeq(vmsa.getConsensusRow(), columnsPerRow, consOrganized);
        System.out.println(consOrganized.size());

        organizeSeq(vmsa.getConsensusRow(), columnsPerRow, consOrganized);

        //build document row by row
        VBox outBox = new VBox();
        int trueIndex = vmsa.getFirstIndex();
        int topNumberingOffset = maxNameChars + SPACER_LEN * 2;
        if (numbering_left) {
            topNumberingOffset += MAX_NUMBERSTR_LEN;
        }
        for (int k = 0; k < setsOfRows; k++) {
            //if (showAnnotations_rtf) {
            if (numbering_top) {
                outBox.getChildren().add(getTopNumberingRow(topNumberingOffset, trueIndex, consOrganized.get(k).size(), numberingSpacing_top));
            }

            for (int j = 0; j < vmsa.annotationNumber(); j++) { //annotations up top
                //getRow(getRowHeader(annoIndexToPrintStr.get(j), trueIndex), annoOrganized.get(j).get(k), ret);
                if (annotationIndexShowMap.get(j)) {
                    boolean isEmpty = true;
                    if (omitEmptySubsections) {
                        String pout = "";
                        for (VisualBioChar vbc : annoOrganized.get(j).get(k)) {
                            if (vbc.getChar() != ' ') {
                                isEmpty = false;
                            }
                            pout += String.valueOf(vbc.getChar());
                        }
                    }
                    if (!(omitEmptySubsections && isEmpty)) {
                        outBox.getChildren().add(getRow(annoOrganized.get(j).get(k), annoIndexToPrintStr.get(j), -1));
                    }
                }
            }
            //}
            for (int j = 0; j < vmsa.sequenceNumber(); j++) { //sequences next
                //getRow(getRowHeader(seqsIndexToPrintStr.get(j), trueIndex), seqsOrganized.get(j).get(k), ret);
                outBox.getChildren().add(getRow(seqsOrganized.get(j).get(k), seqsIndexToPrintStr.get(j), trueIndex));
            }
            //getRow(getRowHeader(consensusPrintStr, trueIndex), consOrganized.get(k), ret);
            if (showConsensus) {
                outBox.getChildren().add(getRow(consOrganized.get(k), consensusPrintStr, trueIndex));
            }
            outBox.getChildren().add(getLabelStr(" "));
            trueIndex += columnsPerRow;
        }

        return outBox;

    }

    private HBox getTopNumberingRow(int offsetChars, int startIndex, int columns, int spacing) {
        HBox ret = new HBox();
        for (int k = 0; k < offsetChars + columns; k++) {
            ret.getChildren().add(getLabelStr(" "));
        }

        int currentIndex = startIndex;
        for (int k = 0; k < columns; k++) {
            currentIndex = startIndex + k;
            if ((k + 1) % spacing == 0) {
                String indexToPrint = String.valueOf(currentIndex);
                if ((columns - k) - indexToPrint.length() >= 0) {
                    for (int j = 0; j < indexToPrint.length(); j++) {
                        ((Label) ret.getChildren().get(offsetChars + k + j)).setText(String.valueOf(indexToPrint.charAt(j)));
                    }
                }
            }
        }

        return ret;
    }

    private String SPACER = "  ";
    private int SPACER_LEN = SPACER.length();

    private HBox getRow(List<VisualBioChar> charset, String name, int index) {
        HBox ret = new HBox();

        ret.getChildren().addAll(getLabels(name.concat("  ")));
        if (numbering_left && index != -1) {
            ret.getChildren().addAll(getNumberLabels(index));
        } else {
            String spaceStr = "";
            for (int k = 0; k < MAX_NUMBERSTR_LEN; k++) {
                spaceStr += " ";
            }
            ret.getChildren().addAll(getLabels(spaceStr));
        }
        ret.getChildren().addAll(getLabels("  "));

        for (VisualBioChar vbc : charset) {
            Label toAdd = new Label(String.valueOf(vbc.getChar()));
            toAdd.setStyle(generateFont(vbc.getBackHex(), vbc.getForeHex()));
            ret.getChildren().add(toAdd);
        }

        if (numbering_right && index != -1) {
            ret.getChildren().addAll(getNumberLabels(index + charset.size() - 1));
        } else {
            String spaceStr = "";
            for (int k = 0; k < MAX_NUMBERSTR_LEN; k++) {
                spaceStr += " ";
            }
            ret.getChildren().addAll(getLabels(spaceStr));
        }

        return ret;
    }

    private final int MAX_NUMBERSTR_LEN = 6;

    private List<Label> getNumberLabels(int index) {
        String indexstr = String.valueOf(index);
        List<Character> indexchars = new ArrayList<>();

        for (int k = 0; k < MAX_NUMBERSTR_LEN; k++) {
            try {
                indexchars.add(indexstr.charAt(indexstr.length() - 1 - k));
            } catch (StringIndexOutOfBoundsException sioobe) {
                indexchars.add(' ');
            }
        }

        StringBuilder indexsb = new StringBuilder();
        for (int j = indexchars.size() - 1; j >= 0; j--) {
            indexsb.append(indexchars.get(j));
        }

        List<Label> ret = new ArrayList<>();
        String toUse = indexsb.toString();
        for (int k = 0; k < toUse.length(); k++) {
            ret.add(getLabelStr(String.valueOf(toUse.charAt(k))));
        }

        return ret;
    }

    private List<Label> getLabels(String s) {
        List<Label> ret = new ArrayList<>();
        for (int j = 0; j < s.length(); j++) {
            ret.add(getLabelStr(String.valueOf(s.charAt(j))));
        }
        return ret;
    }

    private Label getLabelStr(String s) {
        Label lb = new Label(s);
        lb.setStyle(generateFont(null, null));
        return lb;
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private void organizeSeq(HBox hbox, int elementsPerRow, List<List<VisualBioChar>> toAdd) {
        int charIter = 0;
        List<VisualBioChar> currentRowRTF = new ArrayList<>();

        for (int k = 0; k < vmsa.sequenceLength(); k++) {
            currentRowRTF.add((VisualBioChar) hbox.getChildren().get(k));

            charIter++;
            if (charIter >= elementsPerRow) {
                charIter = 0;
                toAdd.add(new ArrayList(currentRowRTF));
                currentRowRTF = new ArrayList<>();
            }
        }
        toAdd.add(new ArrayList(currentRowRTF));
    }

    public String generateFont(String backColor, String foreColor) {
        StringBuilder str = new StringBuilder("");
        String backgroundHex = "#FFFFFF";
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        if (backColor != null) {
            backgroundHex = backColor;
        }
        String foregroundHex = "#000000";
        if (foreColor != null) {
            foregroundHex = foreColor;
        }

        str.append("    -fx-font-family: \"unifont\";\n"); //everything unifont - always
        str.append("    -fx-font-size: ").append(String.valueOf(outputFontSize)).append(";\n");
        str.append("    -fx-background-color: ").append(backgroundHex).append(" ;\n");
        str.append("    -fx-text-fill: ").append(foregroundHex).append(" ;\n");

        return str.toString();
    }

    private Region getSpacer(double fixed_w, double fixed_h) {
        Region r = new Region();
        r.setMaxSize(fixed_w, fixed_h);
        r.setMinSize(fixed_w, fixed_h);
        return r;
    }

}
