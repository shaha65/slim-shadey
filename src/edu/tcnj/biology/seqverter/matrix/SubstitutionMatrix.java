/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.matrix;

import java.io.BufferedReader;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that allows for fast retrieval of values from a substitution matrix.
 * A <code>SubstitutionMatrix</code> object can be generated from a standard
 * matrix comma separated value (.csv) file. The object takes two
 * <code>char</code>acter inputs and finds the corresponding value.
 *
 * @author Avi Shah
 */
public class SubstitutionMatrix {

    private String name;

    private Map<Character, Integer> mapToIndex;
    private double[][] matrix;

    /**
     * Construct a <code>SubstitutionMatrix</code> from a .csv
     * <code>File</code>.
     *
     * @param csv the <code>File</code> from which the
     * <code>SubstitutionMatrix</code> is being created.
     */
    public SubstitutionMatrix(File csv) {
        generate(csv);
    }

    /**
     * Construct a <code>SubstitutionMatrix</code> from a .csv
     * <code>InputStream</code>.
     *
     * @param is the .csv <code>InputStream</code> from which the
     * <code>SubstitutionMatrix</code> is being created.
     * @param name the name of the <code>SubstitutionMatrix</code> being
     * created.
     */
    public SubstitutionMatrix(InputStream is, String name) {
        generate(is, name);
    }

    /**
     * This constructor creates a <code>SubstitutionMatrix</code> from a local
     * .csv resource <code>File</code>.
     *
     * @param resourceName the name of the resource .csv <code>File</code>
     */
    protected SubstitutionMatrix(String resourceName) {
        generate(getClass().getResourceAsStream(resourceName), resourceName);
    }

    /**
     * Returns the value from the <code>SubstitutionMatrix</code> for two
     * defined <code>char</code>acters. If either of the supplied
     * <code>char</code>acters are invalid, the default return value is 0.0.
     *
     * @param c1 the first <code>char</code>acter.
     * @param c2 the second <code>char</code>acter.
     * @return the correct <code>double</code> value, 0.0 if either input is
     * invalid
     */
    public double score(char c1, char c2) {
        double ret;
        try {
            ret = matrix[mapToIndex.get(c1)][mapToIndex.get(c2)];
        } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
            ret = 0;
        }
        return ret;
    }

    /**
     * Returns a <code>char[]</code> containing all valid characters for this
     * <code>SubstitutionMatrix</code>.
     *
     * @return a <code>char[]</code> containing all valid characters for this
     * <code>SubstitutionMatrix</code>.
     */
    public char[] getValidCharacters() {
        char[] ret = new char[mapToIndex.size()];
        int i = 0;
        for (String s : (String[]) mapToIndex.keySet().toArray()) {
            ret[i] = s.charAt(0);
            i++;
        }
        return ret;
    }

    /**
     * Returns the matrix <code>double[][]</code> array of values for this
     * <code>SubstitutionMatrix</code>.
     *
     * @return Returns the matrix <code>double[][]</code> array of values for
     * this <code>SubstitutionMatrix</code>.
     */
    public double[][] getFullMatrixArray() {
        return this.matrix;
    }

    /**
     * Generates a <code>SubstitutionMatrix</code> object from a .csv
     * <code>File</code>. The name property of the object is set to the 
     * name of the <code>File</code> supplied.
     *
     * @param csv the <code>File</code> from which a
     * <code>SubstitutionMatrix</code> is to be created.
     * @return <code>true</code> if the <code>SubstitutionMatrix</code> is
     * successfully created.
     */
    private boolean generate(File csv) {
        try {
            return generate(new FileInputStream(csv), csv.getName());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("CSV Load failed for " + csv.getName());
            return false;
        }
    }

    /**
     * Generates a <code>SubstitutionMatrix</code> object from a .csv
     * <code>InputStream</code>.
     *
     * @param is the <code>InputStream</code> from which a
     * <code>SubstitutionMatrix</code> is to be created.
     * @param name <code>String</code> representation of this
     * <code>SubstitutionMatrix</code>'s name
     * @return <code>true</code> if the <code>SubstitutionMatrix</code> is
     * successfully created.
     */
    private boolean generate(InputStream is, String name) {
        this.name = name;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = br.readLine();
            String[] ss = line.split(",");//s.split("\\s*,\\s*");

            mapToIndex = new HashMap<>();

            //in case user inputs file where first 'cell' is not blank
            int i = 0;
            if ("".equals(ss[0])) {
                i = 1;
            }
            int j = i;
            for (; i < ss.length; i++) {
                mapToIndex.put(ss[i].charAt(0), i);
            }

            matrix = new double[ss.length - j][ss.length - j];

            int rowc = 0;

            while ((line = br.readLine()) != null) {
                ss = line.split(",");//s.split("\\s*,\\s*");

                for (int k = 1; k < ss.length; k++) {
                    matrix[rowc][k - 1] = Double.parseDouble(ss[k]);
                }

                rowc++;

            }

            fillTrans(matrix);

            is.close();

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("IOException encountered reading CSV");
            return false;
        }

    }

    /**
     * Fills the transverse, since by default, substitution matrix .csv files
     * only fill half of the matrix. See supplied or resource sub matrix files
     * for examples.
     * @param matrix the sub matrix's values 
     */
    private void fillTrans(double matrix[][]) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = row + 1; col < matrix[row].length; col++) {
                matrix[row][col] = matrix[col][row];
            }
        }
    }

    /**
     * Returns the name of this <code>SubstitutionMatrix</code>.
     * @return the name of this <code>SubstitutionMatrix</code>.
     */
    public String name() {
        return this.name;
    }

    /**
     * Sets the name of this <code>SubstitutionMatrix</code>.
     * @param name the new name of this <code>SubstitutionMatrix</code>.
     */
    public void setName(String name) {
        this.name = name;
    }

    //print function to check that array is properly held during testing
    private void printArray(double matrix[][]) {
        for (double[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

}
