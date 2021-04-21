/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.documentation;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class Document {

    public final String DOC_ACCESSION;
    public final String DESC;
    public final String DOC;

    public Document(String docaccession, String name, String doc) {

        this.DOC_ACCESSION = docaccession;
        this.DESC = name;
        this.DOC = doc;
    }

    private final String EOL = "\\r|\\n";

    public Document(String entryFile) {
        String docac = null;
        String desc = null;
        String doc = entryFile;

        file = entryFile.split(EOL);
        String line = null;

        boolean hasbegun = false;
        
        while ((line = readLine()) != null) {

            if (!hasbegun && line.contains("BEGIN")) {
                hasbegun = true;
                line = readLine();
                line = readLine();
                desc = line;
                readLine();
                
            } else if (line.startsWith("{")) {
                if (line.contains("END")) {

                } else {
                    if (line.contains(";")) {
                        //not saving pattern accession nums presently
                    } else { //document id
                        docac = line.substring(1, line.length() - 1);
                        //System.out.println(line.substring(1, line.length() - 1));
                    }
                }
            }

        }
        this.DOC_ACCESSION = docac;
        this.DESC = desc;
        this.DOC = doc;
    }

    private int count = -1;
    private String[] file = null;

    public String readLine() {
        if (count++ < file.length - 1) {
            return file[count];
        }
        return null;
    }

}
