/*
    Copyright (alc) 2018 Avi Shah, Sudhir Nayak

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

import com.sun.javafx.scene.control.skin.VirtualFlow;
import edu.tcnj.biology.seqverter.converter.FileFormat;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.converter.SeqVerterReader;
import edu.tcnj.biology.seqverter.graphics.GeneralLoadingManager;
import edu.tcnj.biology.seqverter.sequence.Alphabet;
import edu.tcnj.biology.seqverter.sequence.DefaultAlphabets;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SceneBuilder;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import opalimpl.OpalAgreement;
import resources.skins.CustomTabPaneSkin;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;

/**
 * This class has all the graphical components for the editor pane through which
 * the user can manipulate their multiple sequence alignment.
 *
 * The user will be able to view their MSA, shade in black/white/gray, shade in
 * color, generate graphics (e.g. sequence logos) and publication- quality
 * sequence alignments. When creating alignment figures, users can define the
 * number of columns per row, font size, numbering, and consensus and annotation
 * rows.
 *
 * @author Avi Shah
 */
public class EditorInterface {

    private VisualMultipleSequenceAlignment vmsa;

    private Button loadFile;
    private Button align;
    private Button pasteSequence;

    private DefaultAlphabets alphabets;

    private ManualViewer manualViewer;

    private final List<String> EXAMPLE_FILE_NAMES = Arrays.asList(new String[]{
        "GLD-1",
        "CD44",
        "FOG-2_elegans",
        "FOXP2",
        "HOXA1",
        "HOXA1_(large)",
        "HOXA1_coding",
        "IDO2",
        "LGALS1",
        "NKX2-5",
        "PAX6",
        "RUNX1", //"FBF"
    });

    //private final List<String> EXAMPLE_FILE_NAME_MODIFIERS = Arrays.asList(new String[]{".fasta", "_slim.mm"});
    private final LinkedHashMap<String, FileFormat> EXAMPLE_FILE_NAME_MODIFIERS_MAP
            = new LinkedHashMap<String, FileFormat>() {
        {
            put(".fasta", FileFormat.fasta);
            put("_slim.mm", FileFormat.slim);
        }
    };

    private GeneralLoadingManager glm_graphic;
    private GeneralLoadingManager glm_fileio;
    private GeneralLoadingManager glm_validinput;
    private GeneralLoadingManager glm_validatemsa;

