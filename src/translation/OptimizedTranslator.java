/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translation;

import java.util.ArrayList;
import javafx.scene.image.Image;
import java.util.List;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public final class OptimizedTranslator {

    public static String translate(String DNA, int readingFrame, GeneticCode gc) {

        List<String> codons = splitEqually(DNA.substring(readingFrame - 1), 3);
        if (codons.get(codons.size() - 1).length() != 3) {
            codons.remove(codons.size() - 1);
        }

        StringBuilder proteinSeq = new StringBuilder();

        codons.stream().forEach((codon) -> {
            proteinSeq.append(gc.translateCodon(codon.replaceAll("U", "T").toUpperCase()));
        });

        return proteinSeq.toString();

    }

    public static String backTranslateToDNA(String protseq, String nucseq) {
        StringBuilder ret = new StringBuilder();
        List<String> nucsplit = splitEqually(nucseq, 3);
        int nuciter = 0;
        for (int k = 0; k < protseq.length(); k++) {
            try {
                if (protseq.charAt(k) == '-') {
                    ret.append("---");
                } else {
                    ret.append(nucsplit.get(nuciter++));
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                ret.append("---");
            }

        }
        return ret.toString();
    }

    public static List<String> splitEqually(String text, int size) {

        List<String> ret = new ArrayList<>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }
}
