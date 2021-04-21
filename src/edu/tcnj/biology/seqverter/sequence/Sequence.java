/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.sequence;

import edu.tcnj.biology.seqverter.converter.FileFormat;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A class that represents a sequence of <code>CharacterElement</code>s each
 * with their own indices, and that conforms to a given biological
 * <code>Alphabet</code>. Currently, this class allows for
 * invalid/non-conforming <code>CharacterElement</code>s without throwing an
 * exception. Also stores additional information for the given sequence, such as
 * comments.
 *
 * @author Avi Shah
 */
public class Sequence extends ArrayList<CharacterElement> {

    private String name = null;

    public ArrayList<String> additionalInformation;

    public boolean isFlagSequence = false;

    public Sequence() {
        super();
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the sequence from a <code>Collection</code> of pre-existing
     * <code>CharacterElement</code>s.
     *
     * @param elements the constituent <code>CharacterElement</code>s of this
     * <code>Sequence</code>.
     */
    public void setSequence(Collection<CharacterElement> elements) {
        this.clear();
        this.addAll(elements);
    }

    /**
     * Set the sequence from a <code>CharacterElement[]</code> array of
     * pre-existing <code>CharacterElement</code>s.
     *
     * @param elements the constituent <code>CharacterElement</code>s of this
     * <code>Sequence</code>.
     */
    public void setSequence(CharacterElement[] elements) {
        this.clear();
        this.addAll(Arrays.asList(elements));
    }

    /**
     * Sets the sequence assuming the index of the first character is 1. Gaps
     * are not given indices, and the index counter is incremented only for the
     * next non-gap character. Invalid characters are added.
     *
     * @param sequence the sequence to converted into
     * <code>CharacterElement</code>s
     */
    public void setSequence(String sequence) {
        setSequence(sequence, 1);
    }

    /**
     * Sets the sequence with a given start index for the first character. Gaps
     * are not given indices, and the index counter is incremented only for the
     * next non-gap character. Invalid characters are added.
     *
     * @param sequence the sequence to converted into
     * <code>CharacterElement</code>s
     * @param startIndex the index of the first <code>CharacterElement</code>
     */
    public void setSequence(String sequence, int startIndex) {
        this.removeAll(this);
        int counter = 0;
        for (int k = 0; k < sequence.length(); k++) {
            char c = sequence.charAt(k);
            this.add(new CharacterElement(c, counter + startIndex));
            if (c != '-') {
                counter++;
            }
        }
    }

    /**
     * Returns the <code>CharacterElement</code> whose index matches a given
     * true sequence index. If a <code>CharacterElement</code> matching the
     * input index is not found, no <code>CharacterElement</code> is returned.
     *
     * @param seqIndex the true sequence index of the
     * <code>CharacterElement</code> being requested.
     * @return the <code>CharacterElement</code> whose 'index' value matches the
     * input sequence index. If no matching <code>CharacterElement</code> is
     * found, <code>null</code> is returned.
     */
    public CharacterElement charAtSequenceIndex(int seqIndex) {
        return this.stream()
                .filter((ce) -> (ce.index() == seqIndex))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns this <code>Sequence</code>'s name.
     *
     * @return this <code>Sequence</code>'s name.
     */
    public String name() {
        return this.name;
    }

    /**
     * Add a <code>String</code> comment for this <code>Sequence</code>.
     *
     * @param comment the comment to be added for this <code>Sequence</code>.
     */
    public void addComment(String comment) {
        additionalInformation.add(name);
    }

    /**
     * Retrieve a comment for this <code>Sequence</code> for an index in the
     * order in which it was added.
     *
     * @param index the zero-indexed index of the comment
     * @return the requested comment
     */
    public String getComment(int index) {
        return additionalInformation.get(index);
    }

    /**
     * Returns an <code>ArrayList</code> containing all comments associated with
     * this <code>Sequence</code>.
     *
     * @return an <code>ArrayList</code> containing all comments associated with
     * this <code>Sequence</code>.
     */
    public ArrayList<String> allComments() {
        return this.additionalInformation;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String seqString() {
        StringBuilder sb = new StringBuilder();
        this.forEach((ce) -> {
            sb.append(ce.get());
        });
        return sb.toString();
    }

}

//currently unused exception for when an invalid character is added
class InvalidCharacterException extends Exception {

    public InvalidCharacterException() {
    }

    public InvalidCharacterException(String message) {
        super(message);
    }

    public InvalidCharacterException(Throwable cause) {
        super(cause);
    }

    public InvalidCharacterException(String message, Throwable cause) {
        super(message, cause);
    }
}
