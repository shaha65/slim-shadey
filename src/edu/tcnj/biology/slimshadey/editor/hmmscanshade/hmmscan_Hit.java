/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_Hit {

    String pfam_acc = "";
    String desc = "";
    String name = "";
    String domEval = "";

    List<int[]> displayhits = new ArrayList<>();

    public hmmscan_Hit() {

    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("");
        ret.append("hmm_name -> ").append(this.name).append("\n");
        ret.append("hmm_desc -> ").append(this.desc).append("\n");
        ret.append("pfam_accession -> ").append(this.pfam_acc).append("\n");
        ret.append("overall_eval -> ").append(this.domEval).append("\n");
        for (int k = 0; k < displayhits.size(); k++) {
            ret.append("   coord");
            ret.append(k);
            ret.append("-> (");
            ret.append(displayhits.get(k)[0]);
            ret.append(", ");
            ret.append(displayhits.get(k)[1]);
            ret.append(")\n");
        }
        ret.append("\n");
        return ret.toString();
    }
}
