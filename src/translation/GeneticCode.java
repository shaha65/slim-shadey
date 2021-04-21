/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translation;

import java.io.BufferedReader;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class GeneticCode {

    private HashMap<String, String> geneticCode;
    private ArrayList<String> triplets;
    private String name;

    public GeneticCode(File gcode, String name) {
        this.name = name;
        try {
            generateHashMap(gcode);
        } catch (IOException ex) {
            Logger.getLogger(GeneticCode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public GeneticCode(InputStream gcode, String name) {
        this.name = name;
        try {
            generateHashMap(gcode);
        } catch (IOException ex) {
            Logger.getLogger(GeneticCode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String translateCodon(String triplet) {
        if (geneticCode.get(triplet) == null) {
            if (isNuc(triplet.charAt(0)) && isNuc(triplet.charAt(1))) {
                return checkForFullOverlap(triplet);
            }
            return "X";
        } else {
            return geneticCode.get(triplet);
        }
    }

    public String name() {
        return name;
    }

    private String checkForFullOverlap(String codon) {
        //System.out.println("  " + codon);
        ArrayList<String> repeatAAs = new ArrayList();
        for (String triplet : triplets) {
            System.out.println(triplet);
            if (triplet.substring(0, 2).equals(codon.substring(0, 2))) {
                repeatAAs.add(geneticCode.get(triplet));
            }
        }
        if (allElementsTheSame(repeatAAs)) {
            return repeatAAs.get(0);
        } else {
            return "X";
        }

    }

    private void generateHashMap(File gcode) throws FileNotFoundException, IOException {
        generateHashMap(new BufferedReader(new FileReader(gcode)));
    }

    private void generateHashMap(InputStream is) throws UnsupportedEncodingException, IOException {
        generateHashMap(new BufferedReader(new InputStreamReader(is, "UTF-8")));
    }

    private void generateHashMap(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = br.readLine();
        }
        String everything = sb.toString();
        //System.out.println(everything);

        ArrayList<String> splitted = new ArrayList(Arrays.asList(everything.split("\\s+")));
        String rem2 = splitted.remove(0);
        String rem1 = splitted.remove(0);

        triplets = new ArrayList<>();

        geneticCode = new HashMap<>();
        for (int i = 0; i < splitted.size() && i < 64 * 3; i++) {
            if (i % 3 == 0) {
                triplets.add(splitted.get(i));
                geneticCode.put(splitted.get(i), splitted.get(i + 1));
            }
        }

    }

    private boolean isNuc(char c) {
        switch (c) {
            case 'A':
                return true;
            case 'T':
                return true;
            case 'G':
                return true;
            case 'C':
                return true;
            case 'N':
                return false;
            default:
                return false;
        }
    }

    private boolean allElementsTheSame(ArrayList<String> array) {
        if (array.isEmpty()) {
            return true;
        } else {
            String first = array.get(0);
            for (String element : array) {
                if (!element.equals(first)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
