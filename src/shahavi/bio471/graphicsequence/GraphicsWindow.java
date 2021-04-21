/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.graphicsequence;

import edu.tcnj.biology.slimshadey.editor.VisualBioChar;
import javafx.scene.image.Image;
import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import edu.tcnj.biology.slimshadey.editor.regexshade.RemoteScanPrositeRunner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
import javafx.stage.StageStyle;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;
import shahavi.bio471.scanprosite.database.result.Hit;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class GraphicsWindow {

    public GraphicsWindow() {
        this.FASTAFiles = FXCollections.observableArrayList();

        this.FASTAFiles.addListener(new ListChangeListener<File>() {
            @Override
            public void onChanged(Change<? extends File> c) {
                if (c.next()) {
                    /*for (File f : c.getAddedSubList()) {
                        System.out.println(f.getAbsoluteFile());
                    }*/
                }
            }
        });
    }

    public GraphicSequenceViewer getGSV() {
        return gsv;
    }

    public ObservableList<File> inputFiles() {
        return this.FASTAFiles;
    }

    private GraphicSequenceViewer gsv;
    private ObservableList<File> FASTAFiles;
    private List<UtilitySeq1> sequenceList;

    public void createSeqPopup_online(VisualMultipleSequenceAlignment vmsa,
            List<String> names, List<String> seqlist,
            boolean highSensitivity, boolean includeProfiles, boolean skipHighProb) {
        RemoteScanPrositeRunner rspr = new RemoteScanPrositeRunner(this, vmsa, highSensitivity, includeProfiles, skipHighProb);
        rspr.feedStringInput(names, seqlist);
    }

    public void createSeqPopup_local(VisualMultipleSequenceAlignment vmsa,
            DatabaseSearcher_ProSiteDB_Mar2020 dbs,
            List<String> names, List<String> seqlist, boolean filterLowComplexity) throws InterruptedException {

        CountDownLatch cdl = new CountDownLatch(1);

        Task task = new Task<Void>() {
            @Override
            public Void call() {

                long startTime = System.nanoTime();
                /*
        CountDownLatch cdl = new CountDownLatch(FASTAFiles.size());
        ThreadFactory tf = Executors.defaultThreadFactory();
                 */
                List<UtilitySeq1> sequences = new ArrayList<>();

                for (int k = 0; k < names.size(); k++) {
                    UtilitySeq1 us = new UtilitySeq1();
                    us.seq = seqlist.get(k);
                    us.name = names.get(k);
                    sequences.add(us);
                }

                //search and add sequences here - BEGIN
                for (int k = 0; k < sequences.size(); k++) {
                    System.out.println(sequences.get(k).name + " " + sequences.get(k).seq);
                    List<Hit> hits = dbs.fullPatternSearch(sequences.get(k).seq, true, false);
                    if (hits != null && !hits.isEmpty()) {
                        sequences.get(k).hits = hits;
                    }

                    updateProgress(k, sequences.size());
                    String mess = "Scanning";

                    for (int l = 0; l < Math.random() * 3 + 2; l++) {
                        mess = mess + ".";
                    }

                    updateMessage(mess);
                }

                sequenceList = sequences;
                //search here - END

                long estimatedTime = System.nanoTime() - startTime;
                cdl.countDown();

                return null;
            }
        };

        loadingPopup(true, task.progressProperty(), task.messageProperty());
        task.setOnSucceeded(e -> {
            loadingPopup(false, null, null);
            showSequenceList(vmsa, sequenceList);
        });

        task.setOnCancelled(e -> {
            loadingPopup(false, null, null);
        });

        task.setOnFailed(e -> {
            loadingPopup(false, null, null);
        });

        new Thread(task).start();

    }

    private final boolean REG_DEF = true;

    public void showSequenceList(VisualMultipleSequenceAlignment vmsa, List<UtilitySeq1> sequenceList) {
        Stage popup = new Stage();
popup.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        popup.setTitle("Graphic Sequence View");

        Button applyShade = new Button("Apply shading to alignment");
        applyShade.setFont(Font.font("Arial", FontWeight.BLACK, FontPosture.REGULAR, 12));
        HBox.setHgrow(applyShade, Priority.ALWAYS);

        ScrollPane sp = new ScrollPane();
        VBox intermediate = new VBox();
        gsv = new GraphicSequenceViewer(vmsa.getEditorTab().THE_PROGRAM.dbs);
        intermediate.getChildren().add(gsv);
        sp.setContent(intermediate);

        for (int k = 0; k < sequenceList.size(); k++) {
            gsv.addSequence(600, 40, k, Color.LIGHTBLUE, sequenceList.get(k).seq, sequenceList.get(k).hits, sequenceList.get(k).name/* , cdl, tf*/);
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

    private void regularizeAll(GraphicSequenceViewer gsv) {
        HashMap<String, Color> accessionColorMap = new HashMap<>();

        List<GraphicSequence> graphics = gsv.getgraphics();
        for (GraphicSequence gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                GraphicHit gh = gs.getGraphicHit(k);
                accessionColorMap.put(gh.h.ACCESSION, gh.c);
            }
        }

        for (GraphicSequence gs : graphics) {
            for (int k = 0; k < gs.matchCount(); k++) {
                GraphicHit gh = gs.getGraphicHit(k);
                gh.updateColor(accessionColorMap.get(gh.h.ACCESSION));
            }
        }
    }

    private Stage loadingStage = null;

    private ProgressBar pb;
    private ProgressIndicator pi;
    private int ct = 0;

    private Label status;

    private void loadingPopup(boolean show, ReadOnlyDoubleProperty bind, ReadOnlyStringProperty indicate) {

        if (show && loadingStage == null) {
            loadingStage = new Stage();
loadingStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));

            loadingStage.setTitle("Progress Controls");

            pb = new ProgressBar(0);
            pi = new ProgressIndicator(0);
            pb.progressProperty().bind(bind);
            pi.progressProperty().bind(bind);

            ct = (int) (Math.random() * 360);
            pi.progressProperty().addListener(e -> {
                String colorstr = hexifyColorFX(Color.hsb(ct * 10, 1, 1));
                pb.setStyle(" -fx-accent: " + hexifyColorFX(Color.hsb(ct * 10, 1, 1)) + ";");
                pi.setStyle(" -fx-accent: " + hexifyColorFX(Color.hsb(360 - ct * 10, 1, 1)) + ";");
                ct++;
            });

            status = new Label();
            status.setText(String.format("%1$" + 30 + "s", "Initializing..."));
            status.textProperty().bind(indicate);
            status.setStyle("-fx-font-family: Monospace;");

            HBox box = new HBox();
            box.alignmentProperty().set(Pos.CENTER);
            VBox hb = new VBox(10);

            hb.getChildren().addAll(pb, pi, status);
            box.getChildren().add(hb);
            box.setPadding(new Insets(20));
            Scene scene = new Scene(box);
            loadingStage.setScene(scene);
            //loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.setResizable(false);
            loadingStage.initModality(Modality.APPLICATION_MODAL);
            loadingStage.setAlwaysOnTop(true);
            loadingStage.show();
            loadingStage.setResizable(false);
        } else if (show) {
            loadingStage.show();
        } else if (!show && loadingStage != null) {
            loadingStage.hide();
            loadingStage = null;
            pb = null;
            pi = null;
            status = null;
        }
    }

    public double sceneWidth() {
        if (this.popupScene != null) {
            return this.popupScene.getWidth();
        }
        return 0;
    }

    private Scene popupScene;

    public String hexifyColorFX(Color c) {
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255f),
                (int) (c.getGreen() * 255f),
                (int) (c.getBlue() * 255f));
    }

    public void shadePopup(VisualMultipleSequenceAlignment vmsa, List<GraphicSequence> sequences) {

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

    public void shade(VisualMultipleSequenceAlignment vmsa, List<GraphicSequence> sequences) {
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
                GraphicSequence currentGS = sequences.get(i);

                for (int j = 0; j < currentGS.matchCount(); j++) { //for each hit by acc
                    GraphicHit hit = currentGS.getGraphicHit(j);
                    for (int k = 0; k < hit.h.HIT_LIST.size(); k++) { //for each op in hit
                        GraphicSequenceMatch gsm = hit.gsm.get(k);
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

}
