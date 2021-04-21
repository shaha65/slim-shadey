/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.sequence;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class Matrix {
    
    public String name = null;
    
    public String ordering = null;
    public double[][] matrix = null;
    
    public double gapOpen = 0;
    public double gapExtend = 0;
    
    public double terminalGapOpen = 0;
    public double terminalGapExtend = 0;
    
    public Matrix() {
        
    }
    
    public double get(char row, char col) {
        int indexr = ordering.indexOf(row);
        int indexc = ordering.indexOf(col);
        if (indexr != -1 && indexc != -1) {
            return matrix[indexr][indexc];
        }
        return Double.MIN_VALUE;
    }
    
    private boolean minValsCalculated = false;
    private double[] minsByChar;
    private double matrixMin;
    
    private void setMins() {
        matrixMin = matrix[0][0];
        minsByChar = new double[ordering.length()];
        for (int i = 0; i < ordering.length(); i++) {
            double currentCharMin = matrix[i][0];
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] < currentCharMin) {
                    currentCharMin = matrix[i][j];
                }
                if (matrix[i][j] < matrixMin) {
                    matrixMin = matrix[i][j];
                }
            }
            minsByChar[i] = currentCharMin;
        }
        
    }
    
    public double getOrMin(char query, char subject) {
        int indexQuery = ordering.indexOf(query);
        int indexSubject = ordering.indexOf(subject);
        if (indexQuery != -1 && indexSubject != -1) {
            return matrix[indexQuery][indexSubject];
        }
        if (!minValsCalculated) {
            setMins();
            minValsCalculated = true;
        }
        if (indexQuery == -1) {
            return matrixMin;
        } else if (indexSubject == -1) {
            return minsByChar[indexQuery];
        }
        return Double.MIN_VALUE;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
