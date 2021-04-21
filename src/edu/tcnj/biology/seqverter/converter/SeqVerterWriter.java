/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.converter;

import edu.tcnj.biology.seqverter.sequence.Alphabet;
import javafx.scene.image.Image;
import edu.tcnj.biology.seqverter.sequence.DefaultAlphabets;
import edu.tcnj.biology.seqverter.sequence.SequenceHolder;
import edu.tcnj.biology.seqverter.sequence.Sequence;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.io.FileWriter;
import javafx.scene.control.TextArea;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.time.Month;
import java.time.LocalDate;

/**
 *
 * @author Aneesha Kodati
 */
//The array of sequence names that comes in needs strings with ALL the same length
//Make function that adds spaces so if(sequencenames.get(i)).length()!=(sequencenames.get(i+1)).length()
//for(int j=0; j<(length of one - length of two); j++){append " " to the end of sequence names
public class SeqVerterWriter {

    public static void write(File f, FileFormat ff, SequenceHolder b) throws IllegalArgumentException, IOException {
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequencenames = new ArrayList<>();
        String header = "";

        String fileName = f.getCanonicalPath();

        //sequences = b.getSequences();
        for (int i = 0; i < b.size(); i++) {
            sequences.add(b.get(i).seqString());
            sequencenames.add(b.get(i).name());
            System.out.println("write: " + sequences.get(i));
        }
        //sequencenames = b.getSequenceNames();
        System.out.println("Writer format: " + ff);
        switch (ff) {
            case clustal:
                writeClustal(sequences, sequencenames, header, fileName);
                break;
            case fasta:
                //return $fasta(f, a);
                writeFASTA(f, b);
                break;
            // using default writeFASTA, as above
            //writeFasta(sequences, sequencenames, header, fileName);
            case msf:
                writeMSF(sequences, sequencenames, header, fileName);
                break;
            case phylip_interleaved:
                writePhylipInterleaved(sequences, sequencenames, fileName);
                break;
            case phylip_sequential:
                writePhylipSequential(sequences, sequencenames, fileName);
                break;

        }
    }

