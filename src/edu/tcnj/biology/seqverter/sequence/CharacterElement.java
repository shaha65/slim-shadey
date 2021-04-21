/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.sequence;

/**
 * A wrapper class to hold the individual <code>char</code>acters of
 * <code>Sequence</code> with their indices. Indices are necessary to support
 * file formats that require numbers, and that allow numbering to begin at
 * non-zero indices.
 *
 * @author Avi Shah
 */
public class CharacterElement {

    private char element; //the character to be represented
    private int index;    //the character's index
    private boolean flag; //whether this contains in invalid character

    /**
     * Generates a <code>CharacterElement</code> using a <code>char</code> and
     * an index. Can be flagged if the character is invalid for a given
     * <code>Alphabet</code>.
     *
     * @param element the <code>char</code> that this object represents.
     * @param index the associated index within the <code>Sequence</code>.
     */
    public CharacterElement(char element, int index) {
        this.element = element;
        this.index = index;
        this.flag = false;
    }

    /**
     * Generates a <code>CharacterElement</code> using a <code>Character</code>
     * and an index. Can be flagged if the character is invalid for a given
     * <code>Alphabet</code>.
     *
     * @param element the <code>Character</code> that this object represents.
     * @param index the associated index within the <code>Sequence</code>.
     */
    public CharacterElement(Character element, int index) {
        this.element = element.charValue();
        this.index = index;
        this.flag = false;
    }

    /**
     * Allows the user to edit this <code>CharacterElement</code>'s fields.
     *
     * @param element the new <code>char</code> this will represent.
     * @param index this <code>CharacterElement</code>'s new index.
     */
    public CharacterElement edit(char element, int index) {
        this.element = element;
        this.index = index;
        return this;
    }

    /**
     * Returns the <code>char</code> this <code>CharacterElement</code>
     * represents.
     *
     * @return the <code>char</code> this <code>CharacterElement</code>
     * represents.
     */
    public char get() {
        return element;
    }

    /**
     * Returns the <code>int</code> index of this <code>CharacterElement</code>.
     *
     * @return the <code>int</code> index of this <code>CharacterElement</code>.
     */
    public int index() {
        return index;
    }

    /**
     * Flag this <code>CharacterElement</code> for representing in invalid
     * <code>char</code>acter.
     */
    public void flag() {
        this.flag = true;
    }

    /**
     * Unflag this <code>CharacterElement</code>.
     */
    public void unFlag() {
        this.flag = false;
    }

    /**
     * Check whether this <code>CharacterElement</code> is flagged.
     *
     * @return <code>boolean</code> representing whether this
     * <code>CharacterElement</code> is flagged
     */
    public boolean isFlagged() {
        return flag;
    }

    /**
     * Checks for equivalency between two <code>CharacterElement</code> objects.
     *
     * @param compare the <code>CharacterElement</code> this is being compared
     * to.
     * @return whether or not the two <code>CharacterElement</code>'s fields are
     * all equal.
     */
    public boolean equals(CharacterElement compare) {
        return this.index == compare.index
                && this.element == compare.element
                && this.flag == compare.flag;
    }

    /**
     * Checks for equivalency between two <code>CharacterElement</code> objects
     * without considering whether either is flagged.
     *
     * @param compare the <code>CharacterElement</code> this is being compared
     * to.
     * @return whether or not the two <code>CharacterElement</code>'s fields are
     * all equal ignoring flags.
     */
    public boolean equalsIgnoreFlags(CharacterElement compare) {
        return this.index == compare.index
                && this.element == compare.element;
    }

}
