/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.documentation;

import java.io.IOException;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shahavi.bio471.scanprosite.database.PrositeDatabase;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class DocuBase {

    private HashMap<String, Document> docudb;

    public DocuBase(String localResourcePath) {
        docudb = new HashMap<>();
        if (localResourcePath != null) {
            try {
                this.readDatabase(localResourcePath);
            } catch (IOException ex) {
                Logger.getLogger(DocuBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void readDatabase(String localResourcePath) throws IOException {
        // using code from https://www.baeldung.com/java-read-lines-large-file

        InputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = DocuBase.class.getResourceAsStream(localResourcePath);
            sc = new Scanner(inputStream, "UTF-8");
            StringBuilder sb = new StringBuilder();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("{END}")) {
                    sb.append(line);
                    putEntry(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append(line).append("\n");
                }
                //System.out.println(line);
            }

            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }

    }

    public Document get(String docaccession) {
        return docudb.getOrDefault(docaccession, null);
    }

    private void putEntry(String documentFile) {
        Document d = new Document(documentFile);
        docudb.put(d.DOC_ACCESSION, d);
        //System.out.println(d.DOC_ACCESSION + " " + d.DOC);
    }
}
