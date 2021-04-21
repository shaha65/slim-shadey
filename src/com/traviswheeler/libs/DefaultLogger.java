package com.traviswheeler.libs;

import java.io.IOException;
import javafx.scene.image.Image;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import javafx.scene.control.TextArea;

public class DefaultLogger extends LogWriter implements Logger {

    public DefaultLogger() {
        allowStdOut = true;
    }

    public void log(String s) {
        System.err.print(s);

    }

    public void logln(String s) {
        System.err.println(s);

    }

}
