package shahavi.bio471.graphicsequence;

import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GraphicSequenceMatch extends Rectangle {

    private String sequence;
    private ArrayList<Integer> misMatchIndices;
    private int seqIndex;
    private int maxErr;

    public final String ACCESSION;
    
    private Color fill;

    /**
     * rapid access to sequence matches, used for optimization in imperfect
     * matches
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param fill
     * @param sequence
     * @param misMatchIndices
     * @param seqIndex
     */
    public GraphicSequenceMatch(double x, double y, double width, double height, Color fill, String sequence, String accession, ArrayList<Integer> misMatchIndices, int seqIndex, int maxErr) {
        super(x, y, width, height);
        this.setColor(fill);
        this.sequence = sequence;
        this.misMatchIndices = misMatchIndices;
        this.seqIndex = seqIndex;
        this.maxErr = maxErr;
        ACCESSION = accession;

        this.HEIGHT = height;
        //System.out.println(seqIndex + " h");
    }

    public String sequence() {
        return this.sequence;
    }

    private final double HEIGHT;
    private boolean allowed = true;

    public void allow(boolean allow) {
        allowed = allow;
        if (!allowed) {
            this.setHeight(HEIGHT / 2);
        } else {
            this.setHeight(HEIGHT);
        }
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setColor(Color c) {
        this.setFill(c);
        this.fill = c;
    }
    
    public Color getColor() {
        return fill;
    }
}
