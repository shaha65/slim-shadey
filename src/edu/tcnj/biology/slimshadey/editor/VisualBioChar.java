/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.Arrays;
import java.util.List;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 *
 * @author Avi Shah
 */
public class VisualBioChar extends Label {

    //unifont.ttf contains TRUE unifont style, will use for all
    final public static String FX_FONT_REG = "-fx-font-family : unifont-10.0.07; -fx-font-size : 20;";
    final public static String FX_FONT_SPEC = "-fx-font-family : unifont-10.0.07; -fx-font-size : 20; -fx-background-color : black";
    /* SPECIAL CHARACTERS (LOOP AND ARROWS) ARE 5/3 NORMAL SIZE*/
    //final public Character[] FX_SPEC_ARR = {'➰', '⇐', '⇒'}; //loop, left arrow, right arrow    

    public int index;
    private String origChar = " ";

    public boolean isanno, isnumb, isseq, isgroup, iscons = false;

    public VisualBioChar(boolean isanno, boolean isnumb, boolean isseq, boolean isgroup, boolean iscons) {
        super(" ");
        this.isanno = isanno;
        this.isnumb = isnumb;
        this.isseq = isseq;
        this.isgroup = isgroup;
        this.iscons = iscons;
        //this.setOnMouseClicked(e -> {this.setText("⇐");});
    }

    public VisualBioChar(String s, boolean isanno, boolean isnumb, boolean isseq, boolean isgroup, boolean iscons) {
        super(" ");
        String vbcChar = String.valueOf(s.charAt(0));
        this.setText(vbcChar);
        this.origChar = vbcChar;
        this.isanno = isanno;
        this.isnumb = isnumb;
        this.isseq = isseq;
        this.isgroup = isgroup;
        this.iscons = iscons;
        //this.setOnMouseClicked(e -> {this.setText("⇐");});
    }

    private String backgroundColor;
    private String textColor;

    public String getBackHex() {
        return backgroundColor;
    }

    public String getForeHex() {
        return textColor;
    }

    private String currentStyle;
    private String tempStyle = null;

    public void setCurrentStyle(String currentStyle, String backHex, String foreHex) {
        this.currentStyle = currentStyle;
        this.setStyle(this.currentStyle);
        this.backgroundColor = backHex;
        this.textColor = foreHex;
    }

    public String getCurrentStyle() {
        return currentStyle;
    }

    private boolean selected = false;

    public void select(boolean select, String selectStyle) {
        selected = select;
        if (select) {
            this.setStyle(selectStyle);
        } else {
            this.setStyle(currentStyle);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public VisualBioChar copy() {
        VisualBioChar vbc = new VisualBioChar(String.valueOf(this.getChar()), isanno, isnumb, isseq, isgroup, iscons);
        vbc.index = this.index;
        vbc.backgroundColor = this.backgroundColor;
        vbc.origChar = this.origChar;
        vbc.textColor = this.textColor;
        vbc.currentStyle = this.currentStyle;
        vbc.setCurrentStyle(this.currentStyle, this.backgroundColor, this.textColor);
        return vbc;
    }

    /**
     * A convenience method for temporarily styling a character. Allows for
     * operations like highlighting (for hover). Will not temp-style this if
     * this is currently selected
     *
     * @param style the <tt>String</tt> that holds the desired style, the
     * temporary styling is removed if the passed <tt>String</tt> is
     * <tt>null</tt> and this is not selected
     */
    public void tempStyle(String style) {
        if (!this.isSelected()) {
            this.tempStyle = style;
            if (style != null) {
                this.setStyle(this.tempStyle);
            } else {
                this.setStyle(this.currentStyle);
            }
        }
        /*
        this.tempStyle = style;
        if (this.tempStyle == null) {
            this.setStyle(this.currentStyle);
        } else { //when a style IS PASSED
            if (updateCurrentStyle) {
                this.currentStyle = this.getStyle();
            }
            this.setStyle(this.tempStyle);
            this.tempStyle = null;
        }
         */
    }

    public String getTempStyle() {
        return this.tempStyle;
    }

    private boolean hover;

    public void hover(boolean hover) {
        this.hover = hover;
        if (hover) {

        } else {

        }
    }

    public boolean hovering() {
        return hover;
    }

    /**
     * Returns character shown by this VBC
     *
     * @return char shown by this VBC
     */
    public char getChar() {
        return this.getText().charAt(0);
    }

    /**
     * Returns String of the character that this VBC was initialized with. null
     * if initialized empty
     *
     * @return String of originally assigned character; null if not applicable
     */
    public String getOrigChar() {
        return this.origChar;
    }

    public static final int DEFAULT_FONTSIZE = 18;

    private String generateFont(Color back, Color fore, int size) {
        StringBuilder str = new StringBuilder("");
        String backgroundHex = "white";
        if (back != null) {
            backgroundHex = String.format("#%02X%02X%02X",
                    (int) (back.getRed() * 255f),
                    (int) (back.getGreen() * 255f),
                    (int) (back.getBlue() * 255f));
        }
        String foregroundHex = "black";
        if (fore != null) {
            foregroundHex = String.format("#%02X%02X%02X",
                    (int) (fore.getRed() * 255f),
                    (int) (fore.getGreen() * 255f),
                    (int) (fore.getBlue() * 255f));
        }
        if (size == -1) {
            size = DEFAULT_FONTSIZE;
        }

        str.append("    -fx-font-family: \"unifont\";\n");
        str.append("    -fx-font-size: " + String.valueOf(size) + ";\n");
        str.append("    -fx-background-color: " + backgroundHex + " ;\n");
        str.append("    -fx-text-fill: " + foregroundHex + " ;\n");

        return str.toString();
    }

}
