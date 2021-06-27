/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import com.sun.net.httpserver.HttpServer;
import javafx.scene.image.Image;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @author user1
 */
public class ManualViewer {

    private EditorInterface parentApplication;
    private Stage manualStage;
    private Scene manualScene;
    private WebView manualRoot;

    private String htmlContent;

    private String MANUAL_NAME = "manual_v4.html";

    public ManualViewer(EditorInterface parentApplication) {
        this.manualStage = new Stage();
        this.manualStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        this.manualStage.setTitle("SlimShadey manual");
        this.manualRoot = new WebView();
        this.manualScene = new Scene(manualRoot);
        this.manualStage.setScene(manualScene);

        try {
            this.parentApplication = parentApplication;

            //this.manualStage.show();
            String htmlstr = "";
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            File temp = File.createTempFile("temp_manual", ".html");
            temp.deleteOnExit();
            OutputStream out = new FileOutputStream(temp);

            try (InputStream is = ManualViewer.class.getResourceAsStream(MANUAL_NAME)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    //baos.write(buffer, 0, length);
                    out.write(buffer, 0, length);
                }
            }

            out.close();
            final String IMG_STR = "images";
            Path imgDir = Files.createTempDirectory(IMG_STR);
            //imgDir.deleteOnExit();
            System.out.println("**- " + imgDir.toString());

            File tmpimgdir_orig = imgDir.toFile();
            tmpimgdir_orig.deleteOnExit();
            //File tmpimgdir = new File(imgDir.toString().substring(0, imgDir.toString().lastIndexOf("\\")) + "\\" + IMG_STR);
            //System.out.println(imgDir.toString().substring(0, imgDir.toString().lastIndexOf("\\")) + "\\" + IMG_STR);
            File tmpimgdir = new File(imgDir.toString().substring(0, imgDir.toString().lastIndexOf(IMG_STR) + IMG_STR.length()));
            tmpimgdir.deleteOnExit();

            System.out.println("*****" + imgDir.toString().substring(0, imgDir.toString().lastIndexOf(IMG_STR) + IMG_STR.length()));

            boolean rename = tmpimgdir_orig.renameTo(tmpimgdir);
            System.out.println(rename);

            if (rename) {
                final String[] IMGS = {"image1.png", "image2.png", "image3.png", "image4.png", "image5.png"};

                for (String rscpath : IMGS) {
                    System.out.println(tmpimgdir.getPath() + "\\" + rscpath);
                    File tempimg = new File(tmpimgdir.getPath() + "\\" + rscpath);
                    tempimg.deleteOnExit();
                    if (tempimg.createNewFile()) {
                        OutputStream outimg = new FileOutputStream(tempimg);
                        try (InputStream is = getClass().getClassLoader().getResourceAsStream("resources/images/" + rscpath)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) != -1) {
                                //baos.write(buffer, 0, length);
                                outimg.write(buffer, 0, length);
                            }
                        }
                        outimg.close();
                    }
                }
            }
            //htmlstr = baos.toString(StandardCharsets.UTF_8.name());
            //baos.close();
            htmlContent = htmlstr;
            manualRoot.getEngine().loadContent(htmlstr);
            System.out.println(temp.toURI().toString());
            manualRoot.getEngine().load(temp.toURI().toString());

        } catch (IOException ex) {
            Logger.getLogger(ManualViewer.class.getName()).log(Level.SEVERE, null, ex);
        }

        //manualRoot.getEngine().load(ManualViewer.class.getResource("manual_v2.html").toExternalForm());
        manualRoot.getEngine().getLoadWorker().stateProperty().addListener(new HyperLinkRedirectListener(manualRoot));

    }

    public void accessManual(boolean access) {
        //HttpServer server = HttpServer.create(new InetSocketAddress(25000), 0);
        /*
        server.createContext("/generated", httpExchange -> {
        String content = htmlContent;
        httpExchange.sendResponseHeaders(200, content.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(content.getBytes());
        os.close();
        });
        
        server.setExecutor(null);
        server.start();
         */
        if (access) {
            this.manualStage.show();
        } else {
            this.manualStage.hide();
        }
    }

}
