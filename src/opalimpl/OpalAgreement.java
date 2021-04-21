package opalimpl;

import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import javafx.scene.image.Image;
import java.awt.Dimension;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author Avi Shah
 */
public class OpalAgreement {

    private Stage stage;
    private Scene scene;
    private VBox root;
    private final static int ROOT_SEPARATOR_WIDTH_PX = 36;

    private HBox opalLogo;
    private HBox agreeContainer;
    private Label agree;
    /*always track label-as-button activity*/
    private boolean clicked = false;
    private boolean mouse_in = false;
    private final static String AGREE_STYLE = " -fx-font: 48 Arial; -fx-font-weight:bold; -fx-text-fill:";
    private final static String DEFAULT_STYLE = "black;";
    private final static String HOVER_STYLE = "blue;";
    private final static String CLICKED_STYLE = "green;";

    private EditorInterface origin;

    public OpalAgreement(boolean agreement, EditorInterface origin, File infile) {
        stage = new Stage();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        this.origin = origin;

        root = new VBox(ROOT_SEPARATOR_WIDTH_PX);

        root.getChildren().addAll(
                this.handleLabels(labels, colors, bold, italic),
                this.getHelpSwing());
        if (agreement && origin.isOpalAgreed()) {
            // they want to align and have agreed to the license
            init_OpalRunnerFX(infile);
        } else {
            if (agreement) {
                root.getChildren().add(this.getAgreeLabel(infile));
                stage.setTitle("Launching OpalFX");
            } else {
                stage.setTitle("OpalFX");
            }

            scene = new Scene(root);
            scene.setFill(Color.WHITE);
            scene.getStylesheets().add(getClass().getResource("opalfx.css").toExternalForm());
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            this.manualResize();
        }

    }
    

    private final static double STAGE_HEIGHT_PADDING_PX = 32;

    private void manualResize() {

        sp1.setMaxHeight(2 + ta1.lookup(".text").getBoundsInLocal().getHeight());
        sp2.setMaxHeight(2 + ta2.lookup(".text").getBoundsInLocal().getHeight());

        stage.sizeToScene();
        stage.setHeight(stage.getHeight() + 22);
        stage.setResizable(false);

        stage.centerOnScreen();
    }

