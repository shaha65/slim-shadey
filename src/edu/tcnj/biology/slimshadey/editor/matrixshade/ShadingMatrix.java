/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor.matrixshade;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class ShadingMatrix {

    public String name = null;

    public double[][] scoreMatrix;
    private double minScore = 0d;

    public String ordering = null;
    //format is [subject_index][query_index][rgb_scheme]
    public double[][][] rgbMatrix = null;
    public String[][][] hexMatrix = null;

    private double[] DEFAULT_MATCH_RGB = new double[]{0d, 0d, 0d};
    private double[] DEFAULT_MISMATCH_RGB = new double[]{255d, 255d, 255d};

    public ShadingMatrix(String name, String ordering, double[][] scoreMatrix, double luminanceThreshold) {
        this.name = name;
        this.ordering = ordering;
        this.scoreMatrix = scoreMatrix.clone();
        this.hexMatrix = new String[scoreMatrix.length][scoreMatrix[0].length][2];
        this.rgbMatrix = new double[scoreMatrix.length][scoreMatrix[0].length][3];
        for (int k = 0; k < scoreMatrix.length; k++) {
            for (int j = 0; j < scoreMatrix[k].length; j++) {
                rgbMatrix[k][j] = DEFAULT_MATCH_RGB.clone();
                minScore = Math.min(minScore, scoreMatrix[k][j]);
                hexMatrix[k][j] = new String[]{"#FFFFFF", "#000000"};
            }
        }

        setShadingValues(DEFAULT_MATCH_RGB, DEFAULT_MISMATCH_RGB, luminanceThreshold);
    }

    public void setShadingValues(double[] matchRGB, double[] mismatchRGB, double luminanceThreshold) {
        for (int k = 0; k < ordering.length(); k++) {
            double minsub = 0;
            double maxsub = 0;
            for (int j = 0; j < ordering.length(); j++) {
                minsub = Math.min(minsub, getScore(k, j));
                maxsub = Math.max(maxsub, getScore(k, j));
            }
            double maxDistance = maxsub - minsub;
            for (int j = 0; j < ordering.length(); j++) {
                rgbMatrix[k][j] = distanceRGB(1 - (getScore(k, j) - minsub) / maxDistance, matchRGB, mismatchRGB);
                //System.out.println(ordering.charAt(k) + " " + ordering.charAt(j) + " " + rgbMatrix[k][j][0] + " " + rgbMatrix[k][j][1] + " " + rgbMatrix[k][j][2]);
            }

        }
        for (int k = 0; k < rgbMatrix.length; k++) {
            for (int j = 0; j < rgbMatrix[k].length; j++) {
                String backhex = this.rgbToHex_255(rgbMatrix[k][j]);
                String forehex = this.decideForegroundColor(rgbMatrix[k][j], luminanceThreshold);
                //System.out.println(getScore(k, j) + " " + backhex + " " + forehex);
                hexMatrix[k][j] = new String[]{backhex, forehex};
            }
        }
    }

    public String decideForegroundColor(double[] rgb255, double luminance) {
        //https://codepen.io/znak/pen/aOvMOd

        double[] rgbi = new double[]{rgb255[0] / 255d, rgb255[1] / 255d, rgb255[2] / 255d};
        for (int i = 0; i < rgb255.length; i++) {
            if (rgbi[i] < 0.03928) {
                rgbi[i] = rgbi[i] / 12.92;
            } else {
                rgbi[i] = Math.pow((rgbi[i] + 0.055) / 1.055, 2.4);
            }
        }
        //luminance calculation
        double L = 0.2126 * rgbi[0] + 0.7152 * rgbi[1] + 0.0722 * rgbi[2];
        //System.err.println("luminance " + L);
        //black if luminance exceeds threshold, white if does not
        return (L > luminance ? "#000000" : "#FFFFFF");
    }

    private String rgbToHex_255(double[] rgb) {
        return String.format("#%02X%02X%02X",
                (int) (rgb[0]),
                (int) (rgb[1]),
                (int) (rgb[2]));
    }

    public double[] distanceRGB(double ratio, double[] baseRGB, double[] maxRGB) {
        double r = Math.max(0d, Math.min(255d, baseRGB[0] + (maxRGB[0] - baseRGB[0]) * ratio));
        double g = Math.max(0d, Math.min(255d, baseRGB[1] + (maxRGB[1] - baseRGB[1]) * ratio));
        double b = Math.max(0d, Math.min(255d, baseRGB[2] + (maxRGB[2] - baseRGB[2]) * ratio));
        return new double[]{r, g, b};
        /*
        double[] ret = new double[]{255d, 255d, 255d};
        for (int k = 0; k < 2; k++) {
            double rfr = Math.min(baseRGB[k], maxRGB[k]);
            double rto = Math.max(baseRGB[k], maxRGB[k]);
            double r = rfr + (rto - rfr) * ratio;
            ret[k] = r;
        }
        return ret;
         */
    }

    public String[] getHex(char subject, char query) {
        int sindex = ordering.indexOf(subject);
        int qindex = ordering.indexOf(query);
        if (sindex != -1 && qindex != -1) {
            return hexMatrix[sindex][qindex];
        }
        return new String[]{"#FFFFFF", "#000000"};
    }

    public double[] getRGB(char subject, char query) {
        int sindex = ordering.indexOf(subject);
        int qindex = ordering.indexOf(query);
        if (sindex != -1 && qindex != -1) {
            return rgbMatrix[sindex][qindex];
        }
        return new double[]{255d, 255d, 255d};
    }

    public double getScore(int indexr, int indexc) {
        if (indexr != -1 && indexc != -1) {
            return scoreMatrix[indexr][indexc];
        }
        return minScore;
    }

    public double getScore(char row, char col) {
        int indexr = ordering.indexOf(row);
        int indexc = ordering.indexOf(col);
        if (indexr != -1 && indexc != -1) {
            return scoreMatrix[indexr][indexc];
        }
        return minScore;
    }

    @Override
    public String toString() {
        return name;
    }

}
