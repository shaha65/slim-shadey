/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opalimpl;

import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import javafx.scene.image.Image;
import java.io.File;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah
 */
public class OpalImpl extends Application {

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.close();

    }

    public void run(EditorInterface origin, File infile) {
        this.opal(origin, infile);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void opal(EditorInterface origin, File infile) {
        //OpalRunner or = new OpalRunner(); 
        new OpalFX(origin, infile);
    }

}
