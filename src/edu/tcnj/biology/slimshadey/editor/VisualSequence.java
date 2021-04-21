/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class VisualSequence extends HBox{
    
    public Label label;
    
    public VisualSequence() {
        super();
        label = new Label("");
    }
    
    public VisualSequence(Label lb) {
        super();
        label = lb;
    }
    
}
