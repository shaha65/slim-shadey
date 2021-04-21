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
import edu.tcnj.biology.seqverter.sequence.CharacterElement;
import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;
import java.util.regex.*;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah and Aneesha Kodati
 */
public class SeqVerterReader {

    public static SequenceHolder read(File f, FileFormat ff, DefaultAlphabets a, boolean getAlpha) throws IllegalArgumentException {
        switch (ff) {
            case clustal_with_numbers:
                return $clustal_with_numbers(f, a, getAlpha);
            case clustal_without_numbers:
                return $clustal_without_numbers(f, a, getAlpha);
            case fasta:
                //return $newfasta(f, a);
                return $fasta_ei_11_20(f, a, getAlpha);
            case slim:
                throw new UnsupportedOperationException("Do not use this class"
                        + " to read Shadey project files (extension .mm). To"
                        + " read these files, use the static guess() method to"
                        + " determine that the file is a Shadey project file,"
                        + " and use the read() method found in"
                        + " <EditorInterface>, which will correctly use the"
                        + " parser in <ShadeyProjectMananger>.");
            case msf:
                return $msf(f, a, getAlpha);
            case phylip_interleaved:
                return $phylip_interleaved(f, a, getAlpha);
            case phylip_sequential:
                return $phylip_sequential(f, a, getAlpha);
            case guess:
                return read(f, guess(f), a, getAlpha);
            default:
                return $fasta_ei_11_20(f, a, getAlpha);

        }
    }

    private static SequenceHolder $clustal_with_numbers(File f, DefaultAlphabets a, boolean getAlpha) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        String consensus = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
            for (int i = 0; i < lines.size(); i++) {
                Boolean flag = false;
                int index = sequences.size() + 1;
                if (lines.get(i).contains("*")) {
                    consensus += lines.get(i).replaceAll(" ", "");

                }
                if (lines.get(i).startsWith("CLUSTAL") || lines.get(i).startsWith(" ")) {
                    lines.remove(i);
                }
                String name = lines.get(i).substring(0, 19);
                name = name.replace(" ", "");
                for (int k = 0; k < sequenceNames.size(); k++) {
                    if (sequenceNames.get(k).equals(name)) {
                        flag = true;
                        index = k;
                    }
                }
                if (flag == false) {
                    sequenceNames.add(name);
                }

                String sequence = lines.get(i).substring(20, 59);
                for (int j = 0; j < 10; j++) {

                    sequence = sequence.replace(String.valueOf(j), "");
                }
                sequence = sequence.replace(" ", "");
                String totalSequence = sequences.get(index) + sequence;
                sequences.set(index, totalSequence);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            System.out.println(sequences.get(i) + "\n");
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);