    /**
     * This constructor loads the multiple alignment into a <tt>
     * VisualMultipleSequenceAlignment</tt> object via a
     * <tt>SequenceHolder</tt> object, and names the alignment by a supplied
     * name.
     *
     * @param alphabets Default alphabets, loaded on launch
     * @param sh
     * @param fileName
     */
    public EditorInterface(DefaultAlphabets alphabets) {
        this.alphabets = alphabets;
        this.initializeResources();
        this.initializeUI();

        accessManual.setStyle(" -fx-text-fill: gray");
        accessManual.setOnMouseEntered(e -> {
            accessManual.setStyle(" -fx-text-fill: black");
        });

        accessManual.setOnMouseExited(e -> {
            accessManual.setStyle(" -fx-text-fill: gray");
        });

        accessManual.setOnAction(e -> manualViewer.accessManual(true));

        accessLicense.setStyle(" -fx-text-fill: gray");
        accessLicense.setOnMouseEntered(e -> {
            accessLicense.setStyle(" -fx-text-fill: black");
        });

        accessLicense.setOnMouseExited(e -> {
            accessLicense.setStyle(" -fx-text-fill: gray");
        });

        accessLicense.setOnAction(e -> {
            // OPEN A WINDOW TO VIEW LICENSING AND CONTACT INFORMATION
            licenseWindow();
        });

        addExample.setStyle(" -fx-text-fill: gray");
        addExample.setOnMouseEntered(e -> {
            addExample.setStyle(" -fx-text-fill: black");
        });

        addExample.setOnMouseExited(e -> {
            addExample.setStyle(" -fx-text-fill: gray");
        });

        addExample.setOnAction(e -> {
            Stage tempStage = new Stage();
            tempStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            tempStage.setTitle("Choose a sample dataset to analyze");
            tempStage.initModality(Modality.APPLICATION_MODAL);

            VBox tempRoot = new VBox();

            for (String name : EXAMPLE_FILE_NAMES) {
                HBox exampleOptions = new HBox();
                for (String modifier : EXAMPLE_FILE_NAME_MODIFIERS_MAP.keySet()) {
                    String s = name.concat(modifier);

                    Button exampleButton = new Button();
                    exampleOptions.getChildren().add(exampleButton);

                    exampleButton.setPrefSize(250, 40);
                    final String[] showModifier = new String[]{"", ""};
                    if (modifier.contains(".fasta")) {
                        showModifier[0] = " FASTA";
                        showModifier[1] = "";
                    } else if (modifier.contains(".mm")) {
                        showModifier[0] = " shaded";
                        showModifier[1] = " shaded";
                    }
                    exampleButton.setText(name.replaceAll("_", " ") + showModifier[0]);
                    exampleButton.setOnMousePressed(ev -> exampleButton.setCursor(Cursor.WAIT));
                    exampleButton.setOnMouseReleased(ev -> {
                        if (exampleButton.hoverProperty().get()) {
                            //exampleButton.setCursor(Cursor.WAIT);
                        } else {
                            exampleButton.setCursor(Cursor.DEFAULT);
                        }
                    });
                    exampleButton.setOnAction(ev -> {
                        try {
                            System.out.println(s);
                            File f = EXAMPLE_FILES.getOrDefault(s, null);
                            if (f != null) {
                                read(f, EXAMPLE_FILE_NAME_MODIFIERS_MAP.get(modifier), name + showModifier[1], true, true);
                            } else {
                                throw new NullPointerException("The example file for "
                                        + s + showModifier[1]
                                        + " is not currently available.");
                            }
                        } catch (NullPointerException npe) {
                            //Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, npe);
                            Platform.runLater(() -> confirm_popup("Missing resource", npe.getMessage()));
                        } finally {
                            tempStage.close();
                        }
                    });

                }
                tempRoot.getChildren().add(exampleOptions);
            }
            /*
            for (String s : EXAMPLE_FILE_NAMES) {
                HBox exampleFileOptionsBox = new HBox();
                Button fastaFileButton = new Button(s.replaceAll("_", " ") + " FASTA");
                fastaFileButton.setOnAction(event -> {
                    Scanner sc = null;
                    BufferedWriter bw = null;

                    try {
                        File temp = EXAMPLE_FILES.getOrDefault(s, null);
                        /* = File.createTempFile("tempfile", ".tmp");

                        File temp = File.createTempFile("tempfile", ".tmp");
                        
                        InputStream in = null;
                        sc = null;
                        String fileName = "example_".concat(s).concat(".fasta");
                        in = EditorInterface.class.getResourceAsStream(fileName);

                        OutputStream out = new FileOutputStream(temp);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        in.close();
                        out.close();

                        /*
                        sc = new Scanner(inputStream, "UTF-8");
                        //write it
                        bw = new BufferedWriter(new FileWriter(temp));

                        while (sc.hasNextLine()) {
                            String line = sc.nextLine();
                            bw.write(line + "\n");

                        }
                        bw.close();
             */
 /*
                        read(temp, FileFormat.fasta, s.concat("_alignment"));

                        /*} catch (IOException ex) {
                        Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
             */
 /*        } catch (NullPointerException npe) {
                        confirm_popup("Missing resource", "The example file for "
                                + s
                                + " is not currently available.");
                        Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, npe);
                    } finally {
                        tempStage.close();
                    }
                });
                Button slimFileButton = new Button(s.replaceAll("_", " ") + " shaded");
                slimFileButton.setOnAction(event -> {
                    Scanner sc = null;
                    BufferedWriter bw = null;

                    try {
                        File temp = EXAMPLE_FILES.getOrDefault(s, null);/* = File.createTempFile("tempfile", ".tmp");
                        InputStream in = null;
                        sc = null;
                        String fileName = "example_".concat(s).concat("_slim.mm");
                        in = EditorInterface.class.getResourceAsStream(fileName);

                        OutputStream out = new FileOutputStream(temp);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        in.close();
                        out.close();
             */
 /*
                        sc = new Scanner(inputStream, "UTF-8");
                        //write it
                        bw = new BufferedWriter(new FileWriter(temp));

                        while (sc.hasNextLine()) {
                            String line = sc.nextLine();
                            bw.write(line + "\n");
                            System.out.println(line);
                        }
                        bw.close();
                        System.out.println(fileName);
             */
 /*            if (temp != null) {
                            read(temp, FileFormat.slim, s + ".mm");
                            //newTab(spm.getData(temp), s + ".mm");
                        } else {
                            throw new NullPointerException();
                        }
                        /*
                    } catch (IOException ex) {
                        Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
             */
 /*        } catch (NullPointerException npe) {
                        confirm_popup("Missing resource", "The example file for "
                                + s + " shaded"
                                + " is not currently available.");
                        Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, npe);
                    } finally {
                        tempStage.close();
                    }
                });

                fastaFileButton.setPrefSize(250, 40);
                slimFileButton.setPrefSize(250, 40);
                exampleFileOptionsBox.getChildren().addAll(fastaFileButton, slimFileButton);
                tempRoot.getChildren().add(exampleFileOptionsBox);
            }
             */
            Scene tempScene = new Scene(tempRoot);

            tempStage.setScene(tempScene);
            tempStage.show();

            int opts = EXAMPLE_FILE_NAMES.size();

            /*
            for (Node n : tempRoot.getChildren()) {
                for (Node nn : ((HBox) n).getChildren()) {
                    ((Button) nn).setPrefHeight(tempStage.getHeight() / opts);
                    ((Button) nn).setPrefWidth(tempStage.getWidth() / 2);
                }
            }
             */
            tempStage.heightProperty().addListener(ev -> {

                for (Node n : tempRoot.getChildren()) {
                    for (Node nn : ((HBox) n).getChildren()) {
                        ((Button) nn).setPrefHeight(tempStage.getHeight() / opts);
                    }
                }
            });

            tempStage.widthProperty().addListener(ev -> {
                for (Node n : tempRoot.getChildren()) {
                    for (Node nn : ((HBox) n).getChildren()) {
                        ((Button) nn).setPrefWidth(tempStage.getWidth() / 2);
                    }
                }
            });

            tempStage.setWidth(500);

        });

        addTab.setStyle(" -fx-text-fill: gray");
        addTab.setOnMouseEntered(e -> {
            addTab.setStyle(" -fx-text-fill: black");
        });

        addTab.setOnMouseExited(e -> {
            addTab.setStyle(" -fx-text-fill: gray");
        });

        addTab.setOnAction(e -> {

            Stage tempStage = new Stage();
            tempStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            tempStage.setTitle("Start working on a project");
            tempStage.initModality(Modality.APPLICATION_MODAL);

            VBox tempRoot = new VBox();

            loadFile = new Button("Load alignment from text file, or Shadey project file");
            align = new Button("Align unaligned-FASTA using OpalFX");
            pasteSequence = new Button("Paste FASTA format alignment");

            tempRoot.getChildren().addAll(loadFile, align, pasteSequence);

            pasteSequence.setOnAction(ev -> {
                tempStage.close();

                this.usePasteSequence();
            });

            align.setOnAction(ev -> {
                tempStage.close();

                Stage st = new Stage();
                st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                VBox rt = new VBox();
                st.setTitle("Launch OpalFX");

                TextFlow notif = new TextFlow();
                notif.setTextAlignment(TextAlignment.CENTER);
                Text notifA = new Text("You are about to open OpalFX, which uses "
                        + "a separate program to align your sequences."
                        /*
                        + " OpalFX "
                        + "is entirely modular; you must complete and save your "
                        + "alignment separately, and then load the aligned file "
                        + "again via SlimShadey to work on your sequences. "
                         */
                        + "\nNOTE: Opal's source code has not been modified, and so "
                        + "Opal may exit (i.e. Java will exit) under cer"
                        + "tain conditions, forcing SlimShadey to close, too.");
                //notifA.setStyle(" -fx-text-fill:red;");

                notif.getChildren().add(notifA);
                notif.setPadding(new Insets(15));

                HBox buttons = new HBox(10);

                Button quit = new Button("Cancel");
                quit.setOnAction(eve -> {
                    st.close();
                });

                Button ok = new Button("Launch OpalFX");
                ok.setOnAction(eve -> {
                    new OpalAgreement(true, this, null);
                    st.close();
                });

                buttons.getChildren().addAll(quit, ok);

                buttons.setPadding(new Insets(15));

                rt.getChildren().addAll(notif, buttons);
                rt.setPrefWidth(500);
                VBox.setVgrow(notif, Priority.ALWAYS);

                Scene sc = new Scene(rt);
                st.setScene(sc);
                st.widthProperty().addListener(o -> {
                    for (int i = 0; i < buttons.getChildren().size(); i++) {
                        if (buttons.getChildren().get(i) instanceof Button) {
                            ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                        }
                    }
                });
                ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
                st.showAndWait();
            });

            loadFile.setOnAction(ev -> {
                tempStage.close();

                fileChooser = new FileChooser();

                fileChooser.setTitle("Open - text alignments and Shadey projects");

                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(tempStage);

                //File selectedFile = fileChooser.showOpenDialog(stage);
                //SequenceHolder sh = SeqVerterReader.read(selectedFile, FileFormat.guess, this.alphabets);
                if (selectedFiles != null) {
                    //this.glm_graphic.showLoading(true);

                    try {
                        readFiles(selectedFiles);
                        //read(selectedFile, SeqVerterReader.guess(selectedFile), selectedFile.getName(), true, true);
                        /*
                        BufferedReader brTest = new BufferedReader(new FileReader(selectedFile));
                        String text = brTest.readLine();
                        if (text.equals(".slim")) {
                            newTab(spm.getData(selectedFile), null);
                        } else {
                            
                            SequenceHolder sh = read(selectedFile, null);
                            if (sh != null) {
                                newTab(sh, selectedFile.getName());
                            }
                        }
                         */
                    } catch (Exception exc) {
                        this.glm_graphic.exitLoading();
                        exc.printStackTrace();
                    }
                }
            });

            Scene tempScene = new Scene(tempRoot, 400, 150, true);

            tempStage.setScene(tempScene);
            tempStage.show();

            int opts = 3;

            loadFile.setPrefHeight(tempStage.getHeight() / opts);
            align.setPrefHeight(tempStage.getHeight() / opts);
            pasteSequence.setPrefHeight(tempStage.getHeight() / opts);
            loadFile.setPrefWidth(tempStage.getWidth());
            align.setPrefWidth(tempStage.getWidth());
            pasteSequence.setPrefWidth(tempStage.getWidth());

            tempStage.heightProperty().addListener(ev -> {
                loadFile.setPrefHeight(tempStage.getHeight() / opts);
                align.setPrefHeight(tempStage.getHeight() / opts);
                pasteSequence.setPrefHeight(tempStage.getHeight() / opts);
            });

            tempStage.widthProperty().addListener(ev -> {
                loadFile.setPrefWidth(tempStage.getWidth());
                align.setPrefWidth(tempStage.getWidth());
                pasteSequence.setPrefWidth(tempStage.getWidth());
            });

        });
        this.stage.setOnCloseRequest(e -> System.exit(0));

    }

