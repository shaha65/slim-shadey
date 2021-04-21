/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opalimpl;

import java.awt.*;
import javafx.scene.image.Image;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*


this is from https://stackoverflow.com/a/343007  !!!!!


 */
public class TextAreaOutputStream
        extends OutputStream {

// *************************************************************************************************
// INSTANCE MEMBERS
// *************************************************************************************************
    private byte[] oneByte;                                                    // array for write(int val);
    private Appender appender;                                                   // most recent action

    public TextAreaOutputStream(JTextPane txtara) {
        this(txtara, 1000);
    }

    public TextAreaOutputStream(JTextPane txtara, int maxlin) {
        if (maxlin < 1) {
            throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")");
        }
        oneByte = new byte[1];
        appender = new Appender(txtara, maxlin);
    }

    /**
     * Clear the current console text area.
     */
    public synchronized void clear() {
        if (appender != null) {
            appender.clear();
        }
    }

    public synchronized void close() {
        appender = null;
    }

    public synchronized void flush() {
    }

    public synchronized void write(int val) {
        oneByte[0] = (byte) val;
        write(oneByte, 0, 1);
    }

    public synchronized void write(byte[] ba) {
        write(ba, 0, ba.length);
    }

    public synchronized void write(byte[] ba, int str, int len) {
        if (appender != null) {
            appender.append(bytesToString(ba, str, len));
        }
    }

    static private String bytesToString(byte[] ba, int str, int len) {
        try {
            return new String(ba, str, len, "UTF-8");
        } catch (UnsupportedEncodingException thr) {
            return new String(ba, str, len);
        } // all JVMs are required to support UTF-8
    }

// *************************************************************************************************
// STATIC MEMBERS
// *************************************************************************************************
    static class Appender
            implements Runnable {

        private final JTextPane textArea;
        private final int maxLines;                                                   // maximum lines allowed in text area
        private final LinkedList<Integer> lengths;                                                    // length of lines within text area
        private final List<String> values;                                                     // values waiting to be appended

        private int curLength;                                                  // length of current line
        private boolean clear;
        private boolean queue;

        Appender(JTextPane txtara, int maxlin) {
            textArea = txtara;
            maxLines = maxlin;
            lengths = new LinkedList<Integer>();
            values = new ArrayList<String>();

            curLength = 0;
            clear = false;
            queue = true;
        }

        synchronized void append(String val) {
            values.add(val);
            if (queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        synchronized void clear() {
            clear = true;
            curLength = 0;
            lengths.clear();
            values.clear();
            if (queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        // MUST BE THE ONLY METHOD THAT TOUCHES textArea!
        public synchronized void run() {
            SimpleAttributeSet set = new SimpleAttributeSet();

            StyleConstants.setBold(set, true);
            StyleConstants.setFontSize(set, 14);
            StyleConstants.setFontFamily(set, "Consolas");
            if (clear) {
                textArea.setText("");
            }
            for (String val : values) {
                Document doc = textArea.getStyledDocument();
                curLength += val.length();
                if (val.endsWith(EOL1) || val.endsWith(EOL2)) {
                    if (lengths.size() >= maxLines) {
                        try {
                            doc.remove(0, lengths.removeFirst());
                        } catch (BadLocationException ex) {
                            Logger.getLogger(TextAreaOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    lengths.addLast(curLength);
                    curLength = 0;
                }
                try {
                    doc.insertString(doc.getLength(), val, set);
                } catch (BadLocationException ex) {
                    Logger.getLogger(TextAreaOutputStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            values.clear();
            clear = false;
            queue = true;
        }

        static private final String EOL1 = "\n";
        static private final String EOL2 = System.getProperty("line.separator", EOL1);
    }

}
/* END PUBLIC CLASS */
