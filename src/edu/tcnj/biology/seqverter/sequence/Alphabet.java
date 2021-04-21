/*
    Copyright (c) 2018 Avi Shah, Anudeep Deevi, Sudhir Nayak

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but without any warranty. See the GNU General Public License for more 
    details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package edu.tcnj.biology.seqverter.sequence;

import java.io.File;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A class to define the valid elements of a given alphabet, and what they are
 * defined as biologically. It is a <code>HashMap</code> with a name.
 *
 * @author Avi Shah
 */
public class Alphabet extends HashMap<Character, String> {

    public String name;
        
    public HashMap<Character,String> categories;
    
    public HashMap<Character,String> defaultColorMapping = null;
    
    public ArrayList<Matrix> matrices = null;
    
    public String consensusAlphabetClustal = null;
    
    public List<List<String>> consensusRulesClustal = null;
    
    public HashMap<String, String> colorToHexClustal = null;
    
    public HashMap<Character, String[]> consensusColorClustal = null;
    
    public Alphabet() {
        super();
    }
    
    public Alphabet(File f) {
        
    }
    
    /**
     * Create a new <code>Alphabet</code> with a given name.
     *
     * @param name the name of this <code>Alphabet</code>.
     */
    public Alphabet(String name) {
        super();
        this.name = name;
    }

    /**
     * Returns whether or not a given character is valid in this
     * <code>Alphabet</code>.
     *
     * @param c the <code>Character</code> whose validity is being checked.
     * @return whether or not the input character is a member of this
     * <code>Alphabet</code>.
     */
    public boolean isValidCharacter(Character c) {
        //if (!this.containsKey(c)) System.err.println("not valid: " + c);

        return this.containsKey(c);
    }

    /**
     * Checks to see whether the <code>String</code> representation of a
     * sequence conforms to this <code>Alphabet</code>'s defined elements.
     * <p>
     * For an <code>ArrayList</code> containing all non-conforming characters, a
     * <code>Sequence</code> object must be created with a defined Alphabet, and
     * its <code>checkSequence()</code> method must be used.
     *
     * @param seq the <code>String</code> sequence to be checked for conformity.
     * @return whether this sequence conforms (has no invalid characters) with
     * respect to this <code>Alphabet</code>.
     */
    public boolean conforms(String seq) {
        for (int i = 0; i < seq.length(); i++) {
            if (!this.isValidCharacter(seq.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean conforms(Sequence seq) {
        for (int i = 0; i < seq.size(); i++) {
            if (!this.isValidCharacter(seq.get(i).get())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Generates an <code>Alphabet</code> whose elements are meaningless.
     *
     * @param sequence the sequence whose <code>char</code>acters are used to
     * fill the <code>Alphabet</code>
     */
    public void fillDefault(String sequence) {
        for (int i = 0; i < sequence.length(); i++) {
            this.put(sequence.charAt(i), null);
        }
    }

    public void fillDefault(Sequence sequence) {
        for (int i = 0; i < sequence.size(); i++) {
            this.put(sequence.get(i).get(), null);
        }
    }

    /**
     * Gives meaning to meaningless element. If the element already has meaning,
     * no meaning is given. If the element is not already in the
     * <code>Alphabet</code>, no meaning is given.
     *
     * @param key the <code>Character</code> to be given meaning.
     * @param meaning the meaning to give the <code>Character</code> key.
     * @return <code>true</code> if the key is successfully given meaning.
     */
    public boolean giveMeaning(Character key, String meaning) {
        if (this.containsKey(key) && (this.get(key) == null || this.get(key).isEmpty())) {
            this.put(key, meaning);
            return true;
        }
        return false;
    }

    /**
     * Generates an explicit version of the given <code>Alphabet</code> object
     * without modifying the given alphabet. The returned explicit
     * <code>Alphabet</code> does not contain any repeat characters i.e. the 'B'
     * (ASX) protein FASTA character that represents 'D' (ASP) and 'N' (ASN) or
     * the 'X' (any) which represents any amino acid. Creation of such an
     * alphabet is necessary when calculating the values of some conservation
     * measures for a <code>MultipleSequenceAlignment</code>.
     *
     * @return the explicit version of this <code>Alphabet</code>.
     */
    public Alphabet createExplicitVersion() {
        Alphabet ret = new Alphabet("Explicit:".concat(this.name()));
        this.keySet().stream().filter((key)
                -> (this.get(key).charAt(0) != '*'
                && this.get(key).charAt(0) != '+')).forEachOrdered((key)
                -> {
            ret.put(key, this.get(key));
        });
        return ret;
    }

    /**
     * Get the name of this <code>Alphabet</code>.
     *
     * @return the name of this <code>Alphabet</code>.
     */
    public String name() {
        return this.name;
    }


    /**
     * Returns the name so </tt>ObservableList</tt>s can display name on GUI
     * JavaFX components, but access underlying object for use.
     *
     * @return the <tt>String</tt> representation of this <tt>Alphabet</tt>'s
     * name
     */
    @Override
    public String toString() {
        return this.name();
    }

}
