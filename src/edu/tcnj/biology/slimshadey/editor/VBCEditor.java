/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.tcnj.biology.slimshadey.editor;

import java.util.Arrays;
import javafx.scene.image.Image;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 *
 * @author Avi Shah <shaha65@tcnj.edu>
 */
public class VBCEditor extends HBox {

    private VisualMultipleSequenceAlignment vmsa;

    //private Stage stage;
    private Scene scene;

    //private TabPane root;
    //private Tab editChar;
    //private Tab editColor;
    private HBox editChar;
    private Button changeChar;
    private HBox editColor;

    private TextField entryField;

    private VBox colorBox;
    private HBox backBox;
    private Label lbcpback;
    private ColorPicker cpback;
    private HBox foreBox;
    private Label lbcpfore;
    private ColorPicker cpfore;

    private VisualBioChar vbc;

    private HBox hoverBox;

    private boolean produceWarningPopupEdit = true;

    private final String TEMP_EDIT_FONT;

    //construct one of these for every VMSA
    public VBCEditor(EditorTab parent) {
        super(5);
        this.setAlignment(Pos.CENTER_LEFT);
        this.vmsa = parent.getVMSA();
        this.TEMP_EDIT_FONT = this.vmsa.generateFont(Color.CORNFLOWERBLUE, Color.BLACK, true, false);
        //stage = new Stage();
//stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("resources/icons/icon3.png")));
        //root = new TabPane();

        //options to edit the character
        //editChar = new Tab();
        //editChar.setText("Edit character");
        editChar = new HBox(5);
        editChar.setAlignment(Pos.CENTER_LEFT);
        Label message = new Label("Edit character:");
        entryField = new TextField();
        entryField.setStyle(this.vmsa.getDefaultFont().concat("-fx-background-color:#f0f0f0;"));
        entryField.setMinWidth(150);
        entryField.setPromptText("(-) (default gap)");
        entryField.textProperty().addListener(event -> {
            if (entryField.getText().length() > 1) {
                entryField.setText(entryField.getText().substring(entryField.getLength() - 1, entryField.getLength()));
            }
        });

        //HBox centerBox = new HBox(15);
        //centerBox.getChildren().addAll(message, entryField);
        //centerBox.setAlignment(Pos.CENTER);
        //VBox editBox = new VBox(15);
        changeChar = new Button("Confirm edit");

        changeChar.setPrefWidth(130);
        /*
        stage.setOnShown(e -> {
            changeChar.setPrefWidth(changeChar.getWidth());
            this.vbc.tempStyle(this.TEMP_EDIT_FONT);
        });

        stage.setOnCloseRequest(e -> {
            this.vbc.tempStyle(null);
        });
         */
        changeChar.setOnAction(e -> {
            if (!entryField.getText().isEmpty()) {
                if (produceWarningPopupEdit) {
                    boolean[] retVal = vmsa.modificationWarningPopup(null);
                    //System.out.println(retVal[0] + " " + retVal[1]);
                    produceWarningPopupEdit = !retVal[0]; //does user want to ignore future warnings?
                    if (retVal[1]) { //follow through with edit action
                        performEdit();
                    }
                } else {
                    performEdit();
                }
            }

        });
        changeChar.setOnMouseEntered(e -> {
            if (entryField.getText().isEmpty()) {
                changeChar.textFillProperty().set(Color.RED);
                changeChar.setText("Field empty");
            } else {
                changeChar.textFillProperty().set(Color.FORESTGREEN);
            }
        });
        changeChar.setOnMouseExited(e -> {
            changeChar.textFillProperty().set(Color.BLACK);
            changeChar.setText("Confirm edit");
        });
        //VBox editCharBox = new VBox(10);
        //editCharBox.setPadding(new Insets(10, 10, 10, 10));
        //editCharBox.getChildren().addAll(centerBox, changeChar);
        //editCharBox.setAlignment(Pos.CENTER);
        //editChar.setContent(editCharBox);
        //editChar.setClosable(false);
        editChar.getChildren().addAll(message, entryField, changeChar);

        // options to edit the color
        //editColor = new Tab();
        //editColor.setText("Change color");
        editColor = new HBox();
        editColor.setAlignment(Pos.CENTER_LEFT);

        cpback = new ColorPicker();
        cpback.valueProperty().addListener(e -> {
            String backHex = vmsa.hexifyColorFX(cpback.getValue());
            String foreHex = vmsa.decideForeGroundColor(backHex);
            vbc.setCurrentStyle(vmsa.generateFont(backHex, foreHex, false, false),
                    backHex, foreHex);
        });
        lbcpback = new Label(" Shading color:");
        backBox = new HBox(5);
        backBox.getChildren().addAll(lbcpback, cpback);
        backBox.setAlignment(Pos.CENTER_LEFT);

        cpfore = new ColorPicker();
        cpfore.valueProperty().addListener(e -> {
            String backHex = vmsa.hexifyColorFX(cpback.getValue());
            String foreHex = vmsa.hexifyColorFX(cpfore.getValue());
            vbc.setCurrentStyle(vmsa.generateFont(backHex, foreHex, false, false),
                    backHex, foreHex);
        });
        lbcpfore = new Label(" Text color:");
        foreBox = new HBox(5);
        foreBox.getChildren().addAll(lbcpfore, cpfore);
        foreBox.setAlignment(Pos.CENTER_LEFT);

        vmsa.bindColorPickers(Arrays.asList(new ColorPicker[]{cpback, cpfore}));

        entryField.focusedProperty().addListener(e -> {
            if (entryField.isFocused() || cpback.isFocused() || cpfore.isFocused() || changeChar.isFocused()) {
                //disable(false);
            } else {
                releaseVBC();
            }
        });

        cpback.focusedProperty().addListener(e -> {
            if (entryField.isFocused() || cpback.isFocused() || cpfore.isFocused()|| changeChar.isFocused()) {
                //disable(false);
            } else {
                releaseVBC();
            }
        });

        cpfore.focusedProperty().addListener(e -> {
            if (entryField.isFocused() || cpback.isFocused() || cpfore.isFocused()|| changeChar.isFocused()) {
                //disable(false);
            } else {
                releaseVBC();
            }
        });
        
        changeChar.focusedProperty().addListener(e -> {
            if (entryField.isFocused() || cpback.isFocused() || cpfore.isFocused()|| changeChar.isFocused()) {
                //disable(false);
            } else {
                releaseVBC();
            }
        });

        //colorBox = new VBox(10);
        //colorBox.setPadding(new Insets(10, 10, 10, 10));
        //colorBox.getChildren().addAll(backBox, foreBox);
        editColor.getChildren().addAll(backBox, foreBox);
        //editColor.setContent(colorBox);
        //editColor.setClosable(false);

        hoverBox = new HBox();
        hoverBox.setPadding(new Insets(0, 0, 0, 10));
        hoverBox.setAlignment(Pos.CENTER_LEFT);
        hoverBox.getChildren().add(new VisualBioChar(" ", false, false, false, false, false));
        hoverBox.getChildren().add(new Label(""));
        
        this.getChildren().addAll(editChar, editColor, hoverBox);

        //root.getTabs().addAll(editChar, editColor);
        //scene = new Scene(root);
        this/*root*/.setOnKeyPressed(key -> {
                    key.consume();
                    if (key.getCode() == KeyCode.ENTER) {
                        changeChar.fire();
                    } else {
                        this.setCurrentVBC(this.vmsa.crawl(vbc, key.getCode()));
                    }
                });
        entryField.setOnKeyPressed(key -> {
            switch (key.getCode()) {
                case LEFT:
                case RIGHT:
                case UP:
                case DOWN:
                    key.consume();
                    this.setCurrentVBC(this.vmsa.crawl(vbc, key.getCode()));
                    break;
                case ENTER:
                    //changeChar.fire();
                    break;
                default:
                    break;
            }
        });
        /*
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
         */
    }

