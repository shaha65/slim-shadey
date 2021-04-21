/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade;

import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import shahavi.bio471.graphicsequence.GraphicsWindow;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class OnlineScanRunner {

    public OnlineScanRunner(VisualMultipleSequenceAlignment vmsa,
            boolean highSensitivity, boolean includeProfiles, boolean skipHighProb) {

        VBox namesBox = vmsa.getSequenceNamesBox();
        VBox seqBox = vmsa.getSequencesBox();

        //create convenience sequence objects
        List<String> names = new ArrayList<>();
        List<String> seqs = new ArrayList<>();
        for (int k = 0; k < namesBox.getChildren().size(); k++) {
            String name = ((Label) namesBox.getChildren().get(k)).getText();
            names.add(name);
            StringBuilder seqBuilder = new StringBuilder("");
            //must degap alignment sequence
            for (int i = 0; i < ((HBox) seqBox.getChildren().get(k)).getChildren().size(); i++) {
                char charSeq = ((Label) ((HBox) seqBox.getChildren().get(k)).getChildren().get(i)).getText().charAt(0);
                if (charSeq != '-') { //if not a gap, add to sequence
                    seqBuilder.append(charSeq);
                }
            }
            String seqStr = seqBuilder.toString();
            seqs.add(seqStr);
        }

        GraphicsWindow gw = new GraphicsWindow();
        //gw will direct coloring
        gw.createSeqPopup_online(vmsa, names, seqs, highSensitivity, includeProfiles, skipHighProb);

    }

}
