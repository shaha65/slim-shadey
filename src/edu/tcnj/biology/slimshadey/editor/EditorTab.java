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

import edu.tcnj.biology.seqverter.SeqVerter;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.converter.FileFormat;
import edu.tcnj.biology.seqverter.converter.SeqVerterReader;
import edu.tcnj.biology.seqverter.converter.SeqVerterWriter;
import edu.tcnj.biology.seqverter.sequence.Alphabet;
import edu.tcnj.biology.seqverter.sequence.DefaultAlphabets;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import edu.tcnj.biology.slimshadey.editor.basicshade.BasicShadeRemote;
import edu.tcnj.biology.slimshadey.editor.defaultcolor.RasmolColorRemote;
import edu.tcnj.biology.slimshadey.editor.hmmscanshade.hmmscan_ShadeRemote;
import edu.tcnj.biology.slimshadey.editor.matrixshade.MatrixShadeRemote;
import edu.tcnj.biology.slimshadey.editor.regexshade.PrositeShadeRemote;
import edu.tcnj.biology.slimshadey.editor.regexshade.customregex.CustomRegexShadeRemote;
import edu.tcnj.biology.slimshadey.editor.sequenceshade.SequenceShadeRemote;
import edu.tcnj.biology.slimshadey.editor.uniqueshade.UniqueShadeRemote;
import java.awt.CheckboxMenuItem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import opalimpl.OpalAgreement;
import opalimpl.OpalRunner;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class EditorTab extends Tab {

    private VisualMultipleSequenceAlignment vmsa;
    private VBCEditor vbcEditor;
    private SequenceHolder seqHold;
    private DefaultAlphabets allAlphas;
    private Alphabet alpha;

    private VBox root;

    private SplitPane skeleton;
    private Track mainPane;

    private VBox seqNames;
    private VBox seqRows;

    private AnchorPane anchorEdit;

    public final EditorInterface THE_PROGRAM;

    private final OutputPrinter OUTPUT_PRINTER;

    public EditorTab(SequenceHolder sh, String name, int index, EditorInterface PROGRAM) {
        super(String.valueOf(index).concat(" - ").concat(name));

        THE_PROGRAM = PROGRAM;
        this.allAlphas = new DefaultAlphabets();
        this.alpha = sh.getAlphabet();
        this.initializeUI();

        this.vmsa = new VisualMultipleSequenceAlignment(sh, anchorEdit, mainPane.getRightPane(), this);
        this.vmsa.collapseIdenticalSequences(this.vmsa.autoCollapse);
        collapseIdentical.setSelected(this.vmsa.isCollapsed());
        this.vmsa.parent = anchorEdit;
        seqNames.getChildren().addAll(this.vmsa.getNames());
        seqRows.getChildren().addAll(this.vmsa.getRows());
        this.initializeComponentBehaviors();

        vbcEditor = new VBCEditor(this);
        root.getChildren().add(vbcEditor);
        vbcEditor.disable(true);

        this.setContent(root);
        OUTPUT_PRINTER = new OutputPrinter(this);
        this.setPaneSplit();
    }

    public EditorTab(VBox annotationLabelsTemp,
            VBox annotationRowsTemp, VBox sequenceNamesTemp, VBox visualSequencesTemp,
            HBox consensusTemp,
            int[] range, String name, Alphabet alpha, EditorInterface PROGRAM) {
        super(name);
        THE_PROGRAM = PROGRAM;
        this.allAlphas = new DefaultAlphabets();
        this.alpha = alpha;
        this.initializeUI();

        this.vmsa = new VisualMultipleSequenceAlignment(annotationLabelsTemp,
                annotationRowsTemp, sequenceNamesTemp, visualSequencesTemp,
                consensusTemp, anchorEdit, mainPane.getRightPane(), this,
                range, alpha);
        this.vmsa.parent = anchorEdit;
        seqNames.getChildren().addAll(this.vmsa.getNames());
        seqRows.getChildren().addAll(this.vmsa.getRows());
        this.initializeComponentBehaviors();

        vbcEditor = new VBCEditor(this);
        root.getChildren().add(vbcEditor);
        vbcEditor.disable(true);

        this.setContent(root);
        OUTPUT_PRINTER = new OutputPrinter(this);
        this.setPaneSplit();
    }

    public EditorTab(VBox annotationLabelsTemp,
            VBox annotationRowsTemp, VBox sequenceNamesTemp, VBox visualSequencesTemp,
            HBox consensusTemp,
            int[] range, String name, Alphabet alpha, EditorInterface PROGRAM,
            int fontSize, double luminance, boolean collapse,
            boolean trackLiveHover, long refreshDelay) {
        super(name);
        THE_PROGRAM = PROGRAM;
        this.allAlphas = new DefaultAlphabets();
        this.alpha = alpha;
        this.initializeUI();

        this.vmsa = new VisualMultipleSequenceAlignment(annotationLabelsTemp,
                annotationRowsTemp, sequenceNamesTemp, visualSequencesTemp,
                consensusTemp, anchorEdit, mainPane.getRightPane(), this,
                range, alpha);
        this.vmsa.parent = anchorEdit;
        seqNames.getChildren().addAll(this.vmsa.getNames());
        seqRows.getChildren().addAll(this.vmsa.getRows());
        this.initializeComponentBehaviors();

        vbcEditor = new VBCEditor(this);
        root.getChildren().add(vbcEditor);
        vbcEditor.disable(true);

        this.setContent(root);
        OUTPUT_PRINTER = new OutputPrinter(this);

        vmsa.updateStyleSettings(fontSize, luminance);
        if (collapse) {
            collapseIdentical.setSelected(true);
            vmsa.collapseIdenticalSequences(collapse);
        }

        vmsa.setLiveHover(trackLiveHover);
        vmsa.setRefreshDelay(refreshDelay);
        this.setPaneSplit();
    }

    private void setPaneSplit() {
        Platform.runLater(() -> {
            double conformWidth = 0;
            for (Node nx : this.vmsa.getNames().getChildren()) {
                if (nx instanceof VBox) {
                    for (Node ny : ((VBox) nx).getChildren()) {
                        if (ny instanceof Label) {
                            conformWidth = Math.max(conformWidth, ((Label) ny).getWidth());
                            System.out.println(((Label) ny).getText() + " " + ((Label) ny).getWidth());
                        }
                    }
                }
            }
            
            conformWidth += 30; //px
            System.out.println("Initializing pane proportion; " + conformWidth + "/" + mainPane.getWidth());
            mainPane.setDividerPosition(0, mainPane.getWidth() == 0.0d ? 0.3 : conformWidth / mainPane.getWidth());
        });
    }

    protected VBCEditor vbcEditor() {
        return this.vbcEditor;
    }

    private HBox main_spacer;
    private VisualBioChar vbc_extra_spacer1 = new VisualBioChar(" ", false, false, false, false, false);
    private VisualBioChar vbc_extra_spacer2 = new VisualBioChar(" ", false, false, false, false, false);

    private MenuBar menuBar;

    private Menu menuFile;
    private MenuItem openFile;
    private MenuItem renameTab;
    private MenuItem exportToNewTab;
    private MenuItem realignData;
    private MenuItem printPreview;
    private MenuItem saveAsRTF;
    private MenuItem saveTextAlignment;
    private MenuItem saveFile;
    private MenuItem saveAsFile;
    private FileChooser fileChooser;

    private Menu shadeMenu;
    private MenuItem clearShade;
    private RadioMenuItem uniqueShade;
    private RadioMenuItem basicShadeMenuItem;
    private RadioMenuItem sequenceShade;
    private RadioMenuItem matrixShade;
    private RadioMenuItem customRegexShade;
    private RadioMenuItem prositeShadeMenuItem;
    private RadioMenuItem hmmscanShadeMenuItem;
    private RadioMenuItem defaultColoring;
    private RadioMenuItem clustalxColoring;
    private ToggleGroup shadingModalityGroup;

    private Menu editMenu;
    private MenuItem sessionSettings;
    private MenuItem checkForAllGapColumns;
    private MenuItem deleteAllColumnsWithGaps;
    //private MenuItem setFirstIndex;
    private MenuItem generateConsensus;
    private MenuItem addAnnotationRow;
    private CheckMenuItem collapseIdentical;

    private Region PERMA_GROW;

    //shading controllers and rulesets
    public UniqueShadeRemote usr;

    public BasicShadeRemote bsr;

    public SequenceShadeRemote ssr;

    public MatrixShadeRemote msr;

    public CustomRegexShadeRemote crsr;

    public PrositeShadeRemote psr;

    public hmmscan_ShadeRemote hmmscan_sr;

    public RasmolColorRemote rcr;

    public File projectFile = null;

    private void initializeUI() {
        root = new VBox();
        root.setStyle("-fx-background-color:white;");
        skeleton = new SplitPane();
        skeleton.setOrientation(Orientation.VERTICAL);

        menuBar = new MenuBar();
        menuFile = new Menu("File");
        //openFile = new MenuItem("Open Shadey project");
        renameTab = new MenuItem("Rename tab");

        exportToNewTab = new MenuItem("Export to new tab");

        realignData = new MenuItem("Realign sequences");

        printPreview = new MenuItem("Print preview");

        saveAsRTF = new MenuItem("Output to RTF");

        saveTextAlignment = new MenuItem("Save alignment as text");

        saveFile = new MenuItem("Save project");
        saveAsFile = new MenuItem("Save as project");

        menuFile.getItems().addAll(/*openFile, */renameTab,
                new SeparatorMenuItem(), exportToNewTab,
                new SeparatorMenuItem(), realignData,
                new SeparatorMenuItem(), printPreview,
                new SeparatorMenuItem(), saveAsRTF,
                new SeparatorMenuItem(), saveTextAlignment,
                new SeparatorMenuItem(), saveFile, saveAsFile);

        shadeMenu = new Menu("Shade");
        clearShade = new MenuItem("Clear shading");

        uniqueShade = new RadioMenuItem("Shade by unique residues");
        uniqueShade.setSelected(false);

        basicShadeMenuItem = new RadioMenuItem("Shade by residue frequency");
        basicShadeMenuItem.setSelected(false);

        sequenceShade = new RadioMenuItem("Shade by sequence");
        sequenceShade.setSelected(false);

        customRegexShade = new RadioMenuItem("Custom Prosite regex shading");
        customRegexShade.setSelected(false);

        prositeShadeMenuItem = new RadioMenuItem("Prosite-based database shading");
        prositeShadeMenuItem.setSelected(false);

        hmmscanShadeMenuItem = new RadioMenuItem("hmmscan-based shading");
        hmmscanShadeMenuItem.setSelected(false);

        defaultColoring = new RadioMenuItem("Default RASMOL coloring");
        defaultColoring.setSelected(false);

        clustalxColoring = new RadioMenuItem("ClustalX shading");
        clustalxColoring.setSelected(false);

        matrixShade = new RadioMenuItem("Shade with substitution matrix");
        matrixShade.setSelected(false);

        shadingModalityGroup = new ToggleGroup();
        shadingModalityGroup.getToggles().addAll(
                uniqueShade,
                basicShadeMenuItem,
                sequenceShade,
                customRegexShade,
                prositeShadeMenuItem,
                hmmscanShadeMenuItem,
                defaultColoring,
                clustalxColoring,
                matrixShade);

        shadeMenu.getItems().addAll(
                clearShade, new SeparatorMenuItem(),
                uniqueShade, new SeparatorMenuItem(),
                basicShadeMenuItem, new SeparatorMenuItem(),
                sequenceShade);

        if (this.getAlphabet().matrices != null && !this.getAlphabet().matrices.isEmpty()) {
            shadeMenu.getItems().addAll(
                    new SeparatorMenuItem(), matrixShade);
        }

        shadeMenu.getItems().addAll(new SeparatorMenuItem(), customRegexShade,
                new SeparatorMenuItem(), prositeShadeMenuItem);

        if (alpha != null && alpha.name.toLowerCase().equals("protein")) {
            shadeMenu.getItems().addAll(
                    new SeparatorMenuItem(), hmmscanShadeMenuItem);
        }

        shadeMenu.getItems().addAll(new SeparatorMenuItem(), defaultColoring);

        if (alpha.consensusAlphabetClustal != null && !alpha.consensusAlphabetClustal.isEmpty()) {
            shadeMenu.getItems().addAll(
                    new SeparatorMenuItem(), clustalxColoring);
        }

        sessionSettings = new MenuItem("Session settings");

        checkForAllGapColumns = new MenuItem("Check for gap-only columns");

        deleteAllColumnsWithGaps = new MenuItem("Delete any columns with gaps");

        //setFirstIndex = new MenuItem("Set first column index");
        generateConsensus = new MenuItem("Calculate consensus");

        addAnnotationRow = new MenuItem("Add annotation row");

        collapseIdentical = new CheckMenuItem("Collapse identical sequences");
        collapseIdentical.setOnAction(e -> {
            this.vmsa.collapseIdenticalSequences(!this.vmsa.isCollapsed());
        });

        editMenu = new Menu("Edit");
        editMenu.getItems().addAll(sessionSettings, new SeparatorMenuItem(),
                checkForAllGapColumns, new SeparatorMenuItem(),
                deleteAllColumnsWithGaps, new SeparatorMenuItem(),
                generateConsensus, new SeparatorMenuItem(),
                addAnnotationRow, new SeparatorMenuItem(),
                collapseIdentical);

        menuBar.getMenus().addAll(menuFile, editMenu, shadeMenu);
        //menuBar.prefWidthProperty().bind(this.widthProperty());

        mainPane = new Track(null);

        seqNames = new VBox();
        mainPane.getLeftPane().setContent(seqNames);
        anchorEdit = new AnchorPane();

        PERMA_GROW = new Region();
        VBox.setVgrow(PERMA_GROW, Priority.NEVER);

        skeleton.getItems().addAll(mainPane);

        main_spacer = new HBox();
        vbc_extra_spacer1.getStyleClass().set(0, "biochar");
        vbc_extra_spacer2.getStyleClass().set(0, "biochar");
        seqRows = new VBox();
        anchorEdit.getChildren().addAll(seqRows);
        main_spacer.getChildren().addAll(vbc_extra_spacer1, vbc_extra_spacer2, anchorEdit);

        mainPane.getRightPane().setContent(main_spacer);

        //anchorName.getChildren().addAll(this.seqNames);
        mainPane.setDividerPosition(0, 0.3);

        root.widthProperty().addListener(e -> {

        });

        root.getChildren().addAll(menuBar, skeleton);

        VBox.setVgrow(skeleton, Priority.ALWAYS);

    }

    public Alphabet getAlphabet() {
        return alpha;
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    /*
    private final String[][] FILE_SAVE_EXT = new String[][]{
        new String[]{"FASTA", "*.fasta"},
        new String[]{"CLUSTAL", "*aln"},
        new String[]{"MSF", "*.msf"},
        new String[]{"Phylip sequential", "*.phy"},
        new String[]{"Phylip interleaved", "*.phy"},};
     */
    private final LinkedHashMap<String[], FileFormat> FILE_EXT_MAP = new LinkedHashMap<String[], FileFormat>() {
        {
            put(new String[]{"FASTA", "*.fasta"}, FileFormat.fasta);
            put(new String[]{"CLUSTAL", "*aln"}, FileFormat.clustal);
            put(new String[]{"MSF", "*.msf"}, FileFormat.msf);
            put(new String[]{"Phylip sequential", "*.phy"}, FileFormat.phylip_sequential);
            put(new String[]{"Phylip interleaved", "*.phy"}, FileFormat.phylip_interleaved);
        }
    };

    public void initializeComponentBehaviors() {

        /*
        openFile.setOnAction(e -> {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Open Shadey project");
            File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
            this.vmsa = new VisualMultipleSequenceAlignment(SeqVerterReader.read(selectedFile, FileFormat.guess, new DefaultAlphabets()), anchorEdit, this.mainPane.getRightPane());
        });
         */
        sessionSettings.setOnAction(e -> {
            this.vmsa.changeSettings();
        });

        checkForAllGapColumns.setOnAction(e -> {
            this.vmsa.checkForAllGapColumns(0, vmsa.sequenceLength() - 1, true);
        });

        deleteAllColumnsWithGaps.setOnAction(e -> {
            this.vmsa.deleteAllColumnsWithGaps();
        });

        printPreview.setOnAction(e -> {
            OUTPUT_PRINTER.makeOutput();
        });

        exportToNewTab.setOnAction(e -> {
            this.exportToNewTab(false);
        });

        realignData.setOnAction(e -> {

            String warningText = "Continuing will open OpalFX to align your "
                    + "sequences. The realigned sequences will not retain "
                    + "the present shading.";

            Stage mwpStage = new Stage();
            mwpStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            mwpStage.setTitle("Continue with realignment?");

            Text graphicWarningText = new Text(warningText);
            graphicWarningText.setWrappingWidth(550);
            graphicWarningText.setTextAlignment(TextAlignment.CENTER);
            Button continueAction = new Button("Continue");
            continueAction.setMinWidth(150);
            continueAction.setOnAction(eve -> {
                try {
                    SequenceHolder shwrite = this.getVMSA().getSequenceHolder();
                    for (Sequence seq : shwrite) {
                        seq.setSequence(seq.seqString().replaceAll("-", "")); //degap sequences
                    }
                    File toalign = File.createTempFile("temp_align", ".tmp");
                    this.THE_PROGRAM.writeFASTA(toalign, shwrite);
                    new OpalAgreement(true, this.THE_PROGRAM, toalign);

                } catch (IOException ex) {
                    Logger.getLogger(EditorTab.class.getName()).log(Level.SEVERE, null, ex);
                }
                mwpStage.close();
            });

            VBox rootBox = new VBox(15);
            rootBox.setPadding(new Insets(10, 10, 10, 10));
            rootBox.setAlignment(Pos.CENTER);
            rootBox.getChildren().addAll(graphicWarningText, continueAction);

            Scene mwpScene = new Scene(rootBox);
            mwpStage.setScene(mwpScene);
            mwpStage.initModality(Modality.APPLICATION_MODAL);
            //mwpStage.setAlwaysOnTop(true);
            mwpStage.setMaxWidth(600);
            mwpStage.showAndWait();
        });

        renameTab.setOnAction(e -> {
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            st.setTitle("Rename tab");
            st.setAlwaysOnTop(true);
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);

            Label message = new Label("Tab name: ");
            TextField entryField = new TextField(this.getText());
            entryField.setPromptText(this.getText());
            entryField.textProperty().addListener(event -> {
                if (!entryField.getText().isEmpty() && entryField.getText().length() > 20) {
                    entryField.setText(entryField.getText().substring(0, entryField.getLength() - 1));
                }
            });
            HBox centerBox = new HBox(15);
            centerBox.getChildren().addAll(message, entryField);
            centerBox.setAlignment(Pos.CENTER);

            Button cancel = new Button("Cancel");
            cancel.setOnAction(event -> st.close());
            Button saveNewIndex = new Button("Change name");
            saveNewIndex.setOnAction(event -> {
                String name = "";
                if (entryField.getText().isEmpty()) {
                    name = this.getText();
                } else {
                    name = entryField.getText();
                }
                this.setText(name);
                st.close();
            });
            HBox bottomBox = new HBox(10);
            bottomBox.getChildren().addAll(cancel, saveNewIndex);
            bottomBox.setPadding(new Insets(15, 0, 0, 0));

            VBox stRoot = new VBox();
            stRoot.getChildren().addAll(centerBox, bottomBox);
            stRoot.setPadding(new Insets(20));

            Scene sc = new Scene(stRoot);

            st.setScene(sc);
            st.show();

            cancel.requestFocus();

            centerBox.setPrefHeight(st.getHeight());
            cancel.setPrefWidth(st.getWidth() / 2);
            saveNewIndex.setPrefWidth(st.getWidth() / 2);

            st.heightProperty().addListener(event -> {
                centerBox.setPrefHeight(st.getHeight());
                cancel.setPrefWidth(st.getWidth() / 2);
                saveNewIndex.setPrefWidth(st.getWidth() / 2);
            });

        });

        saveTextAlignment.setOnAction(e -> {
            FileWriter out = null;

            FileChooser fileChooserSave = new FileChooser();
            fileChooserSave.setTitle("Save alignment in text format");

            for (String[] ext : /*FILE_SAVE_EXT*/ FILE_EXT_MAP.keySet()) {
                fileChooserSave.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(ext[0], ext[1]));
            }

            fileChooserSave.setInitialFileName(this.getText());
            File fileSave = fileChooserSave.showSaveDialog(this.THE_PROGRAM.getStage());
            if (fileSave != null) {
                try {
                    System.out.println(fileChooserSave.getSelectedExtensionFilter().toString());
                    String descr = fileChooserSave.getSelectedExtensionFilter().getDescription();
                    String[] keyUse = null;
                    FileFormat ffwrite = FileFormat.fasta;

                    for (String[] key : FILE_EXT_MAP.keySet()) {
                        if (key[0].equals(descr)) {
                            keyUse = key;
                            break;
                        }
                    }

                    ffwrite = FILE_EXT_MAP.getOrDefault(keyUse, FileFormat.fasta);

                    System.out.println("  " + descr + " " + ffwrite);
                    for (String[] key : FILE_EXT_MAP.keySet()) {
                        System.out.println("*" + key[0] + " " + key[1] + " " + FILE_EXT_MAP.get(key));
                    }

                    this.vmsa.updatedSequenceHolder(this.vmsa.getFirstIndex());
                    SequenceHolder towrite = this.vmsa.getSequenceHolder();

                    System.out.println(fileSave.getCanonicalPath());

                    //this.THE_PROGRAM.writeFASTA(fileSave, towrite);
                    SeqVerterWriter.write(fileSave, ffwrite, towrite);

                } catch (IllegalArgumentException | IOException ex) {
                    Logger.getLogger(EditorTab.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        saveAsRTF.setOnAction(e -> {
            outputToRTF();
        });

        saveAsFile.setOnAction(e -> {
            FileChooser fileChooserSave = new FileChooser();
            fileChooserSave.setTitle("Save alignment in FASTA format");
            FileChooser.ExtensionFilter extFilterSave = new FileChooser.ExtensionFilter("Shadey project (*.mm)", "*.mm");
            fileChooserSave.setInitialFileName(this.getText());
            fileChooserSave.getExtensionFilters().add(extFilterSave);
            File fileSave = fileChooserSave.showSaveDialog(this.THE_PROGRAM.getStage());
            if (fileSave != null) {

                this.THE_PROGRAM.writeShadeyProject(this, fileSave);
                projectFile = fileSave;
                System.out.println(fileSave.getAbsolutePath());
            }
        });

        saveFile.setOnAction(e -> {
            if (projectFile == null) {
                saveAsFile.fire();
            } else {
                try {
                    PrintWriter writer = new PrintWriter(projectFile);
                    writer.close();
                    this.THE_PROGRAM.writeShadeyProject(this, projectFile);
                    System.out.println(projectFile.getAbsolutePath());
                } catch (Exception exc) {
                    saveAsFile.fire();
                }
            }
        });

        generateConsensus.setOnAction(e -> {
            this.vmsa.calculateConsensus();
        });

        addAnnotationRow.setOnAction(e -> {
            this.vmsa.addAnnotationRow();
        });

        clearShade.setOnAction(e -> {
            //clear the VMSA
            this.clearShading();
            //deselect all shading modalities
            shadingModalityGroup.getToggles().forEach((t) -> t.setSelected(false));
        });

        /*
        setFirstIndex.setOnAction(e -> {
            Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            st.setTitle("Set first column index");
            st.setAlwaysOnTop(true);
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);

            Label message = new Label("Set index of first column in alignment to:");
            TextField entryField = new TextField();
            entryField.setPromptText(String.valueOf(this.vmsa.getFirstIndex()).concat(" (Current Index)"));
            entryField.textProperty().addListener(event -> {
                if ((!entryField.getText().isEmpty() && !isInteger(entryField.getText())) || entryField.getText().length() > 5) {
                    entryField.setText(entryField.getText().substring(0, entryField.getLength() - 1));
                }
            });
            HBox centerBox = new HBox(15);
            centerBox.getChildren().addAll(message, entryField);
            centerBox.setAlignment(Pos.CENTER);

            Button cancel = new Button("Cancel");
            cancel.setOnAction(event -> st.close());
            Button saveNewIndex = new Button("Set index");
            saveNewIndex.setOnAction(event -> {
                if (entryField.getText().isEmpty()) {
                    this.vmsa.setFirstIndex(1);
                } else {
                    this.vmsa.setFirstIndex(Integer.parseInt(entryField.getText()));
                }
                this.vmsa.numbering(this.vmsa.numberingSpacing, true);
                st.close();
            });
            HBox bottomBox = new HBox();
            bottomBox.getChildren().addAll(cancel, saveNewIndex);

            VBox stRoot = new VBox();
            stRoot.getChildren().addAll(centerBox, bottomBox);

            Scene sc = new Scene(stRoot, 500, 100, true);

            st.setScene(sc);
            st.show();

            cancel.requestFocus();

            centerBox.setPrefHeight(st.getHeight());
            cancel.setPrefWidth(st.getWidth() / 2);
            saveNewIndex.setPrefWidth(st.getWidth() / 2);

            st.heightProperty().addListener(event -> {
                centerBox.setPrefHeight(st.getHeight());
                cancel.setPrefWidth(st.getWidth() / 2);
                saveNewIndex.setPrefWidth(st.getWidth() / 2);
            });

        });
         */
        uniqueShade.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (usr == null) {
                    usr = new UniqueShadeRemote(this);
                } else {
                    usr.unHide(true);
                }
            }
        });

        basicShadeMenuItem.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (bsr == null) {
                    bsr = new BasicShadeRemote(this);
                } else {
                    bsr.unHide(true);
                }
            }
        });

        sequenceShade.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (ssr == null) {
                    ssr = new SequenceShadeRemote(this);
                } else {
                    ssr.unHide(true);
                }
            }
        });

        customRegexShade.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (crsr == null) {
                    crsr = new CustomRegexShadeRemote(this);
                } else {
                    crsr.unHide(true);
                }
            }
        });

        prositeShadeMenuItem.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (psr == null) {
                    psr = new PrositeShadeRemote(this);
                } else {
                    psr.unHide(true);
                }
            }
        });

        hmmscanShadeMenuItem.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (hmmscan_sr == null) {
                    hmmscan_sr = new hmmscan_ShadeRemote(this);
                } else {
                    hmmscan_sr.unHide(true);
                }
            }
        });

        defaultColoring.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (rcr == null) {
                    rcr = new RasmolColorRemote(this);
                } else {
                    rcr.unHide(true);
                }
            }
        });

        matrixShade.setOnAction(e -> {
            updateShadeModality();
            if (ret) {
                if (msr == null) {
                    msr = new MatrixShadeRemote(this);
                } else {
                    msr.unHide(true);
                }
            }
        });

        if (alpha.consensusAlphabetClustal != null && !alpha.consensusAlphabetClustal.isEmpty()) {
            clustalxColoring.setOnAction(e -> {
                updateShadeModality();
                if (ret) {
                    Stage clearStage = new Stage();
                    clearStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                    clearStage.setTitle("Clustal consensus");
                    Label clearShadingLabel = new Label("This coloring mode requires a ClustalX consensus to be calculated");
                    Button yes = new Button("Continue");
                    yes.setOnAction(eve -> {
                        this.vmsa.colorClustalX();
                        clearStage.close();
                    });
                    Button no = new Button("Cancel");
                    no.setOnAction(eve -> {
                        clearStage.close();
                    });

                    VBox clearRoot = new VBox(5);
                    clearRoot.setPadding(new Insets(15, 15, 0, 15));
                    HBox buttons = new HBox();
                    buttons.getChildren().addAll(no, yes);

                    clearRoot.getChildren().addAll(clearShadingLabel, buttons);

                    buttons.setPadding(new Insets(15));

                    clearStage.widthProperty().addListener(o -> {
                        for (int i = 0; i < buttons.getChildren().size(); i++) {
                            if (buttons.getChildren().get(i) instanceof Button) {
                                ((Button) buttons.getChildren().get(i)).setPrefWidth(clearStage.getWidth() / buttons.getChildren().size());
                            }
                        }
                    });

                    Scene clearScene = new Scene(clearRoot);
                    clearStage.setScene(clearScene);
                    clearStage.initModality(Modality.APPLICATION_MODAL);
                    clearStage.setAlwaysOnTop(true);
                    for (int i = 0; i < buttons.getChildren().size(); i++) {
                        if (buttons.getChildren().get(i) instanceof Button) {
                            ((Button) buttons.getChildren().get(i)).setPrefWidth(clearStage.getWidth() / buttons.getChildren().size());
                        }
                    }
                    ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
                    clearStage.showAndWait();
                }
            });
        }

    }

    boolean ret = false;

    private void updateShadeModality() {
        this.closeAllRemotes();
        this.clearShading_popup();
    }

    protected void exportToNewTab(boolean fromCollapse) {
        try {
            File tempShadey = File.createTempFile("temp_shadey", ".tmp");
            THE_PROGRAM.getShadeyProjectManager().setData_collapseOpts(this, tempShadey);
            THE_PROGRAM.read(tempShadey, FileFormat.slim, this.getText().concat(fromCollapse ? "_collapsed" : ""), false, false);
        } catch (IOException ex) {
            Logger.getLogger(EditorTab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeAllRemotes() {
        if (bsr != null) {
            bsr.hide();
        }
        if (ssr != null) {
            ssr.hide();
        }
        if (rcr != null) {
            rcr.hide();
        }
        if (usr != null) {
            usr.hide();
        }
        if (psr != null) {
            psr.hide();
        }
        if (msr != null) {
            msr.hide();
        }
    }

    public boolean clearShading_popup() {
        ret = false;

        Stage clearStage = new Stage();
        clearStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        clearStage.setTitle("Clear shading?");
        Label clearShadingLabel = new Label("Clear or retain the current shading before shading again?");
        Button yes = new Button("Clear");
        yes.setOnAction(e -> {
            this.vmsa.clearShading();
            ret = true;
            clearStage.close();
        });
        Button no = new Button("Retain");
        no.setOnAction(e -> {
            ret = true;
            clearStage.close();
        });

        VBox clearRoot = new VBox(5);
        clearRoot.setPadding(new Insets(15, 15, 0, 15));
        HBox buttons = new HBox(8);
        buttons.getChildren().addAll(no, yes);

        clearRoot.getChildren().addAll(clearShadingLabel, buttons);

        buttons.setPadding(new Insets(15));

        clearStage.widthProperty().addListener(o -> {
            for (int i = 0; i < buttons.getChildren().size(); i++) {
                if (buttons.getChildren().get(i) instanceof Button) {
                    ((Button) buttons.getChildren().get(i)).setPrefWidth(clearStage.getWidth() / buttons.getChildren().size());
                }
            }
        });

        Scene clearScene = new Scene(clearRoot);
        clearStage.setScene(clearScene);
        clearStage.initModality(Modality.APPLICATION_MODAL);
        clearStage.setAlwaysOnTop(true);
        for (int i = 0; i < buttons.getChildren().size(); i++) {
            if (buttons.getChildren().get(i) instanceof Button) {
                ((Button) buttons.getChildren().get(i)).setPrefWidth(clearStage.getWidth() / buttons.getChildren().size());
            }
        }
        ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
        clearStage.showAndWait();
        return ret;
    }

    public VisualMultipleSequenceAlignment getVMSA() {
        return this.vmsa;
    }

    private void clearShading() {
        this.vmsa.clearShading();
    }

    private final int MAX_NAME_CHARS_DEFAULT = 20;
    private final int CHARS_PER_ROW_STD = 80;
    private final int MIN_FONTSIZE_RTF = 1;
    private final int MAX_FONTSIZE_RTF = 20;
    private final int DEFAULT_FONTSIZE_RTF = 10;//80 chars per row at this size
    private final String PADDING = "                              ";
    private final String SPACER = "  ";

    private HashMap<String, String> hexMap;
    private List<String> dictionaryIndices;

    private void outputToRTF() {
        getParamsRTF();
    }

    private void getParamsRTF() {
        Stage paramsStage = new Stage();
        paramsStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        paramsStage.setTitle("Save .rtf");

        VBox paramsRoot = new VBox(10);
        paramsRoot.setPadding(new Insets(10));

        HBox fontSizeBox = new HBox(5);
        Label fontSizeLabel = new Label("Font size (px): ");
        ComboBox<Integer> fontSizeComboBox = new ComboBox();
        for (int k = MIN_FONTSIZE_RTF; k <= MAX_FONTSIZE_RTF; k++) {
            fontSizeComboBox.getItems().add(k);
        }
        fontSizeComboBox.setValue(DEFAULT_FONTSIZE_RTF);
        fontSizeBox.getChildren().addAll(fontSizeLabel, fontSizeComboBox);

        HBox maxNameCharsBox = new HBox(5);
        Label maxNameCharsLabel = new Label("Max characters in name: ");
        TextField maxNameCharsField = new TextField();
        maxNameCharsField.setMaxWidth(60);
        maxNameCharsField.setPromptText(String.valueOf(MAX_NAME_CHARS_DEFAULT));
        maxNameCharsField.setText(String.valueOf(MAX_NAME_CHARS_DEFAULT));
        maxNameCharsField.textProperty().addListener(event -> {
            boolean empty = maxNameCharsField.getText().isEmpty();
            if ((!empty && !isInteger(maxNameCharsField.getText())) || maxNameCharsField.getText().length() > 2
                    || maxNameCharsField.getText().contains("-")
                    || (!empty && Integer.parseInt(maxNameCharsField.getText()) > MAX_NAME_CHARS_DEFAULT)) {
                maxNameCharsField.setText(maxNameCharsField.getText().substring(0, maxNameCharsField.getLength() - 1));
            }
        });
        maxNameCharsBox.getChildren().addAll(maxNameCharsLabel, maxNameCharsField);

        CheckBox showAnnotations = new CheckBox("Show annotations");
        showAnnotations.setSelected(true);
        CheckBox showNumbering = new CheckBox("Show numbering");
        showNumbering.setSelected(true);

        Button execute = new Button("Save to RTF");
        execute.setOnAction(e -> {
            fontSize_rtf = fontSizeComboBox.getValue();
            if (!maxNameCharsField.getText().isEmpty()) {
                maxNameChars_rtf = Integer.parseInt(maxNameCharsField.getText());
            } else {
                maxNameChars_rtf = MAX_NAME_CHARS_DEFAULT;
            }
            showAnnotations_rtf = showAnnotations.isSelected();
            showNumbering_rtf = showNumbering.isSelected();
            paramsStage.close();
            writeFile(writeLinesRTF());
        });
        HBox executeBox = new HBox();
        executeBox.setPadding(new Insets(10, 10, 0, 10));
        HBox.setHgrow(executeBox, Priority.ALWAYS);
        execute.setMinWidth(150);
        executeBox.setAlignment(Pos.CENTER);
        executeBox.getChildren().add(execute);

        paramsRoot.getChildren().addAll(fontSizeBox, maxNameCharsBox, showAnnotations, showNumbering, executeBox);

        Scene paramsScene = new Scene(paramsRoot);
        paramsStage.setScene(paramsScene);
        paramsStage.initModality(Modality.APPLICATION_MODAL);
        paramsStage.setAlwaysOnTop(true);
        paramsStage.setResizable(false);
        paramsStage.showAndWait();

    }

    private int fontSize_rtf = DEFAULT_FONTSIZE_RTF;
    private int maxNameChars_rtf = MAX_NAME_CHARS_DEFAULT;
    private boolean showAnnotations_rtf = false;
    private boolean showNumbering_rtf = false;

    private void writeFile(List<String> lines) {
        FileChooser fileChooserSave = new FileChooser();
        fileChooserSave.setTitle("Save alignment in FASTA format");
        FileChooser.ExtensionFilter extFilterSave = new FileChooser.ExtensionFilter("rich text format (*.rtf)", "*.rtf");
        fileChooserSave.setInitialFileName(this.getText());
        fileChooserSave.getExtensionFilters().add(extFilterSave);
        File fileSave = fileChooserSave.showSaveDialog(this.THE_PROGRAM.getStage());
        if (fileSave != null) {
            try {
                OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(fileSave), "UTF-8");
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                for (String s : lines) {
                    bufferedWriter.write(s);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private List<String> writeLinesRTF() {
        // popup window here to get params
        // we want fontsize, and include/exclude consensus

        int totalCharsPerRow = (int) (((double) fontSize_rtf) / ((double) DEFAULT_FONTSIZE_RTF) * ((double) CHARS_PER_ROW_STD));
        int charsPerRow = totalCharsPerRow - 30;

        /*with default margins, at fontsize 1<=F<=20, */
        List<String> ret = new ArrayList<>();
        ret.add("{\\rtf1\\ansi\\deff0");
        ret.add("{\\fonttbl{\\f0\\fmodern Courier New;}}");
        ret.add("{\\info{\\author SlimShadey}}");
        ret.add("{\\colortbl");
        //format - "\red0\green0\blue0;"
        hexMap = new HashMap<>();
        dictionaryIndices = new ArrayList<>();
        buildColorLibrary(hexMap, dictionaryIndices);
        dictionaryIndices.forEach((s) -> {
            //System.out.println(dictionaryIndices.indexOf(s) + " " + s);
            ret.add(s);
        });

        //add color dictionary
        String inset = "650"; //px?
        ret.add("}"); //end color dictionary
        ret.add("\\paperw11880\\paperh16820\\margl" + inset + "\\margr" + inset);
        ret.add("\\margt" + inset + "\\margb" + inset + "\\sectd\\cols1\\pard\\plain");
        ret.add("\\fs".concat(String.valueOf(fontSize_rtf * 2)));

        //add alignment
        //get max row_header length
        int maxNameChars = 0;
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            String name = vmsa.getAnnotationName(k);
            maxNameChars = Math.min(name.length(), maxNameChars_rtf);
        }
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            String name = vmsa.getSequenceName(k);
            maxNameChars = Math.min(name.length(), maxNameChars_rtf);
        }

        HashMap<Integer, String> annoIndexToPrintStr = new HashMap<>();
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            String name = vmsa.getAnnotationName(k);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < maxNameChars; j++) {
                try {
                    sb.append(name.charAt(j));
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
        int setsOfRows = (int) Math.round(Math.ceil(((double) vmsa.sequenceLength()) / ((double) charsPerRow)));
        System.out.println(setsOfRows);
        HashMap<Integer, List<List<VisualBioChar>>> annoOrganized = new HashMap<>();
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            annoOrganized.put(k, new ArrayList<List<VisualBioChar>>());
            organizeSeq(vmsa.getAnnotationRow(k), charsPerRow, annoOrganized.get(k));
            System.out.println(annoOrganized.get(k).size());
        }
        HashMap<Integer, List<List<VisualBioChar>>> seqsOrganized = new HashMap<>();
        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            seqsOrganized.put(k, new ArrayList<List<VisualBioChar>>());
            organizeSeq(vmsa.getSequence(k), charsPerRow, seqsOrganized.get(k));
            System.out.println(seqsOrganized.get(k).size());
        }
        List<List<VisualBioChar>> consOrganized = new ArrayList<>();
        organizeSeq(vmsa.getConsensusRow(), charsPerRow, consOrganized);
        System.out.println(consOrganized.size());

        organizeSeq(vmsa.getConsensusRow(), charsPerRow, consOrganized);

        //build document row by row
        int trueIndex = vmsa.getFirstIndex();
        for (int k = 0; k < setsOfRows; k++) {
            if (showAnnotations_rtf) {
                for (int j = 0; j < vmsa.annotationNumber(); j++) { //annotations up top
                    getRow(getRowHeader(annoIndexToPrintStr.get(j), trueIndex), annoOrganized.get(j).get(k), ret);
                }
            }
            for (int j = 0; j < vmsa.sequenceNumber(); j++) { //sequences next
                getRow(getRowHeader(seqsIndexToPrintStr.get(j), trueIndex), seqsOrganized.get(j).get(k), ret);
            }
            getRow(getRowHeader(consensusPrintStr, trueIndex), consOrganized.get(k), ret);
            ret.add("\\highlight" + String.valueOf(WHITE_INDEX_RTF)
                    + "\\cf" + String.valueOf(BLACK_INDEX_RTF) + " \\line");
            trueIndex += charsPerRow;
        }

        ret.add("}"); //close document

        System.out.println();
        for (String s : ret) {
            //System.out.println(s);
        }

        return ret;
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

    private final int MAX_INDEX_CHARS = 6;

    private String getRowHeader(String name, int index) {
        String indexstr = String.valueOf(index);
        List<Character> indexchars = new ArrayList<>();
        if (showNumbering_rtf) {
            for (int k = 0; k < MAX_INDEX_CHARS; k++) {
                try {
                    indexchars.add(indexstr.charAt(indexstr.length() - 1 - k));
                } catch (StringIndexOutOfBoundsException sioobe) {
                    indexchars.add(' ');
                }
            }
        }

        StringBuilder indexsb = new StringBuilder();
        for (int j = indexchars.size() - 1; j >= 0; j--) {
            indexsb.append(indexchars.get(j));
        }

        return name.concat(SPACER).concat(indexsb.toString()).concat(SPACER);
    }

    private void getRow(String rowHeader, List<VisualBioChar> row, List<String> toAdd) {
        String ret = "";
        /* //check code
        StringBuilder sb = new StringBuilder(rowHeader);
        for (VisualBioChar vbc : row) {
            sb.append(vbc.getChar());
        }
        System.out.println(sb.toString());
         */
        String rowHeaderToAdd = "\\highlight" + String.valueOf(WHITE_INDEX_RTF)
                + "\\cf" + String.valueOf(BLACK_INDEX_RTF) + " " + rowHeader;
        toAdd.add(rowHeaderToAdd);

        for (VisualBioChar vbc : row) {
            String charFieldToAdd = "\\highlight" + String.valueOf(dictionaryIndices.indexOf(hexMap.get(vbc.getBackHex())))
                    + "\\cf" + String.valueOf(dictionaryIndices.indexOf(hexMap.get(vbc.getForeHex())))
                    + " " + String.valueOf(vbc.getChar());
            toAdd.add(charFieldToAdd);
        }
        toAdd.add("\\highlight" + String.valueOf(WHITE_INDEX_RTF)
                + "\\cf" + String.valueOf(BLACK_INDEX_RTF) + " \\line");
    }

    private int WHITE_INDEX_RTF = 0;
    private int BLACK_INDEX_RTF = 1;

    private void buildColorLibrary(HashMap<String, String> hexMap, List<String> dictionaryIndices) {
        String whitehex = "#FFFFFF";
        String whitergb = this.hexToRGB_rtf(whitehex);
        hexMap.put(whitehex, whitergb);
        dictionaryIndices.add(whitergb);
        WHITE_INDEX_RTF = dictionaryIndices.indexOf(whitergb);
        String blackhex = "#000000";
        String blackrgb = this.hexToRGB_rtf(blackhex);
        hexMap.put(blackhex, blackrgb);
        dictionaryIndices.add(blackrgb);
        BLACK_INDEX_RTF = dictionaryIndices.indexOf(blackrgb);

        for (int k = 0; k < vmsa.sequenceLength(); k++) {
            for (int j = 0; j < vmsa.annotationNumber(); j++) {
                VisualBioChar vbc = vmsa.getAnnoVBC(j, k);
                String backhex = vbc.getBackHex();
                if (hexMap.getOrDefault(backhex, null) == null) {
                    String backrgb = hexToRGB_rtf(backhex);
                    hexMap.put(backhex, backrgb);
                    dictionaryIndices.add(backrgb);
                }
                String forehex = vbc.getForeHex();
                if (hexMap.getOrDefault(forehex, null) == null) {
                    String forergb = hexToRGB_rtf(forehex);
                    hexMap.put(forehex, forergb);
                    dictionaryIndices.add(forergb);
                }
            }
            for (int j = 0; j < vmsa.sequenceNumber(); j++) {
                VisualBioChar vbc = vmsa.getVBC(j, k);
                String backhex = vbc.getBackHex();
                if (hexMap.getOrDefault(backhex, null) == null) {
                    String backrgb = hexToRGB_rtf(backhex);
                    hexMap.put(backhex, backrgb);
                    dictionaryIndices.add(backrgb);
                }
                String forehex = vbc.getForeHex();
                if (hexMap.getOrDefault(forehex, null) == null) {
                    String forergb = hexToRGB_rtf(forehex);
                    hexMap.put(forehex, forergb);
                    dictionaryIndices.add(forergb);
                }
            }
            VisualBioChar vbc = vmsa.getConsensusVBC(k);
            String backhex = vbc.getBackHex();
            if (hexMap.getOrDefault(backhex, null) == null) {
                String backrgb = hexToRGB_rtf(backhex);
                hexMap.put(backhex, backrgb);
                dictionaryIndices.add(backrgb);
            }
            String forehex = vbc.getForeHex();
            if (hexMap.getOrDefault(forehex, null) == null) {
                String forergb = hexToRGB_rtf(forehex);
                hexMap.put(forehex, forergb);
                dictionaryIndices.add(forergb);
            }
        }
    }

    private String hexToRGB_rtf(String hex) {
        int[] rgb = new int[]{(Integer.valueOf(hex.substring(1, 3), 16)),
            (Integer.valueOf(hex.substring(3, 5), 16)),
            (Integer.valueOf(hex.substring(5, 7), 16))};
        System.out.println(hex + " " + rgb[0] + " " + rgb[1] + " " + rgb[2] + " ");
        return "\\red".concat(String.valueOf(rgb[0]))
                .concat("\\green").concat(String.valueOf(rgb[1]))
                .concat("\\blue").concat(String.valueOf(rgb[2]))
                .concat(";");
    }

}
