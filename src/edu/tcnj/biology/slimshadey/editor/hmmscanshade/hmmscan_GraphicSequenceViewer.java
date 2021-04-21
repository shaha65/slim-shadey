/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.HashMap;
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
public class hmmscan_GraphicSequenceViewer extends Group {

    private ArrayList<hmmscan_GraphicSequence> sequences;
    private DoubleProperty widthProperty;

    public hmmscan_GraphicSequenceViewer() {
        super();
        widthProperty = new SimpleDoubleProperty();
        sequences = new ArrayList<>();
        Rectangle r = new Rectangle(0, 0, 0, 0);
        this.getChildren().add(r);
    }

    public void addSequence(double width, double separator, int index,
            Color defCol, String FASTA, List<hmmscan_Hit> gm,
            String fileName
    /*, CountDownLatch cdl, ThreadFactory tf*/
    ) {
        List<hmmscan_GraphicHit> graphichits = new ArrayList<>();
        if (gm != null) {
            for (hmmscan_Hit h : gm) /*gm.forEach((h) ->*/ {

                Random rand = new Random();

                double r = rand.nextFloat();
                double g = rand.nextFloat() / 2f;
                double b = rand.nextFloat() / 2f;
                //System.out.println(h.pfam_acc);
                Color col = Color.color(r, g, b);
                
                graphichits.add(new hmmscan_GraphicHit(h, col, true));

            }/*);*/
        }

        hmmscan_GraphicSequence gs = new hmmscan_GraphicSequence(width, separator, defCol,
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

    public ArrayList<hmmscan_GraphicSequence> getgraphics() {
        return this.sequences;
    }

    public void setWidth() {

    }

    private Label label;

    public void regularize(hmmscan_GraphicHit gh, hmmscan_GraphicSequenceMatch gsm) {
        sequences.forEach((gs) -> {
            gs.applyAll_externalCommand(gh, gsm);
        });
    }

    public void allowAll(hmmscan_GraphicHit gh, boolean allow) {
        sequences.forEach((gs) -> {
            gs.allowAll_externalCommand(gh, allow);
        });
    }
}