    private Stage licenseStage = null;
    private Stage licenseStatementStage = null;

    private AppLogoCube alc;

    private Timeline animation;

    private double sqside = 300;

    private double alcsize = 50;

    private void licenseWindow() {
        if (licenseStage == null) {
            licenseStage = new Stage();
            licenseStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));

            licenseStage.setTitle("About");

            //Group gp = new Group();
            VBox gp = new VBox();
            gp.setDepthTest(DepthTest.ENABLE);

            alc = new AppLogoCube(alcsize);
            alc.rx.setAngle(45);
            alc.ry.setAngle(45);
            //    alc.setTranslateX(103 + adjust);
            //alc.setTranslateY(104);

            animation = new Timeline();
            animation.getKeyFrames().addAll(new KeyFrame(Duration.ZERO,
                    new KeyValue(alc.ry.angleProperty(), 0d),
                    new KeyValue(alc.rx.angleProperty(), 0d),
                    new KeyValue(alc.rz.angleProperty(), 0d)
            ),
                    new KeyFrame(Duration.seconds(6),
                            new KeyValue(alc.ry.angleProperty(), 360d),
                            new KeyValue(alc.rx.angleProperty(), 720d),
                            new KeyValue(alc.rz.angleProperty(), 360d)
                    )
            );
            animation.setCycleCount(Animation.INDEFINITE);

            alc.setTranslateX((sqside / 2) - alcsize / 2);
            alc.setTranslateY(alcsize);

            animation.play();

            VBox textBox = new VBox(10);
            textBox.setDepthTest(DepthTest.ENABLE);
            //textBox.setCursor(Cursor.WAIT);
            textBox.setMouseTransparent(false);
            Text getLicense = new Text("Get license");
            getLicense.setUnderline(true);
            getLicense.setFill(Color.web("#0000EE"));
            //getLicense.setStyle("-fx-underline: true; -fx-text-fill: #0000EE;");
            getLicense.setCursor(Cursor.HAND);
            getLicense.setDepthTest(DepthTest.ENABLE);

