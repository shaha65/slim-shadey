/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade.customregex;

import java.util.ArrayList;
import javafx.scene.image.Image;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class CustomRegexHit {
    public String PATTERNSTR;
    public ArrayList<OrderedPair> HIT_LIST;
    public boolean show = false;
    public String confidence = "N/A";
    
    public CustomRegexHit(String patternstr, ArrayList<OrderedPair> hitList) {
        PATTERNSTR = patternstr;
        HIT_LIST = hitList;
    }
}
