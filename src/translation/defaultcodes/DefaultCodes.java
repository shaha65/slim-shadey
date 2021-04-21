/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package translation.defaultcodes;

import java.io.File;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import translation.GeneticCode;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class DefaultCodes {

    final static private List<String> RESOURCES = Arrays.asList(new String[]{
        "Standard.gcode",
        "Alternative_Flatworm_Mitochondrial.gcode",
        "Alternative_Yeast_Nuclear.gcode",
        "Ascidian_Mitochondrial.gcode",
        "Bacterial_Archaeal_Plant_Plastic.gcode",
        "Blastocrithidia_Nuclear.gcode",
        "Candidate_Division_SR1_and_Gracilibacteria.gcode",
        "Chlorophycean_Mitochondrial.gcode",
        "Ciliate_Dasycladacean_Hexamita.gcode",
        "Condylostoma_Nuclear.gcode",
        "Echinoderm_Flatworm_Mitochondrial.gcode",
        "Euplotid_Nuclear.gcode",
        "Invertebrate_Mitochondrial.gcode",
        "Karyorelict_Nuclear.gcode",
        "Mesodinium_Nuclear.gcode",
        "Mold_Protozoan_Coloenterate_Mitochondrial_and_Mycoplasma_Spiroplasma.gcode",
        "Pachysolen_tannophilus_Nuclear.gcode",
        "Peritrich_Nuclear.gcode",
        "Pterobranchia_Mitochondrial.gcode",
        "Scenedesmus_obliquus_Mitochondrial.gcode",
        "Thraustochytrium_Mitochondrial.gcode",
        "Trematode_Mitochondrial.gcode",
        "Vertebrate_Mitochondrial.gcode",
        "Yeast_Mitochondrial.gcode",});

    public static List<GeneticCode> getDefaultCodes() {
        List<GeneticCode> ret = new ArrayList<>();
        for (String resourceFileName : RESOURCES) {
            String gcodeName = resourceFileName.replace(".gcode", "").replaceAll("_", " ").replaceAll(" and ", "/");
            ret.add(new GeneticCode(DefaultCodes.class.getResourceAsStream(resourceFileName), gcodeName));
        }
        return ret;
    }

}
