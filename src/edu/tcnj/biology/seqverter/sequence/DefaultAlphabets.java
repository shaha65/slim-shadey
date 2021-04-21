/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.sequence;

import java.io.BufferedReader;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * An <code>ArrayList</code> that contains <code>Alphabet</code> objects and can
 * add them from <code>File</code>s and <code>InputStream</code>s.
 *
 * @author Avi Shah
 */
public class DefaultAlphabets extends ArrayList<Alphabet> {

    /**
     * Creates an <code>Alphabets</code> object with standard DNA, RNA, Nucleic,
     * Protein alphabets. Can also add alphabets from .alpha files and
     * <tt>InputStream</tt>s.
     * <p>
     * This is a convenience class to facilitate reuse of associated classes; by
     * adding .alpha files to the package, and accessing them as in the
     * constructor, the program can initialize default <code>Alphabet</code>s
     * upon startup, and add user-defined <code>Alphabet</code>s.
     */
    public DefaultAlphabets() {
        super();
        //if reusing this code, move the default alphabets into your package

        //this.loadDefaults();
        Alphabet protein = load(this.getClass().getResourceAsStream("Protein.salpha"));
        Alphabet DNA = load(this.getClass().getResourceAsStream("DNA.salpha"));
        Alphabet RNA = load(this.getClass().getResourceAsStream("RNA.salpha"));
        this.add(protein);
        this.add(DNA);
        this.add(RNA);
        System.out.println(this.size());
    }

