package shahavi.bio471.graphicsequence;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;
import javafx.scene.paint.Color;
import shahavi.bio471.scanprosite.database.result.Hit;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class GraphicHit {

    public Hit h;
    public Color c;
    public boolean show = false;
    
    public List<GraphicSequenceMatch> gsm;

    public GraphicHit(Hit h, Color c, boolean show) {
        this.h = h;
        this.c = c;
        this.show = show;
        gsm = new ArrayList<>();
    }
    
    public void updateColor(Color c) {
        this.c = c;
        for (GraphicSequenceMatch gsmx : gsm) {
            //gsmx.setFill(this.c);
            gsmx.setColor(this.c);
        }
    }
    
    public void allowAll(boolean allow) {
        for (int k = 0; k < gsm.size(); k++) {
            gsm.get(k).allow(allow);
        }
    }


}