            seq.setName(sequenceNames.get(i));
            seqs.add(seq);
        }
        Sequence cons = new Sequence();
        cons.setName("consensus");
        cons.setSequence(consensus);
        //seqs.add(cons);
        SequenceHolder holder = new SequenceHolder();
        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);
        holder.consensusSequence = cons;

        return holder;
    }

    private static SequenceHolder $fasta_ei_11_20(File f, DefaultAlphabets a, boolean getAlpha) {
        SequenceHolder msa = null;

        try {
            BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));

            List<Sequence> basicSequences = new ArrayList<>();

            String name = null;
            StringBuilder sequence = null;

            String linex;
            while ((linex = fr.readLine()) != null) {
                linex = linex.replaceAll("\\r|\\n", "");
                if (!linex.isEmpty() && linex.charAt(0) == '>') {
                    //linex = linex.replaceAll("\\s+", "_");
                } else {
                    linex = linex.replaceAll("\\s+", "");
                }
                if (linex.isEmpty()) {
                    continue;
                }
                if (linex.charAt(0) == '>') {
                    if (sequence != null) {
                        Sequence seq = new Sequence();
                        seq.setSequence(sequence.toString());
                        seq.setName(name);
                        basicSequences.add(seq);
                    }
                    String[] ss = linex.split("\\s+");
                    name = ss[0].substring(1);
                    sequence = new StringBuilder("");
                } else {
                    sequence.append(linex);
                }
            }

            Sequence seq = new Sequence();
            seq.setSequence(sequence.toString());
            seq.setName(name);
            basicSequences.add(seq);

            msa = new SequenceHolder();

            //Alphabet alpha = guessAlphabet(basicSequences, a);
            msa.setData(basicSequences, getAlpha ? guessAlphabet2(f.getName(), basicSequences, a) : null);

            fr.close();

        } catch (IOException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //this.glm_fileio.exitLoading();
        }

        return msa;
    }

    private static SequenceHolder $newfasta(File f, DefaultAlphabets a, boolean getAlpha) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        int index = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();

            while (line != null) {
                if (!line.replaceAll("\\s+", "").isEmpty()) {
                    lines.add(line);
                }

                // read next line
                line = reader.readLine();
            }
            reader.close();
            for (int i = 0; i < lines.size(); i++) {

                if (lines.get(i).contains(">")) {
                    String name = lines.get(i).replaceAll(">", "");
                    name = name.replace(" ", "");
                    sequenceNames.add(name);
                    index = sequenceNames.size() - 1;
                    sequences.add("");
                } else {
                    sequences.set(index, sequences.get(index) + lines.get(i).replaceAll(" ", ""));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);
            seq.setName(sequenceNames.get(i));
            seqs.add(seq);
        }
        SequenceHolder holder = new SequenceHolder();
        //Alphabet alpha = guessAlphabet(seqs, a);

        //holder.setData(seqs, alpha);
        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);

        return holder;
    }

    private static SequenceHolder $clustal_without_numbers(File f, DefaultAlphabets a, boolean getAlpha) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        String consensus = "";
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
            for (int i = 0; i < lines.size(); i++) {
                Boolean flag = false;
                int index = sequences.size() + 1;
                if (lines.get(i).contains("*")) {
                    consensus += lines.get(i).replaceAll(" ", "");

                }
                if (lines.get(i).startsWith("CLUSTAL") || lines.get(i).startsWith(" ")) {
                    lines.remove(i);
                }
                String name = lines.get(i).substring(0, 19);
                name = name.replace(" ", "");
                for (int k = 0; k < sequenceNames.size(); k++) {
                    if (sequenceNames.get(k).equals(name)) {
                        flag = true;
                        index = k;
                    }
                }
                if (flag == false) {
                    sequenceNames.add(name);
                }

                String sequence = lines.get(i).substring(20, 59);
                for (int j = 0; j < 10; j++) {

                    sequence = sequence.replace(String.valueOf(j), "");
                }
                sequence = sequence.replace(" ", "");
                String totalSequence = sequences.get(index) + sequence;
                sequences.set(index, totalSequence);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            System.out.println(sequences.get(i) + "\n");
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);

            seq.setName(sequenceNames.get(i));
            seqs.add(seq);
        }
        Sequence cons = new Sequence();
        cons.setName("consensus");
        cons.setSequence(consensus);
        //seqs.add(cons);
        SequenceHolder holder = new SequenceHolder();

        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);

        holder.consensusSequence = cons;

        return holder;
    }

    private static SequenceHolder $fasta_simplest(File f, DefaultAlphabets a, boolean getAlpha) {
        SequenceHolder msa = new SequenceHolder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            List<String> lines = new ArrayList<>();

            List<Sequence> basicSequences = new ArrayList<>();

            String name = "";
            StringBuilder sequence = new StringBuilder();

            String linex;
            while ((linex = br.readLine()) != null) {
                linex = linex.replaceAll(System.lineSeparator(), "");
                linex = linex.replaceAll("\\s+", "");
                if (linex.charAt(0) == '>') {
                    if (sequence != null) {
                        Sequence seq = new Sequence();
                        seq.setSequence(sequence.toString());
                        seq.setName(name);
                        basicSequences.add(seq);
                    }
                    String[] ss = linex.split("\\s+");
                    name = ss[0].substring(1);
                    sequence = new StringBuilder("");
                } else {
                    sequence.append(linex);
                }
            }

            Sequence seq = new Sequence();
            seq.setSequence(sequence.toString());
            seq.setName(name);
            basicSequences.add(seq);

            msa = new SequenceHolder();
            msa.setData(basicSequences, getAlpha ? guessAlphabet2(f.getName(), basicSequences, a) : null);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return msa;
    }

    private static SequenceHolder $fasta_simple(File f, DefaultAlphabets a, boolean getAlpha) throws IllegalArgumentException {
        SequenceHolder msa = new SequenceHolder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            List<List<String>> fastaSequences = new ArrayList<>();
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                //load every line of file into memory for easier processing
                if (!line.replaceAll("\\s+ ", "").isEmpty()) {
                    lines.add(line);
                }
            }

            ArrayList<String> fastaSequence = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                if (!lines.get(i).replaceAll("\\r|\\n", "").isEmpty()) {
                    fastaSequence.add(lines.get(i));

                    if ((i + 1) < lines.size() && lines.get(i + 1).charAt(0) == '>') {
                        /* if the beginning of the next line is a new sequence 
                    identifier then this is the last line of the sequence and 
                    a new sequence is going to be created */
                        fastaSequences.add((List<String>) fastaSequence.clone());
                        fastaSequence = new ArrayList<>();
                    }
                }
            }
            fastaSequences.add((List<String>) fastaSequence.clone());

            List<Sequence> basicSequences = new ArrayList<>();
            for (List<String> splitSeq : fastaSequences) {
                //System.out.println(splitSeq.get(0));
                //split first line of fasta sequence by spces
                Sequence newSeq = new Sequence();
                String[] ss = splitSeq.get(0).split("\\s+");
                String name = ss[0].substring(1);

                StringBuilder sb = new StringBuilder();

                for (int i = 1; i < splitSeq.size(); i++) {
                    sb.append(splitSeq.get(i));
                }
                String temp = sb.toString().replaceAll("\\r|\\n", ""); //get rid of all line breaks, mac or windows
                //System.out.println(temp);
                newSeq.setSequence(temp);
                //newSeq.setOrganism(organism);
                //newSeq.setDatabaseIdentifier(databaseIdentifer);
                newSeq.setName(name);
                basicSequences.add(newSeq);

                msa = new SequenceHolder();
                msa.setData(basicSequences, getAlpha ? guessAlphabet2(f.getName(), basicSequences, a) : null);

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return msa;
    }

    private static SequenceHolder $fasta(File f, DefaultAlphabets a, boolean getAlpha) throws IllegalArgumentException {

        SequenceHolder msa = new SequenceHolder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            List<List<String>> fastaSequences = new ArrayList<>();
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.replaceAll(" ", "").isEmpty()) {
                    lines.add(line);
                }
            }

            ArrayList<String> fastaSequence = new ArrayList<>();
            for (int i = 0; i < lines.size() - 2; i++) {
                fastaSequence.add(lines.get(i));
                if ((i + 1) < lines.size() && lines.get(i + 1).charAt(0) == '>') {
                    /* if the beginning of the next line is a new sequence 
                    identifier then this is the last line of the sequence and 
                    a new sequence is going to be created */
                    fastaSequences.add((List<String>) fastaSequence.clone());
                    fastaSequence = new ArrayList<>();
                }
            }
            fastaSequences.add((List<String>) fastaSequence.clone());

            List<Sequence> basicSequences = new ArrayList<>();
            for (List<String> splitSeq : fastaSequences) {
                //System.out.println(splitSeq.get(0));
                //split first line of fasta sequence by spces
                Sequence newSeq = new Sequence();
                String[] ss = splitSeq.get(0).split("\\s+");
                String name = ss[0].substring(1);
                //the name of the sequence is always first, sometimes the name is the database identifier
                String databaseIdentifer = "";
                String organism = "";

                for (int i = 1; i < ss.length; i++) {
                    StringBuilder buildOrganism = new StringBuilder();
                    if (ss[i].contains("|")) {
                        //database identifers always have | character, nothing else should have it
                        databaseIdentifer = ss[i];
                    }
                    //assumes organism name is bounded by brackets [ ], standard
                    if (ss[i].charAt(0) == '[') {
                        for (int j = i; j < ss.length && j < i + 2; j++) {
                            buildOrganism.append(ss[j]).append(' ');
                            if (ss[j].charAt(ss[j].length() - 1) == ']') {
                                break;
                            }
                        }
                        organism = buildOrganism.toString().substring(1);
                        organism = organism.replaceAll("]", "");
                    }
                }

                StringBuilder sb = new StringBuilder();

                for (int i = 1; i < splitSeq.size(); i++) {
                    sb.append(splitSeq.get(i));
                }
                String temp = sb.toString().replaceAll("\\r|\\n", ""); //get rid of all line breaks, mac or windows
                //System.out.println(temp);
                newSeq.setSequence(temp);
                //newSeq.setOrganism(organism);
                //newSeq.setDatabaseIdentifier(databaseIdentifer);
                newSeq.setName(name);
                basicSequences.add(newSeq);
            }

            int len = basicSequences.get(0).size();
            for (Sequence seq : basicSequences) {
                if (seq.size() != len) {

                }
            }

            msa = new SequenceHolder();
            msa.setData(basicSequences, getAlpha ? guessAlphabet2(f.getName(), basicSequences, a) : null);

            System.out.println();
            for (Sequence sss : msa) {
                System.out.println(sss.name());
                //System.out.println(sss.organism());
                //System.out.println(sss.databaseIdentifier());
                System.out.println(sss.toString());
                System.out.println();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msa;
    }

    private static SequenceHolder $msf(File f, DefaultAlphabets a, boolean getAlpha) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        ArrayList<String> header = new ArrayList<>();
        int count = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            while (line != null) {

                if (!(line.contains("!!") || line.replaceAll("\\s+", "").isEmpty() || line.replaceAll(" ", "").isEmpty())) {
                    lines.add(line);
                }

                // read next line
                line = reader.readLine();
            }
            reader.close();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("//")) {
                    count = i;
                    break;
                }
            }
            for (int j = count; j >= 0; j--) {
                header.add(lines.get(j));
                lines.remove(j);
            }
            for (int i = 0; i < lines.size(); i++) {
                Boolean flag = false;
                int index = sequences.size() + 1;

                String[] ss = lines.get(i).split("\\s+");
                String name = ss[0];

                name = name.replace(" ", "");
                for (int k = 0; k < sequenceNames.size(); k++) {
                    if (sequenceNames.get(k).equals(name)) {
                        flag = true;
                        index = k;
                        int gertrude = lines.get(i).indexOf(ss[1]);
                        int bertram = lines.get(i).indexOf(ss[1]) + 53;
                        String sequence;
                        if (lines.get(i).substring(gertrude).length() < 53) {
                            sequence = lines.get(i).substring(gertrude);
                        } else {
                            sequence = lines.get(i).substring(gertrude, bertram);
                        }

                        for (int j = 0; j < 10; j++) {

                            sequence = sequence.replace(String.valueOf(j), "");
                        }
                        sequence = sequence.replace(" ", "");
                        String totalSequence = sequences.get(index) + sequence;
                        sequences.set(index, totalSequence);

                    }
                }
                if (flag == false) {

                    sequenceNames.add(name);
                    String sequence = lines.get(i).substring(lines.get(i).indexOf(ss[1]), lines.get(i).indexOf(ss[1]) + 54);
                    for (int j = 0; j < 10; j++) {

                        sequence = sequence.replace(String.valueOf(j), "");
                    }
                    sequence = sequence.replace(" ", "");
                    String totalSequence = sequence;
                    sequences.add(totalSequence);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            System.out.println(sequences.get(i) + "\n");
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);
            seq.setName(sequenceNames.get(i));
            seqs.add(seq);
        }

        SequenceHolder holder = new SequenceHolder();

        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);
        return holder;
    }

    private static SequenceHolder $phylip_sequential(File f, DefaultAlphabets a, boolean getAlpha) {
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        ArrayList<Integer> nums = new ArrayList<>();
        int count = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            String total = "";

            while (line != null) {

                if (!(line.replaceAll("\\s+", "").isEmpty() || line.replaceAll(" ", "").isEmpty())) {
                    lines.add(line);
                }

                // read next line
                line = reader.readLine();
            }
            reader.close();
            Scanner scanner = new Scanner(lines.get(0));
            while (scanner.hasNext()) {

                if (scanner.hasNextInt()) {
                    nums.add(scanner.nextInt());
                }

            }
            scanner.close();
            for (int i = 0; i < sequences.size(); i++) {
                sequences.set(i, sequences.get(i) + "                                           ");
            }
            String[] ss;
            String name = "";
            int index = 0;
            for (int i = 1; i < lines.size(); i++) {
                if (!lines.get(i).startsWith(" ")) {
                    ss = lines.get(i).split("\\s+");
                    name = ss[0];
                    index++;
                    sequenceNames.add(name);
                    sequences.add(lines.get(i).substring(name.length()).replaceAll(" ", ""));
                } else if (sequences.size() > index - 1) {
                    sequences.set(index - 1, sequences.get(index - 1) + lines.get(i).replaceAll(" ", ""));
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            System.out.println(sequences.get(i) + "\n");
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);
            seq.setName(sequenceNames.get(i));
            seqs.add(seq);
        }

        SequenceHolder holder = new SequenceHolder();

        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);

        return holder;
    }

    private static SequenceHolder $phylip_interleaved(File f, DefaultAlphabets a, boolean getAlpha) {
        //assumes file has 10 character name, 
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();
        ArrayList<String> sequenceNames = new ArrayList<>();
        ArrayList<Integer> nums = new ArrayList<>();
        int count = 0;
        int index = 0;

        BufferedReader reader;
        try {

            reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();

            while (line != null) {
                if (!(line.replaceAll(" ", "").isEmpty())) {
                    lines.add(line);

                }
                line = reader.readLine();

            }

            reader.close();
            Scanner scanner = new Scanner(lines.get(0));
            if (scanner.hasNextInt()) {
                nums.add(scanner.nextInt());
            }

            scanner.close();
            for (int j = 1; j < (nums.get(0) + 1); j++) {
                if (lines.get(j).length() >= 10) {
                    sequenceNames.add(lines.get(j).substring(0, 9));
                    index = j;
                } else {
                    sequenceNames.add(lines.get(j));
                }
                if (lines.get(j).length() > 10) {
                    sequences.add(lines.get(j).substring(10, lines.get(j).length()).replaceAll(" ", ""));
                    index = j;
                }

            }
            for (int i = nums.get(0) + 1; i < lines.size(); i++) {
                if (count == nums.get(0)) {
                    count = 0;
                }
                sequences.set(count, sequences.get(count) + lines.get(i).replaceAll(" ", ""));
                count++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Alphabet alpha = new Alphabet();
        List<Sequence> seqs = new ArrayList();

        for (int i = 0; i < sequences.size(); i++) {
            Sequence seq = new Sequence();
            List<CharacterElement> chars = new ArrayList<>();
            for (int x = 0; x < sequences.get(i).length(); x++) {

                CharacterElement element = new CharacterElement(sequences.get(i).charAt(x), x);
                chars.add(element);
            }
            seq.setSequence(chars);
            seq.setName(sequenceNames.get(i));
            seqs.add(seq);

        }

        SequenceHolder holder = new SequenceHolder();
        //Alphabet alpha = guessAlphabet(seqs, a);
        holder.setData(seqs, getAlpha ? guessAlphabet2(f.getName(), seqs, a) : null);

        return holder;
    }

    public static FileFormat guess(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            List<String> lines = new ArrayList<>();
            String line = new String();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            if (lines.get(0).charAt(0) == '>') {
                System.out.println("fasta");
                return FileFormat.fasta;
            } else if (lines.get(0).equals(".slim")) {
                return FileFormat.slim;
            } else if (lines.get(0).startsWith("!!")) {
                System.out.println("msf");
                return FileFormat.msf;
            } else if (lines.get(0).contains("CLUSTAL")) {
                System.out.println("CLUSTAL");
                return FileFormat.clustal_with_numbers;
            } else if (lines.get(2).startsWith(" ")) {
                System.out.println("Phylip");
                /*
                ADD CODE TO DIFFERENTIAL SEQUENTIAL/INTERLEAVED
                
                 */

                return FileFormat.phylip_sequential;
            } else {
                return null;
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SeqVerterReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Alphabet guessAlphabet2(String name, List<Sequence> sequences, ArrayList<Alphabet> alphas) {
        Alphabet alpha = null;
        System.out.println(alphas.size());

        ArrayList<Alphabet> conforms = new ArrayList<>();

        /*
        In order to accurately guess the alphabet being used, the alphabets
        are sorted in ascending order by size. This is because larger alphabets 
        can be be supersets of smaller alphabets in the absence of explicit definition
        i.e. the default 'DNA' alphabet's valid characters completely overlap 
        with the default 'Protein' alphabet, and so a DNA alignment will 
        incorrectly be guessed as a Protein alignment. This can be avoided by 
        using checking provided sequences against the smallest alphabet first.
         */
        alphas.sort(Comparator.comparing(Alphabet::size));
        boolean match = false;
        outer:
        for (Alphabet a : alphas) {
            inner:
            for (int k = 0; k < sequences.size(); k++) {
                if (!a.conforms(sequences.get(k))) {
                    //if even a single sequence does not conform, go to next seq
                    break inner;
                }
                /*
                This statement can only be true if the last, and every sequence 
                before the last, in the collection of sequences conforms to 
                the given alphabet, thus defining the alphabet as correct.
                 */
                if (k == sequences.size() - 1) {
                    conforms.add(a);
                    //alpha = a;
                    match = true; //at least one match found
                    //System.out.println("Alphabet found: " + alpha.name());

                    //break outer;
                }
            }
        }
        if (!match) {
            System.out.println("No suitable Alphabet found; generating custom");
            alpha = new Alphabet("default");
            for (Sequence s : sequences) {
                alpha.fillDefault(s);
            }
        }
        alphabetPopup(name, sequences, conforms);
        System.out.println(selectedAlphabet);
        return selectedAlphabet;
    }

    private static Alphabet selectedAlphabet = null;

    private static void alphabetPopup(String name, List<Sequence> sequences, ArrayList<Alphabet> conformingAlphabets) {
        Stage st = new Stage();
        st.getIcons().add(new Image(SeqVerterReader.class.getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        VBox rt = new VBox();
        st.setTitle("Alphabet selection");

        TextFlow notif = new TextFlow();
        notif.setTextAlignment(TextAlignment.CENTER);
        Text notifA = new Text("");

        notif.getChildren().add(notifA);
        notif.setPadding(new Insets(15));

        HBox buttons = new HBox(10);

        Button quit = new Button("Quit");
        quit.setOnAction(e -> {
            selectedAlphabet = null;
            st.close();
        });
        buttons.getChildren().add(quit);
        if (conformingAlphabets.size() > 0) {
            String notifText = "Your alignment "
                    + (name == null || name.isEmpty() || name.contains(".tmp") ? "" : "(" + name + ") ")
                    + "could belong to the following "
                    + "default "
                    + (conformingAlphabets.size() == 1 ? "alphabet." : "alphabets.");
            notifA.setText(notifText);

            for (Alphabet a : conformingAlphabets) {
                Button alphaButton = new Button();
                alphaButton.setMnemonicParsing(false);
                alphaButton.setText(a.name());
                alphaButton.setOnAction(e -> {
                    selectedAlphabet = a;
                    st.close();
                });
                buttons.getChildren().add(alphaButton);
            }
        } else {
            notifA.setText("Your alignment does not match any of the default "
                    + "alphabets in SlimShadey.");
            Button alphaButton = new Button();
            alphaButton.setMnemonicParsing(false);
            alphaButton.setText("Create new alphabet");
            alphaButton.setOnAction(e -> {
                selectedAlphabet = new Alphabet();
                for (Sequence s : sequences) {
                    selectedAlphabet.fillDefault(s);
                }
                st.close();
            });
            buttons.getChildren().add(alphaButton);
        }

        buttons.setPadding(new Insets(15));

        rt.getChildren().addAll(notif, buttons);
        VBox.setVgrow(notif, Priority.ALWAYS);

        Scene sc = new Scene(rt, 500, 135, true);
        st.setScene(sc);
        st.widthProperty().addListener(o -> {
            for (int i = 0; i < buttons.getChildren().size(); i++) {
                if (buttons.getChildren().get(i) instanceof Button) {
                    ((Button) buttons.getChildren().get(i)).setPrefWidth(st.getWidth() / buttons.getChildren().size());
                }
            }
        });
        ((Button) buttons.getChildren().get(buttons.getChildren().size() - 1)).requestFocus();
        //st.setOnShown(e -> glm_fileio.exitLoading());
        st.showAndWait();
    }

    private static Alphabet guessAlphabet_old(List<Sequence> sequences, DefaultAlphabets alphas) {
        Alphabet alpha = null;
        System.out.println(alphas.size());
        /*
        In order to accurately guess the alphabet being used, the alphabets
        are sorted in ascending order by size. This is because larger alphabets 
        can be be supersets of smaller alphabets in the absence of explicit definition
        i.e. the default 'DNA' alphabet's valid characters completely overlap 
        with the default 'Protein' alphabet, and so a DNA alignment will 
        incorrectly be guessed as a Protein alignment. This can be avoided by 
        using checking provided sequences against the smallest alphabet first.
         */
        alphas.sort(Comparator.comparing(Alphabet::size));
        boolean match = false;
        outer:
        for (Alphabet a : alphas) {
            inner:
            for (int k = 0; k < sequences.size(); k++) {
                if (!a.conforms(sequences.get(k))) {
                    //if even a single sequence does not conform, go to next seq
                    break inner;
                }
                /*
                This statement can only be true if the last, and every sequence 
                before the last, in the collection of sequences conforms to 
                the given alphabet, thus defining the alphabet as correct.
                 */
                if (k == sequences.size() - 1) {
                    alpha = a;
                    match = true;
                    System.out.println("Alphabet found: " + alpha.name());
                    break outer;
                }
            }
        }
        if (!match) {
            System.out.println("No suitable Alphabet found; generating custom");
            alpha = new Alphabet("default");
            for (Sequence s : sequences) {
                alpha.fillDefault(s);
            }
        }
        return alpha;
    }
//do not use

    public String textAreaReader_fasta(TextArea textArea, SequenceHolder sh_write, DefaultAlphabets a, boolean getAlpha) {

        List<List<String>> fastaSequences = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        for (String line : textArea.getText().split("\\n")) {
            if (line.replaceAll("\\r|\\n", "").isEmpty()) {
                lines.add(line);
            }
        }

        ArrayList<String> fastaSequence = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).replaceAll("\\r|\\n", "").isEmpty()) {
                fastaSequence.add(lines.get(i));

                if ((i + 1) < lines.size() && lines.get(i + 1).charAt(0) == '>') {
                    /* if the beginning of the next line is a new sequence 
                    identifier then this is the last line of the sequence and 
                    a new sequence is going to be created */
                    fastaSequences.add((List<String>) fastaSequence.clone());
                    fastaSequence = new ArrayList<>();
                }
            }
        }
        fastaSequences.add((List<String>) fastaSequence.clone());

        List<Sequence> basicSequences = new ArrayList<>();
        for (List<String> splitSeq : fastaSequences) {
            //System.out.println(splitSeq.get(0));
            //split first line of fasta sequence by spces
            Sequence newSeq = new Sequence();
            String[] ss = splitSeq.get(0).split("\\s+");
            String name = ss[0].substring(1);

            StringBuilder sb = new StringBuilder();

            for (int i = 1; i < splitSeq.size(); i++) {
                sb.append(splitSeq.get(i));
            }
            String temp = sb.toString().replaceAll("\\r|\\n", ""); //get rid of all line breaks, mac or windows
            //System.out.println(temp);
            newSeq.setSequence(temp);
            //newSeq.setOrganism(organism);
            //newSeq.setDatabaseIdentifier(databaseIdentifer);
            newSeq.setName(name);
            basicSequences.add(newSeq);

            sh_write.setData(basicSequences, getAlpha ? guessAlphabet2(null, basicSequences, a) : null);

        }

        /*
        this section error checks a successfully loaded set of sequences
        -sequences are all same length (including gap characters in length)
        -sequence names are all unique
        -alphabet validation***
        
         */
        return "NO ERRORS";

    }

    // alphabet validation method
}
