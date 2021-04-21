/*
    Copyright (c) 2018 Avi Shah, Anudeep Deevi, Sudhir Nayak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but without any warranty. See the GNU General Public License for more 
    details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package edu.tcnj.biology.slimshadey.editor;

import javafx.geometry.Orientation;
import javafx.scene.image.Image;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The <tt>Track</tt> class defines a graphical component that is essentially
 * composed of two components; a left and right container. The two containers
 * are related by a horizontally oriented <tt>SplitPane</tt>. Graphical
 * components may be added to both containers. The divider positions of tracks
 * intended to be displayed simultaneously should be bound bidirectionally.
 *
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class Track extends SplitPane {

    private VBox leftBox;
    private VBox rightBox;
    private ScrollPane leftPane;
    private ScrollPane rightPane;

    public Track(Track binding) {
        super();
        this.setOrientation(Orientation.HORIZONTAL);

        leftBox = new VBox();
        leftPane = new ScrollPane();
        leftPane.vbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.ALWAYS);
        leftPane.hbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.ALWAYS);
        leftBox.getChildren().add(leftPane);
        HBox.setHgrow(leftPane, Priority.NEVER);
        VBox.setVgrow(leftPane, Priority.ALWAYS);
        SplitPane.setResizableWithParent(leftBox, Boolean.FALSE);
        leftPane.setStyle("-fx-background: white;");

        rightBox = new VBox();
        rightPane = new ScrollPane();
        rightPane.vbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightPane.hbarPolicyProperty().set(ScrollPane.ScrollBarPolicy.ALWAYS);
        rightBox.getChildren().add(rightPane);
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        VBox.setVgrow(rightPane, Priority.ALWAYS);
        rightPane.setStyle("-fx-background: white;");

        leftPane.vvalueProperty().bindBidirectional(rightPane.vvalueProperty());
        this.setMinHeight(100);

        this.getItems().addAll(leftBox, rightBox /*, new VBox(50)*/);

        if (binding != null) {
            binding.getDividers().get(0).positionProperty().bindBidirectional(this.getDividers().get(0).positionProperty());
            binding.getRightPane().hvalueProperty().bindBidirectional(rightPane.hvalueProperty());
            binding.getLeftPane().hvalueProperty().bindBidirectional(leftPane.hvalueProperty());
        }
    }

    public ScrollPane getLeftPane() {
        return this.leftPane;
    }

    public ScrollPane getRightPane() {
        return this.rightPane;
    }

}
