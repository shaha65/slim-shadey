package shahavi.bio471.graphicsequence;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import shahavi.bio471.scanprosite.database.DatabaseSearcher_ProSiteDB_Mar2020;
import shahavi.bio471.scanprosite.database.result.Hit;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

public class GraphicSequenceViewer extends Group {

    private ArrayList<GraphicSequence> sequences;
    private DoubleProperty widthProperty;
    private DatabaseSearcher_ProSiteDB_Mar2020 dbs;

    public GraphicSequenceViewer(DatabaseSearcher_ProSiteDB_Mar2020 dbs) {
        super();
        this.dbs = dbs;
        widthProperty = new SimpleDoubleProperty();
        sequences = new ArrayList<>();
        Rectangle r = new Rectangle(0, 0, 0, 0);
        this.getChildren().add(r);
    }

    public void addSequence(double width, double separator, int index,
            Color defCol, String FASTA, List<Hit> gm,
            String fileName
    /*, CountDownLatch cdl, ThreadFactory tf*/
    ) {
        List<GraphicHit> graphichits = new ArrayList<>();
        if (gm != null) {
            gm.forEach((h) -> {
                
                    Random rand = new Random();

                    double r = rand.nextFloat();
                    double g = rand.nextFloat() / 2f;
                    double b = rand.nextFloat() / 2f;
                    System.out.println(h.ACCESSION);
                    Color col = Color.color(r, g, b);
                    graphichits.add(new GraphicHit(h, col, true));
                
            });
        }

        GraphicSequence gs = new GraphicSequence(width, separator, defCol,
                index, FASTA, graphichits, fileName, this);
        sequences.add(gs);
        getChildren().add(sequences.get(sequences.size() - 1));
        widthProperty.set(width);
        /*
        Thread runner = tf.newThread(new Runnable() {
            @Override
            public void run() {
                GraphicSequence gs = new GraphicSequence(width, separator, defCol,
                        index, FASTA, gm, fileName);
                sequences.add(gs);
                getChildren().add(sequences.get(sequences.size() - 1));
                widthProperty.set(width);
                cdl.countDown();
                return;
            }
        });
        runner.start();
         */
    }

    public String getSequence(int index) {
        return sequences.get(index).toString();
    }

    public ArrayList<GraphicSequence> getgraphics() {
        return this.sequences;
    }

    public void setWidth() {

    }

    private Label label;

    public void regularize(GraphicHit gh, GraphicSequenceMatch gsm) {
        sequences.forEach((gs) -> {
            gs.applyAll_externalCommand(gh, gsm);
        });
    }
    
    public void allowAll(GraphicHit gh, boolean allow) {
        sequences.forEach((gs) -> {
            gs.allowAll_externalCommand(gh, allow);
        });
    }

    public DatabaseSearcher_ProSiteDB_Mar2020 $dbs() {
        return this.dbs;
    }
    
}
