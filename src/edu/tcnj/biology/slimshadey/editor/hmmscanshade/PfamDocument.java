/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.hmmscanshade;

import java.io.BufferedReader;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
public class PfamDocument {

    public PfamDocument(String accession) {
        //start a loading animation
        startLoadingAnimation();

        Task<DocumentInfoHolder> task = new Task<DocumentInfoHolder>() {
            @Override
            public DocumentInfoHolder call() {
                try {

                    String lines = getLines(accession);

                    DocumentInfoHolder dif = parseDocument(lines);
                    dif.accession = accession;
                    dif.userFriendlyUrl = DEFAULT_URL_HTML + accession;

                    System.out.println(lines);

                    return dif;
                } catch (IOException | ParserConfigurationException | SAXException ex) {
                    Logger.getLogger(PfamDocument.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            DocumentInfoHolder dif = task.getValue();
            Platform.runLater(() -> {
                // use platform for UI op
                displayHit(dif);
                Platform.runLater(() -> endLoadingAnimation());
            });

        });

        task.setOnFailed(e -> {
            //kill loading animation on failure
            Platform.runLater(() -> endLoadingAnimation());
        });

        task.setOnCancelled(e -> {
            /* task was cancelled */
            Platform.runLater(() -> endLoadingAnimation());
        });

        Thread thread = new Thread(task);
        thread.start();

    }

    private void displayHit(DocumentInfoHolder dif) {
        Stage outstage = new Stage();
outstage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        outstage.setAlwaysOnTop(true);
        outstage.setTitle("Pfam hit info for " + dif.accession);
        VBox root = new VBox();
        TextArea ta = new TextArea();
        HBox.setHgrow(ta, Priority.ALWAYS);
        VBox.setVgrow(ta, Priority.ALWAYS);
        ta.setEditable(false);
        ta.setWrapText(true);
        root.getChildren().add(ta);
        outstage.setScene(new Scene(root, 600, 600, true));
        outstage.show();

        ta.setStyle("-fx-font-family: 'consolas';");

        ta.appendText("Pfam family site: " + dif.userFriendlyUrl);
        ta.appendText("\n");
        ta.appendText("Pfam accession: " + dif.accession + "; Pfam release: " + dif.pfamRelease + "; Pfam release date: " + dif.pfamReleaseDate);
        ta.appendText("\n");
        if (dif.clanInfo[0] != null && dif.clanInfo[1] != null) {
            ta.appendText("Clan accession: " + dif.clanInfo[0] + "; Clan ID: " + dif.clanInfo[1]);
            ta.appendText("\n");
        }
        ta.appendText("\n");
        if (!dif.categories.isEmpty()) {
            ta.appendText("Categories:");
            ta.appendText("\n");
            for (String[] category : dif.categories) {
                ta.appendText("Name: " + category[0] + "; GO ID: " + category[1] + "; Description: " + category[2]);
                ta.appendText("\n");
            }
        }
        ta.appendText("\n\n");
        ta.appendText(dif.familyInfo[0] != null ? dif.familyInfo[0] : "(no domain name available)");
        //ta.appendText("\n");
        ta.appendText(dif.familyInfo[1] != null ? dif.familyInfo[1] : "(no Pfam abstract available under this domain's accession)");

    }

    private DocumentInfoHolder parseDocument(String docLines) throws ParserConfigurationException, SAXException, IOException {
        DocumentInfoHolder dif = new DocumentInfoHolder();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(new ByteArrayInputStream(docLines.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().normalize();
        for (int k = 0; k < doc.getChildNodes().getLength(); k++) {
            Node currentNode = doc.getChildNodes().item(k);
            if (currentNode.getNodeName().equals("pfam")) {
                NamedNodeMap nnlAttr = currentNode.getAttributes();
                for (int t = 0; t < nnlAttr.getLength(); t++) {
                    Node currentItemAttr = nnlAttr.item(t);
                    if (currentItemAttr.getNodeName().equals("release")) {
                        dif.pfamRelease = currentItemAttr.getTextContent();
                    } else if (currentItemAttr.getNodeName().equals("release_date")) {
                        dif.pfamReleaseDate = currentItemAttr.getTextContent();
                    }
                    //System.out.println(nnlAttr.item(t).getNodeName());
                    //System.out.println(nnlAttr.item(t).getTextContent());
                }
                System.out.println();
                NodeList nl1 = currentNode.getChildNodes();
                for (int i = 0; i < nl1.getLength(); i++) {
                    if (nl1.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        if (nl1.item(i).getNodeName().equals("entry")) {
                            NodeList nl2 = nl1.item(i).getChildNodes();
                            for (int j = 0; j < nl2.getLength(); j++) {
                                if (nl2.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                    //System.out.println(nl2.item(j).getNodeName());
                                    if (nl2.item(j).getNodeName().equals("description")) {
                                        dif.familyInfo[0] = nl2.item(j).getTextContent();
                                    } else if (nl2.item(j).getNodeName().equals("comment")) {
                                        dif.familyInfo[1] = nl2.item(j).getTextContent();
                                    } else if (nl2.item(j).getNodeName().equals("clan_membership")) {
                                        // get clan info from tags
                                        NamedNodeMap nnm = nl2.item(j).getAttributes();
                                        for (int t = 0; t < nnm.getLength(); t++) {
                                            Node nnmAttr = nnm.item(t);
                                            if (nnmAttr.getNodeName().equals("clan_acc")) {
                                                dif.clanInfo[0] = nnmAttr.getTextContent();
                                            } else if (nnmAttr.getNodeName().equals("clan_id")) {
                                                dif.clanInfo[1] = nnmAttr.getTextContent();
                                            }
                                            //System.out.println(nnmAttr.getNodeName());
                                            //System.out.println(nnmAttr.getTextContent());
                                        }

                                    } else if (nl2.item(j).getNodeName().equals("go_terms")) {
                                        NodeList nl3 = nl2.item(j).getChildNodes();
                                        // category code goes here
                                        addGoTerms(dif, nl3);
                                    } else if (nl2.item(j).getNodeName().equals("curation_details")) {
                                        // not parsing these
                                    } else if (nl2.item(j).getNodeName().equals("hmm_details")) {
                                        // not parsing these
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        return dif;
    }

    private void addGoTerms(DocumentInfoHolder dif, NodeList nl3) {
        for (int t = 0; t < nl3.getLength(); t++) {
            Node nl3item = nl3.item(t);
            if (nl3item.getNodeType() == Node.ELEMENT_NODE) {
                if (nl3item.getNodeName().equals("category")) {
                    String[] nextCategory = new String[3];
                    String categoryName = nl3item.getAttributes().getNamedItem("name").getTextContent();
                    //System.out.println(categoryName);
                    nextCategory[0] = categoryName;
                    for (int h = 0; h < nl3item.getChildNodes().getLength(); h++) {
                        if (nl3item.getChildNodes().item(h).getNodeType() == Node.ELEMENT_NODE) {
                            if (nl3item.getChildNodes().item(h).getNodeName().equals("term")) {
                                //System.out.println(nl3item.getChildNodes().item(h).getAttributes().getNamedItem("go_id").getTextContent());
                                nextCategory[1] = nl3item.getChildNodes().item(h).getAttributes().getNamedItem("go_id").getTextContent();
                                //System.out.println(nl3item.getChildNodes().item(h).getTextContent());
                                nextCategory[2] = nl3item.getChildNodes().item(h).getTextContent();
                            }
                        }
                    }
                    dif.categories.add(nextCategory);
                }
            }
        }
    }

    private final String DEFAULT_URL_XML = "https://pfam.xfam.org/family?output=xml&acc=";
    private final String DEFAULT_URL_HTML = "https://pfam.xfam.org/family/";

    private String getLines(String accession) throws MalformedURLException, IOException {
        StringBuilder ret = new StringBuilder();

        URL pfamUrl = new URL(DEFAULT_URL_XML + accession);
        URLConnection urlc = pfamUrl.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));

        String line = null;

        // read each line and write to System.out
        while ((line = br.readLine()) != null) {
            String addLine = line.replaceAll("\\R+", "");
            ret.append(addLine).append("\n");
        }

        return ret.toString();
    }

    private Stage loadingStage;
    private ProgressIndicator pi;
    private Label status;

    private void startLoadingAnimation() {

        loadingStage = new Stage();
loadingStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        loadingStage.setTitle("Accessing Xfam");

        pi = new ProgressIndicator(-1);
        pi.setStyle(" -fx-accent: darkgreen;");
        status = new Label("Contacting aliens...");

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

}

class DocumentInfoHolder {

    String userFriendlyUrl = "";

    String pfamRelease = "";
    String pfamReleaseDate = "";

    String accession = "";

    List<String[]> categories = new ArrayList<>();

    String[] clanInfo = {null, null};
    String[] familyInfo = {null, null};

    DocumentInfoHolder() {
        // empty constructor, fields directly accessible within PfamDocument
    }

}
