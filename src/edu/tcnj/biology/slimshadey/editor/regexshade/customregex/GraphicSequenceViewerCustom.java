/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade.customregex;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;
import java.util.Random;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class GraphicSequenceViewerCustom extends Group {
    
    private ArrayList<GraphicSequenceCustom> sequences;
    private DoubleProperty widthProperty;

    public GraphicSequenceViewerCustom() {
        super();
        
        widthProperty = new SimpleDoubleProperty();
        sequences = new ArrayList<>();
        Rectangle r = new Rectangle(0, 0, 0, 0);
        this.getChildren().add(r);
    }

    public void addSequence(double width, double separator, int index,
            Color defCol, String FASTA, List<CustomRegexHit> gm,
            String fileName
    /*, CountDownLatch cdl, ThreadFactory tf*/
    ) {
        List<GraphicHitCustom> graphichits = new ArrayList<>();
        if (gm != null) {
            gm.forEach((h) -> {
                
                    Random rand = new Random();

                    double r = rand.nextFloat();
                    double g = rand.nextFloat() / 2f;
                    double b = rand.nextFloat() / 2f;
                    System.out.println(h.PATTERNSTR);
                    Color col = Color.color(r, g, b);
                    graphichits.add(new GraphicHitCustom(h, col, true));
                
            });
        }

        GraphicSequenceCustom gs = new GraphicSequenceCustom(width, separator, defCol,
                index, FASTA, graphichits, fileName, this);
        sequences.add(gs);
        getChildren().add(sequences.get(sequences.size() - 1));
        widthProperty.set(width);
        /*
        Thread runner = tf.newThread(new Runnable() {
            @Override
            public void run() {
                GraphicSequence gs = new GraphicSequence(width, separator, defCol,
                        index, FASTA, gm, fileName);
                sequences.add(gs);
                getChildren().add(sequences.get(sequences.size() - 1));
                widthProperty.set(width);
                cdl.countDown();
                return;
            }
        });
        runner.start();
         */
    }

    public String getSequence(int index) {
        return sequences.get(index).toString();
    }

    public ArrayList<GraphicSequenceCustom> getgraphics() {
        return this.sequences;
    }

    public void setWidth() {

    }

    private Label label;

    public void regularize(GraphicHitCustom gh, GraphicSequenceMatchCustom gsm) {
        sequences.forEach((gs) -> {
            gs.applyAll_externalCommand(gh, gsm);
        });
    }
    
    public void allowAll(GraphicHitCustom gh, boolean allow) {
        sequences.forEach((gs) -> {
            gs.allowAll_externalCommand(gh, allow);
        });
    }

}
