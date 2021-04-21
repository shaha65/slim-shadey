/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.graphicsequence;

import java.util.List;
import javafx.scene.image.Image;
import shahavi.bio471.scanprosite.database.result.Hit;

public class UtilitySeq1 {

    public String name = null;
    public String seq = null;

    public List<Hit> hits;

    public UtilitySeq1(String name, String seq) {
        this.name = name;
        this.seq = seq;
    }

    public UtilitySeq1() {

    }
}