    public void disable(boolean disable) {
        entryField.setDisable(disable);
        cpback.setDisable(disable);
        cpfore.setDisable(disable);
        changeChar.setDisable(disable);
        if (!disable) {
            getFocus();
        }
    }

    public void liveSwitch(VisualBioChar vbc) {
        setCurrentVBC(vbc);
    }

    public void performEdit() {
        char changeTo = entryField.getText().charAt(0);
        if (vbc.isseq && changeTo == ' ') {
            changeTo = '-';
        }
        this.vbc.setText(String.valueOf(changeTo));
        if (vbc.isseq && changeTo == '-') {
            int index = vbc.getParent().getChildrenUnmodifiable().indexOf(vbc);
            vmsa.checkForAllGapColumns(index, index, false);
        }
        String backHex = vmsa.hexifyColorFX(cpback.getValue());
        String foreHex = vmsa.hexifyColorFX(cpfore.getValue());
        vbc.setCurrentStyle(vmsa.generateFont(backHex, foreHex, false, false),
                backHex, foreHex);
    }

    public void setCurrentVBC_show(VisualBioChar vbc, boolean show) {
        this.setCurrentVBC(vbc);
        /*
        if (show && !stage.isShowing()) {
            stage.show();
        }
         */
    }

    private Rectangle shadingRect = null;

