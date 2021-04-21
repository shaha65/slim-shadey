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
package edu.tcnj.biology.seqverter;

import edu.tcnj.biology.seqverter.converter.FileFormat;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.converter.SeqVerterReader;
import edu.tcnj.biology.seqverter.matrix.SubstitutionMatrices;
import edu.tcnj.biology.seqverter.matrix.SubstitutionMatrix;
import edu.tcnj.biology.seqverter.sequence.Alphabet;
import edu.tcnj.biology.seqverter.sequence.DefaultAlphabets;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This is the main FX application class. Its start method initializes a new
 * <tt>EditorInterface</tt> object to display the editor for multiple sequence
 * alignments.
 *
 * @author Avi Shah
 */
public class SeqVerter extends Application {

    //conversion & calc vars
    private DefaultAlphabets alphabets;
    private SubstitutionMatrices substitutionMatrices;
    //alphabet used by current sequences
    private Alphabet currentAlphabet;

    private EditorInterface child;

    private SubstitutionMatrix currentSubstitutionMatrix;

    //the file from which the MSA is being read
    private File currentFile;
    //object that stores MSA
    private SequenceHolder sequenceHolder;

    @Override
    public void init() {
        //initializeResource();
    }

    boolean agreed = false;

    @Override //start application, 
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        ImageView iv = new ImageView();
        root.getChildren().add(iv);
        //iv.setImage(new Image(getClass().getResourceAsStream("lg_s_bg_old.gif")));
        iv.setImage(new Image(SeqVerter.class.getResourceAsStream("Splash.gif")));
        Stage splash = new Stage();
        splash.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        Scene scene = new Scene(root, iv.getImage().getWidth(), iv.getImage().getHeight());
        splash.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        root.setBackground(Background.EMPTY);
        splash.setScene(scene);
        splash.setAlwaysOnTop(true);
        splash.show();

