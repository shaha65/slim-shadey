/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
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

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_GraphicsWindow {

    private hmmscan_Runner RUNNER;

    public hmmscan_GraphicsWindow(hmmscan_Runner parent) {
        this.RUNNER = parent;
    }

    private hmmscan_GraphicSequenceViewer gsv;
    private HashMap<String, String> seqMap;
    private List<String> orderedSeqNames;

    public void hmmscan_popup(VisualMultipleSequenceAlignment vmsa) {
        //extract sequences and names
        seqMap = new HashMap<>();
        orderedSeqNames = new ArrayList<>();
        VBox namesBox = vmsa.getSequenceNamesBox();
        VBox seqBox = vmsa.getSequencesBox();

        //create convenience sequence objects
        for (int k = 0; k < namesBox.getChildren().size(); k++) {
            String name = ((Label) namesBox.getChildren().get(k)).getText();
            orderedSeqNames.add(name);
            StringBuilder seqBuilder = new StringBuilder("");
            //must degap alignment sequence
            for (int i = 0; i < ((HBox) seqBox.getChildren().get(k)).getChildren().size(); i++) {
                char charSeq = ((Label) ((HBox) seqBox.getChildren().get(k)).getChildren().get(i)).getText().charAt(0);
                if (charSeq != '-') { //if not a gap, add to sequence
                    seqBuilder.append(charSeq);
                }
            }
            String seqStr = seqBuilder.toString();
            seqMap.put(name, seqStr);
        }
        RUNNER.search_hmmscan(seqMap);
    }

    private Scene popupScene;

    private final boolean REG_DEF = true;

    public void showSequenceList(VisualMultipleSequenceAlignment vmsa, HashMap<String, List<hmmscan_Hit>> hitList) {
        Stage popup = new Stage();
popup.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        popup.initModality(Modality.WINDOW_MODAL);
        popup.setTitle("Graphic Sequence View");

        Button applyShade = new Button("Apply shading to alignment");
        applyShade.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 12));
        HBox.setHgrow(applyShade, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane();
        VBox intermediate = new VBox();
        gsv = new hmmscan_GraphicSequenceViewer();
        intermediate.getChildren().add(gsv);
        sp.setContent(intermediate);

        for (int k = 0; k < orderedSeqNames.size(); k++) {
            String nkey = orderedSeqNames.get(k);
            gsv.addSequence(600, 40, k, Color.LIGHTBLUE, seqMap.get(nkey),
                    hitList.get(nkey), nkey/* , cdl, tf*/);
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

    private void regularizeAll(hmmscan_GraphicSequenceViewer gsv) {
        HashMap<String, Color> accessionColorMap = new HashMap<>();

        List<hmmscan_GraphicSequence> graphics = gsv.getgraphics();
        for (hmmscan_GraphicSequence gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                hmmscan_GraphicHit gh = gs.getGraphicHit(k);
                accessionColorMap.put(gh.h.pfam_acc, gh.c);
            }
        }

        for (hmmscan_GraphicSequence gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                hmmscan_GraphicHit gh = gs.getGraphicHit(k);
                gh.updateColor(accessionColorMap.get(gh.h.pfam_acc));
            }
        }
    }

    public void shadePopup(VisualMultipleSequenceAlignment vmsa, List<hmmscan_GraphicSequence> sequences) {

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
        st.setAlwaysOnTop(true);
        st.initModality(Modality.APPLICATION_MODAL);
        st.show();
    }

    public void shade(VisualMultipleSequenceAlignment vmsa, List<hmmscan_GraphicSequence> sequences) {
        int numseq = vmsa.getSequencesBox().getChildren().size();
        if (numseq != sequences.size()) { // data CANNOT MISMATCH
            this.errorPopup_graphicsWindow("The number of sequences scanned by hmmscan "
                    + "is not the same as the number of sequences in the present "
                    + "visual multiple sequence alignment. Ordering cannot be "
                    + "guaranteed, no shading will be performed. Please retry "
                    + "scanning with ScanJam.");
        } else {

            for (int i = 0; i < numseq; i++) { //iterate over each sequence
                /*when shading the ScanProsite results, need to ensure 
                gaps are not shaded. an external iterator that does not increment 
                if the present character is a '-' gap char is needed*/
                hmmscan_GraphicSequence currentGS = sequences.get(i);

                for (int j = 0; j < currentGS.matchCount(); j++) { //for each hit by acc
                    hmmscan_GraphicHit hit = currentGS.getGraphicHit(j);
                    for (int k = 0; k < hit.h.displayhits.size(); k++) { //for each op in hit
                        hmmscan_GraphicSequenceMatch gsm = hit.gsm.get(k);
                        if (gsm.isAllowed()) {
                            String backHex = vmsa.hexifyColorFX(gsm.getColor());
                            String foreHex = vmsa.decideForeGroundColor(backHex);
                            int[] op = hit.h.displayhits.get(k);
                            int begin = op[0];
                            int end = op[1] - 1;
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
}