    private void addShadingRect(VisualBioChar vbc) {
        if (shadingRect != null) {
            vmsa.parent.getChildren().remove(shadingRect);
        }
        if (vbc != null) {
            Bounds bs_orig = vbc.getBoundsInParent();

            int numabove = 0;

            double xval = bs_orig.getMinX();
            double width = vbc.getWidth();

            double height = vbc.getHeight();
            double yval = height * vmsa.absoluteVerticalIndex(vbc);

            this.shadingRect = new Rectangle();
            this.shadingRect.setMouseTransparent(true);
            this.shadingRect.setFill(Color.web("#6699CC"));
            this.shadingRect.setOpacity(0.4);
            this.shadingRect.setY(yval);
            this.shadingRect.setX(xval);
            this.shadingRect.setWidth(width);
            this.shadingRect.setHeight(height);
            vmsa.parent.getChildren().add(shadingRect);
        }
    }

    public void setCurrentVBC(VisualBioChar vbc) {
        if (this.vbc != null) {
            this.vbc.tempStyle(null);
            releaseVBC();
        }
        this.disable(false);
        this.vbc = vbc;
        //this.stage.setTitle("");
        entryField.setPromptText("(" + String.valueOf(vbc.getChar()) + ") (no change)");
        cpback.setValue(Color.web(vbc.getBackHex()));
        cpfore.setValue(Color.web(vbc.getForeHex()));

        addShadingRect(vbc);

        //this.vbc.tempStyle(this.TEMP_EDIT_FONT);
    }

    public void releaseVBC() {
        if (this.vbc != null) {
            //this.vbc.tempStyle(null);

        }
        this.disable(true);
        addShadingRect(null);
    }

    public void updateTempVBC(VisualBioChar vbc) {
        ((VisualBioChar) hoverBox.getChildren().get(0)).setText(String.valueOf(vbc.getChar()));
        ((VisualBioChar) hoverBox.getChildren().get(0)).setCurrentStyle(generateFont(vbc.getBackHex(), vbc.getForeHex()), vbc.getBackHex(), vbc.getForeHex());
        int[] columnIndicesVBC = this.vmsa.getColumnIndices(vbc);
        String indicesToDisplay = "  (col = " + String.valueOf(columnIndicesVBC[0]) + ", seq = " + String.valueOf(columnIndicesVBC[1]) + ")";
        hoverBox.getChildren().set(1, new Label(indicesToDisplay));
    }

    public String generateFont(String backColor, String foreColor) {
        StringBuilder str = new StringBuilder("");
        String backgroundHex = backColor;
        String foregroundHex = foreColor;
        int fontSize = 18;
        str.append("    -fx-font-family: \"unifont\";\n"); //everything unifont - always
        str.append("    -fx-font-size: ").append(String.valueOf(fontSize)).append(";\n");
        str.append("    -fx-background-color: ").append(backgroundHex).append(" ;\n");
        str.append("    -fx-text-fill: ").append(foregroundHex).append(" ;\n");

        return str.toString();
    }

    public void getFocus() {
        this.entryField.requestFocus();
    }

    public void hide() {
        //this.stage.hide();
    }

}
