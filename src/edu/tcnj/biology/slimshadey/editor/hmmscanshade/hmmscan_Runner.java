/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import javafx.scene.image.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class hmmscan_Runner {

    private hmmscan_GraphicsWindow GRAPHICS_PARENT;
    private VisualMultipleSequenceAlignment ALIGNMENT;

    public hmmscan_Runner(VisualMultipleSequenceAlignment vmsa) {
        this.ALIGNMENT = vmsa;
        this.GRAPHICS_PARENT = new hmmscan_GraphicsWindow(this);
        
        GRAPHICS_PARENT.hmmscan_popup(vmsa);
        /*
        List<Sequence> sequences = new ArrayList<>();
        this.addExampleSequences(sequences);
        sequences = this.getDegappedSequences(sequences);
        for (Sequence s : sequences) {
            System.out.println(s.name);
        }
        //getResultsMultithreaded(sequences);
        System.out.println(Arrays.toString("ABC".getBytes()));
        search_hmmscan();*/
    }

    private HashMap<String, String> resultMap;

    public String getResult(String name) {
        return resultMap.getOrDefault(name, "");
    }

    /**
     * Use EBI's hmmscan server to search sequences against pfam. Sequences
     * should be supplied in a HashMap mapping the sequence name to the
     * sequence. Sequence will be degapped before being sent. All instances of
     * HashMap[String, T] maps a sequence name to object in source.
     *
     * @param sequences is a mapping of sequence names to sequence Strings
     */
    public void search_hmmscan(HashMap<String, String> sequences) {
        resultMap = new HashMap<>();
        Task getResults = new Task<HashMap<String, String>>() {
            @Override
            protected HashMap<String, String> call() throws Exception {
                HashMap<String, Future<String>> threads = new HashMap<>();
                ExecutorService es = Executors.newCachedThreadPool();
                for (String seqName : sequences.keySet()) {
                    threads.put(seqName, es.submit(getScannerThread(seqName, sequences.get(seqName))));
                }

                es.shutdown();

                boolean allDone = false;
                HashMap<String, Boolean> doneMap = new HashMap<>();
                for (String seqName : sequences.keySet()) {
                    doneMap.put(seqName, false);
                }
                //colorindex = 0;
                while (!allDone) {
                    for (String seqName : sequences.keySet()) {
                        Future<String> future = threads.get(seqName);
                        if (future.isDone()) {
                            //System.out.println(seqName);
                            //System.out.println(future.get());
                            resultMap.put(seqName, future.get());
                            doneMap.put(seqName, true);
                        }
                    }
                    allDone = true;
                    for (String seqName : sequences.keySet()) {
                        if (!doneMap.get(seqName)) {
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

                return resultMap;
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
            HashMap<String, String> output = (HashMap<String, String>) getResults.getValue();
            for (String seq : output.keySet()) {
                //System.out.println(s.name);
                //System.out.println(s.seqstr);
                //System.out.println(output.get(s));
            }
            //printXML(sequences, output);
            //organizeOutput(output);
            //printHits(organizeOutput(output));
            GRAPHICS_PARENT.showSequenceList(ALIGNMENT, organizeOutput(output));

        });

        new Thread(getResults).start();
    }

    private void printHits(HashMap<String, List<hmmscan_Hit>> hitmap) {
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
        for (String s : hitmap.keySet()) {
            ta.appendText(s.concat("\n"));
            List<hmmscan_Hit> hitlist = hitmap.get(s);
            //System.err.println(hitlist);
            for (hmmscan_Hit sprh : hitlist) {
                ta.appendText(sprh.toString());
            }
            ta.appendText("\n\n");
        }
    }

    private HashMap<String, List<hmmscan_Hit>> organizeOutput(HashMap<String, String> rawXML) {
        HashMap<String, List<hmmscan_Hit>> ret = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        for (String seqName : rawXML.keySet()) {
            //System.out.println(rawXML.get(seqName));
            ret.put(seqName, parsedXML(seqName, rawXML.get(seqName), dbf));
        }
        return ret;
    }

    private List<hmmscan_Hit> parsedXML(String name, String XML, DocumentBuilderFactory dbf) {
        //System.out.println(name);
        List<List<String>> ret = null;
        try {
            //XML = XML.replaceAll("/>", ">");
            //System.err.println(name);
            //System.err.println(XML);
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)));

            return parseHitsXML(doc, true);
            /*
            System.err.println("Root element :" + doc.getDocumentElement().getNodeName());
            
            for (int k = 0; k < doc.getChildNodes().getLength(); k++) {
                Node currentNode = doc.getChildNodes().item(k);
                System.err.println(currentNode + " " + currentNode.getNodeName());
                for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                    Node deeperNode = currentNode.getChildNodes().item(j);
                    System.err.println("    " + deeperNode + " " + deeperNode.getNodeName());
                }
            }
             */
            //printNote(doc.getChildNodes());
            //go from matchset -> match -> elements of match
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(hmmscan_Runner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(hmmscan_Runner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(hmmscan_Runner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private List<hmmscan_Hit> parseHitsXML(Document XML, boolean onlyShownHits) {
        XML.normalizeDocument();

        List<hmmscan_Hit> hitList = new ArrayList<>();

        Node dataNode = XML.getChildNodes().item(0);
        //System.err.println("NEW" + dataNode.getNodeName());
        for (int i = 0; i < dataNode.getChildNodes().getLength(); i++) {
            Node nnm = dataNode.getChildNodes().item(i);
            if (nnm.getNodeType() == Node.ELEMENT_NODE) {
                //System.err.println(nnm.getNodeName());
                for (int k = 0; k < nnm.getChildNodes().getLength(); k++) {
                    Node nnmb = nnm.getChildNodes().item(k);
                    if (nnmb.getNodeType() == Node.ELEMENT_NODE && nnmb.getNodeName().equals("hits")) {
                        hmmscan_Hit currentHit = new hmmscan_Hit();
                        //System.err.println("   " + nnmb.getNodeName());
                        NamedNodeMap attrsHit = nnmb.getAttributes();
                        for (int z = 0; z < attrsHit.getLength(); z++) {
                            Node nodev = attrsHit.item(z);
                            switch (nodev.getNodeName()) {
                                case "name":
                                    currentHit.name = nodev.getNodeValue();
                                    break;
                                case "acc":
                                    currentHit.pfam_acc = nodev.getNodeValue();
                                    break;
                                case "desc":
                                    currentHit.desc = nodev.getNodeValue();
                                    break;
                                case "evalue":
                                    currentHit.domEval = nodev.getNodeValue();
                                    break;
                                default:
                                    break;
                            }
                        }
                        for (int j = 0; j < nnmb.getChildNodes().getLength(); j++) {
                            Node nnmc = nnmb.getChildNodes().item(j);
                            if (nnmc.getNodeType() == Node.ELEMENT_NODE && nnmc.getNodeName().equals("domains")) {
                                //System.err.println("   " + "   " + nnmc.getNodeName());
                                if (nnmc.getNodeName().equals("domains")) {
                                    NamedNodeMap attrMap = nnmc.getAttributes();
                                    if (attrMap.getNamedItem("display") != null) {
                                        int[] coords = new int[2];
                                        for (int y = 0; y < attrMap.getLength(); y++) {
                                            Node nodex = attrMap.item(y);
                                            switch (nodex.getNodeName()) {
                                                case "ienv":
                                                    coords[0] = Integer.parseInt(nodex.getNodeValue());
                                                    break;
                                                case "jenv":
                                                    coords[1] = Integer.parseInt(nodex.getNodeValue());
                                                    break;
                                                default:
                                                    //System.err.println("   " + "   " + "   " + nodex.getNodeName() + " " + nodex.getNodeValue());
                                                    break;
                                            }
                                        }
                                        currentHit.displayhits.add(coords);
                                    }
                                }
                            }
                        }
                        if (onlyShownHits) {
                            if (!currentHit.displayhits.isEmpty()) {
                                hitList.add(currentHit);
                            }
                        } else { //just add all hits
                            hitList.add(currentHit);
                        }
                    }
                }
            }
        }
        return hitList;
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

    private void printXML(HashMap<String, String> sequences, HashMap<String, String> output) {
        Stage outstage = new Stage();
outstage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox root = new VBox();
        TextArea ta = new TextArea();
        HBox.setHgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(ta, Priority.ALWAYS);
        ta.setEditable(false);
        //ta.setWrapText(true);
        root.getChildren().add(ta);
        outstage.setScene(new Scene(root, 600, 600, true));
        outstage.show();
        for (String seqName : sequences.keySet()) {
            ta.appendText(seqName.concat("\n"));
            ta.appendText(sequences.get(seqName).concat("\n"));
            ta.appendText(output.get(seqName).concat("\n"));
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

    private String hexifyColorFX(Color c) {
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255f),
                (int) (c.getGreen() * 255f),
                (int) (c.getBlue() * 255f));
    }

    public void startLoadingAnimation() {

        loadingStage = new Stage();
loadingStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        loadingStage.setTitle("Progress");

        pi = new ProgressIndicator(-1);

        status = new Label("hmmering...");

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

    private Callable<String> getScannerThread(String name, String seqstr) {
        return new Callable<String>() {
            @Override
            public String call() throws ParserConfigurationException {
                try {
                    URL url = new URL("https://www.ebi.ac.uk/Tools/hmmer/search/hmmscan");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setInstanceFollowRedirects(false);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Accept", "application/json");

                    String urlParameters = "hmmdb=" + URLEncoder.encode("pfam", "UTF-8")
                            + "&seq=" + ">" + (name.length() > 30 ? name.substring(30) : name) + "\n" + seqstr;

                    connection.setRequestProperty("Content-Length", ""
                            + Integer.toString(urlParameters.getBytes().length));

                    //Send request
                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();

                    URL respUrl = new URL(connection.getHeaderField("Location"));
                    HttpURLConnection connection2 = (HttpURLConnection) respUrl.openConnection();
                    connection2.setRequestMethod("GET");
                    connection2.setRequestProperty("Accept", "text/xml");
                    System.out.println(respUrl.toString());

                    //Get the response and print it to the screen
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    connection2.getInputStream()));

                    List<String> lines = new ArrayList<>();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {

                        lines.add(inputLine);

                    }

                    String retstr = "";
                    for (String l : lines) {
                        if (Character.isDigit(l.trim().charAt(1))) {
                            //System.out.println(l);
                            //System.out.println(l.replace("<", "<n_fix"));
                            l = l.replace("<", "<n_fix");
                            //System.out.println(l.substring(l.indexOf(" ") + 1));
                            //System.out.println(l.split("\\s+")[0]);
                            //System.out.println(l.replace("/>", ">\n + </" + l.trim().substring(1, l.trim().indexOf(" ")) + ">"));
                            //l = l.replace("/>", ">" + "</" + l.trim().substring(1, l.trim().indexOf(" ")) + ">");
                            //System.out.println(l);

                        }
                        retstr += l + "\n";
                    }

                    in.close();

                    return retstr;

                } catch (IOException ex) {
                    Logger.getLogger(hmmscan_Runner.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };
    }

    public void search_hmmscan() {
        try {
            URL url = new URL("https://www.ebi.ac.uk/Tools/hmmer/search/hmmscan");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json");

            System.out.println("EMGPSENDPNLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPV"
                    + "NSLEKHSWYHGPVSRNAAEYLLSSGINGSFLVRESESSPGQRSISLRYEG"
                    + "RVYHYRINTASDGKLYVSSESRFNTLAELVHHHSTVADGLITTLHYPAP");
            //Add the database and the sequence. Add more options as you wish!
            String urlParameters = "hmmdb=" + URLEncoder.encode("pfam", "UTF-8")
                    + "&seq=" + ">seq\nEMGPSENDPNLFVALYDFVASGDNTLSITKGEKLRVLGYNHNGEWCEAQTKNGQGWVPSNYITPV"
                    + "NSLEKHSWYHGPVSRNAAEYLLSSGINGSFLVRESESSPGQRSISLRYEG"
                    + "RVYHYRINTASDGKLYVSSESRFNTLAELVHHHSTVADGLITTLHYPAP";

            connection.setRequestProperty("Content-Length", ""
                    + Integer.toString(urlParameters.getBytes().length));

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Now get the redirect URL
            URL respUrl = new URL(connection.getHeaderField("Location"));
            HttpURLConnection connection2 = (HttpURLConnection) respUrl.openConnection();
            connection2.setRequestMethod("GET");
            connection2.setRequestProperty("Accept", "application/xml");

            //Get the response and print it to the screen
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection2.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
