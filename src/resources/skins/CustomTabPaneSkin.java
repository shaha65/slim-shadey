/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resources.skins;

import com.sun.javafx.scene.control.skin.TabPaneSkin;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/* 
adapted from 
https://stackoverflow.com/questions/35239420/display-label-if-tabpane-has-no-tabs
last accessed 12/1/2020
 */
public class CustomTabPaneSkin extends TabPaneSkin {

    private final VBox placeHolder;
    private final Label placeHolderText;
    
    private final String DEFAULT_TEXT = "Drag and drop aligned sequences or project files";

    public CustomTabPaneSkin(TabPane tabPane) {
        super(tabPane);

        placeHolderText = new Label(DEFAULT_TEXT);
        placeHolderText.setFont(Font.font(null, FontWeight.BOLD, 20));
        placeHolderText.setTextFill(Color.GRAY);
        placeHolderText.setAlignment(Pos.CENTER);

        placeHolderText.minWidthProperty().bind(getSkinnable().widthProperty());
        placeHolderText.minHeightProperty().bind(getSkinnable().heightProperty());

        placeHolder = new VBox(placeHolderText);

        for (Node node : getChildren()) {
            if (node.getStyleClass().contains("tab-header-area")) {
                Pane headerArea = (Pane) node;
                // Header area is hidden if there is no tabs, thus when the tabpane is "empty"
                headerArea.visibleProperty().addListener((observable, oldValue, newValue)
                        -> {
                    if (newValue) {
                        getChildren().remove(placeHolder);
                    } else {
                        getChildren().add(placeHolder);
                    }
                }
                );

                getChildren().add(placeHolder);

                break;
            }
        }
    }

    public void recolor(Paint p) {
        //System.out.println(p == null ? p : p.toString());
        placeHolderText.setTextFill(p == null ? Color.GRAY : p);
 
    }
    
    public void setText(String s) {
        //System.out.println(s);
        placeHolderText.setText(s == null ? DEFAULT_TEXT : s);
    }

}