    protected Alphabet load(InputStream is) {
        Alphabet ret = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = br.readLine();

            ret = new Alphabet();

            if (line.equals("slim_alpha")) {
                while ((line = br.readLine()) != null) {

                    System.out.println(line);
                    String fieldName = line.substring(1, line.indexOf("="));
                    System.out.println(fieldName);

                    String data = line.substring(line.indexOf("="));
                    data = data.substring(data.indexOf("{") + 1, data.indexOf("}"));

                    if (fieldName.equals("Name")) {
                        ret.name = data;
                    } else if (fieldName.equals("Alphabet")) {
                        ret.defaultColorMapping = new HashMap<>();
                        for (String str : data.split("\\(")) {
                            str = str.trim();
                            str = str.replaceAll("\\)", "");
                            //System.out.print(str + " ");
                            //str = str.substring(0, str.indexOf(")"));
                            if (!str.isEmpty()) {
                                //System.out.println(str);
                                String[] splitstr = str.split(",");
                                //System.out.println(splitstr.length);
                                char alphachar = splitstr[0].charAt(0);
                                ret.put(alphachar, splitstr[1]);
                                ret.defaultColorMapping.put(alphachar, splitstr[2]);
                                //System.out.println(splitstr[0] + " " + splitstr[1] + " " + splitstr[2]);
                            }
                        }
                    } else if (fieldName.equals("Categories")) {
                        ret.categories = new HashMap<>();
                        for (String str : data.split("\\(")) {
                            str = str.trim();
                            str = str.replaceAll("\\)", "");
                            //System.out.print(str + " ");
                            //str = str.substring(0, str.indexOf(")"));
                            if (!str.isEmpty()) {
                                //System.out.println(str);
                                String[] splitstr = str.split(",");
                                String categoryName = splitstr[0];
                                char[] characters = splitstr[1].toCharArray();
                                for (char c : characters) {
                                    ret.categories.put(c, categoryName);
                                }
                            }
                        }

                    } else if (fieldName.equals("Consensus_alphabet")) {
                        ret.consensusAlphabetClustal = data;
                        System.err.println(ret.consensusAlphabetClustal);
                    } else if (fieldName.equals("Consensus_rules")) {
                        System.out.println("Consensus_rules");
                        System.out.println(data);
                        ret.consensusRulesClustal = new ArrayList<>();
                        String[] splitted = data.split("\\),");
                        for (int it = splitted.length - 1; it >= 0; it--) {
                            System.out.println(splitted[it]);

                            String clean = splitted[it].replaceAll("\\(", "").replaceAll("\\)", "");
                            List<String> datapts = Arrays.asList(clean.split(","));

                            //for (String s : datapts) {System.out.println("   " + s);}
                            ret.consensusRulesClustal.add(datapts);
                        }
                    } else if (fieldName.equals("Color_define")) {
                        ret.colorToHexClustal = new HashMap<>();
                        String[] splitted = data.split("\\),");
                        for (String s : splitted) {
                            System.out.println(s);
                            String clean = s.replaceAll("\\(", "").replaceAll("\\)", "");
                            String[] datapts = clean.split(",");
                            ret.colorToHexClustal.put(datapts[0], datapts[1]);
                        }
                        for (String s : ret.colorToHexClustal.keySet()) {
                            System.out.println(ret.colorToHexClustal.get(s));
                        }
                    } else if (fieldName.equals("Color_consensus_rules")) {
                        ret.consensusColorClustal = new HashMap<Character, String[]>();
                        String[] splitted = data.split("\\),");
                        for (String s : splitted) {
                            System.out.println(s);
                            String clean = s.replaceAll("\\(", "").replaceAll("\\)", "");
                            String[] datapts = clean.split(",");
                            char charToPut = datapts[0].charAt(0);
                            ret.consensusColorClustal.put(charToPut, datapts[1].split("/"));
                        }
                        /*
                        for (char ch : ret.consensusColorClustal.keySet()) {
                            String[] strarr = ret.consensusColorClustal.get(ch);
                            System.out.println(ch);
                            for (String str : strarr) {
                                System.out.println("  " + str);
                            }
                        }
                        */
                    } else if (fieldName.equals("MATRIX")) {
                        if (ret.matrices == null) {
                            ret.matrices = new ArrayList<Matrix>();
                        }
                        Matrix mat = new Matrix();
                        System.out.println(data.substring(0, data.indexOf("[")));
                        String[] matrixData = data.substring(1, data.indexOf("[")).split(",");

                        mat.name = matrixData[0];
                        mat.ordering = matrixData[1];
                        mat.gapOpen = Double.parseDouble(matrixData[2]);
                        mat.gapExtend = Double.parseDouble(matrixData[3]);
                        mat.terminalGapOpen = Double.parseDouble(matrixData[4]);
                        mat.terminalGapExtend = Double.parseDouble(matrixData[5]);

                        for (int w = 0; w < 6; w++) {
                            //System.out.println(matrixData[w]);
                        }
                        //System.out.println(data.substring(data.indexOf("[") + 1, data.indexOf("]")));
                        String[] rows = data.substring(data.indexOf("[") + 2, data.indexOf("]")).split("\\(");
                        double[][] filledMatrix = new double[rows.length][rows.length];

                        for (int z = 0; z < rows.length; z++) {

                            String rowData = rows[z].replace(")", "");
                            String[] rowDataSplit = rowData.split(",");
                            for (int y = 0; y < rowDataSplit.length; y++) {
                                filledMatrix[z][y] = Double.parseDouble(rowDataSplit[y]);
                                //System.out.print(rowDataSplit[y] + " " + y + ":");
                            }
                            //System.out.println();

                        }
                        //System.out.println(Arrays.deepToString(filledMatrix).replaceAll(" ", "").replaceAll("\\[", "(").replaceAll("]", ")"));
                        mat.matrix = filledMatrix;

                        ret.matrices.add(mat);
                    }

                }
            }
            System.out.println();

        } catch (IOException ex) {
            System.err.println("IOException encountered reading CSV");
        }
        return ret;
    }

    /**
     * Returns <code>true</code> if this object contains an
     * <code>Alphabet</code> whose name matches the parameter.
     *
     * @param name the name of the <code>Alphabet</code> to be found
     * @return <code>true</code> if an <code>Alphabet</code> is found whose name
     * contains the input name.
     */
    public boolean containsName(String name) {
        return this.stream().filter(x -> x.name().equals(name)).findFirst().isPresent();
    }

    /**
     * Adds an <code>Alphabet</code> from a .alpha file.
     *
     * @param alpha the <code>File</code> to be added.
     * @return <code>true</code> if the <code>Alphabet</code> is successfully
     * added.
     */
    public Alphabet add(File alpha) {
        try {
            return this.add(new FileInputStream(alpha));
        } catch (FileNotFoundException ex) {
            System.err.println(" Alphabet file not found");
            return null;
        }
    }

    /**
     * Adds an <code>Alphabet</code> from an <code>InputStream</code>.
     *
     * @param is the <code>InputStream</code> to be added.
     * @return <code>true</code> if the <code>Alphabet</code> is successfully
     * added.
     */
    public Alphabet add(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = br.readLine();

            Alphabet alpha = new Alphabet(line);

            while ((line = br.readLine()) != null) {

                Character c = line.charAt(0);
                String val = line.substring(2);
                alpha.put(c, val);
                System.out.print("(" + c + "," + alpha.get(c) + "),");
            }
            System.out.println();
            this.add(alpha);
            return alpha;
        } catch (IOException ex) {
            System.err.println("IOException encountered reading CSV");
            return null;
        }
    }

    private static final String DNA_FILENAME = "DNA.alpha";
    private static final String RNA_FILENAME = "RNA.alpha";
    private static final String NUCLEIC_FILENAME = "Nucleic.alpha";
    private static final String PROTEIN_FILENAME = "Protein.alpha";
    //private static final String PROTEIN_EXTENDED_FILENAME = "Protein_extended.alpha";

    //populate these later
    private static final String DNA_BASICRULES_FILENAME = null;
    private static final String RNA_BASICRULES_FILENAME = null;
    private static final String NUCLEIC_BASICRULES_FILENAME = null;
    private static final String PROTEIN_BASICRULES_FILENAME = "Protein.alpha";
    //private static final String PROTEIN_BASICRULES_EXTENDED_FILENAME = null;

    private void loadDefaults() {
        this.defaultAdd(DNA_BASICRULES_FILENAME, this.getClass().getResourceAsStream(DNA_FILENAME));
        this.defaultAdd(RNA_BASICRULES_FILENAME, this.getClass().getResourceAsStream(RNA_FILENAME));
        this.defaultAdd(NUCLEIC_BASICRULES_FILENAME, this.getClass().getResourceAsStream(NUCLEIC_FILENAME));
        this.defaultAdd(PROTEIN_BASICRULES_FILENAME, this.getClass().getResourceAsStream(PROTEIN_FILENAME));
        //this.defaultAdd(PROTEIN_BASICRULES_EXTENDED_FILENAME, this.getClass().getResourceAsStream(PROTEIN_EXTENDED_FILENAME));
    }

    private Alphabet defaultAdd(String basicRulesFilename, InputStream is) {
        Alphabet a = this.add(is);
        if (basicRulesFilename != null && !basicRulesFilename.isEmpty()) {

        }
        return a;
    }

}