        Platform.runLater(() -> {
            final Stage window = new Stage();
            window.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            Task tk = new Task() {
                @Override
                protected Void call() throws Exception {
                    initializeResource();
                    try {
                        //Thread.sleep(1500);
                    } catch (Exception ex) {
                        Logger.getLogger(SeqVerter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Platform.runLater(() -> {
                        try {
                            //<editor-fold defaultstate="collapsed" desc="activate in final - license agreement">

                            final int maxw = 600;

                            VBox root = new VBox(7);
                            root.setAlignment(Pos.CENTER);
                            ImageView iv = new ImageView();
                            Label lb = new Label(" I agree, continue to Slim Shadey ");
                            lb.setCursor(Cursor.HAND);
                            lb.setStyle("-fx-border-width : 2; -fx-border-color: white; -fx-font-family : Arial; -fx-font-size : 32; -fx-font-weight : bold");
                            lb.setOnMouseEntered(e -> {
                                lb.setStyle("-fx-border-width : 2; -fx-border-color: white; -fx-font-family : Arial; -fx-font-size : 32; -fx-font-weight : bold; -fx-text-fill : #228B22;");
                            });
                            lb.setOnMouseExited(e -> {
                                lb.setStyle("-fx-border-width : 2; -fx-border-color: white; -fx-font-family : Arial; -fx-font-size : 32; -fx-font-weight : bold; -fx-text-fill : black;");
                            });
                            lb.setAlignment(Pos.CENTER);
                            lb.impl_processCSS(true);
                            lb.setOnMouseClicked(e -> {
                                agreed = true;
                                window.close();
                            });

                            iv.setImage(new Image(getClass().getResourceAsStream("lg_s_bg_cr.gif")));
                            iv.setPreserveRatio(true);
                            iv.setFitWidth(maxw);
                            Text throwawayClone = new Text(lb.getText());
                            throwawayClone.setStyle("-fx-border-width : 2; -fx-border-color: white; -fx-font-family : Monospace; -fx-font-size : 32; -fx-font-weight : bold; -fx-text-fill : black;");

                            TextArea license = new TextArea();
                            license.setFocusTraversable(false);
                            license.setEditable(false);
                            license.setWrapText(true);
                            final int fixedLicenseHeight = 160;
                            license.setMinHeight(fixedLicenseHeight);
                            license.setMaxHeight(fixedLicenseHeight);
                            license.setPrefWidth(1.14 * maxw);
                            //license.getStyleClass().add("biochar");
                            root.getChildren().addAll(iv, license, lb);
                            root.applyCss();
                            root.layout();

                            new Scene(new Group(throwawayClone));
                            throwawayClone.applyCss();

                            Scene scene;
                            //scene = new Scene(root, iv.getImage().getWidth(), iv.getImage().getHeight() + throwawayClone.getLayoutBounds().getHeight() + 37 + fixedLicenseHeight);
                            scene = new Scene(root);
                            window.initStyle(StageStyle.UTILITY);
                            scene.setFill(Color.TRANSPARENT);
                            root.setBackground(Background.EMPTY);
                            window.setScene(scene);
                            window.setOnShown(e -> license.scrollTopProperty().set(0.0));
                            Font.loadFont(getClass().getResource("unifont.ttf").toExternalForm(), 20);
                            scene.getStylesheets().add(getClass().getResource("ApplicationStyle.css").toExternalForm());
                            //license.appendText("This is SlimShadey v1.\nVersioning, and citation information will go here.");
                            license.setFont(Font.font("Consolas", FontWeight.BLACK, FontPosture.REGULAR, 14));

                            BufferedReader br = new BufferedReader(new InputStreamReader(SeqVerter.class.getResourceAsStream("agree_licensing")));

                            String brline;
                            while ((brline = br.readLine()) != null) {
                                license.appendText(brline + "\n");
                            }

                            window.showingProperty().addListener(e -> {
                                //System.err.println("||");
                                if (!window.isShowing()) {
                                    child.show(!agreed);
                                }
                            });
                            mainStage = primaryStage;

                            initializeUI();
                            child = new EditorInterface(alphabets);

                            splash.close();
                            window.setAlwaysOnTop(true);
                            window.setResizable(false);
                            window.showAndWait();

                            //</editor-fold>
                        } catch (IOException ex) {
                            Logger.getLogger(SeqVerter.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    return null;
                }
            };

            Thread t = new Thread(tk);

            tk.setOnCancelled(e -> {
                System.exit(1);
            });

            tk.setOnFailed(e -> {
                System.exit(1);
            });

            tk.setOnSucceeded(e -> {
                //splash.close();
            });

            t.start();

            //this.mainStage.show();
        });
    }

    public File getCurrentFile() {
        return this.currentFile;
    }

    /* Generates all containers for all objects in the GUI. Also defines 
     * behaviors for all graphical components.
     * 
     */
    private void initializeUI() {
        mainStage = new Stage();
        mainStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        mainStage.setTitle("SeqVerter");
        mainContainer = new VBox();

        menuBar = new MenuBar();
        menuFile = new Menu("File");
        openFile = new MenuItem("Open input file");
        openFile.setOnAction(e -> {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            this.setFile(fileChooser.showOpenDialog(mainStage));
        });
        saveFile = new MenuItem("Save output file");

        menuFile.getItems().addAll(openFile, saveFile);
        menuBar.getMenus().add(menuFile);
        menuBar.prefWidthProperty().bind(mainStage.widthProperty());
        mainContainer.getChildren().add(menuBar);

        inputFileArea = new TextArea();
        inputFileArea.setEditable(false);
        inputFileArea.prefHeightProperty().bind(mainStage.heightProperty());
        inputFileArea.setStyle("-fx-font-family : Monospace;");

        outputFormats = new ComboBox<>();
        outputFormats.getItems().addAll(Arrays.asList(FileFormat.values()));
        outputFormats.getItems().remove(FileFormat.guess);
        outputFormats.prefWidthProperty().bind(mainStage.widthProperty());
        outputFormats.setPromptText("<Select an output file format>");
        outputFormats.setOnAction(e -> {
            System.out.println(outputFormats.getValue());
        });

        outputFileArea = new TextArea();
        outputFileArea.prefHeightProperty().bind(mainStage.heightProperty());
        outputFileArea.setStyle("-fx-font-family : Consolas;");
        outputFileArea.setEditable(false);

        goShadey = new Button("Will the Real Slim Shady please stand up?");
        goShadey.prefWidthProperty().bind(mainStage.widthProperty());
        goShadey.setOnAction(e -> {
//            EditorInterface sep = new EditorInterface(sequenceHolder, currentFile.getName());
        });

        mainContainer.getChildren().addAll(inputFileArea, outputFormats, outputFileArea, goShadey);

        //curly loop ➰ AND arrow left⇐ right⇒ 3/5 normal font in monospace
        //Label testL1 = new Label("asdf asdf asdf asdf asdf asdf ");
        //testL1.setStyle("-fx-font-family : Monospace; -fx-font-size : 20");
        //Label testL2 = new Label("⇒⇒⇒⇒⇒⇒⇒⇒⇒⇒");
        //testL2.setStyle("-fx-font-family : Monospace; -fx-font-size : 12");
        //mainContainer.getChildren().add(testL1);
        //mainContainer.getChildren().add(testL2);
        scene1 = new Scene(mainContainer, 1000, 600);
        mainStage.setScene(scene1);
        //this.setFile(new File("C:/Users/ashah_admin/Desktop/readseq_src/Z/clustal_omega/GLD-1_clustalo.fasta.txt"));
    }

    /* loads in default (DNA, RNA, protein) alphabets, and substitution matrices
     * (PAM250 and BLOSUM62)
     */
    private void initializeResource() {
        this.alphabets = new DefaultAlphabets();
        this.substitutionMatrices = new SubstitutionMatrices(true);
    }

    private void setFile(File file) {
        this.currentFile = file;
        System.out.println(file.getAbsolutePath());
        if (currentFile != null) {
            try {
                //SeqVerterReader.read(selectedFile, FileFormat.fasta, alphabets);
                sequenceHolder = SeqVerterReader.read(currentFile, FileFormat.guess, alphabets, true);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(SeqVerter.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                inputFileArea.clear();
                FileInputStream fis;
                BufferedReader br = new BufferedReader(new FileReader(currentFile));
                String line = null;
                while ((line = br.readLine()) != null) {
                    inputFileArea.appendText(line.concat("\n"));
                    System.out.println(line);
                }
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //launches the FX applicaiton
        for (String s : args) {
            System.out.println(s);
        }
        System.out.println("");

        launch(args);
    }

    /*
    prints array - for testing
     */
    private void printArray(double matrix[][]) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    //UI COMPONENTS
    private Stage mainStage;
    private Scene scene1;
    private VBox mainContainer;

    private MenuBar menuBar;
    private Menu menuFile;
    private MenuItem openFile;
    private MenuItem saveFile;
    private Menu menuAlphabet;
    private Menu substitutionMatrix;

    private TextArea inputFileArea;
    private ComboBox<FileFormat> outputFormats;
    private TextArea outputFileArea;

    private Button goShadey;

    private FileChooser fileChooser;

}
