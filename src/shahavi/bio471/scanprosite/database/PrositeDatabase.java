/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database;

import java.io.IOException;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositeDatabase {

    //as of now can be "prosite.dat.txt" or "evaluator.dat.txt"
    public final String IDENTIFIER;

    public PrositeDatabase(String identifier, boolean inputStreamLocal) {
        this.IDENTIFIER = identifier;
        if (inputStreamLocal) {
            try {
                readDatabase(IDENTIFIER);
            } catch (IOException ex) {
                Logger.getLogger(PrositeDatabase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private HashMap<String, PrositeEntry> entryMap;

    public PrositeEntry get(int index) {
        int i = 0;
        Iterator<PrositeEntry> it = entryMap.values().iterator();
        while (it.hasNext()) {
            if (i == index) {
                return it.next();
            }
            i++;
        }
        return null;
    }

    public PrositeEntry get(String accession) {
        return entryMap.getOrDefault(accession, null);
    }

    private List<String> accessionSet = null;

    public List<String> getAccessionSet() {
        return accessionSet;
    }

    private void readDatabase(String localResourcePath) throws IOException {
        // using code from https://www.baeldung.com/java-read-lines-large-file
        entryMap = new HashMap<>();

        InputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = PrositeDatabase.class.getResourceAsStream(localResourcePath);
            sc = new Scanner(inputStream, "UTF-8");
            StringBuilder sb = null;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("ID")) {
                    if (sb != null) {
                        this.addEntryString(sb.toString());
                    }
                    sb = new StringBuilder();
                }
                if (sb != null) {
                    sb.append(line).append("\n");
                }

                //System.out.println(line);
            }

            if (sb != null && !sb.toString().isEmpty()) {
                this.addEntryString(sb.toString());
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

        accessionSet = new ArrayList<String>();
        entryMap.values().forEach((pe) -> {
            accessionSet.add(pe.getAccession());
        });
/*
        entryMap.values().forEach((pe) -> {
            System.out.println(pe.getAccession() + " "
                    + (pe.isPattern() ? "1" : "0") + " "
                    + (pe.isProfile() ? "1" : "0") + " "
                    + (pe.skip() ? "1" : "0") + " "
                    + pe.getName());
        });
*/
    }

    private void addEntryString(String entryStringFile) {
        PrositeEntry addEntry = new PrositeEntry(entryStringFile);
        entryMap.put(addEntry.getAccession(), addEntry);
    }

}
