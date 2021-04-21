/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.matrix;

import java.io.File;
import javafx.scene.image.Image;
import java.util.ArrayList;

/**
 * A container class for several <code>SubstitutionMatrix</code> objects.
 * Initializing the <code>SubstitutionMatrices</code> object generates default
 * substitution matrices from existing resource files and makes an organized
 * structure for holder future <code>SubstitutionMatrix</code> objects.
 *
 * @author Avi Shah
 */
public class SubstitutionMatrices extends ArrayList<SubstitutionMatrix> {

    /**
     * Constructor for <code>SubstitutionMatrices</code>, makes generating
     * default matrices optional.
     *
     * @param generateDefault if <code>true</code> executes internal methods to
     * access local resources and load listed matrices.
     */
    public SubstitutionMatrices(boolean generateDefault) {
        super();
        if (generateDefault) {
            this.addPAM250();
            this.addBLOSUM62();
        }
    }

    /**
     * Add a substitution matrix from a standard comma separated value
     * <code>File</code>.
     *
     * @param csv the <code>File</code> from which a
     * <code>SubstitutionMatrix</code> is to be created and added to the active
     * <code>SubstitutionMatrices</code>
     * @return <code>true</code> if a <code>SubstitutionMatrix</code> is
     * successfully created and added from the specified .csv <code>File</code>.
     */
    public boolean add(File csv) {
        try {
            this.add(new SubstitutionMatrix(csv));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds the PAM250 substitution from a resource .csv file.
     *
     * @return <code>true</code> if PAM250 is successfully loaded from resources
     */
    public boolean addPAM250() {
        try {
            this.add(new SubstitutionMatrix("PAM250.csv"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds the BLOSUM62 substitution from a resource .csv file.
     *
     * @return <code>true</code> if BLOSUM62 is successfully loaded from
     * resources
     */
    public boolean addBLOSUM62() {
        try {
            this.add(new SubstitutionMatrix("BLOSUM62.csv"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