    private HBox getAgreeLabel(File infile) {

        agreeContainer = new HBox();
        agree = new Label("I agree");
        agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));
        agree.setCursor(Cursor.HAND);
        agreeContainer.setAlignment(Pos.CENTER);

        agree.setOnMouseEntered(e -> {
            mouse_in = true;
            System.out.println("mouse_in = true;");
            if (!clicked) {
                agree.setStyle(AGREE_STYLE.concat(HOVER_STYLE));
            }
        });

        agree.setOnMouseExited(e -> {
            mouse_in = false;
            System.out.println("mouse_in = false;");
            if (!clicked) {
                agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));
            }
        });

        agree.setOnMousePressed(e -> {
            clicked = true;
            System.out.println("clicked = true;");
            agree.setStyle(AGREE_STYLE.concat(CLICKED_STYLE));
        });

        agree.setOnMouseReleased(e -> {
            clicked = false;
            System.out.println("clicked = false;");
            if (mouse_in) {
                //moot since Pressed event (Above) kills this.stage
                agree.setStyle(AGREE_STYLE.concat(HOVER_STYLE));
            } else {
                agree.setStyle(AGREE_STYLE.concat(DEFAULT_STYLE));

            }
        });

        agree.setOnMouseClicked(e -> {
            origin.opalAgreementAccepted();
            init_OpalRunnerFX(infile);
        });

        agreeContainer.getChildren().add(agree);
        return agreeContainer;
    }

    private void init_OpalRunnerFX(File infile) {
        if (infile == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load sequences to Align (Opal)");
            
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                new OpalRunner(selectedFile, this.stage, origin);
            } else {
                stage.close();
            }
        } else {
            new OpalRunner(infile, this.stage, origin);
        }
    }

    private final static String LOGO_STYLE = " -fx-font: 72 Arial;";
    private final static String LOGO_FONT_BOLD = " -fx-font-weight:bold;";
    private final static String LOGO_FONT_ITALIC = " -fx-font-style:italic;";
    private final static String LOGO_FONT_TEXT_FILL_PROPERTY_SKELETON = " -fx-text-fill:";

    private HBox handleLabels(Label[] labels, String[] colors, boolean[] bold, boolean[] italic) {
        opalLogo = new HBox();
        opalLogo.setAlignment(Pos.CENTER);

        for (int i = 0; i < labels.length; i++) {
            labels[i].setStyle(LOGO_STYLE.concat(this.getLogoTextFillPropertyString(colors[i])).concat(";"));
            if (bold[i]) {
                labels[i].setStyle(labels[i].getStyle().concat(LOGO_FONT_BOLD));
            }
            if (italic[i]) {
                labels[i].setStyle(labels[i].getStyle().concat(LOGO_FONT_ITALIC));
            }
            opalLogo.getChildren().add(labels[i]);
        }

        return opalLogo;
    }

    private String getLogoTextFillPropertyString(String color) {
        return LOGO_FONT_TEXT_FILL_PROPERTY_SKELETON.concat(color).concat(";");
    }

    private Label O_lb = new Label("O");
    private Label p_lb = new Label("p");
    private Label a_lb = new Label("a");
    private Label l_lb = new Label("l ");
    private Label F_lb = new Label("F");
    private Label X_lb = new Label("X");
    private final Label[] labels = new Label[]{O_lb, p_lb, a_lb, l_lb, F_lb, X_lb};

    private String O_col = "green";
    private String p_col = "blue";
    private String a_col = "orange";
    private String l_col = "red";
    private String F_col = "darkgoldenrod";
    private String X_col = "darkgoldenrod";
    private final String[] colors = new String[]{O_col, p_col, a_col, l_col, F_col, X_col};

    private boolean O_bold = true;
    private boolean p_bold = true;
    private boolean a_bold = true;
    private boolean l_bold = true;
    private boolean F_bold = true;
    private boolean X_bold = true;
    private final boolean[] bold = new boolean[]{O_bold, p_bold, a_bold, l_bold, F_bold, X_bold};

    private boolean O_italic = true;
    private boolean p_italic = true;
    private boolean a_italic = true;
    private boolean l_italic = true;
    private boolean F_italic = true;
    private boolean X_italic = true;
    private final boolean[] italic = new boolean[]{O_italic, p_italic, a_italic, l_italic, F_italic, X_italic};

    private final static int INFORMATION_JTEXT_SIZE = 14;
    private final static int CITATION_JTEXT_SIZE = 18;
    private final static java.awt.Color CITATION_TEXT_FILL_SWING = java.awt.Color.blue;

    private JTextPane textPane__Swing_in_FX;
    private JScrollPane scrollPane__Swing_in_FX;
    private final static double HEIGHT_JTEXTPANE_PX = 160.0;

    private ScrollPane sp1;
    private ScrollPane sp2;
    private TextArea ta1;
    private TextArea ta2;

    private VBox getHelpSwing() {

        String helpString1 = "OpalFX aligns sequences using Opal. "
                + System.lineSeparator()
                + System.lineSeparator()
                + "If you publish results based on an alignment made using"
                + " OpalFX, please cite the paper that describes Opal:"
                + System.lineSeparator()
                + System.lineSeparator();

        String helpString2 = "Wheeler, T. J., & Kececioglu, J. D. (2007). Multiple "
                + "alignments by aligning alignments. ";

        String helpString3 = "Bioinformatics";
        String helpString4 = ", 23, i559-i568." + System.lineSeparator() + System.lineSeparator();

        ta1 = new TextArea();
        ta2 = new TextArea();

        ta1.appendText(helpString1);
        ta2.appendText(helpString2 + helpString3 + helpString4);

        ta1.setEditable(false);
        ta2.setEditable(false);

        ta1.setWrapText(true);
        ta2.setWrapText(true);

        ta1.setStyle(" -fx-font: 24 Arial; -fx-font-weight: bold;");
        ta2.setStyle(" -fx-font: 24 Arial; -fx-font-weight: bold; -fx-text-fill: blue");

        sp1 = new ScrollPane(ta1);
        sp2 = new ScrollPane(ta2);

        sp1.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp1.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp2.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox ret = new VBox();
        ret.getChildren().addAll(sp1, sp2);
        return ret;
    }

    public void OpalFX_information_popup() {
        Stage st = new Stage();
st.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        st.setTitle("Launching OpalFX");

        VBox rt = new VBox(ROOT_SEPARATOR_WIDTH_PX);

        rt.getChildren().addAll(
                this.handleLabels(labels, colors, bold, italic),
                this.getHelpSwing());

        Scene sc = new Scene(rt, 850, 500, true);
        st.setScene(sc);
        st.setAlwaysOnTop(true);
        st.initModality(Modality.APPLICATION_MODAL);
        st.show();
        final double rootPrefHeight = rt.prefHeight(-1);
        final double decorationHeight = st.getHeight() - sc.getHeight();
        st.setHeight(rootPrefHeight + decorationHeight + HEIGHT_JTEXTPANE_PX);
        st.setResizable(false);
    }

}
