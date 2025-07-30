package dev.sch.simpletexteditor.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.Getter;

@Getter
public class EditorComponent extends VBox {
    private final TextArea editorTextArea;

    public EditorComponent(double prefHeight, Insets padding){
        this.editorTextArea = new TextArea();
        this.editorTextArea.setWrapText(true);
        this.editorTextArea.setPrefHeight(prefHeight);

        this.setPadding(padding);
        this.getChildren().addAll(editorTextArea);
    }

    public EditorComponent(){
        this(600, new Insets(10));
    }
}
