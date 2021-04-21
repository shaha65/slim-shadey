package shahavi.bio471.graphicsequence;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
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
import javafx.scene.input.MouseEvent;
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
import shahavi.bio471.scanprosite.database.documentation.DocuBase;
import shahavi.bio471.scanprosite.database.documentation.Document;
import shahavi.bio471.scanprosite.database.result.Hit;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

public class GraphicSequence extends Group {

    private GraphicSequenceViewer gsvorig;

    private ArrayList<GraphicSequenceMatch> graphicList;
    private List<GraphicHit> match;
    private Rectangle base;
    private String FASTA;

    private final double RECTANGLE_HEIGHT = 25;

    private double width;
    private final Color defCol;

    private double transx = 30;
    private double transy;

    private ColorableLabel cl;

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
    public GraphicSequence(double width, double graphicSeparator, Color defCol,
            int index, String FASTA, List<GraphicHit> match, String name,
            GraphicSequenceViewer originator) {
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

    private void createGraphics(String sequence, List<GraphicHit> match) {

        //CountDownLatch cdl = new CountDownLatch(match.size());
        base = new Rectangle(transx, transy, width, RECTANGLE_HEIGHT);
        base.setFill(defCol);
        this.getChildren().add(base);

        for (int k = 0; k < match.size(); k++) {
            GraphicHit gh = match.get(k);
            Color c = gh.c;
            List<OrderedPair> opl = gh.h.HIT_LIST;
            System.out.println(gh.h.ACCESSION + " " + gh.c.toString() + " asdf");

            for (int j = 0; j < opl.size(); j++) {
                OrderedPair op = opl.get(j);
                double x = (double) transx + (((double) op.X) / ((double) sequence.length()) * (double) this.width);
                System.out.println("X:asdfasdf: " + x);
                double y = transy;
                double w = ((((double) op.Y) - ((double) op.X)))
                        / ((double) sequence.length()) * this.width;
                double h = this.RECTANGLE_HEIGHT;
                GraphicSequenceMatch attempt = new GraphicSequenceMatch(x, y, w, h, c, gh.h.ACCESSION, null, null, -1, -1);
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
                    System.out.println(gsvorig.$dbs().getPrositeEntryByAccession(gh.h.ACCESSION).getDocumentationAccession());
                    docPopup(attempt, gh, gsvorig.$dbs().getPrositeEntryByAccession(gh.h.ACCESSION).getDocumentationAccession());
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

        cl = new ColorableLabel(text);

        for (GraphicHit gh : match) {
            Hit h = gh.h;
            for (OrderedPair op : h.HIT_LIST) {
                int x_c = op.X;
                int y_c = op.Y;
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

        cl = new ColorableLabel(cl, start + 1, end + 1);

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

    private void docPopup(GraphicSequenceMatch gsm, GraphicHit origin, String docac) {

        DocuBase db = gsvorig.$dbs().getDocumentation();
        Document doc = db.get(docac);
        if (doc == null) {
            doc = new Document(null, "empty", "empty");
        }
        VBox docRoot = new VBox();

        Stage popup = new Stage();
popup.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        popup.setTitle(docac + ": " + doc.DESC);

        VBox vb = new VBox();
        TabPane tp = new TabPane();
        Tab docTab = new Tab("Hit documentation");
        docTab.setClosable(false);

        TextArea ta = new TextArea();
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setStyle("-fx-font-family: Monospace; -fx-font-weight: bold;");
        ta.setText(doc.DOC);

        vb.getChildren().add(ta);

        vb.setAlignment(Pos.CENTER);
        docTab.setContent(vb);

        Tab graphicsControls = new Tab("Hit graphics");
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
        graphicsBox.getChildren().addAll(showHitGlobal, showHit, showMatch, colorBox, regularize);
        graphicsControls.setContent(graphicsBox);
        graphicsControls.setClosable(false);
        
        tp.getTabs().addAll(graphicsControls, docTab);
        Scene sc = new Scene(tp, 400, 400);

        popup.setScene(sc);

        popup.setAlwaysOnTop(true);
        popup.show();
        //popup.opacityProperty().set(0.95f);

        ta.prefHeightProperty().bind(popup.heightProperty());
        ta.prefWidthProperty().bind(popup.widthProperty());
    }

    public void regularizeColorsPopup(GraphicHit gh, GraphicSequenceMatch gsm) {
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

    public void regularize(GraphicHit gh, GraphicSequenceMatch gsm) {
        this.gsvorig.regularize(gh, gsm);
    }

    public void applyAll_externalCommand(GraphicHit gh, GraphicSequenceMatch gsm) {
        for (GraphicHit ghit : match) {
            if (gh.h.ACCESSION.equals(ghit.h.ACCESSION)) {
                ghit.updateColor(gsm.getColor());
            }
        }
    }

    public void allowAll_externalCommand(GraphicHit gh, boolean allow) {
        for (GraphicHit ghit : match) {
            if (gh.h.ACCESSION.equals(ghit.h.ACCESSION)) {
                ghit.allowAll(allow);
            }
        }
    }
    
    public int matchCount() {
        return match.size();
    }
    
    public GraphicHit getGraphicHit(int k) {
        return match.get(k);
    }
}
