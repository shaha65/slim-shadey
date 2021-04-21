/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqverter.sequence;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Avi Shah
 */
public class SequenceHolder extends ArrayList<Sequence> {

    private Alphabet alpha;

    public SequenceHolder(Collection<String> sequences, Alphabet alpha) {
        super();
        this.alpha = alpha;
        //the passed alphabet is assumed to be correct
        int counter = 0;
        for (String seqstr : sequences) {
            Sequence seq = new Sequence();
            seq.setSequence(seqstr);
            seq.setName("Sequence_".concat(String.valueOf(++counter)));
        }
    }

    public Sequence consensusSequence = null;

    public SequenceHolder() {
        super();
    }
    
    public void setData(Collection<Sequence> sequences, Alphabet a) {
        this.addAll(sequences);
        this.setAlphabet(a);
    }

    
    public Alphabet getAlphabet() {
        return this.alpha;
    }
    
    public void setAlphabet(Alphabet a) {
        this.alpha = a;
    }
    
    public int sequenceLength() {
        return this.get(0).size();
    }

    public SequenceHolder copyNew() {
        SequenceHolder ret = new SequenceHolder();
        ret.setAlphabet(this.getAlphabet());
        this.forEach((seq) -> {
            ret.add(seq);
        });
        return ret;
    }
    
}