            getLicense.setOnMouseClicked(e -> {
                if (licenseStatementStage == null) {
                    try {
                        licenseStatementStage = new Stage();
                        licenseStatementStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));

                        licenseStatementStage.setTitle("Licensing and citing");

                        TextArea license = new TextArea();

                        license.setFocusTraversable(false);
                        license.setEditable(false);
                        license.setWrapText(true);

                        license.setFont(Font.font("Arial"));

                        BufferedReader br = new BufferedReader(new InputStreamReader(EditorInterface.class.getClassLoader().getResourceAsStream("edu/tcnj/biology/seqverter/agree_licensing")));

                        List<String> contents = new ArrayList<>();

                        String brline;
                        while ((brline = br.readLine()) != null) {
                            brline = brline.trim();
                            if (brline.isEmpty()) {
                                contents.add("\n\n");
                            } else {
                                contents.add(brline + " ");
                            }
                        }

                        for (String s : contents) {
                            license.appendText(s);
                        }

                        license.heightProperty().addListener(ev -> {
                            Platform.runLater(() -> {
                                license.scrollTopProperty().set(0.0);
                            });
                        });
                        license.widthProperty().addListener(ev -> {
                            Platform.runLater(() -> {
                                license.scrollTopProperty().set(0.0);
                            });
                        });

                        VBox root = new VBox();

                        //ScrollPane licenseScroll = new ScrollPane();
                        //licenseScroll.setContent(license);
                        license.setPrefHeight(Screen.getPrimary().getBounds().getHeight() - 200);
                        //license.prefHeightProperty().bind(root.heightProperty());
                        VBox.setVgrow(license, Priority.ALWAYS);

                        root.getChildren().add(license);

                        Scene licenseStatementScene = new Scene(root);

                        licenseStatementStage.setScene(licenseStatementScene);

                        licenseStatementStage.setOnShown(ev -> license.scrollTopProperty().set(0.0));
                    } catch (IOException ex) {
                        Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                if (!licenseStatementStage.isShowing()) {
                    licenseStatementStage.show();
                }

            });

            getLicense.setTranslateX(30);
            //getLicense.setTranslateY(2 * alcsize + 20);
            Text getDoi = new Text("doi: https://dx.doi/xxxxxxxxxxxxxxxx");
            getDoi.setUnderline(true);
            getDoi.setFill(Color.web("#0000EE"));
            //getDoi.setStyle("-fx-underline: true; -fx-text-fill: #0000EE;");
            getDoi.setCursor(Cursor.HAND);
            getDoi.setOnMouseClicked(e -> {

            });
            getDoi.setTranslateX(30);
            //getDoi.setTranslateY(2 * alcsize + 30);
            //getDoi.setPadding(new Insets(0, 15, 15, 0));

            gp.getChildren().add(alc);

            SubScene gpscene = new SubScene(gp, sqside, alcsize * 3, true, SceneAntialiasing.BALANCED);
            gpscene.setDepthTest(DepthTest.ENABLE);
            gpscene.setCamera(new PerspectiveCamera());

            VBox root = new VBox();
            root.getChildren().add(gpscene);

            textBox.getChildren().addAll(getLicense, getDoi);
            root.getChildren().addAll(textBox);

            //gp.getChildren().addAll(alc, textBox);
            Scene sc = new Scene(root, sqside, sqside, false);

            //sc.setCamera(new ParallelCamera());
            //sc.getCamera().setNearClip(0.01);
            //sc.getCamera().setFarClip(10000);
            licenseStage.setScene(sc);
            //licenseStage.setOpacity(0.10);
            //gp.setOpacity(0.1);

            licenseStage.focusedProperty().addListener(e -> {
                if (!licenseStage.isFocused()) {
                    //licenseStage.hide();
                    //animation.pause();
                }
            });

