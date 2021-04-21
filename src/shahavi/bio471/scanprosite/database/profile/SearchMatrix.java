/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.profile;

import java.util.List;
import javafx.scene.image.Image;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class SearchMatrix {
    
    private double[][] scoreMatrix;
    
    public SearchMatrix(String seq, List<ProfileStep> model) {
        
        //2d object [x][y] x rows and y columns
        scoreMatrix = new double[seq.length()][model.size()]; 
        
        
        
        
    }
    
}
