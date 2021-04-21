/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database;

import shahavi.bio471.scanprosite.database.pattern.PrositePattern;
import javafx.scene.image.Image;
import shahavi.bio471.scanprosite.database.result.OrderedPair;
import shahavi.bio471.scanprosite.database.result.Hit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import shahavi.bio471.scanprosite.database.documentation.DocuBase;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class DatabaseSearcher_ProSiteDB_Mar2020 {

    private PrositeDatabase prosite;
    private PrositeDatabase evaluator;
    
    private DocuBase docuBase;
    

    public DatabaseSearcher_ProSiteDB_Mar2020(boolean usedefaults) {
        if (usedefaults) {
            prosite = new PrositeDatabase("prosite.dat.txt", true);
            evaluator = new PrositeDatabase("evaluator.dat.txt", true);
            docuBase = new DocuBase("prosite.doc.txt");
        } else {

        }
        pattern_test();
    }
    
    public DocuBase getDocumentation() {
        return docuBase;
    }

    public List<Hit> fullPatternSearch(String seq, boolean skipactive, boolean fxactive) {
        List<Hit> ret = new ArrayList<>();
        int numberPatterns = patternAccessionMap.keySet().size();
        int count = 0;

        if (fxactive) {
            searchingPopup(true, count, numberPatterns);
        }

        for (String accession : patternAccessionMap.keySet()) {
            //System.out.println(accession + " " + this.patternAccessionMap.get(accession));
            if (!(prosite.get(accession).skip() && skipactive)) {
                PrositePattern psp = new PrositePattern(this.patternAccessionMap.get(accession));
                if (fxactive) {
                    searchingPopup(true, count, numberPatterns);
                }

                ArrayList<OrderedPair> results = psp.hitList(seq);
                if (!results.isEmpty()) {
                    Hit hit = new Hit(accession, results);
                    ret.add(hit);
                }
                count++;
            }
        }

        if (fxactive) {
            //searchingPopup(false, 0, 0);
        }
        return ret;
    }

    private Stage stage;

    private Group root;
    private Scene scene;

    private ProgressBar pb;
    private ProgressIndicator pi;

    private HBox hb;

    private void searchingPopup(boolean show, int numer, int denom) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double fraction = ((double) numer) / ((double) denom);

                if (show) {
                    if (stage == null) {
                        stage = new Stage();
stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
                        stage.setTitle("Progress Controls");

                        stage.setScene(scene);
                        stage.setTitle(
                                "Progress Controls");

                        pb = new ProgressBar(0);
                        pi = new ProgressIndicator(0);

                        hb = new HBox();
                        hb.setSpacing(5);
                        hb.setAlignment(Pos.CENTER);
                        hb.getChildren().addAll(pb, pi);
                        scene = new Scene(hb);
                        stage.setScene(scene);
                        stage.show();

                    }
                    pb.setProgress(fraction);
                    pi.setProgress(fraction);
                } else {
                    stage.hide();
                    stage = null;

                    root = null;
                    scene = null;

                    pb = null;
                    pi = null;

                    hb = null;
                }
            }
        });
    }

    /**
     * return list of accession
     *
     * @param seq
     * @param accession
     * @return
     */
    public Hit patternSearch(String seq, String accession) {
        Hit ret = null;

        PrositePattern psp = new PrositePattern(this.patternAccessionMap.get(accession));
        List<OrderedPair> hitList = psp.hitList(seq);

        return ret;
    }

    private HashMap<String, String> patternAccessionMap;

    private void pattern_test() {
        List<String> accessionSet = prosite.getAccessionSet();

        patternAccessionMap = new HashMap<>();

        for (String accession : accessionSet) {
            PrositeEntry currentEntry = prosite.get(accession);
            if (currentEntry.isPattern()) {
                patternAccessionMap.put(accession, prosite.get(accession).getPattern());
            }
        }

    }

    public PrositeEntry getPrositeEntryByAccession(String accession) {
        return this.prosite.get(accession);
    }

}
