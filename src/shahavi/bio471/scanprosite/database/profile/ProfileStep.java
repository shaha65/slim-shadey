/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.profile;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class ProfileStep {

    private String state = null;

    public final boolean IS_M;
    public final boolean IS_I;

    public final char SY;

    public boolean IS_SINGLE_MATCH;
    public double M_single;
    public double[] M_vector; //Match extension scores
    public double M0; //Match extension score, char not in alphabet
    public double D; //Deletion extension score
    public double I_single; //Insert extension score(s) for characters included in the alphabet
    public double I0; //Insert extension score for a character not included in the alphabet

    private List<String> otherStateNames = Arrays.asList(" M=", "M0=", "I0=", " I=", " D=", "B0=", "B1=", "E0=", "E1=");

    // {B, M, I, D} to {M, I, D, E};
    public final double B0; //external initiation
    public final double B1; //internal initiation
    public final double E0; //external termination
    public final double E1; //internal termination

    public final List<Character> startStates = Arrays.asList('B', 'M', 'I', 'D');
    public final List<Character> endStates = Arrays.asList('M', 'I', 'D', 'E');
    private double[][] stateMatrix = {
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0},
        {0, 0, 0, 0}};

    private final String[][] stateMatrixNames = {
        {"BM=", "BI=", "BD=", "BE="},
        {"MM=", "MI=", "MD=", "ME="},
        {"IM=", "II=", "ID=", "IE="},
        {"DM=", "DI=", "DD=", "DE="}};
    // [from][to]

    private final String ALPHABET_ORDER;

    /*
      1  external initiation score
      1  internal initiation score
     16  state transition scores for all transitions between
         elements of {B,M,I,D} and {M,I,D,E}
      K  insert extension scores for each character of the alphabet
      1  insert extension score for an unexpected character
      1  internal termination score
      1  external termination score
     */
    public ProfileStep(String orig, String alphabetOrder, int index) {
        this.state = orig;
        this.ALPHABET_ORDER = alphabetOrder;
        int i = index;

        //determine m or i
        if (orig.charAt(0) == 'M') {
            IS_M = true;
            IS_I = false;
        } else if (orig.charAt(0) == 'I') {
            IS_M = false;
            IS_I = true;
        } else {
            IS_M = false;
            IS_I = false;
        }

        //SY
        if (state.contains("SY")) {
            String sf_sf = "SY=";
            SY = state.charAt(state.indexOf(sf_sf) + sf_sf.length() + 1);
        } else {
            if (IS_M) {
                SY = 'X';
            } else if (IS_I) {
                SY = '-';
            } else {
                SY = '\u0000';
            }
        }

        for (int k = 0; k < stateMatrixNames.length; k++) {
            for (int j = 0; j < stateMatrixNames[k].length; j++) {
                if (state.contains(stateMatrixNames[k][j])) {
                    System.out.println();
                    String sf_name = stateMatrixNames[k][j];
                    String sub = state.substring(state.indexOf(sf_name) + sf_name.length());
                    double value = Double.parseDouble(sub.substring(0, sub.indexOf(";")));
                    stateMatrix[k][j] = value;
                    System.out.println(sf_name + " " + value);
                    break;
                }
            }
        }

        boolean IS_SINGLE_MATCH = false;
        double M_single = 0;
        double[] M_vector = null; //Match extension scores
        double M0 = 0; //Match extension score, char not in alphabet
        double D = 0; //Deletion extension score
        double I_single = 0; //Insert extension score(s) for characters included in the alphabet
        double I0 = 0; //Insert extension score for a character not included in the alphabet

        double B0 = 0; //external initiation
        double B1 = 0; //internal initiation
        double E0 = 0; //external termination
        double E1 = 0; //internal termination

        for (String sf : otherStateNames) {
            String stateinfo = state.substring(3);
            if (stateinfo.contains(sf)) {
                String sub = stateinfo.substring(stateinfo.indexOf(sf) + sf.length());
                //System.out.println(sub);
                String sub2 = sub.substring(0, sub.indexOf(";"));
                //System.out.println(sub2);
                double value = 0;
                if (sub2.contains(",")) {
                    value = 0;
                } else {
                    value = Double.parseDouble(sub.substring(0, sub.indexOf(";")));
                }
                //System.out.println(value);
                //Arrays.asList(/*"M"*/"M0", "I0", "I", "D", "B0", "B1", "E0", "E1");
                if (sf.equals("M0=")) {
                    M0 = value;
                } else if (sf.equals("I0=")) {
                    I0 = value;
                } else if (sf.equals(" I=")) {
                    I_single = value;
                } else if (sf.equals(" D=")) {
                    D = value;
                } else if (sf.equals("B0=")) {
                    B0 = value;
                } else if (sf.equals("B1=")) {
                    B1 = value;
                } else if (sf.equals("E0=")) {
                    E0 = value;
                } else if (sf.equals("E1=")) {
                    E1 = value;
                } else if (sf.equals(" M=")) {
                    String sub3 = stateinfo.substring(stateinfo.indexOf(" M=") + " M=".length());
                    String[] splitComma = sub3.substring(0, sub3.indexOf(";")).split(",");
                    if (splitComma.length == this.ALPHABET_ORDER.length()) {
                        double[] vec = new double[splitComma.length];
                        for (int o = 0; o < splitComma.length; o++) {
                            vec[o] = Double.parseDouble(splitComma[o]);
                        }
                        this.M_vector = vec;
                        //System.out.println(splitComma.length + " " + Arrays.toString(vec));
                    }
                }
            }
        }

        this.IS_SINGLE_MATCH = IS_SINGLE_MATCH;
        this.M_single = M_single;
        this.M_vector = M_vector; //Match extension scores
        this.M0 = M0; //Match extension score, char not in alphabet
        this.D = D; //Deletion extension score
        this.I_single = I_single; //Insert extension score(s) for characters included in the alphabet
        this.I0 = I0; //Insert extension score for a character not included in the alphabet

        this.B0 = B0; //external initiation
        this.B1 = B1; //internal initiation
        this.E0 = E0; //external termination
        this.E1 = E1; //internal termination

        //System.out.println(SY);
        this.toString();
    }

    private void set(String stateName, double value) {
        for (int i = 0; i < stateMatrixNames.length; i++) {
            for (int j = 0; j < stateMatrixNames[i].length; j++) {
                if (stateMatrixNames[i][j].equals(stateName)) {
                    stateMatrix[i][j] = value;
                    break;
                }
            }
        }
    }

    public String toString() {
        StringBuilder ret = new StringBuilder("");

        for (int i = 0; i < stateMatrix.length; i++) {
            for (int j = 0; j < stateMatrix[i].length; j++) {
                ret.append(" ").append(stateMatrixNames[i][j]).append(" ").append(String.valueOf(stateMatrix[i][j])).append(";");
            }
        }
        //Arrays.asList(/*"M"*/"M0", "I0", "I", "D", "B0", "B1", "E0", "E1");
        if (this.IS_M) {
            if (this.IS_SINGLE_MATCH) {
                ret.append(" ").append("M_single=").append(" ").append(String.valueOf(M_single)).append(";");
            } else {
                ret.append(" ").append("M_vector=").append(" ").append(Arrays.toString(M_vector)).append(";");
            }
        }
        ret.append(" ").append("I_single=").append(" ").append(String.valueOf(I_single)).append(";");
        ret.append(" ").append("I0=").append(" ").append(String.valueOf(I0)).append(";");
        ret.append(" ").append("D=").append(" ").append(String.valueOf(D)).append(";");
        ret.append(" ").append("B0=").append(" ").append(String.valueOf(B0)).append(";");
        ret.append(" ").append("B1=").append(" ").append(String.valueOf(B1)).append(";");
        ret.append(" ").append("E0=").append(" ").append(String.valueOf(E0)).append(";");
        ret.append(" ").append("E1=").append(" ").append(String.valueOf(E1)).append(";");

        //System.out.println(ret.toString());
        return ret.toString();
    }

    public void initialize() {

    }
}
