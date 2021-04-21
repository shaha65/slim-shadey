/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;
import shahavi.bio471.scanprosite.database.profile.PrositeProfile;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositeEntry {

    //variables necessary to characterize an entry
    private String entryName = null; //name of entry
    private boolean isPattern = false; //is pattern?
    private boolean isProfile = false; //is profile?

    private String psaccession = null; //accessionn num
    private String description = ""; //pattern desc
    
    private String docaccession = null; //documentation accession

    private String strpattern = ""; //prosite pattern expression

    //matrix info
    private String alphabetOrder = null;
    private List<String> matrix = null;

    private String comments = "";
    private boolean skip = false;

    private final String EOL = "\\r|\\n";

    public PrositeEntry(String entryFile) {
        initialize(entryFile.split(EOL));
    }

    private void initialize(String[] entryFile) {
        String str = null;

        file = entryFile;

        String matrixData = "";

        while ((str = readLine()) != null) {
            String lineType = str.substring(0, 2);
            String lineData = str.substring(2).trim();

            if (lineType.equals("ID")) {  //ID
                entryName = lineData.substring(0, lineData.indexOf(";")); //get name of motif
                String entryType = lineData.substring(lineData.indexOf(";") + 1, //type of scan
                        lineData.length() - 1).trim();
                if (entryType.equals("PATTERN")) {

                    isPattern = true;
                } else if (entryType.equals("MATRIX")) {
                    matrix = new ArrayList<>();
                    isProfile = true;
                } else {
                    break; //the entry must be either a isPattern or a matrix
                }
            } else if (lineType.equals("AC")) { //ACCESSION
                psaccession = lineData.substring(0, lineData.indexOf(";"));
            } else if (lineType.equals("DT")) { //date and time (not particularly important?)

            } else if (lineType.equals("DE")) {
                description = description.concat(lineData);
            } else if (lineType.equals("PA") && isPattern) { //pattern line encountered, has pattern
                if (lineData.charAt(lineData.length() - 1) == '.') {
                    strpattern = strpattern.concat(lineData.substring(0, lineData.indexOf(".")));
                } else {
                    strpattern = strpattern.concat(lineData);
                }
            } else if (lineType.equals("MA") && isProfile) { //matrix line encountered, is profile
                matrixData = matrixData.concat(lineData);
            } else if (lineType.equals("CC")) {
                comments = comments.concat(lineData);
                if (comments.contains("SKIP-FLAG=TRUE")) {
                    skip = true;
                }
            } else if (lineType.equals("DO")) { //documentation accession
                docaccession = lineData.substring(0, lineData.indexOf(";"));
            }

        }

        // matrix loading - are we using the scanprosite matrix profiles??
        // ftp://ftp.expasy.org/databases/prosite/profile.txt
        String[] matrixDataEntries = matrixData.split("/");

        alphabetOrder = "";

        for (String mde : matrixDataEntries) {
            if (mde != null && !mde.isEmpty()) {
                String blockType = mde.substring(0, mde.indexOf(":"));
                String blockData = mde.substring(mde.indexOf(":") + 1);
                //System.out.println(blockType + "|||" + blockData);

                if (blockType.equals("GENERAL_SPEC")) {
                    String alphabetMarker = "ALPHABET='";
                    alphabetOrder = blockData.substring(blockData.indexOf(alphabetMarker) + "ALPHABET='".length());
                    alphabetOrder = alphabetOrder.substring(0, alphabetOrder.indexOf("'"));
                } else if (blockType.equals("DISJOINT")) {

                } else if (blockType.equals("NORMALIZATION")) {

                } else if (blockType.equals("CUT_OFF")) {

                } else if (blockType.equals("DEFAULT")) {

                } else if (blockType.equals("I") || blockType.equals("M")) {
                    matrix.add(blockType.concat("+").concat(blockData));
                }

            }
        }
        
    }

    private int count = -1;
    private String[] file = null;

    public String readLine() {
        if (count++ < file.length - 1) {
            return file[count];
        }
        return null;
    }

    /**
     * returns patternstring
     *
     * @return
     */
    public String getPattern() {
        return strpattern;
    }

    /**
     * Name of entry
     *
     * @return name
     */
    public String getName() {
        return entryName;
    }

    /**
     * Accession of entry
     *
     * @return accession
     */
    public String getAccession() {
        return psaccession;
    }
    
    /**
     * accession of prosite doc.
     * @return docacc
     */
    public String getDocumentationAccession() {
        return docaccession;
    }

    /**
     * Does this entry have a profile associated with it?
     *
     * @return whether this entry has a profile associated with it
     */
    public boolean isProfile() {
        return isProfile;
    }

    /**
     * Does this entry have a pattern associated with it?
     *
     * @return whether this entry has a pattern associated with it
     */
    public boolean isPattern() {
        return isPattern;
    }

    /**
     * Whether to skip the present entry when assigning motifs to a sequence
     *
     * @return whether this entry needs to be skipped
     */
    public boolean skip() {
        return skip;
    }
    
    public PrositeProfile getProfile() {
        if (isProfile) {
            return new PrositeProfile(alphabetOrder, matrix, psaccession);
        } else {
            return null;
        }
    }

}
