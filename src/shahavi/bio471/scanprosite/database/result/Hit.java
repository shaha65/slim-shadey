/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.result;

import java.util.ArrayList;
import javafx.scene.image.Image;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class Hit {
    
    public String ACCESSION;
    public ArrayList<OrderedPair> HIT_LIST;
    public boolean show = false;
    public String confidence = "N/A";
    
    public Hit(String accession, ArrayList<OrderedPair> hitList) {
        ACCESSION = accession;
        HIT_LIST = hitList;
    }
    
}
