/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_ColorableLabel extends HBox {

    private String css = "-fx-font-weight: bold; -fx-underline: true;";

    public hmmscan_ColorableLabel(String text) {
        super();
        for (int i = 0; i < text.length(); i++) {
            Label add = new Label(String.valueOf(text.charAt(i)));
            getChildren().add(add);
        }
    }

    public hmmscan_ColorableLabel(hmmscan_ColorableLabel cl, int start, int end) {
        super();
        getChildren().add(new Label(String.valueOf(start) + " - "));
        getChildren().addAll(cl.getChildren());
        getChildren().add(new Label(" - " + String.valueOf(end)));
    }

    public void setColor(int start, int end, Color col) {
        for (int i = start; i < end; i++) {
            Label lb = (Label) getChildren().get(i);
            lb.setTextFill(col);
            lb.setStyle(css);

        }

    }

    public void setLowerCase(int index) {
        ((Label) this.getChildren().get(index)).setText(((Label) this.getChildren().get(index)).getText().toLowerCase());

    }

    public void setUpperCase(int index) {
        ((Label) this.getChildren().get(index)).setText(((Label) this.getChildren().get(index)).getText().toUpperCase());

    }

}
