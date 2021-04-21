/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade.customregex;

import edu.tcnj.biology.slimshadey.editor.EditorTab;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import shahavi.bio471.scanprosite.database.pattern.PrositePattern;
import shahavi.bio471.scanprosite.database.result.Hit;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class CustomRegexShadeRemote {

    public static void main(String[] args) {
        // main test method

    }

    private boolean agree = false;

    private VisualMultipleSequenceAlignment vmsa;

    private Stage stage;
    private Scene scene;
    private AnchorPane root;

    private EditorTab originator;

    public CustomRegexShadeRemote(EditorTab et) {
        this.originator = et;
        this.vmsa = originator.getVMSA();

        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        stage.setTitle("Prosite pattern based shading: " + originator.getText());
        //stage.setAlwaysOnTop(true);

        root = getRoot();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private boolean isInside = false;
    private boolean isPressed = false;

    private Stage docstage;

    private AnchorPane getRoot() {
        root = new AnchorPane();

        VBox rootBox = new VBox(8);
        rootBox.setAlignment(Pos.CENTER);
        rootBox.setPadding(new Insets(12));

        HBox rootInfoBox = new HBox();
        Label rootLabel = new Label("Scan against custom Prosite-style regex  ");
        Label rootLabelInteractive = new Label("(read more)");
        rootInfoBox.getChildren().addAll(rootLabel, rootLabelInteractive);
        rootInfoBox.setPadding(new Insets(0, 0, 12, 0));
        rootLabelInteractive.setCursor(Cursor.HAND);

        rootLabelInteractive.setOnMouseEntered(e -> {
            isInside = true;
            rootLabelInteractive.setStyle(" -fx-underline: true; -fx-text-fill: #32e7ff;");
            if (isPressed) {
                rootLabelInteractive.setStyle(" -fx-underline: true; -fx-text-fill: indigo;");
            }
        });

        rootLabelInteractive.setOnMouseExited(e -> {
            isInside = false;
            if (!isPressed) {
                rootLabelInteractive.setStyle(" -fx-underline: false; -fx-text-fill: black;");
            }
        });

        rootLabelInteractive.setOnMousePressed(e -> {
            isPressed = true;
            rootLabelInteractive.setStyle(" -fx-underline: true; -fx-text-fill: indigo;");
        });

        rootLabelInteractive.setOnMouseReleased(e -> {
            isPressed = false;
            if (isInside) {
                rootLabelInteractive.setStyle(" -fx-underline: true; -fx-text-fill: #32e7ff;");
            } else {
                rootLabelInteractive.setStyle(" -fx-underline: false; -fx-text-fill: black;");
            }
        });

        rootLabelInteractive.setOnMouseClicked(e -> {
            if (docstage != null) {
                docstage.show();
            } else {
                docstage = new Stage();
docstage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                docstage.setAlwaysOnTop(true);
                docstage.setTitle("About Prosite-regex");
                VBox rootout = new VBox();
                TextArea ta = new TextArea();
                HBox.setHgrow(ta, Priority.ALWAYS);
                VBox.setVgrow(ta, Priority.ALWAYS);
                ta.setEditable(false);
                ta.setWrapText(true);
                rootout.getChildren().add(ta);
                docstage.setScene(new Scene(rootout, 600, 600, true));
                docstage.show();

                ta.setStyle("-fx-font-family: 'consolas';");

                ta.appendText(DOCUMENTATION);
            }
        });

        Button addRegex = new Button("Add a regular expression");
        //addRegex.setAlignment(Pos.CENTER);
        //addRegex.prefWidthProperty().bind(stage.widthProperty());
        //HBox.setHgrow(addRegex, Priority.ALWAYS);
        VBox inputArea = new VBox();

        final int tfwidth = 260;
        HBox regexBox = new HBox(5);
        TextField regexField = new TextField();
        regexField.setMinWidth(tfwidth);
        regexField.setStyle("-fx-font: 12 consolas;");
        regexField.setPromptText("(type expression here)");

        Button deleteRegex = new Button("Delete");
        deleteRegex.setOnAction(ev -> {
            inputArea.getChildren().remove(regexBox);
        });

        regexBox.getChildren().addAll(regexField, deleteRegex);
        inputArea.getChildren().add(regexBox);

        addRegex.setOnAction(e -> {
            HBox regexBox2 = new HBox(5);
            TextField regexField2 = new TextField();
            regexField2.setMinWidth(tfwidth);
            regexField2.setStyle("-fx-font: 12 consolas;");
            regexField2.setPromptText("(type expression here)");

            Button deleteRegex2 = new Button("Delete");
            deleteRegex2.setOnAction(ev -> {
                //stage.setHeight(stage.getHeight() - regexBox2.getHeight());
                inputArea.getChildren().remove(regexBox2);
                scene.setRoot(new VBox());
                scene = new Scene(root);
                stage.setScene(scene);
            });

            regexBox2.getChildren().addAll(regexField2, deleteRegex2);
            inputArea.getChildren().add(regexBox2);
            scene.setRoot(new VBox());
            scene = new Scene(root);
            stage.setScene(scene);
        });

        Button exec = new Button("Run scan");
        //exec.setAlignment(Pos.CENTER);
        //exec.prefWidthProperty().bind(stage.widthProperty());
        // HBox.setHgrow(exec, Priority.ALWAYS);
        exec.setOnAction(e -> {
            List<String> patternStrs = new ArrayList<>();
            for (Node n : inputArea.getChildren()) {
                if (n instanceof HBox) {
                    Node nn = ((HBox) n).getChildren().get(0);
                    if (nn instanceof TextField) {
                        patternStrs.add(((TextField) nn).getText());
                    }
                }
            }
            this.parsePatterns(patternStrs);
            stage.hide();
        });

        addRegex.prefWidthProperty().bind(inputArea.widthProperty());
        exec.prefWidthProperty().bind(inputArea.widthProperty());

        rootBox.getChildren().addAll(rootInfoBox, addRegex, inputArea, exec);
        root.getChildren().add(rootBox);
        return root;
    }
    HashMap<Integer, List<CustomRegexHit>> results;
    HashMap<Integer, String> degappedSequences;

    private void getResults() {
        HashMap<Integer, List<CustomRegexHit>> res = new HashMap<>();
        HashMap<Integer, String> degapped = new HashMap<>();
        Task task = new Task<Void>() {
            @Override
            protected Void call() {
                for (int seqIndex = 0; seqIndex < vmsa.sequenceNumber(); seqIndex++) {
                    StringBuilder currentSeq = new StringBuilder();
                    for (int colIndex = 0; colIndex < vmsa.sequenceLength(); colIndex++) {
                        VisualBioChar vbc = vmsa.getVBC(seqIndex, colIndex);
                        currentSeq.append(vbc.getChar() != '-' ? vbc.getChar() : "");
                    }
                    String scanSeq = currentSeq.toString();
                    degapped.put(seqIndex, scanSeq);
                    List<CustomRegexHit> hitList = new ArrayList<>();
                    for (int patternIndex = 0; patternIndex < patterns.size(); patternIndex++) {
                        PrositePattern pt = patterns.get(patternIndex);
                        try {
                            CustomRegexHit crh = new CustomRegexHit(pt.PATTERN, pt.hitList(scanSeq));
                            //res.put(seqIndex, crh);
                            if (!crh.HIT_LIST.isEmpty()) {
                                hitList.add(crh);
                            }
                        } catch (Exception e) {
                            // something failed while running here
                            String msg = "Failed while scanning " + pt.PATTERN
                                    + " against " + vmsa.getSequenceName(seqIndex);
                            errorPopup_graphicsWindow(msg);
                            e.printStackTrace();
                        }
                    }
                    res.put(seqIndex, hitList);
                }
                results = res;
                degappedSequences = degapped;
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            this.showSequenceList(vmsa);
        });

        new Thread(task).start();

    }

    private final boolean REG_DEF = true;
    private Scene popupScene;

    private GraphicSequenceViewerCustom gsv;

    public void showSequenceList(VisualMultipleSequenceAlignment vmsa) {
        Stage popup = new Stage();
popup.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        popup.setTitle("Graphic Sequence View");

        Button applyShade = new Button("Apply shading to alignment");
        applyShade.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 12));
        HBox.setHgrow(applyShade, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane();
        VBox intermediate = new VBox();
        gsv = new GraphicSequenceViewerCustom();
        intermediate.getChildren().add(gsv);
        sp.setContent(intermediate);

        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            gsv.addSequence(600, 40, k, Color.LIGHTBLUE, degappedSequences.get(k), results.get(k), vmsa.getSequenceName(k)/* , cdl, tf*/);
        }

        applyShade.setOnAction(eve -> {
            shadePopup(vmsa, gsv.getgraphics());
        });

        VBox mainRoot = new VBox();
        mainRoot.getChildren().addAll(applyShade, sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        popupScene = new Scene(mainRoot, 660, 500);
        popupScene.widthProperty().addListener(ev -> {
            gsv.getgraphics().stream().forEach((gs) -> {
                gs.horizontalResize(popupScene.getWidth());
            });
            applyShade.setPrefWidth(popupScene.getWidth());
        });

        popup.setScene(popupScene);

        //cdl.await();
        popup.show();
        applyShade.setPrefWidth(popupScene.getWidth());
        if (REG_DEF) {
            this.regularizeAll(gsv);
        }
    }

    private void regularizeAll(GraphicSequenceViewerCustom gsv) {
        HashMap<String, Color> accessionColorMap = new HashMap<>();

        List<GraphicSequenceCustom> graphics = gsv.getgraphics();
        for (GraphicSequenceCustom gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                GraphicHitCustom gh = gs.getGraphicHit(k);
                accessionColorMap.put(gh.h.PATTERNSTR, gh.c);
            }
        }

        for (GraphicSequenceCustom gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                GraphicHitCustom gh = gs.getGraphicHit(k);
                gh.updateColor(accessionColorMap.get(gh.h.PATTERNSTR));
            }
        }
    }

    public void shadePopup(VisualMultipleSequenceAlignment vmsa, List<GraphicSequenceCustom> sequences) {

        Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox rt = new VBox();
        st.setTitle("ScanJam shading");

        TextFlow notif = new TextFlow();
        notif.setTextAlignment(TextAlignment.CENTER);
        Text notifA = new Text("This will produce shading for all sequences for "
                + "which hits are found and allowed.");

        notif.getChildren().add(notifA);
        notif.setPadding(new Insets(15));

        HBox buttons = new HBox();

        Button quit = new Button("Cancel");
        quit.setOnAction(e -> {
            st.close();
        });
        Button shade = new Button("Shade");
        shade.setOnAction(e -> {
            if (vmsa.clearShading_popup()) {
                st.close();
                shade(vmsa, sequences);
            }
        });
        buttons.getChildren().addAll(quit, shade);

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
        //st.setAlwaysOnTop(true);
        
        st.initModality(Modality.APPLICATION_MODAL);
        st.show();
    }

    public void shade(VisualMultipleSequenceAlignment vmsa, List<GraphicSequenceCustom> sequences) {
        int numseq = vmsa.getSequencesBox().getChildren().size();
        if (numseq != sequences.size()) { // data CANNOT MISMATCH
            this.errorPopup_graphicsWindow("The number of sequences scanned by ScanJam "
                    + "is not the same as the number of sequences in the present "
                    + "visual multiple sequence alignment. Ordering cannot be "
                    + "guaranteed, no shading will be performed. Please retry "
                    + "scanning with ScanJam.");
        } else {

            for (int i = 0; i < numseq; i++) { //iterate over each sequence
                /*when shading the ScanProsite results, need to ensure 
                gaps are not shaded. an external iterator that does not increment 
                if the present character is a '-' gap char is needed*/
                GraphicSequenceCustom currentGS = sequences.get(i);

                for (int j = 0; j < currentGS.matchCount(); j++) { //for each hit by acc
                    GraphicHitCustom hit = currentGS.getGraphicHit(j);
                    for (int k = 0; k < hit.h.HIT_LIST.size(); k++) { //for each op in hit
                        GraphicSequenceMatchCustom gsm = hit.gsm.get(k);
                        if (gsm.isAllowed()) {
                            String backHex = vmsa.hexifyColorFX(gsm.getColor());
                            String foreHex = vmsa.decideForeGroundColor(backHex);
                            OrderedPair op = hit.h.HIT_LIST.get(k);
                            int begin = op.X;
                            int end = op.Y - 1;
                            int gapIter = 0;
                            // now iterate over sequence, find ungapped match region and shade
                            innermost_loop_AVS:
                            for (int s = 0; s < vmsa.getSequence(i).getChildren().size(); s++) {
                                VisualBioChar currentVBC = vmsa.getVBC(i, s);
                                if (currentVBC.getChar() != '-') {
                                    if (gapIter < begin) {
                                        //continue; DO NOT CONTINUE UPDATE ITERATOR ALWAYS
                                    } else if (gapIter >= begin && gapIter <= end) {
                                        //found corresponding stretch
                                        currentVBC.setCurrentStyle(vmsa.generateFont(backHex, foreHex, false, false), backHex, foreHex);
                                    } else if (gapIter > end) {
                                        break innermost_loop_AVS;
                                    }
                                    //increment gapIter++ this was NOT a gap char
                                    gapIter++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void errorPopup_graphicsWindow(String message) {
        Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox rt = new VBox();
        st.setTitle("Error in graphics window");

        TextFlow notif = new TextFlow();
        notif.setTextAlignment(TextAlignment.CENTER);
        Text notifA = new Text(message);

        notif.getChildren().add(notifA);
        notif.setPadding(new Insets(15));

        HBox buttons = new HBox();

        Button quit = new Button("Ok");
        quit.setOnAction(e -> {
            st.close();
        });

        buttons.getChildren().addAll(quit);

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
        st.setAlwaysOnTop(true);
        st.initModality(Modality.APPLICATION_MODAL);
        st.show();
    }

    private List<PrositePattern> patterns;

    private void parsePatterns(List<String> inputs) {
        List<PrositePattern> psps = new ArrayList<>();
        String currentstr = "";
        try {
            for (String input : inputs) {
                currentstr = input;
                PrositePattern psp = new PrositePattern(input);
                if (psp.IS_VALID) {
                    psps.add(new PrositePattern(input));
                } else {
                    throw new Exception("Invalid Prosite regex: " + currentstr);
                }

            }
        } catch (Exception e) {
            // error popup here
            // give the user back currentstr variable, as it has the problematic
            // regular expression
        }
        patterns = (!psps.isEmpty() ? psps : null);
        boolean allValid = true;
        for (PrositePattern psp : patterns) {
            if (!psp.IS_VALID) {
                allValid = false;
                String msg = "The pattern \"" + psp.PATTERN + "\" is not valid. "
                        + "Please check the pattern syntax and try again.";
                errorPopup_graphicsWindow(msg);
            }
        }
        if (allValid) {
            this.getResults();
        }
    }

    public void hide() {
        stage.hide();
    }

    public void unHide(boolean paint) {
        stage.show();
    }

    private final String DOCUMENTATION = "NOTE - SlimShadey will test and display "
            + "patterns in the order they are given. If one of your patterns is a "
            + "sub-pattern of another pattern, then place the smaller pattern after "
            + "the larger pattern, so that the smaller pattern will still be visible, "
            + "and overlaid the larger pattern, when both are found in a sequence.\n\n"
            + "Pattern syntax\n"
            + "The standard IUPAC one letter code for the amino acids is used in PROSITE.\n"
            + "The symbol 'x' is used for a position where any amino acid is accepted.\n"
            + "Ambiguities are indicated by listing the acceptable amino acids for a given position, between square brackets '[ ]'. For example: [ALT] stands for Ala or Leu or Thr.\n"
            + "Ambiguities are also indicated by listing between a pair of curly brackets '{ }' the amino acids that are not accepted at a given position. For example: {AM} stands for all any amino acid except Ala and Met.\n"
            + "Each element in a pattern is separated from its neighbor by a '-'.\n"
            + "Repetition of an element of the pattern can be indicated by following that element with a numerical value or, if it is a gap ('x'), by a numerical range between parentheses. \n"
            + "Examples:\n"
            + "x(3) corresponds to x-x-x\n"
            + "x(2,4) corresponds to x-x or x-x-x or x-x-x-x\n"
            + "A(3) corresponds to A-A-A\n"
            + "When a pattern is restricted to either the N- or C-terminal of a sequence, that pattern respectively starts with a '<' symbol or ends with a '>' symbol. \n"
            + "In some rare cases (e.g. PS00267 or PS00539), '>' can also occur inside square brackets for the C-terminal element. 'F-[GSTV]-P-R-L-[G>]' is equivalent to 'F-[GSTV]-P-R-L-G' or 'F-[GSTV]-P-R-L>'.\n"
            + "Note:\n"
            + "Ranges can only be used with with 'x', for instance 'A(2,4)' is not a valid pattern element.\n"
            + "Ranges of 'x' are not accepted at the beginning or at the end of a pattern unless resticted/anchored to respectively the N- or C-terminal of a sequence, for instance 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)' is not accepted but 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)>' is.\n"
            + "     \n"
            + "    \n"
            + "Extended syntax for ScanProsite:\n"
            + "If your pattern does not contain any ambiguous residues, you don't need to specify separation with '-'. \n"
            + "Example: M-A-S-K-E can be written as MASKE. \n"
            + "It means that in such a case you can directly copy/paste peptide sequences into the textfield.\n"
            + "To search all sequences which do not contain a certain amino acid, e.g. Cys, you can use <{C}*>.";
}
