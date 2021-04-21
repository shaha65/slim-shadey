/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.pattern;

import shahavi.bio471.scanprosite.database.result.OrderedPair;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositePattern {

    public final String PATTERN;
    public final List<String> OPERATION_SET;
    public final List<String> DECOMP_SET;

    public final int NUMOPS;

    public final boolean IS_VALID;

    public PrositePattern(String pattern) {
        List<String> operations = new ArrayList<>();

        ArrayList<Character> tempCharHolder = null;

        pattern = pattern.toUpperCase();
        if (pattern.startsWith("<{") && pattern.endsWith("}*>")) {
            operations.add(pattern); //special case: all sequences without the given characters
        } else {
            for (int i = 0; i < pattern.length(); i++) {
                StringBuilder operation = null;
                boolean skipFlag = false;
                char currentChar = pattern.charAt(i);
                if (currentChar != '-') {
                    operation = new StringBuilder();
                    if (currentChar == '{') {
                        operation.append(currentChar);
                        while ((currentChar = pattern.charAt(++i)) != '}') {
                            operation.append(currentChar);
                        }

                    } else if (currentChar == '[') {
                        operation.append(currentChar);
                        while ((currentChar = pattern.charAt(++i)) != ']') {
                            operation.append(currentChar);
                        }
                    }

                    if (i < pattern.length() - 3) {
                        char nextChar = pattern.charAt(i + 1);
                        if (currentChar == '(') {
                            while ((currentChar = pattern.charAt(++i)) != ')') {

                                operation.append(currentChar);
                            }
                        }
                        if (nextChar == '(') {
                            operation.append(currentChar);
                            while ((currentChar = pattern.charAt(++i)) != ')') {
                                operation.append(currentChar);
                            }
                        }
                    }

                    operation.append(currentChar);

                }

                if (operation != null) {
                    operations.add(operation.toString());
                }

            }
        }

        if (isValid(operations)) {
            PATTERN = pattern;
            OPERATION_SET = operations;
            DECOMP_SET = this.decomposePattern(operations);

            IS_VALID = true;

            int count = 0;

            for (String op : OPERATION_SET) {
                //System.out.println(op);
                if (!(op.equals(">") || op.equals(">"))) {
                    count++;
                }
            }
            this.NUMOPS = count;
        } else {
            System.err.println(pattern);
            for (String op : operations) {
                System.err.println("  " + op);
            }
            PATTERN = null;
            OPERATION_SET = null;
            NUMOPS = -1;
            DECOMP_SET = null;
            
            IS_VALID = false;
        }

    }

    private boolean isValid(List<String> ops) {
        for (String op : ops) {
            if (op.charAt(0) == '[') {
                if (op.contains("]")) {

                } else {
                    return false;
                }
            }
            if (op.charAt(0) == '{') {
                if (op.contains("}")) {

                } else {
                    return false;
                }
            }
            if (op.length() > 1) {
                if (op.contains("(")) {
                    if (op.contains(")")) {
                        if (op.contains(",")) {
                            if (op.charAt(0) == 'X') {

                            } else {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        }

        /*
        Ranges of 'x' are not accepted at the beginning or at the end of a 
        pattern unless resticted/anchored to respectively the N- or C-terminal 
        of a sequence, for instance 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)' is not 
        accepted but 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)>' is.
         */
        if (ops.size() > 1
                && ops.get(ops.size() - 1).charAt(0) == 'X'
                && ops.get(ops.size() - 1).length() > 1
                && ops.get(ops.size() - 1).contains(",")) {
            return false;
        }
        return true;
    }

    public ArrayList<OrderedPair> hitList(String seq) {
        ArrayList<OrderedPair> ret = new ArrayList<>();
        //System.out.println(seq);
        //System.out.println(this.scan3(OPERATION_SET, seq, 0));
        //CSDCGKTFFDHSSLTRHQRTH
        //determines in N- or C-terminus anchors, and gets hits as 2D points
        //System.out.println("test " + String.valueOf(DECOMP_SET.get(0)));
        if (DECOMP_SET.get(0).endsWith("<")) {
            //System.out.println("test2 " + DECOMP_SET.get(0));
            int r = scan3(DECOMP_SET.subList(1, DECOMP_SET.size()), seq, 0);
            if (r != -1) {
                ret.add(new OrderedPair(0, r));
            }
        } else {
            search_loop:
            for (int i = 0; i < seq.length(); i++) {
                //int match = this.scan(OPERATION_SET, seq.substring(i));
                int match2 = this.scan3(DECOMP_SET, seq.substring(i), 0);
                if (match2 != -1) {
                    System.out.println(i + " " + (i + match2) + " " + seq.substring(i, i + match2));
                    ret.add(new OrderedPair(i, i + match2));
                }
            }
        }

        return ret;
    }
    
    private int scan3(List<String> decomp, String seq, int index) {

        //print_ops(decomp);
        int seqindex = index;

        if (decomp.isEmpty()) {
            return index;
        } else if (seq.isEmpty()) {
            if (decomp.size() == 1 && decomp.get(0).length() > 1 && decomp.get(0).endsWith(">")) {
                //In some rare cases (e.g. PS00267 or PS00539), '>' can also occur 
                //inside square brackets for the C-terminal element.
                //'F-[GSTV]-P-R-L-[G>]' is equivalent to
                //'F-[GSTV]-P-R-L-G' or 'F-[GSTV]-P-R-L>'.
                return index;
            } else {
                return -1;
            }
        } else if (decomp.get(0).charAt(0) != '$') {
            //System.out.print(decomp.get(0) + " ");

            if (conforms_3(decomp.get(0).substring(1), seq.charAt(0), decomp.get(0).charAt(0) == '-')) {
                //System.out.println("   " + (decomp.size() > 1 ? decomp.get(1) : "empty") + " " + seqindex + 1 + " " + seq.substring(1));
                return scan3(decomp.subList(1, decomp.size()), seq.substring(1), ++seqindex);
            } else {
                return -1;
            }
        } else {
            // System.out.print(decomp.get(0) + " ");
            int count = Integer.parseInt(decomp.get(0).substring(1));
            for (int k = 0; k <= count; k++) {
                int r;
                //System.out.println("$: " + decomp.get(1) + " " + seqindex + 1 + k + " " + seq.substring(1 + k));
                try {
                    r = scan3(decomp.subList(1, decomp.size()), seq.substring(k), seqindex + k);
                } catch (StringIndexOutOfBoundsException sioobe) {
                    //indicates substring exception, sequence ends with pattern unfulfilled
                    return -1;
                }
                //System.out.println(r + "S");
                if (r != -1) {
                    return r;
                }
            }
            return -1;
        }
    }

    private boolean conforms_3(String matchChar, char seqChar, boolean exclude) {
        if (matchChar.contains("X")) {
            return true;
        } else {
            return (matchChar.contains(String.valueOf(seqChar)) != exclude);
        }
    }

    private List<String> decomposePattern(List<String> pattern) {
        List<String> decomposed = new ArrayList<>();
        for (int k = 0; k < pattern.size(); k++) {
            String operation = pattern.get(k);
            OrderedPair range = getRange(operation);
            String chars = associatedCharacters(operation);
            boolean exclude = exclude(operation);
            if (range == null) {
                decomposed.add((exclude ? "-" : "+").concat(chars));
            } else {
                for (int i = 0; i < range.X; i++) {
                    decomposed.add((exclude ? "-" : "+").concat(chars));
                }
                if (range.Y != -1) { //'X' character only; must have been +
                    decomposed.add("$".concat(String.valueOf(range.Y - range.X)));
                }
            }
        }
        this.print_ops(decomposed);
        return decomposed;
    }

    private OrderedPair getRange(String operation) {
        OrderedPair range = null;
        if (operation.endsWith(")")) {
            String indices = operation.substring(operation.indexOf("(") + 1, operation.indexOf(")"));
            int x = -1;
            int y = -1;
            if (operation.contains(",")) {  //X(a,b) returns (a,b), only for X
                String xstr = indices.substring(0, indices.indexOf(","));
                String ystr = indices.substring(indices.indexOf(",") + 1);
                x = Integer.parseInt(xstr);
                y = Integer.parseInt(ystr);
            } else { //arbitrary range of an amino acid or acids
                x = Integer.parseInt(indices);
            }
            range = new OrderedPair(x, y);
        }
        return range;
    }

    private String associatedCharacters(String operation) {
        if (operation.length() == 1) {
            return operation;
        } else if (operation.charAt(0) == '[') {
            return operation.substring(operation.indexOf("[") + 1, operation.indexOf("]"));
        } else if (operation.charAt(0) == '{') {
            return operation.substring(operation.indexOf("{") + 1, operation.indexOf("}"));
        } else if (!operation.isEmpty()) {
            return String.valueOf(operation.charAt(0));
        }
        return null;
    }

    private boolean exclude(String operation) {
        return operation.charAt(0) == '{';
    }

    /*
Pattern syntax
The standard IUPAC one letter code for the amino acids is used in PROSITE.
The symbol 'x' is used for a position where any amino acid is accepted.
Ambiguities are indicated by listing the acceptable amino acids for a given position, between square brackets '[ ]'. For example: [ALT] stands for Ala or Leu or Thr.
Ambiguities are also indicated by listing between a pair of curly brackets '{ }' the amino acids that are not accepted at a given position. For example: {AM} stands for all any amino acid except Ala and Met.
Each element in a pattern is separated from its neighbor by a '-'.
Repetition of an element of the pattern can be indicated by following that element with a numerical value or, if it is a gap ('x'), by a numerical range between parentheses. 
Examples:
x(3) corresponds to x-x-x
x(2,4) corresponds to x-x or x-x-x or x-x-x-x
A(3) corresponds to A-A-A
When a pattern is restricted to either the N- or C-terminal of a sequence, that pattern respectively starts with a '<' symbol or ends with a '>' symbol. 
In some rare cases (e.g. PS00267 or PS00539), '>' can also occur inside square brackets for the C-terminal element. 'F-[GSTV]-P-R-L-[G>]' is equivalent to 'F-[GSTV]-P-R-L-G' or 'F-[GSTV]-P-R-L>'.
Note:
Ranges can only be used with with 'x', for instance 'A(2,4)' is not a valid pattern element.
Ranges of 'x' are not accepted at the beginning or at the end of a pattern unless resticted/anchored to respectively the N- or C-terminal of a sequence, for instance 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)' is not accepted but 'P-x(2)-G-E-S-G(2)-[AS]-x(0,200)>' is.
     
    
Extended syntax for ScanProsite:
If your pattern does not contain any ambiguous residues, you don't need to specify separation with '-'. 
Example: M-A-S-K-E can be written as MASKE. 
It means that in such a case you can directly copy/paste peptide sequences into the textfield.
To search all sequences which do not contain a certain amino acid, e.g. Cys, you can use <{C}*>.
    
    
    while (diff > 0) {
                        if (conforms(seq.charAt(0), chars, exclude)) {
                            int r = scan2(pattern.subList(1, pattern.size() - 1), seq.substring(1), seqindex++);
                            if (r != -1) { //match! found
                                return r;
                            } else {
                                diff--;
                            }
                        } else {
                            return -1;
                        }
                    }
        private int scan(List<String> pattern, String seq) {
        boolean match = true;
        int seqindex = 0;
        //print_ops(pattern);

        for (int k = 0; k < OPERATION_SET.size(); k++) {
            char currentChar = seq.charAt(seqindex);
            String operation = OPERATION_SET.get(k);

            int repeat = -1;
            OrderedPair range = getRange(operation);
            String chars = associatedCharacters(operation);
            boolean exclude = exclude(operation);
            System.out.println(
                    ((range == null) ? "!" : String.valueOf(range.X)) + " "
                    + ((range == null) ? "!" : String.valueOf(range.Y)) + " "
                    + exclude + " " + chars
            );
            /*
            if (range == null) {
                if (conforms(currentChar, chars, exclude)) {

                } else {
                    return -1;
                }
            } else if (range.Y == -1) {
                if (range.X > 1) {
                    for (int j = 0; j < range.X; j++) {
                        seqindex++;
                        currentChar = seq.charAt(seqindex);
                        System.out.println(seq);
                        if (conforms(currentChar, chars, exclude)) {

                        } else {
                            return -1;
                        }
                    }
                }
            } else { //range.Y is nonnegative
                if (chars.equals("X") && range.Y > range.X) {
                    for (int o = 0; o < range.Y; o++) {
                        seqindex++;
                        currentChar = seq.charAt(seqindex);
                        if (o >= range.X) {
                            print_ops(pattern.subList(k, pattern.size() - 1));

                            int submatch = scan(pattern.subList(k + 1, pattern.size() - 1), seq.substring(seqindex));
                        }
                        if (conforms(currentChar, chars, exclude)) {

                        } else {
                            return -1;
                        }
                    }
                } else {
                    return -1;
                }
            }
            seqindex++;
        }

        return -1;
    }

    private int scan2(List<String> pattern, String seq, int index) {
        System.out.print(pattern.get(0));
        int seqindex = index;
        //System.out.println("RAN");
        if (seq.isEmpty() || pattern.isEmpty()) {
            return -1;
        }

        String operation = pattern.get(0);
        OrderedPair range = getRange(operation);
        String chars = associatedCharacters(operation);
        boolean exclude = exclude(operation);

        if (pattern.size() == 1) {
            if (conforms(seq.charAt(0), chars, exclude, seqindex)) {
                return ++seqindex;
            } else {
                return -1;
            }
        } else if (pattern.size() > 1) {
            if (range != null) {
                for (int k = 0; k < range.X; k++) {
                    if (!seq.isEmpty()) {
                        if (conforms(seq.charAt(0), chars, exclude, seqindex)) {
                            seq = seq.substring(1);
                            seqindex++;
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                }
                if (range.Y != -1) { //multi-range only allowed with X
                    int diff = range.Y - range.X;
                    for (int m = 0; m < diff; m++) {
                        int r = scan2(pattern.subList(1 + m, pattern.size() - 1), seq.substring(1 + m), seqindex + 1 + m);
                        if (r != -1) {
                            return r;
                        } else if (conforms(seq.charAt(0), chars, exclude, seqindex)) {
                            continue;
                        } else {
                            return -1;
                        }
                    }
                    return -1;
                }
            } else if (conforms(seq.charAt(0), chars, exclude, seqindex)) {
                return scan2(pattern.subList(1, pattern.size() - 1), seq.substring(1), seqindex++);
            } else {
                return -1;
            }
        }

        return -1;
    }

    private boolean conforms(char seqchar, String chars, boolean exclude, int seqindex) {
        System.out.println(seqchar + " " + (exclude ? "1" : "0") + " " + chars + " " + seqindex);

        if (chars.contains("X")) { //X-any
            return true;
        }
        return (chars.contains(String.valueOf(seqchar)) != exclude);
    }

    
    
     */
    private void print_ops(List<String> operations) {
        System.out.print("OPSET:");
        for (String str : operations) {
            System.out.print(str + " ");
        }
        System.out.println();
    }

}
