/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import edu.tcnj.biology.seqverter.sequence.Alphabet;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class ShadeyProjectManager {

    //each annotation/sequence is a hashmap; the name points to a list of 
    //string[] of size 3, string[0] is the character at that index in the list
    //and string[1] is the hexcode for background color, string[3] is the
    //hexcode for foreground color
    List<HashMap<String, List<String[]>>> annoData;
    List<HashMap<String, List<String[]>>> seqData;
    List<String[]> consensus;

    int firstIndex = 1;
    int fontSize = -1;
    double luminanceThreshold = 0.179;
    boolean collapse = false;

    boolean trackLiveHover = true;
    long refreshDelay = 0;

    private EditorInterface owner;

    public ShadeyProjectManager(EditorInterface owner) {
        this.owner = owner;
    }

    private List<String> fileContents;

    public EditorTab getData(File f) {
        VBox annotationLabelsTemp = new VBox();
        VBox annotationRowsTemp = new VBox();
        VBox sequenceNamesTemp = new VBox();
        VBox visualSequencesTemp = new VBox();
        HBox consensusTemp = new HBox();
        String name = f.getName();
        Alphabet alpha = null;

        try {
            //BufferedReader br = new BufferedReader(new FileReader(f));
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));

            String line = br.readLine();
            if (line.equals(".slim")) {
                while ((line = br.readLine()) != null) {
                    if (line.charAt(0) == '$') {
                        String fieldName = line.substring(1);
                        if (line.substring(1).contains("fontsize")) {
                            fontSize = Integer.parseInt(getLineData(line));
                        } else if (line.substring(1).contains("firstindex")) {
                            firstIndex = Integer.parseInt(getLineData(line));
                        } else if (line.substring(1).contains("collapse")) {
                            collapse = getLineData(line).equals("true");
                        } else if (line.substring(1).contains("luminance")) {
                            luminanceThreshold = Double.parseDouble(getLineData(line));
                        } else if (line.substring(1).contains("livehover")) {
                            trackLiveHover = getLineData(line).equals("true");
                        } else if (line.substring(1).contains("refreshdelay")) {
                            refreshDelay = Long.parseLong(getLineData(line));
                        } else if (line.substring(1).contains("alphabet")) {
                            String alphaname = getLineData(line);
                            alpha_loop:
                            for (Alphabet a : owner.getAlphabets()) {
                                if (alphaname.equals(a.toString())) {
                                    alpha = a;
                                    break alpha_loop;
                                }
                            }
                        } else if (line.substring(1).contains("annotation_data")) {
                            line = br.readLine();
                            HBox annotation = new HBox();
                            Label annoLabel = new Label();
                            annoLabel.setText(line.substring(1));
                            annotationRowsTemp.getChildren().add(annotation);
                            annotationLabelsTemp.getChildren().add(annoLabel);

                            boolean isanno = true;
                            boolean isnumb = false;
                            boolean isseq = false;
                            boolean isgroup = false;
                            boolean iscons = false;

                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                if (line.startsWith("}")) {

                                    break;
                                } else if (line.startsWith("%")) {
                                    annotation = new HBox();
                                    annoLabel = new Label();
                                    annoLabel.setText(line.substring(1));
                                    annoLabel.setStyle(generateFont("#FFFFFF", "#000000"));
                                    annotationRowsTemp.getChildren().add(annotation);
                                    annotationLabelsTemp.getChildren().add(annoLabel);
                                } else {
                                    String[] data = new String[]{"", "", ""};
                                    //String[] data = line.split("\\.");
                                    data[0] = String.valueOf(line.charAt(0));
                                    data[1] = String.valueOf(line.substring(2, 9));
                                    data[2] = String.valueOf(line.substring(10));
                                    //System.out.println(Arrays.toString(data));
                                    VisualBioChar vbc = new VisualBioChar(data[0],
                                            isanno, isnumb, isseq, isgroup, iscons);
                                    vbc.setCurrentStyle(generateFont(data[1], data[2]), data[1], data[2]);
                                    annotation.getChildren().add(vbc);
                                }
                            }

                        } else if (line.substring(1).contains("sequence_data")) {
                            line = br.readLine();
                            HBox sequence = new HBox();
                            Label seqLabel = new Label();
                            seqLabel.setText(line.substring(1));

                            visualSequencesTemp.getChildren().add(sequence);
                            sequenceNamesTemp.getChildren().add(seqLabel);

                            boolean isanno = false;
                            boolean isnumb = false;
                            boolean isseq = true;
                            boolean isgroup = false;
                            boolean iscons = false;

                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                if (line.startsWith("}")) {

                                    break;
                                } else if (line.startsWith("%")) {
                                    sequence = new HBox();
                                    seqLabel = new Label();
                                    seqLabel.setText(line.substring(1));
                                    seqLabel.setStyle(generateFont("#FFFFFF", "#000000"));
                                    visualSequencesTemp.getChildren().add(sequence);
                                    sequenceNamesTemp.getChildren().add(seqLabel);
                                } else {
                                    String[] data = new String[]{"", "", ""};
                                    //String[] data = line.split("\\.");
                                    data[0] = String.valueOf(line.charAt(0));
                                    data[1] = String.valueOf(line.substring(2, 9));
                                    data[2] = String.valueOf(line.substring(10));
                                    //System.out.println(Arrays.toString(data));
                                    VisualBioChar vbc = new VisualBioChar(data[0],
                                            isanno, isnumb, isseq, isgroup, iscons);
                                    vbc.setCurrentStyle(generateFont(data[1], data[2]), data[1], data[2]);
                                    sequence.getChildren().add(vbc);
                                }
                            }
                        } else if (line.substring(1).contains("consensus_data")) {
                            consensusTemp = new HBox();

                            boolean isanno = false;
                            boolean isnumb = false;
                            boolean isseq = false;
                            boolean isgroup = false;
                            boolean iscons = true;

                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                if (line.startsWith("}")) {

                                    break;
                                } else {
                                    String[] data = line.split("\\.");
                                    //System.out.println(Arrays.toString(data));
                                    VisualBioChar vbc = new VisualBioChar(data[0],
                                            isanno, isnumb, isseq, isgroup, iscons);
                                    vbc.setCurrentStyle(generateFont(data[1], data[2]), data[1], data[2]);
                                    consensusTemp.getChildren().add(vbc);
                                }
                            }
                        }
                    }
                }

                EditorTab et = new EditorTab(annotationLabelsTemp,
                        annotationRowsTemp, sequenceNamesTemp, visualSequencesTemp,
                        consensusTemp,
                        new int[]{firstIndex - 1, 0}, name, alpha, this.owner, fontSize, luminanceThreshold,
                        collapse, trackLiveHover, refreshDelay);
                return et;

            } else {
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(ShadeyProjectManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String getLineData(String line) {
        return line.substring(line.indexOf("{") + 1, line.indexOf("}"));
    }

    public void setData(EditorTab et, File f) {
        VisualMultipleSequenceAlignment vmsa = et.getVMSA();
        boolean recollapse = vmsa.isCollapsed();
        if (vmsa.isCollapsed()) {
            collapse = true;
            vmsa.collapseIdenticalSequences(false);
        }
        this.firstIndex = vmsa.getFirstIndex();
        this.fontSize = vmsa.fontSize();
        this.luminanceThreshold = vmsa.luminanceThreshold();

        this.trackLiveHover = vmsa.getLiveHover();
        this.refreshDelay = vmsa.getRefreshDelay();

        fileContents = new ArrayList<>();
        fileContents.add(".slim");
        fileContents.add("$fontsize{" + String.valueOf(fontSize) + "}");
        fileContents.add("$firstindex{" + String.valueOf(firstIndex) + "}");
        fileContents.add("$luminance{" + String.valueOf(luminanceThreshold) + "}");
        fileContents.add("$alphabet_def{" + vmsa.getAlphabet().toString() + "}");
        fileContents.add("$collapse{" + (collapse ? "true" : "false") + "}");
        fileContents.add("$livehover{" + (trackLiveHover ? "true" : "false") + "}");
        fileContents.add("$refreshdelay{" + String.valueOf(refreshDelay) + "}");
        fileContents.add("$annotation_data{");
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            fileContents.add("%" + vmsa.getAnnotationName(k));
            for (int j = 0; j < vmsa.sequenceLength(); j++) {
                VisualBioChar vbc = vmsa.getAnnoVBC(k, j);
                fileContents.add(String.valueOf(vbc.getChar()) + "."
                        + vbc.getBackHex() + "." + vbc.getForeHex());
            }
        }
        fileContents.add("}");
        fileContents.add("$sequence_data{");

        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            fileContents.add("%" + vmsa.getSequenceName(k));
            for (int j = 0; j < vmsa.sequenceLength(); j++) {
                VisualBioChar vbc = vmsa.getVBC(k, j);
                fileContents.add(String.valueOf(vbc.getChar()) + "."
                        + vbc.getBackHex() + "." + vbc.getForeHex());
            }
        }
        fileContents.add("}");
        fileContents.add("$consensus_data{");
        for (int j = 0; j < vmsa.sequenceLength(); j++) {
            VisualBioChar vbc = vmsa.getConsensusVBC(j);
            fileContents.add(String.valueOf(vbc.getChar()) + "."
                    + vbc.getBackHex() + "." + vbc.getForeHex());
        }
        fileContents.add("}");

        write(f, fileContents);

        if (recollapse) {
            vmsa.collapseIdenticalSequences(true);
        }
    }

    public void setData_collapseOpts(EditorTab et, File f) {
        VisualMultipleSequenceAlignment vmsa = et.getVMSA();

        this.firstIndex = vmsa.getFirstIndex();
        this.fontSize = vmsa.fontSize();
        this.luminanceThreshold = vmsa.luminanceThreshold();

        this.trackLiveHover = vmsa.getLiveHover();
        this.refreshDelay = vmsa.getRefreshDelay();

        fileContents = new ArrayList<>();
        fileContents.add(".slim");
        fileContents.add("$fontsize{" + String.valueOf(fontSize) + "}");
        fileContents.add("$firstindex{" + String.valueOf(firstIndex) + "}");
        fileContents.add("$luminance{" + String.valueOf(luminanceThreshold) + "}");
        fileContents.add("$alphabet_def{" + vmsa.getAlphabet().toString() + "}");
        fileContents.add("$collapse{" + (collapse ? "true" : "false") + "}");
        fileContents.add("$livehover{" + (trackLiveHover ? "true" : "false") + "}");
        fileContents.add("$refreshdelay{" + String.valueOf(refreshDelay) + "}");
        fileContents.add("$annotation_data{");
        for (int k = 0; k < vmsa.annotationNumber(); k++) {
            fileContents.add("%" + vmsa.getAnnotationName(k));
            for (int j = 0; j < vmsa.sequenceLength(); j++) {
                VisualBioChar vbc = vmsa.getAnnoVBC(k, j);
                fileContents.add(String.valueOf(vbc.getChar()) + "."
                        + vbc.getBackHex() + "." + vbc.getForeHex());
            }
        }
        fileContents.add("}");
        fileContents.add("$sequence_data{");

        for (int k = 0; k < vmsa.sequenceNumber(); k++) {
            fileContents.add("%" + vmsa.getSequenceName(k));
            for (int j = 0; j < vmsa.sequenceLength(); j++) {
                VisualBioChar vbc = vmsa.getVBC(k, j);
                fileContents.add(String.valueOf(vbc.getChar()) + "."
                        + vbc.getBackHex() + "." + vbc.getForeHex());
            }
        }
        fileContents.add("}");
        fileContents.add("$consensus_data{");
        for (int j = 0; j < vmsa.sequenceLength(); j++) {
            VisualBioChar vbc = vmsa.getConsensusVBC(j);
            fileContents.add(String.valueOf(vbc.getChar()) + "."
                    + vbc.getBackHex() + "." + vbc.getForeHex());
        }
        fileContents.add("}");

        write(f, fileContents);
    }

    public void write(File f, List<String> contents) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (String s : contents) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public final int DEFAULT_FONTSIZE = 18;
    public final String DEFAULT_BACKGROUND = "#FFFFFF";
    public final String DEFAULT_FOREGROUND = "#000000";
    private final String DEFAULT_STYLE = "\n"
            + "    -fx-font-family: \"unifont\";\n"
            + "    -fx-font-size: " + String.valueOf(DEFAULT_FONTSIZE) + ";\n"
            + "    -fx-background-color:" + DEFAULT_BACKGROUND + ";\n"
            + "    -fx-text-fill: " + DEFAULT_FOREGROUND + ";\n"
            + "    -fx-underline: false ;\n"
            + "";

    public String generateFont(String backColor, String foreColor) {
        StringBuilder str = new StringBuilder("");
        String backgroundHex = DEFAULT_BACKGROUND;
        //https://stackoverflow.com/questions/17925318/how-to-get-hex-web-string-from-javafx-colorpicker-color
        if (backColor != null) {
            backgroundHex = backColor;
        }
        String foregroundHex = DEFAULT_FOREGROUND;
        if (foreColor != null) {
            foregroundHex = foreColor;
        }
        if (fontSize == -1) {
            fontSize = DEFAULT_FONTSIZE;
        }

        str.append("    -fx-font-family: \"unifont\";\n"); //everything unifont - always
        str.append("    -fx-font-size: ").append(String.valueOf(fontSize)).append(";\n");
        str.append("    -fx-background-color: ").append(backgroundHex).append(" ;\n");
        str.append("    -fx-text-fill: ").append(foregroundHex).append(" ;\n");

        return str.toString();
    }

}
