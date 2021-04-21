package opalimpl;

import edu.tcnj.biology.slimshadey.editor.EditorInterface;
import javafx.scene.image.Image;
import java.io.File;

/**
 *
 * @author Avi Shah
 */
public class OpalFX {

    public OpalFX(EditorInterface origin, File infile) {
        new OpalAgreement(true, origin, infile);
    }

}
