/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opalimpl;

import java.awt.Dimension;
import javafx.scene.image.Image;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Avi Shah
 */
public class OpalFXLogger {

    public JScrollPane container;
    public JTextPane loggerArea;
    TextAreaOutputStream taos;

    private JFrame stage;

    /*private Scene scene;
    private VBox root;

    private final static double WDPX = 400;
    private final static double HTPX = 400;*/
    public OpalFXLogger(String name, boolean display) {
        loggerArea = new JTextPane();
        container = new JScrollPane(loggerArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.setMinimumSize(new Dimension(0, 1000));
        taos = new TextAreaOutputStream(loggerArea, 4096);

        loggerArea.setEditable(false);
        container.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());

            }
        });
        //SwingNode swnd = new SwingNode();

        //swnd.setContent(container);
        stage = new JFrame();
        stage.setSize(600, 600);
        this.reset(name, true);
        //root = new VBox();
        //root.getChildren().add(swnd);
        //scene = new Scene(root, WDPX, HTPX, true);
        //stage.setScene(scene);
        stage.add(container);
        this.displayLogger(display);
        stage.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.stage.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                displayLogger(false);
            }
        });
        //root.heightProperty().addListener(e -> {
        //    swnd.resize(root.getWidth(), root.getHeight());
        //    container.setSize((int) root.getWidth(), (int) root.getHeight());
        //});

        //root.widthProperty().addListener(e -> {
        //    //swnd.resize(root.getWidth(), root.getHeight());
        //    container.setSize((int) root.getWidth(), (int) root.getHeight());
        //});
    }

    public void displayLogger(boolean display) {
        this.stage.setVisible(display);

        /*if (display) {
            this.stage.show();
        } else {
            this.stage.hide();
        }*/
    }

    public void reset(String fileName, boolean clear) {
        if (clear) {
            this.loggerArea.setText("");
        }
        String title = "OpalFX Logger";
        if (fileName != null && !fileName.isEmpty()) {
            title = title.concat(" - ").concat(fileName);
        }
        stage.setTitle(title);
    }

    public void kill() {
        this.stage.dispose();
    }

}
