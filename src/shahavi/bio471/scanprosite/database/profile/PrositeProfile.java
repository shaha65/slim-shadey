/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shahavi.bio471.scanprosite.database.profile;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class PrositeProfile {

    final public String ALPHABET_ORDER;
    final public List<ProfileStep> PROFIILE;

    final public boolean LINEAR = false;
    final public boolean CIRCULAR = false;

    public PrositeProfile(String alphabetOrder, List<String> states, String accession) {
        this.ALPHABET_ORDER = alphabetOrder;
        this.PROFIILE = this.getProfile(states);
    }

    private List<ProfileStep> getProfile(List<String> states) {
        List<ProfileStep> ret = new ArrayList<>();

        for (String state : states) {
            System.out.println(state);
        }
        System.out.println();

        for (int i = 0; i < states.size(); i++) {
            String state = states.get(i);

            ProfileStep ps = new ProfileStep(state, this.ALPHABET_ORDER, i);
            ret.add(ps);
        }

        return ret;
    }

}
