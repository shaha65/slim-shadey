/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade;
import java.io.PrintStream;
import javafx.scene.image.Image;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class ScanPrositeRemoteHit {

    public String SEQUENCE_AC = null;
    public int START = -1;
    public int STOP = -1;
    public String SIGNATURE_AC = null;
    public double SCORE = -1;
    public String LEVEL = null;
    public String LEVEL_TAG = null;

    public ScanPrositeRemoteHit(String SEQUENCE_AC, int START, int STOP, String SIGNATURE_AC,
            double SCORE, String LEVEL, String LEVEL_TAG) {
        this.SEQUENCE_AC = SEQUENCE_AC;
        this.START = START;
        this.STOP = STOP;
        this.SIGNATURE_AC = SIGNATURE_AC;
        this.SCORE = SCORE;
        this.LEVEL = LEVEL;
        this.LEVEL_TAG = LEVEL_TAG;
    }

    public ScanPrositeRemoteHit() {

    }
    
    public void print(PrintStream ps) {
        ps.println(this.toString());
    }
    
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("");
        ret.append("sequence_ac -> ").append(this.SEQUENCE_AC).append("\n");
        ret.append("start -> ").append(this.START).append("\n");
        ret.append("stop -> ").append(this.STOP).append("\n");
        ret.append("signature_ac -> ").append(this.SIGNATURE_AC).append("\n");
        ret.append("score -> ").append(this.SCORE).append("\n");
        ret.append("level -> ").append(this.LEVEL).append("\n");
        ret.append("level -> ").append(this.LEVEL_TAG).append("\n");
        return ret.toString();
    }

}