    /**
     * Originally an EditorInterface class method, writes a set of sequences to
     * FASTA format, with at most 60 characters per line when writing the
     * sequence. There is no limit on the length of the name.
     *
     * @param f = <code>File</code> to be written to
     * @param sh = <code>SequenceHolder</code> containing seq data
     */
    public static void writeFASTA(File f, SequenceHolder sh) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for (Sequence s : sh) {
                bufferedWriter.write(">".concat(s.name()));
                bufferedWriter.newLine();
                List<String> seqparts = splitEqually(s.seqString(), 60);
                for (String str : seqparts) {
                    bufferedWriter.write(str);
                    bufferedWriter.newLine();
                }
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(SeqVerterWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    DO NOT CHANGE THIS METHOD - writeFASTA() method depends on this method
     */
    private static List<String> splitEqually(String text, int size) {
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }

    // this method is not used
    /**
     * @deprecated @param sequences
     * @param sequencenames
     * @param header
     * @param fileName
     */
    public static void writeFasta(ArrayList<String> sequences, ArrayList<String> sequencenames, String header, String fileName) {
        //fileName = "\\Users\\shrih\\OneDrive\\Documents\\NetBeansProjects\\formatchanger\\src\\seqverter\\converter\\fasta.txt";
        System.out.println("Fasta");
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for (int i = 0; i < sequences.size(); i++) {
                myWriter.write("\n");
                if (!sequencenames.get(i).equals("consensus") && !sequencenames.get(i).replaceAll(" ", "").isEmpty() && !sequences.get(i).replaceAll(" ", "").isEmpty()) {
                    myWriter.write(">" + sequencenames.get(i) + "\n");
                    for (int j = 0; j < sequences.get(i).length() - 60; j += 60) {
                        if (sequences.get(i).contains("*") || sequences.get(i).contains(":")) {
                            myWriter.write("\n");
                        } else {
                            myWriter.write(sequences.get(i).substring(j, j + 60) + "\n");
                            if (j + 60 > sequences.get(i).length() - 60) {
                                myWriter.write(sequences.get(i).substring(j + 60) + "\n");
                            }
                        }

                    }
                }
            }
            System.out.println("Successfully wrote to the file.");

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    //This code doesn't completely work because it assumes the sequence length is divisible by sixty- which it's not. 
    //Figure out a way for the program to do this without a sequence length of exactly sixty- maybe add characters???
    //assumes sequences are same length
    public static void writeClustal(ArrayList<String> sequences, ArrayList<String> sequencenames, String header, String fileName) {
        //fileName = "\\Users\\shrih\\OneDrive\\Documents\\NetBeansProjects\\formatchanger\\src\\seqverter\\converter\\clustal.txt";
        System.out.println("Clustal");
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

            System.out.println("File created successfully");
            out.write("CLUSTAL O(1.2.4) multiple sequence alignment" + header + "\n\n");
            String k = "";
            for (int i = 0; i < sequences.size(); i++) {
                if (sequences.get(i).length() > k.length()) {
                    k = sequences.get(i);
                }
            }

            for (int j = 0; j < k.length(); j += 60) {

                for (int i = 0; i < sequences.size(); i++) {

                    String s = sequences.get(i);

                    for (int m = 0; m < 100; m++) {
                        s += " ";
                    }

                    if (sequencenames.get(i).equalsIgnoreCase("consensus")) {
                        out.write("          ");
                    } else {
                        if (sequencenames.get(i).length() > 15) {
                            sequencenames.set(i, sequencenames.get(i).substring(0, 14));
                        }
                        out.write(sequencenames.get(i) + " ");

                        for (int h = 0; h < 15 - sequencenames.get(i).length(); h++) {
                            out.write(" ");
                        }

                    }
                    if (j + 60 < s.length()) {
                        out.write(s.substring(j, j + 59) + "\n");
                        System.out.println(s.substring(j, j + 59) + "\n");
                    } else {

                        out.write(s.substring(s.length() - s.length() % 60, s.length()) + "\n");
                    }

                }
                out.write("\n\n");
                System.out.print("\n\n");

            }

            out.close();
            System.out.println("Successfully wrote CLUSTAL to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void writePhylipSequential(ArrayList<String> sequences, ArrayList<String> sequencenames, String fileName) {
        System.out.println("Phylip");
        //fileName = "\\Users\\shrih\\OneDrive\\Documents\\NetBeansProjects\\formatchanger\\src\\seqverter\\converter\\phylip.txt";

        for (int i = 0; i < sequencenames.size(); i++) {
            String addition = sequencenames.get(i);
            if (sequencenames.get(i).length() < 10) {
                for (int j = 0; j < (10 - sequencenames.get(i).length()); j++) {
                    addition += " ";
                }
                sequencenames.set(i, addition);
            }
            if (sequencenames.get(i).length() > 10) {
                addition = sequencenames.get(i).substring(0, 9);
                sequencenames.set(i, addition);
            }
        }
        for (int i = 0; i < sequences.size(); i++) {
            String sequence = "";
            for (int j = 0; j < sequences.get(i).length() - 9; j += 10) {
                sequence += sequences.get(i).substring(j, j + 10);
                sequence += " ";
            }
            sequences.set(i, sequence);
        }

        try {
            FileWriter myWriter = new FileWriter(fileName);
            String k = "";
            for (int i = 0; i < sequences.size(); i++) {
                if (k.length() < sequences.get(i).length()) {
                    k = sequences.get(i);
                }
            }
            myWriter.write(" " + sequences.size() + "  " + k.replaceAll(" ", "").length() + " \n");
            for (int i = 0; i < sequences.size(); i++) {
                if (!sequencenames.get(i).contains("consensus")) {
                    myWriter.write(sequencenames.get(i) + " ");
                }

                for (int z = 0; z < sequences.get(i).length(); z++) {

                    if (z + 1 % 55 == 0 && !sequences.get(i).contains("*")) {
                        myWriter.write(sequences.get(i).charAt(z) + " \n");

                    } else if (!sequences.get(i).contains("*")) {
                        if (z % 55 == 0 && z != 0) {
                            myWriter.write("\n           ");
                        }
                        myWriter.write(sequences.get(i).charAt(z));

                    }
                }

                myWriter.write("\n");

            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void writeMSF(ArrayList<String> sequences, ArrayList<String> sequencenames, String header, String fileName) {
        //fileName = "\\Users\\shrih\\OneDrive\\Documents\\NetBeansProjects\\formatchanger\\src\\seqverter\\converter\\msf.txt";
        System.out.println("MSF");
        try {
            int maxsize = 0;
            for (int z = 0; z < sequencenames.size(); z++) {
                if (maxsize < sequencenames.get(z).replaceAll(" ", "").length()) {
                    maxsize = sequencenames.get(z).replaceAll(" ", "").length();
                }
            }
            for (int i = 0; i < sequencenames.size(); i++) {
                /*
          
          if(sequencenames.get(i).replaceAll(" ","").length()>=maxsize)
            sequencenames.set(i, sequencenames.get(i).substring(0, maxsize));
                 */
                String sequence = sequencenames.get(i).replaceAll(" ", "");
                if (sequencenames.get(i).length() >= maxsize) {
                    for (int j = 0; j < ((maxsize) - sequencenames.get(i).replaceAll(" ", "").length()); j++) {
                        sequence += " ";
                    }
                    sequencenames.set(i, sequence);

                }
            }
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write("!!AA_MULTIPLE_ALIGNMENT 1.0\n");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.now();
            LocalDate currentDate
                    = LocalDate.parse(dtf.format(localDate));
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date date = new Date(System.currentTimeMillis());

            myWriter.write(" convertedfile.msf MSF: " + sequences.get(0).length() + " Type: P" + " " + currentDate.getMonth() + " " + currentDate.getDayOfMonth() + ", " + currentDate.getYear() + " " + formatter.format(date) + "  Check: 0000  . .");
            myWriter.write("\n\n");

            for (int i = 0; i < sequencenames.size(); i++) {

                if (!sequencenames.get(i).equals("consensus") && !sequences.get(i).contains("*") && !sequences.get(i).replaceAll(" ", "").isEmpty()) {
                    myWriter.write(" Name: " + sequencenames.get(i) + " Len:  " + sequences.get(i).length() + " Check: 0000" + " Weight: -1.00" + "\n");//what's a check/weight??
                }
            }

            myWriter.write("\n//\n\n");
            String m = "";
            for (int i = 0; i < sequences.size(); i++) {
                if (m.length() < sequences.get(i).length()) {
                    m = sequences.get(i);
                }
            }
            int count = 0;
            for (int k = 0; k < m.length() / 50; k++) {
                if ((50 * k + 1) < 10) {
                    for (int l = 0; l < maxsize + 1; l++) {
                        myWriter.write(" ");
                    }
                    myWriter.write(((50 * k) + 1) + "                                                   " + ((50 * k) + 50) + "\n");
                } else if ((50 * k + 1) < 100) {
                    for (int l = 0; l < maxsize + 1; l++) {
                        myWriter.write(" ");
                    }
                    myWriter.write(((50 * k) + 1) + "                                                 " + ((50 * k) + 50) + "\n");
                } else {
                    for (int l = 0; l < maxsize + 1; l++) {
                        myWriter.write(" ");
                    }
                    myWriter.write(((50 * k) + 1) + "                                                " + ((50 * k) + 50) + "\n");
                }

                for (int j = 0; j < sequences.size(); j++) {
                    if (!sequencenames.get(j).contains("consensus")) {
                        myWriter.write(sequencenames.get(j) + " ");
                    }
                    if (sequences.get(j).length() >= 50 && !sequences.get(j).contains("*")) {
                        for (int i = 0; i < 50; i += 10) {
                            myWriter.write(sequences.get(j).substring(i + count, i + count + 10) + " ");
                        }

                    } else if (!sequences.get(j).contains("*")) {
                        for (int i = sequences.get(j).length(); i >= 9; i -= 9) {
                            myWriter.write(sequences.get(j).substring(i - 9, i) + " ");
                        }
                    }
                    myWriter.write("\n");

                }
                if (k == m.length() / 50 - 1) {
                    String d = "";
                    String temp = "";
                    myWriter.write("\n");
                    for (int l = 0; l < maxsize + 1; l++) {
                        myWriter.write(" ");
                    }
                    myWriter.write(((50 * k) + 1) + "                                                " + (m.length() + "\n"));
                    for (int j = 0; j < sequences.size(); j++) {
                        myWriter.write(sequencenames.get(j) + " ");
                        d = (sequences.get(j).substring(m.length() - (m.length() % 50) - 1, m.length() - 1) + "\n");
                        temp = d + "                          ";
                        for (int c = 0; c < d.length(); c += 10) {
                            myWriter.write(temp.substring(c, c + 9) + " ");
                        }
                        myWriter.write("\n");
                    }
                }

                myWriter.write("\n");
                count += 50;
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public static void writePhylipInterleaved(ArrayList<String> sequences, ArrayList<String> sequencenames, String fileName) {
        //fileName = "\\Users\\shrih\\OneDrive\\Documents\\NetBeansProjects\\formatchanger\\src\\seqverter\\converter\\phylipinterleaved.txt";

        System.out.println("Phylip");
        for (int i = 0; i < sequencenames.size(); i++) {
            String addition = sequencenames.get(i);

            if (sequencenames.get(i).length() < 10) {
                for (int j = 0; j < (10 - sequencenames.get(i).length()); j++) {
                    addition += " ";
                }
                sequencenames.set(i, addition);

            }
            if (sequencenames.get(i).length() > 10) {
                addition = sequencenames.get(i).substring(0, 9);
                sequencenames.set(i, addition);
            }
            if (sequences.get(i).length() % 20 != 0) {
                String sequence = sequences.get(i).replaceAll(" ", "");
                for (int l = 0; l < (20 - sequences.get(i).length() % 20); l++) {
                    sequence += " ";

                }
                sequences.set(i, sequence);

            }
            for (int k = 0; k < sequences.size(); k++) {
                String sequence = "";

                if (sequences.get(k).length() > 10) {

                    for (int g = 0; g < sequences.get(k).length(); g += 11) {
                        if (sequences.get(k).length() >= g + 11) {
                            sequence += sequences.get(k).substring(g, g + 11).replaceAll(" ", "");
                        } else {
                            sequence += sequences.get(k).substring(g);
                        }

                    }
                    sequences.set(k, sequence);
                } else {
                    for (int g = 0; g < sequences.get(k).length(); g += 10) {
                        sequence += sequences.get(k).substring(g, g + 10);
                        sequence += " ";
                    }
                    sequences.set(k, sequence);
                }

            }

        }
        try {
            FileWriter myWriter = new FileWriter(fileName);
            String k = "";
            int count = 0;
            for (int i = 0; i < sequences.size(); i++) {
                if (sequences.get(i).length() > k.length()) {
                    k = sequences.get(i);
                }
            }
            myWriter.write(" " + sequences.size() + "  " + k.replaceAll(" ", "").length() + "\n");
            for (int i = 0; i < sequences.size(); i++) {

                if (sequencenames.get(i).contains("consensus")) {
                    myWriter.write("");
                } else {
                    myWriter.write(sequencenames.get(i) + " ");
                }
                if (sequences.get(i).length() > 87 && !(sequencenames.get(i).contains("consensus"))) {
                    myWriter.write(sequences.get(i).substring(0, 50) + "\n");
                    count++;
                } else if (!sequencenames.get(i).contains("consensus")) {
                    myWriter.write(sequences.get(i).substring(0) + "\n");
                    count++;
                }
                if (count % sequences.size() == 0) {
                    myWriter.write("\n");
                }

            }

            count = 0;
            for (int j = 50; j < k.length(); j += 50) {
                for (int i = 0; i < sequences.size(); i++) {
                    if (sequencenames.get(i).contains("consensus")) {
                        myWriter.write("\n");
                    } else {

                        if (sequences.get(i).length() >= (j + 50) && !sequences.get(i).contains("*")) {
                            myWriter.write(sequences.get(i).substring(j, j + 50));
                        } else if (sequences.get(i).length() >= j && !sequences.get(i).contains("*")) {
                            myWriter.write(sequences.get(i).substring(j, sequences.get(i).length()));
                        } else if (!sequences.get(i).contains("*") && sequences.get(i).length() > 20) {
                            myWriter.write(sequences.get(i).substring(sequences.get(i).length() - 19, sequences.get(i).length()));
                        } else {
                            myWriter.write(sequences.get(i));
                        }
                        myWriter.write("\n");
                        count++;
                        if (count % sequences.size() == 0) {
                            myWriter.write("\n");
                        }
                    }

                }
            }

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}
