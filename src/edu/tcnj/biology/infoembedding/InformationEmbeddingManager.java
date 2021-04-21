/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.infoembedding;

import java.util.HashMap;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class InformationEmbeddingManager {
    
    /**
     * Most basic constructor to associate a JavaFX node with an information
     * display
     * @param n <code>Node</code> to apply information to
     * @param resource <code>String</code> with plain-text name of resource
     */
    public InformationEmbeddingManager(Node n, String resource) {
        Stage s = null;
        //put stage together
        setStage(s, resource);
        n.setOnMouseClicked(e -> s.show());
    }
    
    /**
     * Basic constructor to associate a JavaFX node with an information
     * display, allows binding to specific mouse events
     * @param n <code>Node</code> to apply information to
     * @param resource <code>String</code> with plain-text name of resource
     * @param meCustom <code>MouseEvent</code> to bind information popup to
     */
    public InformationEmbeddingManager(Node n, String resource, MouseEvent meCustom) {
        Stage s = null;
        // put stage together
        setStage(s, resource);
        n.addEventFilter(MouseEvent.ANY, (MouseEvent me1) -> {
            if (me1 == meCustom) {
                s.show();
            }
        });
    }
    
    private void setStage(Stage s, String resource) {
        // if resource does not link to an html resource held within this
        // package, resource is interpreted to be a string containing raw
        // information which will be displayed as flat text
        
    }

}
