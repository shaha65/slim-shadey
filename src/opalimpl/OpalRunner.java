/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opalimpl;

import edu.tcnj.biology.seqverter.converter.FileFormat;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import opal.IO.CostMatrix;
import opal.Opal;
import opal.exceptions.GenericOpalException;
import translation.GeneticCode;
import translation.OptimizedTranslator;
import translation.defaultcodes.DefaultCodes;

/**
 *
 * @author Avi Shah
 */
public class OpalRunner {

    private Stage stage;
    private Scene scene;

    private VBox root;
    //citation for Opal
    private static final String CITESTR = "Please cite: "
            + "Wheeler, T.J. and Kececioglu, J.D. "
            + "Proceedings of the 15th ISCB Conference on Intelligent "
            + "Systems for Molecular Biology, Bioinformatics 23, i559-i568, 2007.";
    private Text cite = new Text(CITESTR);
    private TextFlow citeFlow = new TextFlow(cite);
    private VBox citeBox = new VBox();

    private File tempOutputFile;

    private EditorInterface origin;

    protected OpalRunner(File in, Stage priorToClose, EditorInterface origin) {
        if (priorToClose != null) {
            priorToClose.close();
        }
        this.origin = origin;
        this.initialize(in);
    }

    private String absFilePath = "";

    private void initialize(File f) {
        absFilePath = f.getAbsolutePath();
        this.outputWindows = new ArrayList<>();
        this.checkLSEP(f);
    }

    public static final String DEFAULT_MATRIX_NAME = "BLOSUM62";

    //<editor-fold defaultstate="collapsed" desc="vars">
    private String alignMethod;
    private String polishAlignMethod;
    private String polishMethod;
    int protGamma, protGammaTerm, protLambda, protLambdaTerm;
    int dnaGamma, dnaGammaTerm, dnaLambda, dnaLambdaTerm;
    private int dnaSubAG, dnaSubCT, dnaTransversion;
    private String protCostName = DEFAULT_MATRIX_NAME;
    private String extraArguments = "";

    private HBox inputBox;
    private Label currentFileLabel = new Label("Current file: ");
    private Label currentFile = new Label();
    private Button openFile = new Button("Different file");
//</editor-fold>
    //common UI components
    //<editor-fold defaultstate="collapsed" desc="common">
    private HBox costs;
    private Label costsLabel = new Label("Gap costs: ");
    private Label gammaLabel = new Label("Open"),
            gammaTermLabel = new Label("Terminal Open"),
            lambdaLabel = new Label("Extension"),
            lambdaTermLabel = new Label("Terminal Extension");
    private TextField gammaField = new TextField(),
            gammaTermField = new TextField(),
            lambdaField = new TextField(),
            lambdaTermField = new TextField();
    private Label[] costLabels = new Label[]{gammaLabel, gammaTermLabel, lambdaLabel, lambdaTermLabel};
    private TextField[] costFields = new TextField[]{gammaField, gammaTermField, lambdaField, lambdaTermField};
    //query for property

    private HBox methods;

    private HBox logBox;
    private Button showLog;
    private CheckBox showLogOnRun = new CheckBox("Show log on Run");
    private Button runOpal;

    private HBox alignmentMethodsBox;
    private Label alignmentMethodsLabel = new Label("Alignment method: ");
    private RadioButton profile = new RadioButton("profile (fastest)");
    private RadioButton mixed = new RadioButton("mixed");
    private RadioButton exact = new RadioButton("exact");
    private ToggleGroup alignmentMethodsGroup; //query for property

    private HBox polishAlignmentMethodsBox;
    private Label polishAlignmentMethodsLabel = new Label("Polish alignment method: ");
    private RadioButton profile_polish = new RadioButton("profile");
    private RadioButton mixed_polish = new RadioButton("mixed");
    private RadioButton exact_polish = new RadioButton("exact");
    private ToggleGroup polishAlignmentMethodsGroup; //query for property

    private HBox polishMethodsBox;
    private Label polishMethodsLabel = new Label("Polish method: ");
    private RadioButton exhaust_twocut = new RadioButton("exhaust_twocut");
    private RadioButton exhaust_threecut = new RadioButton("exhaust_threecut");
    private RadioButton random_tree_twocut = new RadioButton("random_tree_twocut");
    private RadioButton random_twocut = new RadioButton("random_twocut");
    private RadioButton random_threecut = new RadioButton("random_threecut");
    private RadioButton none = new RadioButton("none");
    private ToggleGroup polishMethodsGroup; //query for property

    private HBox additionalCommandsBox;
    private Label additionalCommandsLabel = new Label("Additional commands: ");
    private TextField additionalCommands; //query for commands
//</editor-fold>
    //nucleotide specific
    //<editor-fold defaultstate="collapsed" desc="nuc">
    private HBox nucleotideBox;
    private Label dnaSubAGLabel,
            dnaSubCTLabel //,dnaTransversionLabel
            ;
    private TextField dnaSubAGField,
            dnaSubCTField //,dnaTransversionField
            ; //query for property

    private VBox nucleotideAlignmentTypeBox;
    private RadioButton doNucleotideAlignment;
    private RadioButton doTranslatedAlignment;
    private RadioButton doCodonDelimited;
    private ToggleGroup nucleotideAlignmentGroup;
    private HBox geneticCodeComboBoxHolder;
    private Label geneticCodeLabel;
    private Label translateWarning;
    private ComboBox<GeneticCode> geneticCodeComboBox;
//</editor-fold>
    //protein specific
    //<editor-fold defaultstate="collapsed" desc="pro">
    private HBox proteinCostBox;
    private Label proteinCostLabel = new Label("Protein cost matrix: ");
    private ComboBox<String> proteinCostMatrixBox;
//</editor-fold>

