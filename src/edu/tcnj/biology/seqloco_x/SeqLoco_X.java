/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.seqloco_x;

import edu.tcnj.biology.slimshadey.editor.VisualMultipleSequenceAlignment;
import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class SeqLoco_X {

    private VisualMultipleSequenceAlignment vmsa;

    public SeqLoco_X(VisualMultipleSequenceAlignment vmsa) {
        this.vmsa = vmsa;
        //load styling constants here, if needed
        //this object must be kept by its VMSA for duration of runtime
    }

    //these vars are now specific to the window produced
    /*
    private Stage locoStage;
    private Scene locoScene;
    private MenuBar locoMenuBar;
    private Menu locoFileMenu;
    private MenuItem locoSaveMenuItem;
    private VBox locoRoot;

    private ScrollPane locoScrollRootForCanvas;
    private Pane locoCanvasRoot;
    private Canvas locoCanvas;
     */
    private double locoCanvasWidth = 800;
    private double locoCanvasHeight = 500;

    //canvas parameters
    private final double ORIGIN_X = 35;
    private final double ORIGIN_Y = 5;

    private final double AXIS_WIDTH = 2;

    private final double DEFAULT_ORDINATE_HEIGHT = 400;
    private final double DEFAULT_CHAR_WIDTH = 30;

    private double workingHeight = DEFAULT_ORDINATE_HEIGHT;
    private double characterWidth = DEFAULT_CHAR_WIDTH;

    private final String DEFAULT_FONT_FAMILY = "Consolas";
    private String characterFont = DEFAULT_FONT_FAMILY;

    public void setupSeqLoco(String[] seqs, HashMap<Character, String> colorMap) {
        Stage setupLocoStage = new Stage();
        setupLocoStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        setupLocoStage.setTitle("Choose logo parameters");

        VBox setupLocoRoot = new VBox(15);
        setupLocoRoot.setPadding(new Insets(20, 20, 20, 20));

        HBox chooseCharacterHBox = new HBox(10);
        Label chooseCharacterWidthLabel = new Label("Character width (px)");
        TextField chooseCharacterWidthTextField = new TextField();
        chooseCharacterWidthTextField.setText(String.valueOf(DEFAULT_CHAR_WIDTH));
        chooseCharacterWidthTextField.setPromptText(String.valueOf(DEFAULT_CHAR_WIDTH));
        chooseCharacterHBox.getChildren().addAll(chooseCharacterWidthLabel, chooseCharacterWidthTextField);

        chooseCharacterWidthTextField.textProperty().addListener(e -> {
            if ((!chooseCharacterWidthTextField.getText().isEmpty() && !isDouble(chooseCharacterWidthTextField.getText())) || chooseCharacterWidthTextField.getText().length() > 4) {
                chooseCharacterWidthTextField.setText(chooseCharacterWidthTextField.getText().substring(0, chooseCharacterWidthTextField.getLength() - 1));
            }
        });

        HBox chooseWorkingHeightHBox = new HBox(10);
        Label chooseWorkingHeightLabel = new Label("Figure height (px)");
        TextField chooseWorkingHeightTextField = new TextField();
        chooseWorkingHeightTextField.setText(String.valueOf(DEFAULT_ORDINATE_HEIGHT));
        chooseWorkingHeightTextField.setPromptText(String.valueOf(DEFAULT_ORDINATE_HEIGHT));
        chooseWorkingHeightHBox.getChildren().addAll(chooseWorkingHeightLabel, chooseWorkingHeightTextField);
        chooseWorkingHeightTextField.textProperty().addListener(e -> {
            if ((!chooseWorkingHeightTextField.getText().isEmpty() && !isDouble(chooseWorkingHeightTextField.getText())) || chooseWorkingHeightTextField.getText().length() > 4) {
                chooseWorkingHeightTextField.setText(chooseWorkingHeightTextField.getText().substring(0, chooseWorkingHeightTextField.getLength() - 1));
            }
        });

        HBox chooseFontHBox = new HBox(10);
        Label chooseFontLabel = new Label("Character font ");
        TextField chooseFontTextField = new TextField();
        chooseFontTextField.setText(DEFAULT_FONT_FAMILY);
        chooseFontTextField.setPromptText(DEFAULT_FONT_FAMILY);
        chooseFontHBox.getChildren().addAll(chooseFontLabel, chooseFontTextField);

        CheckBox useSmallSampleError = new CheckBox("Use small sample error");
        useSmallSampleError.setSelected(true);

        CheckBox closeThisWindowOnExecute = new CheckBox("Close this options window on 'Continue'");
        closeThisWindowOnExecute.setSelected(true);

        Button setLocoParamsButton = new Button("Continue");
        setLocoParamsButton.setOnAction(e -> {

            if (!chooseCharacterWidthTextField.getText().isEmpty()) {
                characterWidth = Double.parseDouble(chooseCharacterWidthTextField.getText());
            } else {
                characterWidth = DEFAULT_CHAR_WIDTH;
            }
            if (!chooseWorkingHeightTextField.getText().isEmpty()) {
                workingHeight = Double.parseDouble(chooseWorkingHeightTextField.getText());
            } else {
                workingHeight = DEFAULT_ORDINATE_HEIGHT;
            }
            if (!chooseFontTextField.getText().isEmpty()) {
                characterFont = chooseFontTextField.getText();
            } else {
                characterFont = DEFAULT_FONT_FAMILY;
            }
            locoCanvasWidth = ORIGIN_X + seqs[0].length() * characterWidth + 50;
            locoCanvasHeight = workingHeight + 100;

            boolean useError = useSmallSampleError.isSelected();

            makeWindow_actual(seqs, colorMap, useError, characterFont);
            if (closeThisWindowOnExecute.isSelected()) {
                setupLocoStage.close();
            }
        });

        setupLocoRoot.setAlignment(Pos.CENTER);
        setupLocoRoot.getChildren().addAll(chooseCharacterHBox,
                chooseWorkingHeightHBox, chooseFontHBox, useSmallSampleError,
                closeThisWindowOnExecute, setLocoParamsButton);

        Scene setupLocoScene = new Scene(setupLocoRoot);
        setupLocoStage.setScene(setupLocoScene);
        setupLocoStage.show();
        setLocoParamsButton.requestFocus();
    }

    //ensures the d can be held (i.e. precludes things like 9999....9)
    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public void makeWindow_actual(String[] seqs, HashMap<Character, String> colorMap, boolean useError, String fontFamily) {

        /*first preprocess data into a 2d character array, allows 
        passing of individual character columns to the method that draws
        each index in the sequence logo*/
        Character[][] columns = new Character[seqs[0].length()][seqs.length];

        for (int i = 0; i < seqs.length; i++) {
            for (int k = 0; k < seqs[i].length(); k++) {
                columns[k][i] = seqs[i].charAt(k);
            }
        }

        //System.out.println(Arrays.deepToString(columns));
        //System.out.println(Arrays.deepToString(columns[0]));
        Stage locoStage = new Stage();
        locoStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        locoStage.setTitle("Sequence logo");

        MenuBar locoMenuBar = new MenuBar();
        Menu locoFileMenu = new Menu("File");
        MenuItem locoSaveMenuItem = new MenuItem("Save");
        locoFileMenu.getItems().add(locoSaveMenuItem);
        locoMenuBar.getMenus().add(locoFileMenu);

        Canvas locoCanvas = new Canvas(locoCanvasWidth, locoCanvasHeight);

        ScrollPane locoScrollRootForCanvas = new ScrollPane();
        locoScrollRootForCanvas.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        locoScrollRootForCanvas.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Pane locoCanvasRoot = new Pane();
        locoCanvasRoot.setStyle(" -fx-background-color: white");
        locoCanvasRoot.getChildren().add(locoCanvas);

        locoScrollRootForCanvas.setContent(locoCanvasRoot);
        //locoScrollRootForCanvas.setPrefWidth(400);

        VBox locoRoot = new VBox(0);
        locoRoot.getChildren().addAll(locoMenuBar, locoScrollRootForCanvas);

        Scene locoScene = new Scene(locoRoot);
        locoStage.setScene(locoScene);
        locoStage.show();

        GraphicsContext gc = locoCanvas.getGraphicsContext2D();

        generateAxes(gc, ((int) Math.ceil(Math.log(colorMap.keySet().size()) / Math.log(2))), columns.length);

        for (int j = 0; j < columns.length; j++) {
            drawColumn(gc, columns[j], colorMap, j, useError, fontFamily);
        }

        locoSaveMenuItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Save logo as PNG");
            fileChooser.setInitialFileName("seq_logo");

            File file = fileChooser.showSaveDialog(locoStage);
            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage((int) locoCanvas.getWidth(), (int) locoCanvas.getHeight());
                    locoCanvas.snapshot(null, writableImage);

                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                } catch (IOException ex) {
                    Logger.getLogger(SeqLoco_X.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //gc.drawImage(textImage(tx), 100, 100);
        //gc.strokeRect(0, 0, 10, 10);
    }

    private void drawColumn(GraphicsContext gc, Character[] column, HashMap<Character, String> colormapping, int index, boolean useError, String fontFamily) {
        int alphabetSize = colormapping.keySet().size();
        //String defaultColorHex = colormapping.getOrDefault(c, "#FFFFFF");
        HashMap<Character, Double> frequencyMapping = new HashMap<>();
        int sampleSize = 0;
        for (char c : column) {
            if (c != '-') {
                sampleSize++;
            }
        }

        for (char c : colormapping.keySet()) {
            if (c != '-') {
                int charCounter = 0;
                for (int k = 0; k < column.length; k++) {
                    if (column[k] == c) {
                        charCounter++;
                    }
                }
                frequencyMapping.put(c, ((double) charCounter) / ((double) sampleSize));
                System.out.println("    " + c + " " + frequencyMapping.get(c));
            }
        }

        double uncertainty = 0;
        for (char c : frequencyMapping.keySet()) {
            if (frequencyMapping.get(c) != 0) {
                uncertainty += frequencyMapping.get(c) * Math.log(frequencyMapping.get(c)) / Math.log(2.0);
            }
            System.out.println("    " + uncertainty);
        }
        uncertainty = -1 * uncertainty;

        double errest = 1 / Math.log(2.0) * (alphabetSize - 1) / (2 * sampleSize);
        if (!useError) {
            errest = 0;
        }
        System.out.println("err:    " + errest);
        double columnInformationContent = (Math.log(alphabetSize) / Math.log(2.0)) - (uncertainty + errest);
        System.out.println(columnInformationContent);
        columnInformationContent = Math.max(columnInformationContent, 0);

        HashMap<Character, Double> informationMapping = new HashMap<>();
        for (char c : frequencyMapping.keySet()) {
            double information = frequencyMapping.get(c) * columnInformationContent;
            informationMapping.put(c, information);
        }
        double maxinfo = Math.log(alphabetSize) / Math.log(2.0);
        double actualWorkingHeight = maxinfo / Math.ceil(maxinfo) * workingHeight;
        double columnWorkingHeight = columnInformationContent / maxinfo * actualWorkingHeight;
        //System.out.println(actualWorkingHeight + " " + columnWorkingHeight);
        HashMap<Character, Image> visualMapping = new HashMap<>();

        for (char c : informationMapping.keySet()) {
            double currentFrequency = frequencyMapping.get(c);
            if (currentFrequency != 0) {
                double height = currentFrequency * columnWorkingHeight;

                double width = characterWidth;
                Text tx = getCharacterImage(c, width, height, fontFamily);
                tx.setFill(Color.web(colormapping.get(c)));
                WritableImage textImage = textImage(tx);
                //adjust 1px
                Image rescaledTextImage = scale(textImage, ((int) textImage.getHeight() - 1), true);
                //Image rescaledTextImage = textImage;
                rescaledTextImage = this.removeWhitespaceRows(rescaledTextImage);

                visualMapping.put(c, rescaledTextImage);
            }
            //System.out.println(c + " " + visualMapping.get(c).getHeight());
        }
        List<Image> imageList = new ArrayList<>(visualMapping.values());

        imageList.sort(Comparator.comparingDouble(Image::getHeight));
        //Collections.reverse(imageList);
        double xval = ORIGIN_X + characterWidth * (index);
        double yval = ORIGIN_Y + workingHeight;
        for (int k = 0; k < imageList.size(); k++) {
            yval -= imageList.get(k).getHeight();
            gc.drawImage(imageList.get(k), xval, yval);
        }

    }

    public Image scale(Image source, int targetHeight, boolean preserveRatio) {
        ImageView imageView = new ImageView(source);
        imageView.setPreserveRatio(preserveRatio);
        imageView.setFitHeight(targetHeight);
        return imageView.snapshot(null, null);
    }

    private void generateAxes(GraphicsContext gc, int maxBits, int len) {
        //draw y axis
        gc.setFill(Color.BLACK);

        gc.fillRect(ORIGIN_X - AXIS_WIDTH, ORIGIN_Y, AXIS_WIDTH, workingHeight + AXIS_WIDTH);
        //draw x axis
        gc.fillRect(ORIGIN_X - AXIS_WIDTH, ORIGIN_Y + workingHeight, characterWidth * len + AXIS_WIDTH, AXIS_WIDTH);
        //gc.strokeLine(ORIGIN_X, ORIGIN_Y, ORIGIN_X, ORIGIN_Y + workingHeight);
        //add tick marks
        double xpostickx = ORIGIN_X - 2 * AXIS_WIDTH;
        double numdrawlength = 0;
        double labelStretchFactor = 4;
        for (int k = 0; k <= maxBits; k++) {
            double fraction = ((double) k) / ((double) maxBits);
            double yposk = (fraction * workingHeight) + ORIGIN_Y;
            gc.fillRect(xpostickx, yposk, AXIS_WIDTH, AXIS_WIDTH);
            Text number = getStringImage(String.valueOf(maxBits - k), AXIS_WIDTH * labelStretchFactor);
            WritableImage numdraw = textImage(number);
            gc.drawImage(numdraw, xpostickx - numdraw.getWidth() - 2, yposk - 2 - labelStretchFactor / 2);
            numdrawlength = numdraw.getWidth();
        }
        Text bitsLabel = getStringImage("bits", 1.2 * AXIS_WIDTH * labelStretchFactor);
        bitsLabel.rotateProperty().set(270);
        WritableImage bitsLabelImage = textImage(bitsLabel);
        gc.drawImage(bitsLabelImage, ORIGIN_X - 6 * AXIS_WIDTH - numdrawlength - bitsLabelImage.getWidth(), ORIGIN_Y + workingHeight / 2);

        double yposxlabs = ORIGIN_Y + workingHeight + AXIS_WIDTH + 4;
        for (int k = 1; k <= len; k++) {
            Text number = getStringImage(String.valueOf(k), AXIS_WIDTH * labelStretchFactor);
            WritableImage numdraw = textImage(number);
            double xposxlab = ORIGIN_X + (k - 1) * characterWidth + characterWidth / 2 - numdraw.getWidth() / 2;
            numdraw = removeWhitespaceRows(numdraw);
            gc.drawImage(numdraw, xposxlab, yposxlabs);
        }
    }

    private WritableImage removeWhitespaceRows(Image img) {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        boolean[] rewriteRow = new boolean[h];
        WritableImage ret = new WritableImage(w, h);
        for (int r = 0; r < h; r++) {
            boolean isAllWhitespace = true;
            for (int i = 0; i < w; i++) {
                if (!img.getPixelReader().getColor(i, r).equals(Color.WHITE)) {
                    isAllWhitespace = false;
                    break;
                }
            }
            rewriteRow[r] = isAllWhitespace;
        }
        int retCount = 0;
        for (int l = 0; l < rewriteRow.length; l++) {
            if (!rewriteRow[l]) {
                for (int x = 0; x < w; x++) {
                    ret.getPixelWriter().setColor(x, retCount, img.getPixelReader().getColor(x, l));
                }
                retCount++;
            }
        }
        return ret;
    }

    private Text getStringImage(String str, double h) {
        Text tx = new Text(str);
        tx.setFont(Font.font("Arial", FontWeight.BLACK, 50));
        tx.boundsTypeProperty().set(TextBoundsType.VISUAL);
        double computedWidth = tx.getLayoutBounds().getWidth();
        double computedHeight = tx.getLayoutBounds().getHeight();
        tx.setScaleX(h / computedHeight);
        tx.setScaleY(h / computedHeight);
        return tx;
    }

    private Text getCharacterImage(char character, double w, double h, String fontFamily) {
        String toDraw = String.valueOf(character);
        Text tx = new Text(toDraw);
        if (fontFamily == null || fontFamily.isEmpty()) {
            fontFamily = DEFAULT_FONT_FAMILY;
        }
        tx.setFont(Font.font(fontFamily, FontWeight.BLACK, 50));
        tx.boundsTypeProperty().set(TextBoundsType.VISUAL);

        double computedWidth = tx.getLayoutBounds().getWidth();
        double computedHeight = tx.getLayoutBounds().getHeight();
        tx.setScaleX(w / computedWidth);
        tx.setScaleY(h / computedHeight);
        return tx;
    }

    private WritableImage textImage(Text tx) {
        Scene scene = new Scene(new StackPane(tx));
        return tx.snapshot(null, null);
    }
}
