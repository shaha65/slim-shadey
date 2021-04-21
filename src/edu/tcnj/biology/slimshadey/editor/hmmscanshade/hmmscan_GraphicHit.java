/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;
import javafx.scene.paint.Color;


/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_GraphicHit {

    public hmmscan_Hit h;
    public Color c;
    public boolean show = false;
    
    public List<hmmscan_GraphicSequenceMatch> gsm;

    public hmmscan_GraphicHit(hmmscan_Hit h, Color c, boolean show) {
        this.h = h;
        this.c = c;
        this.show = show;
        gsm = new ArrayList<>();
    }
    
    public void updateColor(Color c) {
        this.c = c;
        for (hmmscan_GraphicSequenceMatch gsmx : gsm) {
            //gsmx.setFill(this.c);
            gsmx.setColor(this.c);
        }
    }
    
    public void allowAll(boolean allow) {
        for (int k = 0; k < gsm.size(); k++) {
            gsm.get(k).allow(allow);
        }
    }


}
