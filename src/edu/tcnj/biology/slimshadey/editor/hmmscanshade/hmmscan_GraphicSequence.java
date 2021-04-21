/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_GraphicSequence extends Group {

    private hmmscan_GraphicSequenceViewer gsvorig;

    private ArrayList<hmmscan_GraphicSequenceMatch> graphicList;
    private List<hmmscan_GraphicHit> match;
    private Rectangle base;
    private String FASTA;

    private final double RECTANGLE_HEIGHT = 25;

    private double width;
    private final Color defCol;

    private double transx = 30;
    private double transy;

    private hmmscan_ColorableLabel cl;

    private final int SUBSEQLENGTH = 30;

    /**
     * class to represent single sequence graphically
     *
     * @param width
     * @param graphicSeparator
     * @param defCol
     * @param index
     * @param FASTA
     * @param match
     * @param originator
     */
    public hmmscan_GraphicSequence(double width, double graphicSeparator, Color defCol,
            int index, String FASTA, List<hmmscan_GraphicHit> match, String name,
            hmmscan_GraphicSequenceViewer originator) {
        super();
        this.FASTA = FASTA;
        transy = (((double) graphicSeparator) * (index + 1)) + (RECTANGLE_HEIGHT * index);
        this.width = width;
        this.defCol = defCol;
        this.match = match;
        gsvorig = originator;

        Label nameLabel = new Label(name);

        nameLabel.setTranslateX(transx);
        nameLabel.setTranslateY(transy + RECTANGLE_HEIGHT);
        this.getChildren().add(nameLabel);

        nameLabel.setCursor(Cursor.HAND);
        String origfont = nameLabel.getStyle();
        nameLabel.setOnMouseEntered(e -> {
            nameLabel.setStyle("-fx-underline: true; -fx-text-fill: blue;");
        });

        nameLabel.setOnMouseExited(e -> {
            nameLabel.setStyle(origfont);
        });

        graphicList = new ArrayList<>();
        createGraphics(FASTA, match);
    }

    private void createGraphics(String sequence, List<hmmscan_GraphicHit> match) {

        //CountDownLatch cdl = new CountDownLatch(match.size());
        base = new Rectangle(transx, transy, width, RECTANGLE_HEIGHT);
        base.setFill(defCol);
        this.getChildren().add(base);

        for (int k = 0; k < match.size(); k++) {
            hmmscan_GraphicHit gh = match.get(k);
            Color c = gh.c;
            List<int[]> opl = gh.h.displayhits;
            System.out.println(gh.h.pfam_acc + " " + gh.c.toString() + " asdf");

            for (int j = 0; j < opl.size(); j++) {
                int[] op = opl.get(j);
                double x = (double) transx + (((double) op[0]) / ((double) sequence.length()) * (double) this.width);
                System.out.println("X:asdfasdf: " + x);
                double y = transy;
                double w = ((((double) op[1]) - ((double) op[0])))
                        / ((double) sequence.length()) * this.width;
                double h = this.RECTANGLE_HEIGHT;
                hmmscan_GraphicSequenceMatch attempt = new hmmscan_GraphicSequenceMatch(x, y, w, h, c, gh.h.pfam_acc, null, null, -1, -1);
                //attempt.setMouseTransparent(true);
                attempt.setOnMouseMoved(e -> update(e.getX()));
                attempt.setOnMouseExited(e -> {
                    attempt.setFill(gh.c);
                    if (cl != null) {
                        this.getChildren().remove(cl);
                    }
                });
                getChildren().add(attempt);
                graphicList.add(attempt);
                attempt.setOnMouseEntered(e -> attempt.setFill(gh.c.brighter()));

                attempt.setOnMouseClicked(e -> {
                    //System.out.println(gsvorig.$dbs().getPrositeEntryByAccession(gh.h.ACCESSION).getDocumentationAccession());
                    docPopup(attempt, gh);
                });
                attempt.setCursor(Cursor.HAND);

                gh.gsm.add(attempt);
            }
        }

        base.setOnMouseMoved(e -> {
            update(e.getX());
        });

        base.setOnMouseExited(e -> {
            if (cl != null) {
                this.getChildren().remove(cl);
            }
        });

        base.setCursor(Cursor.HAND);

        /*

        if (match.size() > 0) {
            Executor threadPool = Executors.newFixedThreadPool(match.size());
            //CompletionService<Boolean> completionService = new ExecutorCompletionService(threadPool);

            for (int k = 0; k < match.size(); k++) {
                threadPool.execute(new RectangleGeneratorThread(FASTA, match,
                        graphicList, width, RECTANGLE_HEIGHT, transx, transy, cdl));
                if (k == match.size() - 1) {

                }
            }

            try {
                cdl.await();
                for (Rectangle graphic : graphicList) {
                    graphic.setOnMouseMoved(mouse1);
                    getChildren().add(graphic);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(GraphicSequence.class.getName()).log(Level.SEVERE, null, ex);
            }

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                }
            });

            int received = 0;
            boolean errors = false;

        }
         */
    }

    private void update(double x) {

        if (cl != null) {
            getChildren().remove(cl);
        }

        int textIndex = (int) (((x - transx) / width) * FASTA.length());
        String text;

        int len = (int) (0.5 * SUBSEQLENGTH);

        int start;
        int end;

        try {
            start = textIndex - len;
            end = textIndex + len;
            text = FASTA.substring(start, end);
        } catch (StringIndexOutOfBoundsException sioobe) {
            if (textIndex - len <= 0) {
                start = 0;
                end = len * 2;
                text = FASTA.substring(start, end);
            } else if (textIndex + len >= FASTA.length()) {
                start = FASTA.length() - (len * 2);
                end = FASTA.length() - 1;
                text = FASTA.substring(start);
            } else {
                start = -1;
                end = -1;
                text = "err";
            }
        }

        cl = new hmmscan_ColorableLabel(text);

        for (hmmscan_GraphicHit gh : match) {
            hmmscan_Hit h = gh.h;
            for (int[] op : h.displayhits) {
                int x_c = op[0];
                int y_c = op[1];
                boolean beginInside = x_c >= start && x_c <= end;
                boolean endInside = y_c >= start && y_c <= end;
                if (beginInside || endInside) {
                    cl.setColor(Math.max(start, x_c) - start, Math.min(y_c, end) - start, gh.c);
                }

                boolean beginBefore = x_c <= start;
                boolean endAfter = y_c >= end;
                if (beginBefore && endAfter) {
                    cl.setColor(0, end - start, gh.c);
                }
            }
        }

        cl = new hmmscan_ColorableLabel(cl, start + 1, end + 1);

        cl.translateYProperty().set(transy - 16);
        Label label = new Label(String.valueOf(start + 1) + " - " + text + " - " + String.valueOf(end + 1));
        double adjpx = 15;
        cl.translateXProperty().set((x - transx) / width * (width
                - adjpx - Toolkit.getToolkit()
                        .getFontLoader()
                        .computeStringWidth(label.getText(), label.getFont()))
                + transx);

        getChildren().add(cl);
    }

    public void horizontalResize(double windowWidth) {
        double newWidth = windowWidth - 2 * transx;
        double ratio = newWidth / width;
        this.width = newWidth;

        for (Rectangle r : graphicList) {
            r.setX(ratio * (r.getX() - transx) + transx);

            r.widthProperty().set(ratio * r.getWidth());
        }
        base.widthProperty().set(ratio * base.getWidth());
    }

    private boolean isPressed = false;
    private boolean isInside = false;

    private void docPopup(hmmscan_GraphicSequenceMatch gsm, hmmscan_GraphicHit origin) {

        //DocuBase db = gsvorig.$dbs().getDocumentation();
        //Document doc = db.get(docac);
        //if (doc == null) {
        //doc = new Document(null, "empty", "empty");
        //}
        //VBox docRoot = new VBox();
        Stage popup = new Stage();
popup.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        //popup.setTitle(docac + ": " + doc.DESC);
        popup.setTitle("Pfam hit: " + origin.h.pfam_acc);

        VBox vb = new VBox();
        //TabPane tp = new TabPane();
        //Tab docTab = new Tab("Hit documentation");
        //docTab.setClosable(false);

        TextArea ta = new TextArea();
        //ta.setWrapText(true);
        //ta.setEditable(false);
        //ta.setStyle("-fx-font-family: Monospace; -fx-font-weight: bold;");
        //ta.setText(doc.DOC);

        HBox hitInfoBox = new HBox(5);

        Label getHitInfoLabel = new Label("Get hit info for: ");
        Label accessionLabel = new Label(origin.h.pfam_acc);
        accessionLabel.setCursor(Cursor.HAND);

        accessionLabel.setOnMouseEntered(e -> {
            isInside = true;
            accessionLabel.setStyle(" -fx-underline: true; -fx-text-fill: #32e7ff;");
            if (isPressed) {
                accessionLabel.setStyle(" -fx-underline: true; -fx-text-fill: indigo;");
            }
        });

        accessionLabel.setOnMouseExited(e -> {
            isInside = false;
            if (!isPressed) {
                accessionLabel.setStyle(" -fx-underline: false; -fx-text-fill: black;");
            }
        });

        accessionLabel.setOnMousePressed(e -> {
            isPressed = true;
            accessionLabel.setStyle(" -fx-underline: true; -fx-text-fill: indigo;");
        });

        accessionLabel.setOnMouseReleased(e -> {
            isPressed = false;
            if (isInside) {
                accessionLabel.setStyle(" -fx-underline: true; -fx-text-fill: #32e7ff;");
            } else {
                accessionLabel.setStyle(" -fx-underline: false; -fx-text-fill: black;");
            }
        });
        
        accessionLabel.setOnMouseClicked(e -> {
            new PfamDocument(origin.h.pfam_acc);
        });

        hitInfoBox.getChildren().addAll(getHitInfoLabel, accessionLabel);
        //vb.getChildren().add(ta);
        //vb.setAlignment(Pos.CENTER);
        //docTab.setContent(vb);
        //Tab graphicsControls = new Tab("Hit graphics");
        VBox graphicsBox = new VBox(20);
        graphicsBox.setPadding(new Insets(20, 20, 20, 20));
        //show only this particular match or not?
        CheckBox showMatch = new CheckBox("Show this particular match?");

        showMatch.setSelected(gsm.isAllowed());

        showMatch.setOnAction(e -> {
            gsm.allow(showMatch.isSelected());
        });

        //show all matches corresponding to this hit
        CheckBox showHit = new CheckBox("Show this hit in this sequence?");

        showHit.setSelected(gsm.isAllowed());

        showHit.setOnAction(e -> {
            showMatch.setSelected(showHit.isSelected());
            origin.allowAll(showHit.isSelected());
        });

        //show this match at all for this alignment
        CheckBox showHitGlobal = new CheckBox("Show this hit in any sequences?");

        showHitGlobal.setSelected(gsm.isAllowed());

        showHitGlobal.setOnAction(e -> {
            showMatch.setSelected(showHitGlobal.isSelected());
            showHit.setSelected(showHitGlobal.isSelected());
            gsvorig.allowAll(origin, showHitGlobal.isSelected());
        });

        //end hit processing setup
        HBox colorBox = new HBox();
        Label lb = new Label("Hit color: ");
        lb.setTextFill(origin.c);

        ColorPicker cp = new ColorPicker();
        cp.setValue(origin.c);
        cp.valueProperty().addListener(e -> {
            origin.c = cp.getValue();
            lb.setTextFill(origin.c);
            gsm.setColor(cp.getValue());

        });
        colorBox.getChildren().addAll(lb, cp);

        Button regularize = new Button("Regularize colors");
        regularize.setOnAction(e -> {
            regularizeColorsPopup(origin, gsm);
        });
        graphicsBox.getChildren().addAll(hitInfoBox, showHitGlobal, showHit, showMatch, colorBox, regularize);
        //graphicsControls.setContent(graphicsBox);
        //graphicsControls.setClosable(false);

        //tp.getTabs().addAll(graphicsControls, docTab);
        Scene sc = new Scene(graphicsBox, 400, 400);

        popup.setScene(sc);

        popup.setAlwaysOnTop(true);
        popup.show();
        //popup.opacityProperty().set(0.95f);

    }

    public void regularizeColorsPopup(hmmscan_GraphicHit gh, hmmscan_GraphicSequenceMatch gsm) {
        Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox rt = new VBox();
        st.setTitle("Regularize colors");

        TextFlow notif = new TextFlow();
        notif.setTextAlignment(TextAlignment.CENTER);
        Text notifA = new Text("This will set the color of all hits with the same accession "
                + "(including in "
                + "other sequences) to the color of this hit.");

        notif.getChildren().add(notifA);
        notif.setPadding(new Insets(15));

        HBox buttons = new HBox();

        Button quit = new Button("Cancel");
        quit.setOnAction(e -> {
            st.close();
        });
        Button regularize = new Button("Regularize");
        regularize.setOnAction(e -> {
            st.close();
            regularize(gh, gsm);
        });
        buttons.getChildren().addAll(quit, regularize);

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

    public void regularize(hmmscan_GraphicHit gh, hmmscan_GraphicSequenceMatch gsm) {
        this.gsvorig.regularize(gh, gsm);
    }

    public void applyAll_externalCommand(hmmscan_GraphicHit gh, hmmscan_GraphicSequenceMatch gsm) {
        for (hmmscan_GraphicHit ghit : match) {
            if (gh.h.pfam_acc.equals(ghit.h.pfam_acc)) {
                ghit.updateColor(gsm.getColor());
            }
        }
    }

    public void allowAll_externalCommand(hmmscan_GraphicHit gh, boolean allow) {
        for (hmmscan_GraphicHit ghit : match) {
            if (gh.h.pfam_acc.equals(ghit.h.pfam_acc)) {
                ghit.allowAll(allow);
            }
        }
    }

    public int matchCount() {
        return match.size();
    }

    public hmmscan_GraphicHit getGraphicHit(int k) {
        return match.get(k);
    }

}
