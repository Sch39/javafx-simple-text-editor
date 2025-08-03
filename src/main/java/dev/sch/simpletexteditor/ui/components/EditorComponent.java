package dev.sch.simpletexteditor.ui.components;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Objects;

@Getter
public class EditorComponent extends VBox {
    private final TextArea editorTextArea;

    public EditorComponent(double prefHeight, Insets padding, TextArea editorTextArea){
        this.editorTextArea = editorTextArea;
        this.editorTextArea.setWrapText(true);
        this.editorTextArea.setPrefHeight(prefHeight);
        this.editorTextArea
                .getStylesheets()
                .add(Objects.requireNonNull(SimpleTextEditorApp.class.getResource("styles/component/editor-component.css")).toExternalForm());

        this.setPadding(padding);
        this.getChildren().addAll(this.editorTextArea);
    }

    public EditorComponent(TextArea editorTextArea){
        this(600, new Insets(10), editorTextArea);
    }
}
