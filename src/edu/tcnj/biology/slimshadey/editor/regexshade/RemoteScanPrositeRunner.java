/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.regexshade;

import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import javafx.scene.image.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import shahavi.bio471.graphicsequence.GraphicsWindow;
import shahavi.bio471.graphicsequence.UtilitySeq1;
import shahavi.bio471.scanprosite.database.result.Hit;
import shahavi.bio471.scanprosite.database.result.OrderedPair;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class RemoteScanPrositeRunner {

    private final GraphicsWindow GRAPHICS_PARENT;
    private final VisualMultipleSequenceAlignment ALIGNMENT;

    public RemoteScanPrositeRunner(GraphicsWindow gwParent, VisualMultipleSequenceAlignment vmsa,
            boolean highSensitivity, boolean includeProfiles, boolean skipHighProb) {
        this.GRAPHICS_PARENT = gwParent;
        this.ALIGNMENT = vmsa;
        this.highSensitivity = highSensitivity;
        this.includeProfiles = includeProfiles;
        this.skipHighProb = skipHighProb;
    }

    private final String URL_BASE = "https://prosite.expasy.org/cgi-bin/prosite/PSScan.cgi?";
    private final String URL_NEWLINE = "%0A";
    private boolean highSensitivity = false;
    private boolean includeProfiles = true;
    private boolean skipHighProb = true;

    private List<Sequence> seqs;

    public void feedStringInput(List<String> names, List<String> sequences) {
        seqs = new ArrayList<>();
        for (int k = 0; k < names.size(); k++) {
            seqs.add(new Sequence(names.get(k), sequences.get(k)));
        }
        this.getResultsMultithreaded(seqs);
    }

    private void getResultsMultithreaded(List<Sequence> seqs) {
        this.seqs = seqs;
        Task getResults = new Task<HashMap<Sequence, String>>() {
            @Override
            protected HashMap<Sequence, String> call() throws Exception {
                HashMap<Sequence, String> ret = new HashMap<>();
                try {

                    //build request url
                    HashMap<Sequence, Future<String>> threads = new HashMap<>();
                    ExecutorService es = Executors.newCachedThreadPool();
                    for (Sequence s : seqs) {
                        URL request = new URL(buildXMLRequest(Arrays.asList(new Sequence[]{s}), highSensitivity, includeProfiles, skipHighProb));
                        threads.put(s, es.submit(getRequestXMLThread(request)));
                        System.out.println(request.toString());
                    }

                    es.shutdown();

                    boolean allDone = false;
                    HashMap<Sequence, Boolean> doneMap = new HashMap<>();
                    for (Sequence s : seqs) {
                        doneMap.put(s, false);
                    }
                    colorindex = 0;
                    while (!allDone) {
                        for (Sequence s : seqs) {
                            Future<String> future = threads.get(s);
                            if (future.isDone()) {
                                ret.put(s, future.get());
                                doneMap.put(s, true);
                            }
                        }
                        allDone = true;
                        for (Sequence s : seqs) {
                            if (!doneMap.get(s)) {
                                allDone = false;
                                break;
                            }
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                pi.setStyle(colorStyles.get(colorindex));
                                colorindex++;
                                if (colorindex >= colorStyles.size()) {
                                    colorindex = 0;
                                }
                            }
                        });
                        Thread.sleep(250);
                    }

                } catch (MalformedURLException ex) {
                    Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
                } /*catch (IOException ex) {
                    Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
                } */ catch (InterruptedException ex) {
                    Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
                } finally {

                }
                return ret;
            }
        };

        getResults.setOnRunning(e -> {
            startLoadingAnimation();
        });

        getResults.setOnFailed(e -> {
            endLoadingAnimation();
        });

        getResults.setOnSucceeded(e -> {
            endLoadingAnimation();
            HashMap<Sequence, String> output = (HashMap<Sequence, String>) getResults.getValue();
            for (Sequence s : output.keySet()) {
                //System.out.println(s.name);
                //System.out.println(s.seqstr);
                //System.out.println(output.get(s));
            }
            //printXML(output);
            //printHits(organizeOutput(output));
            GRAPHICS_PARENT.showSequenceList(ALIGNMENT, convertTo471Format(organizeOutput(output)));

        });

        new Thread(getResults).start();
    }

    private HashMap<Sequence, List<ScanPrositeRemoteHit>> organizeOutput(HashMap<Sequence, String> rawXML) {
        HashMap<Sequence, List<ScanPrositeRemoteHit>> ret = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (Sequence s : rawXML.keySet()) {
            ret.put(s, parsedXML(s, rawXML.get(s), dbf));
        }
        return ret;
    }

    private List<UtilitySeq1> convertTo471Format(HashMap<Sequence, List<ScanPrositeRemoteHit>> organizedOutput) {
        List<UtilitySeq1> ret = new ArrayList<>();
        /*the original list of seqs is in the order of the sequences the user
        sees in the original rendering, and corresponds to the keyset of the 
        hashmap that has the XML parsed output, so this original list's values
        will be used as keys*/
        for (int k = 0; k < seqs.size(); k++) {
            Sequence currentSeq = seqs.get(k);
            List<Hit> hitList471 = new ArrayList<>();
            List<ScanPrositeRemoteHit> parsedHitList = organizedOutput.get(currentSeq);
            for (int a = 0; a < parsedHitList.size(); a++) {
                ScanPrositeRemoteHit currentParsedHit = parsedHitList.get(a);
                boolean accessionRepeated = false;
                look_for_repeat_loop:
                for (int b = 0; b < hitList471.size(); b++) {
                    Hit currentHit471 = hitList471.get(b);
                    if (currentHit471.ACCESSION.equals(currentParsedHit.SIGNATURE_AC)) {
                        accessionRepeated = true;
                        currentHit471.HIT_LIST.add(new OrderedPair(currentParsedHit.START, currentParsedHit.STOP));
                        break look_for_repeat_loop;
                    }
                }
                if (!accessionRepeated) {
                    ArrayList<OrderedPair> newHitList = new ArrayList<>();
                    newHitList.add(new OrderedPair(currentParsedHit.START, currentParsedHit.STOP));
                    Hit newHit = new Hit(currentParsedHit.SIGNATURE_AC, newHitList);
                    hitList471.add(newHit);
                }
            }
            UtilitySeq1 us1toAdd = new UtilitySeq1(currentSeq.name, currentSeq.seqstr);
            us1toAdd.hits = hitList471;
            ret.add(us1toAdd);
        }
        return ret;
    }

    private void printHits(HashMap<Sequence, List<ScanPrositeRemoteHit>> hitmap) {
        Stage outstage = new Stage();
        outstage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox root = new VBox();
        TextArea ta = new TextArea();
        HBox.setHgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(ta, Priority.ALWAYS);
        ta.setEditable(false);
        ta.setWrapText(true);
        root.getChildren().add(ta);
        outstage.setScene(new Scene(root, 600, 600, true));
        outstage.show();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (Sequence s : hitmap.keySet()) {
            ta.appendText(s.name.concat("\n"));
            ta.appendText(s.seqstr.concat("\n"));
            List<ScanPrositeRemoteHit> hitlist = hitmap.get(s);
            //System.err.println(hitlist);
            for (ScanPrositeRemoteHit sprh : hitlist) {
                ta.appendText(sprh.toString());
            }
            ta.appendText("\n\n");
        }
    }

    private void printXML(HashMap<Sequence, String> output) {
        Stage outstage = new Stage();
        outstage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox root = new VBox();
        TextArea ta = new TextArea();
        HBox.setHgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(ta, Priority.ALWAYS);
        ta.setEditable(false);
        ta.setWrapText(true);
        root.getChildren().add(ta);
        outstage.setScene(new Scene(root, 600, 600, true));
        outstage.show();
        for (Sequence s : output.keySet()) {
            ta.appendText(s.name.concat("\n"));
            ta.appendText(s.seqstr.concat("\n"));
            ta.appendText(output.get(s).concat("\n"));
        }

    }

    private List<ScanPrositeRemoteHit> parsedXML(Sequence s, String XML, DocumentBuilderFactory dbf) {
        System.out.println(s.name);
        System.out.println(s.seqstr);
        List<ScanPrositeRemoteHit> ret = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

            //go from matchset -> match -> elements of match
            NodeList nlmatchset = doc.getChildNodes().item(0).getChildNodes();
            System.out.println("Number hits :" + doc.getDocumentElement().getAttribute("n_match"));
            int expectedHits = Integer.parseInt(doc.getDocumentElement().getAttribute("n_match"));
            //printNote(nlmatchset);
            int matchCount = 0;
            ret = new ArrayList<ScanPrositeRemoteHit>();

            for (int k = 0; k < nlmatchset.getLength(); k++) {
                Node matchset = nlmatchset.item(k);
                //System.out.println("Current Element :" + matchset.getNodeName());

                if (matchset.getNodeName().equals("match")) {
                    NodeList hitinfo = matchset.getChildNodes();
                    ScanPrositeRemoteHit sprh = new ScanPrositeRemoteHit();
                    for (int t = 0; t < hitinfo.getLength(); t++) {
                        Node currentItem = hitinfo.item(t);
                        if (!currentItem.getNodeName().equals("#text")) {
                            if (currentItem.getNodeName().equals("sequence_ac")) {
                                sprh.SEQUENCE_AC = currentItem.getTextContent();
                            } else if (currentItem.getNodeName().equals("start")) {
                                sprh.START = Integer.parseInt(currentItem.getTextContent());
                            } else if (currentItem.getNodeName().equals("stop")) {
                                sprh.STOP = Integer.parseInt(currentItem.getTextContent());
                            } else if (currentItem.getNodeName().equals("signature_ac")) {
                                sprh.SIGNATURE_AC = currentItem.getTextContent();
                            } else if (currentItem.getNodeName().equals("score")) {
                                sprh.SCORE = Double.parseDouble(currentItem.getTextContent());
                            } else if (currentItem.getNodeName().equals("level")) {
                                sprh.LEVEL = currentItem.getTextContent();
                            } else if (currentItem.getNodeName().equals("level_tag")) {
                                sprh.LEVEL_TAG = currentItem.getTextContent();
                            }
                            System.out.println("        Current Element :" + hitinfo.item(t).getNodeName());
                            System.out.println("        Current info :" + hitinfo.item(t).getTextContent());
                        }
                    }
                    ret.add(sprh);
                    matchCount++;
                    System.out.println("match " + matchCount + " out of " + expectedHits);
                }
            }
            //enforce that all hits belong to same sequence
            //NOT NEEDED ANYMORE WITH MULTIPLE THREADS
            return ret;
            /*
            String sequenceName = ret.get(0).SEQUENCE_AC;
            boolean allSameNameEnforced = true;
            name_check_loop:
            for (int p = 1; p < ret.size(); p++) {

                if (!sequenceName.equals(ret.get(p).SEQUENCE_AC)) {
                    allSameNameEnforced = false;
                    break name_check_loop;
                }
            }
            // scanprosite replaces '.' with '-' in names
            if (allSameNameEnforced && s.name.replaceAll("\\.", "-").equals(sequenceName)) {
                return ret;
            }*/

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static void printNote(NodeList nodeList) {

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);

            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {

                // get node name and value
                System.out.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
                System.out.println("Node Value =" + tempNode.getTextContent());

                if (tempNode.hasAttributes()) {

                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();

                    for (int i = 0; i < nodeMap.getLength(); i++) {

                        Node node = nodeMap.item(i);
                        System.out.println("attr name : " + node.getNodeName());
                        System.out.println("attr value : " + node.getNodeValue());

                    }

                }

                if (tempNode.hasChildNodes()) {

                    // loop again if has child nodes
                    printNote(tempNode.getChildNodes());

                }

                System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");

            }

        }

    }

    private Stage loadingStage;
    private ProgressIndicator pi;
    private Label status;

    private int colorindex = 0;
    private List<String> colorStyles = Arrays.asList(new String[]{
        " -fx-accent: " + hexifyColorFX(Color.DARKRED) + ";",
        " -fx-accent: " + hexifyColorFX(Color.RED) + ";",
        " -fx-accent: " + hexifyColorFX(Color.ORANGERED) + ";",
        " -fx-accent: " + hexifyColorFX(Color.DARKORANGE) + ";",
        " -fx-accent: " + hexifyColorFX(Color.ORANGE) + ";",
        " -fx-accent: " + hexifyColorFX(Color.YELLOW.darker()) + ";",
        " -fx-accent: " + hexifyColorFX(Color.YELLOWGREEN) + ";",
        " -fx-accent: " + hexifyColorFX(Color.LIGHTGREEN) + ";",
        " -fx-accent: " + hexifyColorFX(Color.GREEN) + ";",
        " -fx-accent: " + hexifyColorFX(Color.LIGHTBLUE) + ";",
        " -fx-accent: " + hexifyColorFX(Color.BLUE) + ";",
        " -fx-accent: " + hexifyColorFX(Color.BLUEVIOLET) + ";",
        " -fx-accent: " + hexifyColorFX(Color.VIOLET) + ";",
        " -fx-accent: " + hexifyColorFX(Color.VIOLET.brighter()) + ";",});

    public void startLoadingAnimation() {

        loadingStage = new Stage();
        loadingStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        loadingStage.setTitle("Progress");

        pi = new ProgressIndicator(-1);

        status = new Label("Scanning...");

        HBox box = new HBox();
        box.alignmentProperty().set(Pos.CENTER);
        VBox hb = new VBox(10);

        hb.getChildren().addAll(pi, status);
        box.getChildren().add(hb);
        box.setPadding(new Insets(20));
        Scene scene = new Scene(box);
        loadingStage.setScene(scene);
        //loadingStage.initStyle(StageStyle.UNDECORATED);
        loadingStage.setResizable(false);
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setAlwaysOnTop(true);
        loadingStage.show();

    }

    private void endLoadingAnimation() {
        if (loadingStage != null) {
            loadingStage.hide();
        }
    }

    private Callable<String> getRequestXMLThread(URL url) {
        return new Callable<String>() {
            @Override
            public String call() {
                try {
                    URLConnection con = url.openConnection();
                    InputStream is = con.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));

                    StringBuilder returnToCaller = new StringBuilder();
                    String line = null;

                    // read each line and write to System.out
                    while ((line = br.readLine()) != null) {
                        returnToCaller.append(line).append("\n");
                    }
                    return returnToCaller.toString();
                } catch (IOException ex) {
                    Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
    }

    private String buildXMLRequest(List<Sequence> seqs, boolean highSensitivity, boolean includeProfiles, boolean skipHighProb) {
        StringBuilder urlrequest = new StringBuilder();
        urlrequest.append(URL_BASE).append("seq=");
        urlrequest.append('>').append(seqs.get(0).name).append(URL_NEWLINE).append(seqs.get(0).seqstr).append(URL_NEWLINE);
        for (int k = 1; k < seqs.size(); k++) {
            urlrequest.append('>').append(seqs.get(k).name).append(URL_NEWLINE).append(seqs.get(k).seqstr).append(URL_NEWLINE);
        }
        if (highSensitivity) {
            //allow weak (low probability) matches to be included
            urlrequest.append("&lowscore=1");
        } else {
            urlrequest.append("&lowscore=0");
        }
        if (includeProfiles) {
            urlrequest.append("&lowscore=0");
        } else {
            // show matches with low level scores (level = -1)
            urlrequest.append("&lowscore=1");
        }
        if (skipHighProb) {
            urlrequest.append("&skip=1");
        } else {
            // include matches to high prob motifs (i.e. allow low complexity patterns)
            urlrequest.append("&skip=0");
        }
        urlrequest.append("&output=xml");
        return urlrequest.toString();
    }

    // everything below is all FASTA reading of example sequences
    private void addExampleSequences(List<Sequence> toadd) {
        File temp;
        Scanner sc = null;
        BufferedWriter bw = null;

        try {
            temp = File.createTempFile("tempfile", ".tmp");
            InputStream inputStream = null;
            sc = null;
            inputStream = RemoteScanPrositeRunner.class.getResourceAsStream("examples");
            sc = new Scanner(inputStream, "UTF-8");
            //write it
            bw = new BufferedWriter(new FileWriter(temp));

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                bw.write(line + "\n");
            }
            bw.close();

            toadd.addAll($fasta_simple(temp));

        } catch (IOException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //if all else fails
        }
    }

    private List<Sequence> $fasta_simple(File f) {
        List<Sequence> ret = null;
        //List<String> ret = null;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            List<String> lines = new ArrayList<>();

            ret = new ArrayList<>();

            String name = null;
            StringBuilder sequence = null;

            String linex;
            while ((linex = br.readLine()) != null) {
                linex = linex.replaceAll("\\R+", "");
                linex = linex.replaceAll("\u2028", "");
                linex = linex.replaceAll("\\s+", "");
                if (!linex.isEmpty() && linex.charAt(0) == '>') {
                    if (name != null && sequence != null) {
                        ret.add(new Sequence(name, sequence.toString()));
                    }
                    String[] ss = linex.split("\\s+");
                    name = ss[0].substring(1);
                    sequence = new StringBuilder("");
                } else {
                    sequence.append(linex);
                }
            }
            if (name != null && sequence != null) {
                ret.add(new Sequence(name, sequence.toString()));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemoteScanPrositeRunner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException npe) {
            System.err.println("The FASTA file's first character was not '>'");
        }
        return ret;
    }

    private List<Sequence> getDegappedSequences(List<Sequence> sequences) {
        List<Sequence> ret = new ArrayList<>();
        for (Sequence seq : sequences) {
            ret.add(new Sequence(seq.name, seq.seqstr.replaceAll("-", "")));
        }
        return ret;
    }

    private String hexifyColorFX(Color c) {
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255f),
                (int) (c.getGreen() * 255f),
                (int) (c.getBlue() * 255f));
    }

}

class Sequence {

    public String name;
    public String seqstr;

    public Sequence() {

    }

    public Sequence(String name, String seqstr) {
        this.name = name;
        this.seqstr = seqstr;
    }
}