    private void initializeUI(File f) {

        root = new VBox(15);
        root.setPadding(new Insets(15, 15, 15, 15));

        inputBox = new HBox(15);
        currentFile.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        inputBox.getChildren().addAll(style_component(currentFileLabel), style_component(currentFile), style_component(openFile));
        openFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load sequences to Align (Opal)");
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                this.loggerWindow.displayLogger(false);
                new OpalRunner(selectedFile, this.stage, origin);
            }
        });
        root.getChildren().add(inputBox);

        alignmentMethodsBox = new HBox(15);
        alignmentMethodsBox.getChildren().add(style_component(alignmentMethodsLabel));
        this.alignmentMethodsGroup = this.bindToggles(alignmentMethodsBox, 3,
                profile,
                mixed,
                exact);
        polishAlignmentMethodsBox = new HBox(15);
        polishAlignmentMethodsBox.getChildren().add(style_component(polishAlignmentMethodsLabel));
        this.polishAlignmentMethodsGroup = this.bindToggles(polishAlignmentMethodsBox, 3,
                profile_polish,
                mixed_polish,
                exact_polish);
        methods = new HBox(25);
        methods.getChildren().addAll(alignmentMethodsBox, polishAlignmentMethodsBox);
        root.getChildren().add(methods);

        polishMethodsBox = new HBox(15);
        polishMethodsBox.getChildren().add(style_component(polishMethodsLabel));
        this.polishMethodsGroup = this.bindToggles(polishMethodsBox, 3,
                exhaust_twocut,
                exhaust_threecut,
                random_tree_twocut,
                random_twocut,
                random_threecut,
                none);
        root.getChildren().add(polishMethodsBox);

        costs = new HBox();
        costsLabel.setPadding(new Insets(0, 15, 0, 0));
        costs.getChildren().add(style_component(costsLabel));
        costFields = this.integerField(costFields);
        for (int i = 0; i < costLabels.length / 2; i++) {
            VBox add = new VBox(15);
            VBox add1 = new VBox();
            VBox add2 = new VBox();
            add1.getChildren().addAll(style_component(costLabels[i]), style_component(costFields[i]));
            add2.getChildren().addAll(style_component(costLabels[i + costLabels.length / 2]), style_component(costFields[i + costLabels.length / 2]));
            add.getChildren().addAll(add1, add2);
            costs.getChildren().add(add);
        }
        root.getChildren().add(costs);

        proteinCostMatrixBox = new ComboBox<String>();
        proteinCostMatrices.forEach((String s) -> proteinCostMatrixBox.getItems().add(s));
        proteinCostBox = new HBox(15);
        proteinCostBox.getChildren().addAll(style_component(proteinCostLabel), style_component(proteinCostMatrixBox));
        proteinCostMatrixBox.setValue(proteinCostMatrixBox.getItems().get(0));
        if (this._type == DataType.PRO) {
            root.getChildren().add(proteinCostBox);
        } else if (this._type == DataType.NUC) {
            dnaSubAGLabel = new Label("A ↔ G");
            dnaSubCTLabel = new Label("C ↔ T");
            //dnaTransversionLabel = new Label();
            dnaSubAGField = new TextField();
            dnaSubCTField = new TextField();
            //dnaTransversionField = new TextField();
            nucleotideBox = new HBox();
            TextField[] tfs = new TextField[]{dnaSubAGField, dnaSubCTField
        //, dnaTransversionField
            };
            Label[] labels = new Label[]{dnaSubAGLabel, dnaSubCTLabel
        //, dnaTransversionLabel
            };
            tfs = integerField(tfs);
            for (int i = 0; i < labels.length; i++) {
                VBox add = new VBox();
                add.getChildren().addAll(this.style_component(labels[i]), this.style_component(tfs[i]));
                nucleotideBox.getChildren().add(add);
            }

            nucleotideAlignmentTypeBox = new VBox(5);
            doNucleotideAlignment = new RadioButton("Standard ncleotide alignment");
            doNucleotideAlignment.setSelected(true);
            doTranslatedAlignment = new RadioButton("Translate and align");
            doTranslatedAlignment.setSelected(false);
            doCodonDelimited = new RadioButton("Codon delimited nucleotide alignment");
            doCodonDelimited.setSelected(false);
            nucleotideAlignmentGroup = new ToggleGroup();
            nucleotideAlignmentGroup.getToggles().addAll(doNucleotideAlignment,
                    doTranslatedAlignment, doCodonDelimited);

            geneticCodeLabel = new Label("Genetic code: ");
            geneticCodeComboBox = new ComboBox<>();

            geneticCodeComboBox.getItems().addAll(DefaultCodes.getDefaultCodes());
            geneticCodeComboBox.disableProperty().set(doNucleotideAlignment.isSelected());
            geneticCodeComboBox.setValue(geneticCodeComboBox.getItems().get(0));

            proteinCostMatrixBox.disableProperty().set(doNucleotideAlignment.isSelected());
            translateWarning = new Label("Translation assumes +1 reading frame and replaces stop codons \"*\" with \"X\"");
            translateWarning.disableProperty().set(doNucleotideAlignment.isSelected());
            nucleotideAlignmentGroup.selectedToggleProperty().addListener(e -> {
                boolean setbool = doNucleotideAlignment.isSelected();
                geneticCodeComboBox.disableProperty().set(setbool);
                proteinCostMatrixBox.disableProperty().set(setbool);
                translateWarning.disableProperty().set(setbool);
                if (setbool) {
                    this.setDefaultSettingsUI(DataType.NUC);
                } else {
                    this.setDefaultSettingsUI(DataType.PRO);
                }
            });
            geneticCodeComboBoxHolder = new HBox(10);
            geneticCodeComboBoxHolder.getChildren().addAll(style_component(geneticCodeLabel),
                    style_component(geneticCodeComboBox));

            nucleotideAlignmentTypeBox.getChildren().addAll(style_component(doNucleotideAlignment),
                    style_component(doTranslatedAlignment), style_component(doCodonDelimited),
                    geneticCodeComboBoxHolder, proteinCostBox, translateWarning);

            root.getChildren().addAll(nucleotideBox, nucleotideAlignmentTypeBox);

            //this.init_nuc_UI(nucleotideBox, lbs, tfs, doCodonDelimited);
        }

        additionalCommandsBox = new HBox(15);
        additionalCommands = new TextField();
        additionalCommands.setPromptText("Additional commands...");
        additionalCommandsBox.getChildren().addAll(this.style_component(additionalCommandsLabel),
                this.style_component(additionalCommands));
        root.getChildren().add(additionalCommandsBox);

        logBox = new HBox(15);
        showLog = new Button("Show log");
        showLog.setOnAction(e -> {
            this.loggerWindow.displayLogger(DISPLAY_LOGGER);
        });
        logBox.getChildren().addAll(style_component(showLog), style_component(showLogOnRun));

        runOpal = new Button("Run Opal");

        root.getChildren().addAll(logBox, style_component(runOpal));

        scene = new Scene(root);
        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setScene(scene);
        stage.setTitle("OpalFX - ".concat(f.getName()));
        stage.show();
        runOpal.requestFocus();
        this.setDefaultSettingsUI(this._type);
        this.forceResize();
        stage.setOnCloseRequest(e -> {
            this.loggerWindow.displayLogger(false);
            this.stage.close();
        });

        new InformationPopup(alignmentMethodsLabel, "", __align_method, __align_method_desc);
        new InformationPopup(polishAlignmentMethodsLabel, "", __polish_align_method, __polish_align_method_desc);
        new InformationPopup(polishMethodsLabel, "", __polish_method, __polish_method_desc);

        new InformationPopup(gammaLabel, "", __gamma, __gamma_desc);
        new InformationPopup(gammaTermLabel, "", __gamma_term, __gamma_term_desc);
        new InformationPopup(lambdaLabel, "", __lambda, __lambda_desc);
        new InformationPopup(lambdaTermLabel, "", __lambda_term, __lambda_term_desc);

        this.setCiteBehavior();
    }

    private List<String> prepareSettings(File selectedFile) {
        List<String> ret = new ArrayList<String>();

        if (this._type == DataType.NUC) {
            if (doTranslatedAlignment.isSelected()) {
                selectedFile = translate(selectedFile);
            } else if (doCodonDelimited.isSelected()) {
                selectedFile = translate(selectedFile);
            } else {

            }
        }

        String inputFile = " --in ".concat(selectedFile.getAbsolutePath());
        ret.add(inputFile);

        String outFile = "";
        ret.add(outFile);

        try {
            this.tempOutputFile = File.createTempFile("temp_alignment", ".tmp");
            tempOutputFile.deleteOnExit();
            System.out.println(tempOutputFile.canWrite() + " " + tempOutputFile.getFreeSpace() + " " + tempOutputFile.getName());
            outFile = " --out ".concat(tempOutputFile.getAbsolutePath());
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        ret.add(outFile);

        String alignMethod = " --align_method ".concat(alignmentMethodsGroup.getSelectedToggle().getUserData().toString());
        ret.add(alignMethod);

        String polishAlignMethod = " --polish_align_method ".concat(polishAlignmentMethodsGroup.getSelectedToggle().getUserData().toString());
        ret.add(polishAlignMethod);

        String polishMethod = " --polish ".concat(polishMethodsGroup.getSelectedToggle().getUserData().toString());
        ret.add(polishMethod);

        String gamma = " --gamma ".concat(gammaField.getText());
        ret.add(gamma);

        String gammaTerm = " --gamma_term ".concat(gammaTermField.getText());
        ret.add(gammaTerm);

        String lambda = " --lambda ".concat(lambdaField.getText());
        ret.add(lambda);

        String lambdaTerm = " --lambda_term ".concat(lambdaTermField.getText());
        ret.add(lambdaTerm);

        if (_type == DataType.NUC) {
            if (doNucleotideAlignment.isSelected()) {
                CostMatrix.dnaSubAG = Integer.parseInt(dnaSubAGField.getText());
                CostMatrix.dnaSubCT = Integer.parseInt(dnaSubCTField.getText());
            } else if (doTranslatedAlignment.isSelected()) {
                String cost = " --cost ".concat(proteinCostMatrixBox.getValue());
                ret.add(cost);
            } else if (doCodonDelimited.isSelected()) {
                String cost = " --cost ".concat(proteinCostMatrixBox.getValue());
                ret.add(cost);
            }
            //dna transversion !!!!!!!!!!!!!!!!!!!!!!!!!!1
        } else if (_type == DataType.PRO) {
            String cost = " --cost ".concat(proteinCostMatrixBox.getValue());
            ret.add(cost);
        }

        ret.add(" ".concat(additionalCommands.getText()));

        return ret;
    }

    private ToggleGroup bindToggles(HBox toadd, int percol, RadioButton... toggles) {
        ToggleGroup tg = new ToggleGroup();
        int iter = 0;
        VBox add = null;
        for (RadioButton toggle : toggles) {
            if (iter % percol == 0) {
                add = new VBox(8);
            }
            add.getChildren().add(this.style_component(toggle));

            toggle.setMnemonicParsing(false);
            toggle.setUserData(
                    toggle.getText().split("\\s+")[0]);
            tg.getToggles().add(toggle);

            if (iter % percol == (percol - 1)) {
                toadd.getChildren().add(add);
                add = null;
            }
            iter++;
        }
        return tg;
    }

    private final static int MAXCHAR_TF = 3; //max allowable digits in any TextField that accepts numbers only

    private TextField[] integerField(TextField... tfs) {
        for (TextField tf : tfs) {
            tf.textProperty().addListener(e -> {
                if (tf.getText().length() > MAXCHAR_TF || !isNumeric(tf.getText())) {
                    tf.setText(tf.getText().substring(0, tf.getText().length() - 1));
                }
            });
        }
        return tfs;
    }

    private static final String NODE_STYLE
            = " -fx-font: 12 Arial;"
            + " -fx-font-weight: bold;";

    private Node style_component(Node n) {
        n.setStyle(NODE_STYLE);
        return n;
    }

    private final static boolean CLEAR_WINDOW_ON_SETFILE = true;
    private final static boolean DISPLAY_LOGGER = true;

    private OpalFXLogger loggerWindow;

    private void setFile(File f) {
        this.runOpal.setOnAction(e -> {
            runOpal(f);

        });
        this.fileDone.addListener(e -> {
            if (fileDone.get()) {
                this.output(tempOutputFile);
            }
        });

    }

    private void runOpal(File selectedFile) {
        this.loggerWindow.displayLogger(showLogOnRun.isSelected());
        this.fileDone.set(false);
        List<String> commands = this.prepareSettings(selectedFile);
        this.loggerWindow.reset(selectedFile.getName(), CLEAR_WINDOW_ON_SETFILE);
        String[] argv = new String[1];
        //argv[0] = " --verbose ";
        argv[0] = "";
        commands.forEach((command) -> argv[0] = argv[0].concat(command));

        PrintStream stdOut = System.out;
        PrintStream stdErr = System.err;
        PrintStream ps = new PrintStream(this.loggerWindow.taos, true);
        this.resetPrintStreams(ps, ps);

        OpalRunner this_runner = this;
        Task task = new Task<Void>() {
            @Override
            public Void call() {
                System.out.println(argv[0]);
                opal.Opal.main(argv, this_runner);
                //output(tempOutputFile);
                resetPrintStreams(stdOut, stdErr);
                return null;
            }
        };
        new Thread(task).start();
    }

    private static void resetPrintStreams(PrintStream stdOut, PrintStream stdErr) {
        System.setOut(stdOut);
        System.setErr(stdErr);
    }

    private void forceResize() {
        //needed to have proper filepath sizing
        currentFileLabel.setMinWidth(currentFileLabel.getWidth());
        openFile.setMinWidth(openFile.getWidth());
        currentFile.setText(absFilePath);

        additionalCommandsLabel.setMinWidth(additionalCommandsLabel.getWidth() + 0);
        additionalCommands.prefWidthProperty().bind(stage.widthProperty());
        runOpal.prefWidthProperty().bind(stage.widthProperty());

        showLogOnRun.setMinWidth(showLogOnRun.getWidth());
        showLog.prefWidthProperty().bind(stage.widthProperty());

        double prevmax = root.getMaxWidth();
        root.setMaxWidth(root.getWidth());
        citeBox.getChildren().add(citeFlow);
        root.getChildren().addAll(citeBox /*, new Label("OpalFX prelim build")*/);
        stage.sizeToScene();
        stage.setHeight(stage.getHeight() + 10);
        root.setMaxWidth(prevmax);

    }

    private List<String> outputData;
    private List<Stage> outputWindows;

    private GeneticCode gcodeCurrent = null;
    private SequenceHolder rawNucSH = null;

    private File translate(File in) {
        File retfile = null;
        gcodeCurrent = this.geneticCodeComboBox.getValue();
        try {
            retfile = File.createTempFile("translated", ".tmp");

            SequenceHolder toTranslate = this.readFASTA(in);
            rawNucSH = toTranslate;
            SequenceHolder translated = new SequenceHolder();
            for (Sequence s : toTranslate) {
                Sequence toAdd = new Sequence();
                toAdd.setName(s.name());
                toAdd.setSequence(OptimizedTranslator.translate(s.seqString(), 1, gcodeCurrent).replaceAll("\\*", "X"));
                translated.add(toAdd);
            }

            writeFASTA(retfile, translated);
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retfile;
    }

    private File unpackCodonDelimitedAlignment(File in) {
        File retfile = null;
        gcodeCurrent = this.geneticCodeComboBox.getValue();
        try {
            retfile = File.createTempFile("translated", ".tmp");

            SequenceHolder toUnpack = this.readFASTA(in);
            SequenceHolder unpacked = new SequenceHolder();
            for (Sequence s : toUnpack) {
                Sequence rawNuc = new Sequence();
                for (Sequence seq : rawNucSH) {
                    if (seq.name().equals(s.name())) {
                        rawNuc = seq;
                        break;
                    }
                }
                Sequence toAdd = new Sequence();
                toAdd.setName(s.name());
                toAdd.setSequence(OptimizedTranslator.backTranslateToDNA(s.seqString(), rawNuc.seqString()));
                unpacked.add(toAdd);
            }

            writeFASTA(retfile, unpacked);
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retfile;
    }

    private File outFile;

    private void output(File f) {

        if (this._type == DataType.NUC && doCodonDelimited.isSelected()) {
            f = unpackCodonDelimitedAlignment(f);
        }

        outFile = f;

        Stage outputWindow = new Stage();
outputWindow.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        outputWindow.setTitle("Aligned FASTA");
        MenuBar mb = new MenuBar();
        Menu filem = new Menu("File");
        mb.getMenus().add(filem);
        MenuItem save = new MenuItem("Save alignment in FASTA format");

        save.setOnAction(e -> {
            FileWriter out = null;

            FileChooser fileChooserSave = new FileChooser();
            fileChooserSave.setTitle("Save alignment in FASTA format");
            FileChooser.ExtensionFilter extFilterSave = new FileChooser.ExtensionFilter("FASTA aligned (*.txt)", "*.txt");
            fileChooserSave.getExtensionFilters().add(extFilterSave);
            File fileSave = fileChooserSave.showSaveDialog(outputWindow);
            if (fileSave != null) {
                try {
                    out = new FileWriter(fileSave);
                    //Files.copy(f.toPath(), fileSave.toPath());

                    for (String linex : outputData) {
                        out.write(linex);
                    }
                    out.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        MenuItem exportToShadey = new MenuItem("Export to SlimShadey");

        exportToShadey.setOnAction(e -> {
            outputWindow.close();
            stage.close();
            this.getConsole().kill();
            /*
            SequenceHolder sh = origin.read(outFile, FileFormat.fasta);
            origin.newTab(sh, (outFile.getName().length() < 21) ?
                    outFile.getName().concat("_Opal").concat(String.valueOf(origin.getOpalCount())) 
                            : "OpalFX_al_".concat(String.valueOf(origin.getOpalCount())));
            */
            // read now accessed directly for new tabs
            origin.read(outFile, FileFormat.fasta, (outFile.getName().length() < 21) ?
                    outFile.getName().concat("_Opal").concat(String.valueOf(origin.getOpalCount())) 
                            : "OpalFX_al_".concat(String.valueOf(origin.getOpalCount())),
                    true, true);
        });
        filem.getItems().addAll(save, exportToShadey);
        VBox rt = new VBox();
        TextArea ta = new TextArea();
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setStyle(" -fx-font: 14 Monospace; -fx-font-weight: bold;");
        rt.getChildren().addAll(mb, ta);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            outputData = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.concat(System.lineSeparator());
                outputData.add(line);
                ta.appendText(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        Scene sc = new Scene(rt);
        outputWindow.setScene(sc);
        outputWindow.show();
        outputWindow.sizeToScene();
        VBox.setVgrow(ta, Priority.ALWAYS);
        HBox.setHgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(rt, Priority.ALWAYS);
        HBox.setHgrow(rt, Priority.ALWAYS);
        outputWindow.setWidth(840);
        outputWindow.setHeight(600);

        mb.prefWidthProperty().bind(outputWindow.widthProperty());
        this.outputWindows.add(outputWindow);
    }

    private void getDefaultSettings() {

        alignMethod = "profile"; //default
        //polish = true;
        protGamma = CostMatrix.protDefaultGamma;
        protGammaTerm = CostMatrix.protDefaultGammaTerm;
        protLambda = CostMatrix.protDefaultLambda;
        protLambdaTerm = CostMatrix.protDefaultLambdaTerm;
        dnaGamma = CostMatrix.dnaDefaultGamma;
        dnaGammaTerm = CostMatrix.dnaDefaultGammaTerm;
        dnaLambda = CostMatrix.dnaDefaultLambda;
        dnaLambdaTerm = CostMatrix.dnaDefaultLambdaTerm;
        protCostName = "BLOSUM62";
        dnaSubAG = CostMatrix.dnaSubAG;
        dnaSubCT = CostMatrix.dnaSubCT;
        dnaTransversion = 100;
    }

    private void setDefaultSettingsUI(DataType dt) {
        //this.getDefaultSettings();
        //currentFileLabel.setText(currentFileLabel.getText().concat(f.getAbsolutePath()));

        profile.setSelected(true);
        profile_polish.setSelected(true);
        random_tree_twocut.setSelected(true);

        showLogOnRun.setSelected(DISPLAY_LOGGER);

        if (dt == DataType.NUC) {
            gammaField.setText(String.valueOf(dnaGamma));
            gammaTermField.setText(String.valueOf(dnaGammaTerm));
            lambdaField.setText(String.valueOf(dnaLambda));
            lambdaTermField.setText(String.valueOf(dnaLambdaTerm));

            this.dnaSubAGField.setText(String.valueOf(dnaSubAG));
            this.dnaSubCTField.setText(String.valueOf(dnaSubCT));
            //this.dnaTransversionField.setText(String.valueOf(dnaTransversion));
        } else if (dt == DataType.PRO) {
            gammaField.setText(String.valueOf(protGamma));
            gammaTermField.setText(String.valueOf(protGammaTerm));
            lambdaField.setText(String.valueOf(protLambda));
            lambdaTermField.setText(String.valueOf(protLambdaTerm));

            proteinCostMatrixBox.setValue(this.protCostName);
        } else {

        }

    }

    List<String> proteinCostMatrices;

    private void initializeResources() {
        this.getDefaultSettings();

        proteinCostMatrices = new ArrayList<>();

        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(OpalRunner.class.getResourceAsStream("ProteinCostMatrices")));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                proteinCostMatrices.add(line);
                System.out.println("OPALRUNNER - new matrix name loaded: " + line);
            }

        } catch (IOException e) {
            //what happened?
            proteinCostMatrices.add("BLOSUM62");
            e.printStackTrace();
        }
    }

    char[] NUCLEOTIDE = "ACGTUKMRYSWBVHDNX-".toCharArray();
    char[] PROTEIN = "ARNDCQEGHILKMFPSTWYVBZX-".toCharArray();

    private DataType _type = null;

    public OpalFXLogger getConsole() {
        return this.loggerWindow;
    }

    protected enum DataType {
        NUC("nucleotide"), PRO("protein");

        private String name;

        DataType(String s) {
            this.name = s;
        }

        String str_name() {
            return name;
        }
    }

    boolean isNucleotide = false;
    boolean isProtein = false;

    //<editor-fold defaultstate="collapsed" desc="The curse of LSEP (\u2028)">
    /*
    The LSEP unicode character is fatal to Opal; a first pass that replaces these
    characters with standard line separators is an appropriate fix. NOTE:
    the user's actual input file remains unmodified, and a temporary file is
    created and written to. While writing, LSEP characters are detected and replaced
    by the standard \n line separator (originally System.lineSeparator();)
    
    The LSEP character should NOT be used in FASTA files
    
    Opal will also exit Java fatally if the path to the the input file has any
    spaces. To deal with this, the file is read by SlimShadey, and rewritten to 
    a tempfile, the path for which is guaranteed to be space-free
     */
    //</editor-fold>
    private void checkLSEP(File f) {
        boolean has_LSEP = false;

        try {
            Reader fr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);

            int c;
            while ((c = fr.read()) != -1) {
                System.out.println(Integer.toHexString(c) + " " + ((char) c));
                //the LSEP character causes problems in opal (\u2028)
                if (Integer.toHexString(c).equals("2028")) {
                    has_LSEP = true;
                    //break;
                }
                //System.out.println(Integer.toHexString(c));
            }

            fr.close();

        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (has_LSEP) {
            Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            VBox rt = new VBox();
            st.setTitle("LSEP replacement");

            TextFlow notif = new TextFlow();
            notif.setTextAlignment(TextAlignment.CENTER);
            Text notifA = new Text("Your FASTA file uses the LSEP (\\u2028) line"
                    + " separator, which will cause an error in Opal. Would you "
                    + "like OpalFX to copy your sequences to a temporary file in"
                    + " which the LSEP is repl"
                    + "aced with standard (\\n) line separators and try again?");
            notifA.setStyle(" -fx-text-fill:red;");

            notif.getChildren().add(notifA);
            notif.setPadding(new Insets(15));

            HBox buttons = new HBox(10);

            Button ok = new Button("Copy without LSEP and retry");
            ok.setOnAction((ActionEvent e) -> {
                try {
                    File newinput = File.createTempFile("LSEPCROP_".concat(f.getName().replaceAll("\\s+", "")), ".tmp");
                    newinput.deleteOnExit();

                    Reader fr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
                    Writer fw = new OutputStreamWriter(new FileOutputStream(newinput), StandardCharsets.UTF_8);
                    int c;
                    while ((c = fr.read()) != -1) {

                        //the LSEP character causes problems in opal (\u2028)
                        if (Integer.toHexString(c).equals("2028")) {
                            c = '\n';
                        }
                        fw.write(c);
                    }
                    new OpalRunner(newinput, st, origin);

                    fr.close();
                    fw.close();

                } catch (IOException ex) {
                    Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Button cancel = new Button("Quit OpalFX");
            cancel.setOnAction(e -> {
                st.close();
            });
            buttons.getChildren().addAll(cancel, ok);
            buttons.setPadding(new Insets(15));

            rt.getChildren().addAll(notif, buttons);
            VBox.setVgrow(notif, Priority.ALWAYS);

            Scene sc = new Scene(rt, 500, 135, true);
            st.setScene(sc);
            st.widthProperty().addListener(e -> {
                for (int i = 0; i < buttons.getChildren().size(); i++) {
                    ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                }
            });
            st.show();
            ok.requestFocus();
        } else {
            try {
                File newinput = File.createTempFile("tmpf+_".concat(f.getName().replaceAll("\\s+", "")), ".tmp");
                newinput.deleteOnExit();

                Reader fr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
                Writer fw = new OutputStreamWriter(new FileOutputStream(newinput), StandardCharsets.UTF_8);
                int c;
                while ((c = fr.read()) != -1) {

                    //the LSEP character causes problems in opal (\u2028)
                    if (Integer.toHexString(c).equals("2028")) {
                        c = '\n';
                    }
                    fw.write(c);
                }
                //new OpalRunner(newinput, st, origin);

                fr.close();
                fw.close();

                this.detectAlphabet(newinput, true, null);
            } catch (IOException ex) {
                Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void detectAlphabet(File f, boolean newNotif, Stage opendialog) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linex;
            List<String> lines = new ArrayList<>();

            boolean containsLSEP;
            /*
             * The LSEP character is a UTF line break character. It doesn't 
             * render on some platforms (e.g. Google Chrome, Netbeans, this) 
             * and will cause a runtime fail in Opal since LSEP is not 
             * recognized as a line break ("[LSEP]".contains("\n") returns 
             * false.
             */

            while ((linex = br.readLine()) != null) {
                linex = linex.replaceAll("\\r|\\n", "");
                if (!linex.isEmpty() && linex.charAt(0) != '>') {
                    linex = linex.replaceAll("\\s+", "");
                }
                if (linex != null && !linex.isEmpty()) {
                    lines.add(linex);
                }
            }

            boolean isNucleotide = true;
            check_nucleotide:
            for (String line : lines) {
                if (line.charAt(0) != '>') {

                    iter_line:
                    for (char seqChar : line.toUpperCase().toCharArray()) {
                        boolean found = false;

                        iter_alpha:
                        for (char alphaChar : NUCLEOTIDE) {
                            if (seqChar == alphaChar) {
                                found = true;
                                break iter_alpha;
                            }
                        }
                        if (!found) {
                            isNucleotide = false;
                            break check_nucleotide;
                        }
                    }
                }
            }

            boolean isProtein = true;
            check_protein:
            for (String line : lines) {
                if (line.charAt(0) != '>') {

                    iter_line:
                    for (char seqChar : line.toUpperCase().toCharArray()) {
                        boolean found = false;

                        iter_alpha:
                        for (char alphaChar : PROTEIN) {
                            if (seqChar == alphaChar) {
                                found = true;
                                break iter_alpha;
                            }
                        }
                        if (!found) {
                            isProtein = false;
                            break check_protein;
                        }
                    }
                }
            }

            this.isNucleotide = isNucleotide;
            this.isProtein = isProtein;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (isNucleotide) {
            if (opendialog != null) {
                opendialog.close();
            }
            this.alphabetNotification(f, DataType.NUC, true, DataType.PRO);
        } else if (isProtein) {
            if (opendialog != null) {
                opendialog.close();
            }
            this.alphabetNotification(f, DataType.PRO, false, (DataType[]) null);
        } else {
            if (newNotif) {
                this.alphabetNotification(f, (DataType) null, false, (DataType[]) null);
            }
        }

    }

    private void alphabetNotification(File f, DataType alpha, boolean allowSwitch, DataType... switches) {
        if (alpha != null) {
            Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            VBox rt = new VBox();
            st.setTitle("Confirm detected data type");

            HBox notif = new HBox();
            notif.setAlignment(Pos.CENTER);
            Label notifA = new Label("OpalFX detects that you are trying to align ");
            Label notifB = new Label(alpha.str_name());
            Label notifC = new Label(" sequences.");
            notif.getChildren().addAll(notifA, notifB, notifC);

            HBox buttons = new HBox();
            buttons.setPadding(new Insets(15));

            if (allowSwitch && switches != null && switches.length != 0) {
                Button[] switchButtons = new Button[switches.length];
                _type = alpha;
                int iter = -1;
                for (DataType s : switches) {
                    Button switchTo = new Button("Switch to ".concat(s.str_name()));
                    switchTo.setOnAction(e -> {
                        _type = s;
                        this.initializeResources();
                        this.initializeUI(f);
                        loggerWindow = new OpalFXLogger(null, false);
                        this.setFile(f);
                        st.hide();
                    });
                    switchButtons[++iter] = switchTo;
                }
                buttons.getChildren().addAll(switchButtons);
            }

            Button ok = new Button("Confirm ".concat(alpha.str_name()));
            ok.setOnAction(e -> {
                _type = alpha;
                this.initializeResources();
                this.initializeUI(f);
                loggerWindow = new OpalFXLogger(null, false);
                this.setFile(f);
                st.hide();
            });
            buttons.getChildren().add(ok);

            rt.getChildren().addAll(notif, buttons);
            VBox.setVgrow(notif, Priority.ALWAYS);
            buttons.setPrefHeight(0.0);

            Scene sc = new Scene(rt, 500, 135, true);
            st.setScene(sc);

            st.widthProperty().addListener(e -> {
                for (int i = 0; i < buttons.getChildren().size(); i++) {
                    ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                }
            });
            st.showAndWait();
        } else {
            Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            VBox rt = new VBox();
            st.setTitle("Error reading file");

            HBox notif = new HBox();
            notif.setAlignment(Pos.CENTER);
            Label notifA = new Label("OpalFX did not detect a nucleotide/protein FASTA file");
            notifA.setStyle(" -fx-text-fill:red;");

            notif.getChildren().addAll(notifA);

            HBox buttons = new HBox();

            Button ok = new Button("Try another file");
            ok.setOnAction(e -> {
                File selectedFile = null;
                while (selectedFile == null) {
                    _type = null;
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Load sequences to Align (Opal)");
                    selectedFile = fileChooser.showOpenDialog(st);
                }
                detectAlphabet(selectedFile, false, st);
            });
            buttons.getChildren().add(ok);
            buttons.setPadding(new Insets(15));

            rt.getChildren().addAll(notif, buttons);
            VBox.setVgrow(notif, Priority.ALWAYS);

            Scene sc = new Scene(rt, 500, 135, true);
            st.setScene(sc);
            st.widthProperty().addListener(e -> {
                for (int i = 0; i < buttons.getChildren().size(); i++) {
                    ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                }
            });
            st.showAndWait();

        }
    }

    private boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public volatile BooleanProperty fileDone = new SimpleBooleanProperty(false);

    /**
     * Returns the output if the current alignment is done
     *
     * @return the output in a FASTA format temp<tt>File</tt>
     */
    public File getOutput() {
        return fileDone.get() ? tempOutputFile : null;
    }

    private void setCiteBehavior() {

        Timeline loadingThread = new Timeline(new KeyFrame(Duration.millis(25), new EventHandler<ActionEvent>() {

            final int L = 80, H = 220;

            int r = H, g = L, b = L;
            int[] rgb = new int[]{r, g, b};

            int active = 1; // 0->r 1->g 2->b
            boolean increment = true;

            @Override
            public void handle(ActionEvent event) {
                cycle();
                cite.setFill(Color.rgb(rgb[0], rgb[1], rgb[2]).darker());
            }

            private void cycle() {
                rgb[active] += (increment ? 1 : -1) * 10;
                if (rgb[active] == H || rgb[active] == L) {
                    active = ++active % rgb.length;
                    switch (rgb[active]) {
                        case L:
                            increment = true;
                            break;
                        case H:
                            increment = false;
                            break;
                    }
                }
            }

        }));

        loadingThread.setCycleCount(Timeline.INDEFINITE);

        cite.setStyle(" -fx-font: 12 Arial; -fx-font-weight: bold;");
        cite.setCursor(Cursor.HAND);

        cite.setOnMouseEntered(e -> {
            cite.setFill(Color.BLUE);
            cite.setUnderline(true);
            loadingThread.play();

        });

        cite.setOnMouseExited(e -> {
            loadingThread.pause();
            cite.setFill(Color.BLACK);
            cite.setUnderline(false);
        });

        cite.setOnMouseClicked(e -> {
            new OpalAgreement(false, origin, null);
        });

    }

    //help strings
    //<editor-fold defaultstate="collapsed" desc="help str">
    String __align_method = "--align_method [exact|profile|mixed] ";
    String __align_method_desc = "Default = profile" + System.lineSeparator()
            + "Alignment method used in building initial alignment (befor"
            + "e polishing) "
            + System.lineSeparator()
            + "* Exact method shows slightly better recovery of ben"
            + "chmarks. "
            + System.lineSeparator()
            + "* Profile is much faster for large inputs. "
            + System.lineSeparator()
            + "* Mixed metho"
            + "d performs exact (slower) alignment on small subproblems, and pro"
            + "file (faster) alignment on larger subproblems.";

    String __polish_align_method = "--polish_align_method";
    String __polish_align_method_desc = "Default = value of align_method " + System.lineSeparator()
            + "Alignment method used when performing post-polishing step See --a"
            + "lign_method";

    String __polish_method = "--polish [exhaust_twocut" + "|exhaust_threecut"
            + "|random_twocut|random_tree_twocut|random_threecut" + "|none" + "]";
    String __polish_method_desc = "Default = random_tree_twocut" + System.lineSeparator()
            + "See ISMB paper for details";

    String __gamma = "--gamma n";
    String __gamma_desc = "Gap open penalty." + System.lineSeparator()
            + "Defaults: Amino acid = 45; Nucleotide = 260.";
    String __gamma_term = "--gamma_term n";
    String __gamma_term_desc = "Open penalty for terminal gaps." + System.lineSeparator()
            + "Defaults: Amino acid = 11; Nucleotide = 100.";

    String __lambda = "--lambda n";
    String __lambda_desc = "Gap extension penalty." + System.lineSeparator()
            + "Defaults: Amino acid = 42; Nucleotide = 69.";
    String __lambda_term = "--lambda_term n";
    String __lambda_term_desc = "Extension penalty for terminal gaps." + System.lineSeparator()
            + "Defaults: Amino acid = 40; Nucleotide = 66.";
//</editor-fold>

    private SequenceHolder readFASTA(File f) {
        SequenceHolder msa = null;

        try {
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));

            List<Sequence> basicSequences = new ArrayList<>();

            String name = null;
            StringBuilder sequence = null;

            String linex;
            while ((linex = fr.readLine()) != null) {
                linex = linex.replaceAll("\\r|\\n", "");
                if (!linex.isEmpty() && linex.charAt(0) != '>')
                linex = linex.replaceAll("\\s+", "");
                if (linex.isEmpty()) {
                    continue;
                }
                if (linex.charAt(0) == '>') {
                    if (sequence != null) {
                        Sequence seq = new Sequence();
                        seq.setSequence(sequence.toString());
                        seq.setName(name);
                        basicSequences.add(seq);
                    }
                    String[] ss = linex.split("\\s+");
                    name = ss[0].substring(1);
                    sequence = new StringBuilder("");
                } else {
                    sequence.append(linex);
                }
            }

            Sequence seq = new Sequence();
            seq.setSequence(sequence.toString());
            seq.setName(name);
            basicSequences.add(seq);

            msa = new SequenceHolder();

            msa.setData(basicSequences, null);

            fr.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OpalRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msa;
    }

    private void writeFASTA(File f, SequenceHolder sh) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (Sequence s : sh) {
                bufferedWriter.write(">".concat(s.name()));
                bufferedWriter.newLine();
                List<String> seqparts = splitEqually(s.seqString(), 60);
                for (String str : seqparts) {
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<String> splitEqually(String text, int size) {
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }
}