            licenseStage.setOnHidden(e -> {
                animation.pause();
                licenseStatementStage.hide();
            });

        }
        if (!licenseStage.isShowing()) {
            animation.play();
            licenseStage.show();
            //alc.setTranslateX(licenseStage.getWidth() / 2);
        }
    }

    public void newTab(EditorTab et, String tabName) {
        this.glm_graphic.showLoading(true);
        Platform.runLater(() -> {
            System.out.println(et);
            this.tabs.getTabs().add(et);
            /*setting the name is optional; ShadeyProjectManager sets the name 
            to file.getName() by default, so this code will not do anything new*/
            if (tabName == null || tabName.isEmpty()) {

            } else {
                et.setText(tabName);
            }
            Platform.runLater(() -> {
                this.glm_graphic.exitLoading();
            });
        });

    }

    public void newTab(SequenceHolder sh, String tabName) {
        this.glm_graphic.showLoading(true);

        Platform.runLater(() -> {
            EditorTab et = new EditorTab(sh, tabName, tabs.getTabs().size() + 1, this);
            tabs.getTabs().add(et);
            Platform.runLater(() -> {
                this.glm_graphic.exitLoading();
            });
        });

    }

    private int opalct = 1;

    public int getOpalCount() {
        return opalct++;
    }

    protected VisualMultipleSequenceAlignment tabFromSubset(VBox annotationLabelsTemp,
            VBox annotationRowsTemp, VBox sequenceNamesTemp, VBox visualSequencesTemp,
            HBox consensusTemp,
            int[] range, String name, Alphabet alpha) {
        glm_graphic.showLoading(true);

        EditorTab et = new EditorTab(annotationLabelsTemp,
                annotationRowsTemp, sequenceNamesTemp, visualSequencesTemp,
                consensusTemp,
                range, name, alpha, this);
        tabs.getTabs().add(et);

        Platform.runLater(() -> {
            this.glm_graphic.exitLoading();
        });
        return et.getVMSA();
    }

    private Stage stage;

    public Stage getStage() {
        return this.stage;
    }

    private Scene scene;
    private AnchorPane anchor;

    private FileChooser fileChooser;

    private TabPane tabs;

    private Button addTab;
    private Button addExample;
    private Button accessManual;
    private Button accessLicense;

    public ObservableList<Color> recentColors;

    private final ShadeyProjectManager spm = new ShadeyProjectManager(this);

    private final Font MAIN_BUTTON_FONT = Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14);

    private final Map<String, File> EXAMPLE_FILES = new HashMap<String, File>();

    private void initializeResources() {

        this.manualViewer = new ManualViewer(this);

        this.glm_graphic = new GeneralLoadingManager("Loading graphics...");
        this.glm_fileio = new GeneralLoadingManager("Organizing input...");
        this.glm_validinput = new GeneralLoadingManager("Screening file...");
        this.glm_validatemsa = new GeneralLoadingManager("Validating alignment...");

        this.recentColors = FXCollections.observableArrayList();
        this.recentColors.addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                if (recentColors.size() > 24) {
                    //recentColors = FXCollections.observableArrayList(recentColors.subList(Math.max(recentColors.size() - 24, 0), recentColors.size()));
                    recentColors.remove(0);
                }
            }
        }
        );

        for (int k = 0; k < this.EXAMPLE_FILE_NAMES.size(); k++) {
            String exampleName = EXAMPLE_FILE_NAMES.get(k);
            for (String modifier : EXAMPLE_FILE_NAME_MODIFIERS_MAP.keySet()) {
                try {

                    String s = exampleName.concat(modifier);

                    File temp = File.createTempFile(s, ".tmp");

                    temp.deleteOnExit();

                    InputStream in = null;
                    //sc = null;
                    String fileName = "example_".concat(s);
                    in = getClass().getClassLoader().getResourceAsStream("resources/examples/" + fileName);
                    System.out.println(fileName);
                    if (in != null) {
                        OutputStream out = new FileOutputStream(temp);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        in.close();
                        out.close();
                        EXAMPLE_FILES.put(s, temp);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void initializeUI() {

        tabs = new TabPane();
        tabs.getTabs().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change c) {
                if (tabs.getTabs().size() > 0) {
                    tabs.getSelectionModel().select(tabs.getTabs().get(tabs.getTabs().size() - 1));
                }
            }
        }
        );

        tabs.setSkin(new CustomTabPaneSkin(tabs));
        //tabs.setStyle("  -fx-skin: \"resources.skins.CustomTabPaneSkin\";");

        tabs.setOnMouseClicked(e -> {
            System.out.println(" clicked tabpane");
        });

        VBox.setVgrow(tabs, Priority.ALWAYS);

        addTab = new Button("Start");
        addExample = new Button("Sample data");
        accessManual = new Button("Manual");
        accessLicense = new Button("License");
        //manualViewer = new ManualViewer(this);

        HBox hbox = new HBox(5);
        hbox.getChildren().addAll(accessLicense, accessManual, addExample, addTab);

        anchor = new AnchorPane();
        anchor.getChildren().addAll(tabs, hbox);
        AnchorPane.setTopAnchor(hbox, 3.0);
        AnchorPane.setRightAnchor(hbox, 5.0);
        AnchorPane.setTopAnchor(tabs, 1.0);
        AnchorPane.setRightAnchor(tabs, 1.0);
        AnchorPane.setLeftAnchor(tabs, 1.0);
        AnchorPane.setBottomAnchor(tabs, 1.0);

        //main stage
        stage = new Stage();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("SlimShadey");
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));

        /*menuBar = new MenuBar();
        menuFile = new Menu("File");
        openFile = new MenuItem("Open Shadey project");
        openFile.setOnAction(e -> {
            fileChooser = new FileChooser();
            fileChooser.setTitle("Open Shadey project");
            File selectedFile = fileChooser.showOpenDialog(stage);
        });
        saveFile = new MenuItem("Save project");
        saveAsFile = new MenuItem("Save as project");

        menuFile.getItems().addAll(openFile, new SeparatorMenuItem(), saveFile, saveAsFile);

        editMenu = new Menu("Edit RUNTIMEtest");
        basicShade = new MenuItem("Basic_Shade_RUNTIMEtest");
        //basicShade tester button
        editMenu.getItems().add(basicShade);

        menuBar.getMenus().addAll(menuFile, editMenu);
        menuBar.prefWidthProperty().bind(stage.widthProperty());

        root.getChildren().add(menuBar);*/
        //anchor.getChildren().add(tabs);
        /*
        mainPane = new Track(null);

        names = new VBox();
        mainPane.getLeftPane().setContent(names);
        anchorEdit = new AnchorPane();
        mainPane.getRightPane().setContent(anchorEdit);
        
        
        
        root.getChildren().add(mainPane);
         */
        //VBox.setVgrow(programLogoGroupBox, Priority.SOMETIMES);
        //VBox.setVgrow(nameScrollPane, Priority.ALWAYS);
        Rectangle2D bd = Screen.getPrimary().getVisualBounds();

        scene = new Scene(anchor, bd.getWidth() * 0.75, bd.getHeight() * 0.75, true);
        scene.setOnDragExited(e -> {
            if (tabs.getSkin() instanceof CustomTabPaneSkin) {
                ((CustomTabPaneSkin) tabs.getSkin()).recolor(null);
                ((CustomTabPaneSkin) tabs.getSkin()).setText(null);
            }
            scene.setCursor(Cursor.DEFAULT);
        });

        scene.setOnDragOver(e -> {
            if (e.getGestureSource() != scene && e.getDragboard().hasFiles()) {
                //System.out.println("files " + e.getDragboard().getFiles().size());
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                e.consume();
                if (tabs.getSkin() instanceof CustomTabPaneSkin) {
                    ((CustomTabPaneSkin) tabs.getSkin()).recolor(Color.BLACK);
                    String detectstr = String.valueOf(e.getDragboard().getFiles().size());
                    detectstr += e.getDragboard().getFiles().size() == 1 ? " file detected" : " files detected";
                    ((CustomTabPaneSkin) tabs.getSkin()).setText(detectstr);
                }
            }
            scene.setCursor(Cursor.WAIT);
        });

        scene.setOnDragDropped(e -> {
            if (e.getGestureSource() != scene && e.getDragboard().hasFiles()) {
                System.out.println("files " + e.getDragboard().getFiles().size());
                if (tabs.getSkin() instanceof CustomTabPaneSkin) {
                    ((CustomTabPaneSkin) tabs.getSkin()).recolor(null);
                    ((CustomTabPaneSkin) tabs.getSkin()).setText(null);
                }

                List<File> inputFiles = e.getDragboard().getFiles();
                e.setDropCompleted(true);
                e.consume();

                Platform.runLater(() -> {
                    readFiles(inputFiles);
                });

            }
        });

        scene.setOnDragDone(e -> {
            if (tabs.getSkin() instanceof CustomTabPaneSkin) {
                ((CustomTabPaneSkin) tabs.getSkin()).recolor(null);
            }
        });

        stage.setScene(scene);
        Font.loadFont(getClass().getResource("unifont.ttf").toExternalForm(), 20);
        scene.getStylesheets().add(getClass().getResource("ApplicationStyle.css").toExternalForm());

        addTab.setFont(MAIN_BUTTON_FONT);
        addExample.setFont(MAIN_BUTTON_FONT);
        accessManual.setFont(MAIN_BUTTON_FONT);
        accessLicense.setFont(MAIN_BUTTON_FONT);
        //scene.addEventFilter(MouseEvent.ANY, e -> System.out.println(e));
        //mainPane.setDividerPosition(0, 0.3);
    }

    private void readFiles(List<File> inputFiles) {
        List<File> invalidFiles = new ArrayList<>();

        boolean isSingleton = inputFiles.size() == 1;
        boolean hasUnaligned = false;
        for (File f : inputFiles) {
            FileFormat fileff = SeqVerterReader.guess(f);
            if (fileff == null) {
                invalidFiles.add(f);
            } else {

                if (!read(f, fileff, f.getName(), isSingleton, isSingleton) && !isSingleton) {
                    invalidFiles.add(f);
                    hasUnaligned = true;
                }
            }
        }

        scene.setCursor(Cursor.DEFAULT);
        if (invalidFiles.size() > 0) {
            if (hasUnaligned && invalidFiles.size() == 1) {
                File unalignedFile = invalidFiles.get(0);
                read(unalignedFile, SeqVerterReader.guess(unalignedFile), unalignedFile.getName(), true, true);
            } else {
                String warnstr
                        = (invalidFiles.size() == 1
                        ? "Your file " + invalidFiles.get(0).getName()
                        + " is either unaligned "
                        + "or in an unrecognizable format\n"
                        : "The following files are"
                        + " either unaligned "
                        + "or in unrecognizable formats\n\n");
                if (invalidFiles.size() != 1) {
                    for (File invf : invalidFiles) {
                        warnstr += invf.getName() + "\n";
                    }
                }
                confirm_popup("Invalid files", warnstr, TextAlignment.LEFT);
            }
        }
    }

    private boolean shownCommand = false;

    public void show(boolean quit) {
        if (quit) {
            Platform.runLater(() -> {
                System.exit(0);
            });
        } else if (!shownCommand) {
            System.err.println("|||");
            stage.show();
            addTab.requestFocus();
            shownCommand = true;
        }
    }

    protected DefaultAlphabets getAlphabets() {
        return this.alphabets;
    }

    private Stage pasteSeqStage;
    private Scene pasteSeqScene;

    private VBox pasteSeqRoot;
    private SplitPane pasteSeqSplitPane;
    private TextArea leftPasteSeqArea;
    private VBox rightPasteSeqRoot;

    private HBox buttonHBox;
    private Button cancelPasteSeq;
    private Button loadPasteSeq;

    private void usePasteSequence() {

        pasteSeqStage = new Stage();
        pasteSeqStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        pasteSeqStage.setTitle("Paste FASTA multiple alignment");

        pasteSeqSplitPane = new SplitPane();
        pasteSeqSplitPane.setOrientation(Orientation.HORIZONTAL);

        leftPasteSeqArea = new TextArea();
        leftPasteSeqArea.setPromptText("Paste FASTA multiple alignment here");
        leftPasteSeqArea.setWrapText(false);

        leftPasteSeqArea.setStyle("-fx-font-family: Consolas;");

        rightPasteSeqRoot = new VBox();
        this.rightPasteSequenceSetup(rightPasteSeqRoot);

        pasteSeqSplitPane.getItems().addAll(leftPasteSeqArea/*, rightPasteSeqRoot*/);

        cancelPasteSeq = new Button("Cancel");
        cancelPasteSeq.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        cancelPasteSeq.setOnAction(e -> {
            pasteSeqStage.close();
        });
        loadPasteSeq = new Button("Load");
        loadPasteSeq.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 14));
        loadPasteSeq.setOnAction(e -> {
            validatePastedSequences();
            //tempHolder = validateSequences(tempHolder);
            if (tempHolder != null) {
                newTab(tempHolder, "New multiple alignment");
                pasteSeqStage.close();
            }
        });
        //loadPasteSeq.setDisable(true);
        buttonHBox = new HBox();
        buttonHBox.getChildren().addAll(cancelPasteSeq, loadPasteSeq);

        pasteSeqRoot = new VBox();
        pasteSeqRoot.getChildren().addAll(pasteSeqSplitPane, buttonHBox);

        pasteSeqScene = new Scene(pasteSeqRoot, 800, 600, true);
        pasteSeqStage.setScene(pasteSeqScene);
        pasteSeqStage.show();

        pasteSeqSplitPane.setPrefHeight(pasteSeqStage.getHeight());
        sequenceSummary.setPrefHeight(pasteSeqStage.getHeight());

        pasteSeqStage.heightProperty().addListener(e -> {
            pasteSeqSplitPane.setPrefHeight(pasteSeqStage.getHeight());
            sequenceSummary.setPrefHeight(pasteSeqStage.getHeight());
        });

        cancelPasteSeq.setPrefWidth(pasteSeqStage.getWidth());
        loadPasteSeq.setPrefWidth(pasteSeqStage.getWidth());
        validatePaste.setPrefWidth(pasteSeqStage.getWidth());
        pasteSeqStage.widthProperty().addListener(e -> {
            cancelPasteSeq.setPrefWidth(pasteSeqStage.getWidth());
            loadPasteSeq.setPrefWidth(pasteSeqStage.getWidth());
            validatePaste.setPrefWidth(pasteSeqStage.getWidth());
        });

        loadPasteSeq.requestFocus();
    }

    private Button validatePaste;
    private VBox sequenceSummary;
    private ScrollPane sequenceSummaryPane;
    private TextField pasteErrFeed;

    public SequenceHolder tempHolder = null;

    private void validatePastedSequences() {
        try {
            tempHolder = textAreaReader_fasta2(leftPasteSeqArea);
            if (tempHolder != null) {
                sequenceSummary.getChildren().removeAll(sequenceSummary.getChildren());

                for (Sequence seq : tempHolder) {
                    VBox seqBox = new VBox();

                    TextField tf = new TextField(seq.name());
                    TextArea ta = new TextArea(seq.seqString());
                    ta.setMinHeight(90);
                    ta.setWrapText(true);

                    seqBox.getChildren().addAll(tf, ta);

                    sequenceSummary.getChildren().add(seqBox);

                }
            }
        } catch (Exception ex) {
            tempHolder = null;
            ex.printStackTrace();
        }
    }

    private void rightPasteSequenceSetup(VBox root) {
        validatePaste = new Button("Validate pasted alignment");

        validatePaste.setOnAction(e -> {
            validatePastedSequences();
        });

        sequenceSummary = new VBox(30);
        sequenceSummaryPane = new ScrollPane();
        sequenceSummaryPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sequenceSummaryPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sequenceSummaryPane.setContent(sequenceSummary);

        pasteErrFeed = new TextField("ERRFEED: EMPTY");
        pasteErrFeed.setEditable(false);

        root.getChildren().addAll(this.validatePaste, this.sequenceSummaryPane, this.pasteErrFeed);
    }

    private int tempAlignCounter = 0;

    public SequenceHolder textAreaReader_fasta2(TextArea textArea) {

        SequenceHolder sh = null;

        try {
            File newinput = File.createTempFile("temp_aln", ".tmp");
            newinput.deleteOnExit();

            Writer fw = new OutputStreamWriter(new FileOutputStream(newinput), StandardCharsets.UTF_8);
            int c;

            String text = textArea.getText();
            int iter = 0;

            while (iter < text.length()) {
                fw.write(text.charAt(iter));
                iter++;
            }

            fw.close();

            read(newinput, FileFormat.fasta, "temp_aln_" + String.valueOf(++tempAlignCounter) + ".fasta", true, true);

            /*
            this section error checks a successfully loaded set of sequences
            -sequences are all same length (including gap characters in length)
            -sequence names are all unique
            -alphabet validation***
            
             */
        } catch (IOException ex) {
            Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sh;
    }

    private File toRead;

    private enum filetype {
        fasta, slim
    }
    private filetype type = null;

    private void checkLSEP(File f) {
        this.glm_validinput.showLoading(true);
        toRead = f;
        boolean has_LSEP = false;

        try {
            Reader fr = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);

            int c;
            while ((c = fr.read()) != -1) {

                //System.out.println(Integer.toHexString(alc) + " " + ((char) alc));
                //the LSEP character causes problems in opal (\u2028)
                if (Integer.toHexString(c).equals("2028")) {
                    has_LSEP = true;
                    //break;
                }
                //System.out.println(Integer.toHexString(alc));
            }

            fr.close();

        } catch (IOException ex) {
            Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (has_LSEP) {
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
            VBox rt = new VBox();
            st.setTitle("LSEP replacement");

            TextFlow notif = new TextFlow();
            notif.setTextAlignment(TextAlignment.CENTER);
            Text notifA = new Text("Your file uses the LSEP (\\u2028) line"
                    + " separator, which will cause an error during rading. Would you "
                    + "like to copy your sequences to a temporary file in"
                    + " which the LSEP is repl"
                    + "aced with standard (\\n) line separators and try again?");

            notif.getChildren().add(notifA);
            notif.setPadding(new Insets(15));

            HBox buttons = new HBox();

            Button ok = new Button("Copy without LSEP and retry");
            ok.setOnAction((ActionEvent e) -> {
                try {
                    File newinput = File.createTempFile("LSEPCROP_".concat(f.getName()), ".tmp");
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
                    //new OpalRunner(newinput, st);
                    //fasta(newinput);
                    fr.close();
                    fw.close();

                    toRead = newinput;
                    st.close();
                } catch (IOException ex) {
                    Logger.getLogger(EditorInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            Button cancel = new Button("Quit");
            cancel.setOnAction(e -> {
                st.close();
            });
            st.setOnHidden(e -> {
                this.glm_validinput.exitLoading();
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
            ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
            st.showAndWait();
        } else {
            this.toRead = f;
            this.glm_validinput.exitLoading();
        }
    }

    public boolean read(File f, FileFormat ff, String tabName, boolean getAlphabetOnRead, boolean doValidate) {

        if (ff == FileFormat.slim) {
            EditorTab et = spm.getData(f);
            if (et != null) {
                newTab(spm.getData(f), tabName);
                return true;
            } else {
                return false;
            }
        } else {
            SequenceHolder msa = null;

            checkLSEP(f);
            this.glm_fileio.showLoading(true);
            f = toRead;

            msa = SeqVerterReader.read(f, ff == null ? FileFormat.guess : ff, alphabets, getAlphabetOnRead);
            this.glm_fileio.exitLoading();

            if (msa.getAlphabet() == null && getAlphabetOnRead) {
                return false;
            }

            msa = validateSequences(f, msa, !doValidate);

            if (msa != null) {
                if (msa.getAlphabet() == null) {
                    // by now, the alphabet must be set; the user quit otherwise
                    return false;
                }
                newTab(msa, tabName);
                return true;
            }
            return false;
        }
    }

    protected void writeShadeyProject(EditorTab t, File f) {
        this.spm.setData(t, f);
    }

    protected ShadeyProjectManager getShadeyProjectManager() {
        return this.spm;
    }

    protected void writeFASTA(File f, SequenceHolder sh) {
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

    //make sure all sequences are of equal length
    private SequenceHolder validateSequences(File inFile, SequenceHolder sequences, boolean quickExit) {
        this.glm_validatemsa.showLoading(true);
        final int seqlen = sequences.get(0).seqString().length();

        for (Sequence seq : sequences) {
            if (seq.seqString().length() != seqlen) {
                if (quickExit) {
                    this.glm_validatemsa.exitLoading();
                    return null;
                }

                Stage st = new Stage();
                st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                VBox rt = new VBox();
                st.setTitle("Sequence validation failure");

                TextFlow notif = new TextFlow();
                notif.setTextAlignment(TextAlignment.CENTER);
                Text notifA = new Text("Your sequences "
                        + (!inFile.getName().contains(".tmp") ? "(" + inFile.getName() + ") " : "")
                        + "are of unequal length. "
                        + "Please align them, or correct any issues with the "
                        + "alignment and try again.");

                notif.getChildren().add(notifA);
                notif.setPadding(new Insets(15));

                HBox buttons = new HBox(10);

                Button quit = new Button("Cancel");
                quit.setOnAction(e -> {
                    st.close();
                });

                Button align = new Button("Align with Opal");
                align.setOnAction(e -> {
                    new OpalAgreement(true, this, inFile);
                    st.close();
                });

                st.setOnHidden(e -> {
                    this.glm_validatemsa.exitLoading();
                    this.glm_graphic.exitLoading();
                });
                buttons.getChildren().addAll(quit, align);

                buttons.setPadding(new Insets(15));

                rt.getChildren().addAll(notif, buttons);
                VBox.setVgrow(notif, Priority.ALWAYS);

                Scene sc = new Scene(rt, 500, 135, true);
                st.setScene(sc);
                st.widthProperty().addListener(o -> {
                    for (int i = 0; i < buttons.getChildren().size(); i++) {
                        if (buttons.getChildren().get(i) instanceof Button) {
                            ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                        }
                    }
                });
                ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
                st.showAndWait();

                return null;
            }
        }
        this.glm_validatemsa.exitLoading();
        if (sequences.getAlphabet() == null) {
            sequences.setAlphabet(SeqVerterReader.guessAlphabet2(inFile.getName(), sequences, alphabets));
        }
        return sequences;
    }

    private boolean PROSITE_LICENSE = false;
    public DatabaseSearcher_ProSiteDB_Mar2020 dbs = null;

    public boolean isPrositeAgreed() {
        return PROSITE_LICENSE;
    }

    public void prositeAgreementAccepted() {
        PROSITE_LICENSE = true;
    }

    private boolean HMMER_LICENSE = false;

    public boolean isHmmerAgreed() {
        return HMMER_LICENSE;
    }

    public void hmmerAgreementAccepted() {
        HMMER_LICENSE = true;
    }

    private boolean OPAL_LICENSE = false;

    public boolean isOpalAgreed() {
        return OPAL_LICENSE;
    }

    public void opalAgreementAccepted() {
        OPAL_LICENSE = true;
    }

    private void confirm_popup(String title, String message) {
        confirm_popup(title, message, null);
    }

    private void confirm_popup(String title, String message, TextAlignment ta) {
        Stage allGapsStage2 = new Stage();
        allGapsStage2.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        allGapsStage2.setTitle(title);

        HBox textBox = new HBox();
        textBox.setAlignment(Pos.CENTER);
        Label graphicWarningText2 = new Label(message);
        //graphicWarningText2.setWrappingWidth(550);
        graphicWarningText2.setMaxWidth(550);
        graphicWarningText2.setWrapText(true);
        graphicWarningText2.setTextAlignment(ta == null ? TextAlignment.CENTER : ta);
        graphicWarningText2.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(graphicWarningText2);

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
        rootBox.setPrefWidth(550);
        rootBox.setPadding(new Insets(10, 10, 10, 10));
        rootBox.setAlignment(Pos.CENTER);
        rootBox.getChildren().addAll(textBox, buttonBox);

        Scene mwpScene = new Scene(rootBox);
        allGapsStage2.setScene(mwpScene);
        allGapsStage2.initModality(Modality.APPLICATION_MODAL);
        allGapsStage2.setAlwaysOnTop(true);
        allGapsStage2.setMaxWidth(600);
        allGapsStage2.show();
        no2.requestFocus();
    }

}
