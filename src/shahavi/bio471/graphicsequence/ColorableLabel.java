package shahavi.bio471.graphicsequence;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ColorableLabel extends HBox {

    private String css = "-fx-font-weight: bold; -fx-underline: true;";

    public ColorableLabel(String text) {
        super();
        for (int i = 0; i < text.length(); i++) {
            Label add = new Label(String.valueOf(text.charAt(i)));
            getChildren().add(add);
        }
    }

    public ColorableLabel(ColorableLabel cl, int start, int end) {
        super();
        getChildren().add(new Label(String.valueOf(start) + " - "));
        getChildren().addAll(cl.getChildren());
        getChildren().add(new Label(" - " + String.valueOf(end)));
    }

    public void setColor(int start, int end, Color col) {
        for (int i = start; i < end; i++) {
            Label lb = (Label) getChildren().get(i);
            lb.setTextFill(col);
            lb.setStyle(css);

        }

    }

    public void setLowerCase(int index) {
        ((Label) this.getChildren().get(index)).setText(((Label) this.getChildren().get(index)).getText().toLowerCase());

    }

    public void setUpperCase(int index) {
        ((Label) this.getChildren().get(index)).setText(((Label) this.getChildren().get(index)).getText().toUpperCase());

    }

}
