/**
 * Copyright © 2016 Avi Shah
 * Distributed under an informal license.
 */
package edu.tcnj.biology.slimshadey.editor;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

/**
 * This is a purely superficial class to display the program logo; its only
 * function is to determine whether the computer is too slow to view 3D protein
 * structures. If the cube does not display, does not display properly, or its
 * movement is slowed in the 'information' pop-up, the user will not be able to
 * properly experience a 3D protein structure on that computer using c2P.
 *
 * @author Avi_Shah
 */
public class AppLogoCube extends Group {

    final Rotate rx = new Rotate(0, Rotate.X_AXIS);
    final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
    final Rotate rz = new Rotate(0, Rotate.Z_AXIS);

    /**
     * Constructs the application's logo cube.
     *
     * @param size the designated side length of the cube as <tt>double</tt>
     */
    public AppLogoCube(double size) {
        //back
        ImageView image1 = new ImageView();
        image1.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image1.translateXProperty().set(-0.5 * size);
        image1.translateYProperty().set(-0.5 * size);
        image1.translateZProperty().set(0.5 * size);

        //bottom
        ImageView image2 = new ImageView();
        image2.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image2.translateXProperty().set(-0.5 * size);
        image2.translateYProperty().set(0);
        image2.rotationAxisProperty().set(Rotate.X_AXIS);
        image2.rotateProperty().set(90);

        //right
        ImageView image3 = new ImageView();
        image3.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image3.translateXProperty().set(-1 * size);
        image3.translateYProperty().set(-0.5 * size);
        image3.rotationAxisProperty().set(Rotate.Y_AXIS);
        image3.rotateProperty().set(90);

        getTransforms().addAll(rz, ry, rx);

        //left
        ImageView image4 = new ImageView();
        image4.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image4.translateXProperty().set(0);
        image4.translateYProperty().set(-0.5 * size);
        image4.rotationAxisProperty().set(Rotate.Y_AXIS);
        image4.rotateProperty().set(90);

        //top
        ImageView image5 = new ImageView();
        image5.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image5.translateXProperty().set(-0.5 * size);
        image5.translateYProperty().set(-1 * size);
        image5.rotationAxisProperty().set(Rotate.X_AXIS);
        image5.rotateProperty().set(90);

        //final
        ImageView image6 = new ImageView();
        image6.setImage(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png"), size, size, false, false));

        image6.translateXProperty().set(-0.5 * size);
        image6.translateYProperty().set(-0.5 * size);
        image6.translateZProperty().set(-0.5 * size);

        getTransforms().addAll(rz, ry, rx);
        getChildren().addAll(
                image1,
                image2,
                image3,
                image4,
                image5,
                image6
        );
    }
}
