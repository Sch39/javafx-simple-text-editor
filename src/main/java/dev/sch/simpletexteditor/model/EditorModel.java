package dev.sch.simpletexteditor.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;

import java.nio.file.Path;

public class EditorModel {
    private final String DEFAULT_NEW_FILE_NAME = "new file";
    private final StringProperty currentFileName = new SimpleStringProperty(DEFAULT_NEW_FILE_NAME);
    private final StringProperty editorContent = new SimpleStringProperty("");
    private final BooleanProperty fileModified = new SimpleBooleanProperty(false);

    @Getter
    private Path currentFilePath;

    private String initialContent = "";

    public EditorModel(){
        editorContent.addListener((obs, oldVal, newVal)->{
            fileModified.set(!newVal.equals(initialContent));
        });
    }

    public StringProperty currentFileNameProperty() {
        return currentFileName;
    }
    public StringProperty editorContentProperty() {
        return editorContent;
    }
    public BooleanProperty fileModifiedProperty() {
        return fileModified;
    }

    public String getCurrentFileName(){return currentFileName.get();}
    public String getEditorContent(){return editorContent.get();}
    public boolean getFileModified(){return fileModified.get();}

    public void setCurrentFilePath(Path currentFilePath) {
        this.currentFilePath = currentFilePath;
        if (currentFilePath != null){
            currentFileName.set(currentFilePath.getFileName().toString());
        }else {
            currentFileName.set(DEFAULT_NEW_FILE_NAME);
        }
    }

    public void newFile(){
        setCurrentFilePath(null);
        editorContent.set("");
        initialContent = "";
        fileModified.set(false);
    }

    public void setContentAndMarkAsSaved(String content, Path path) {
        this.initialContent = content;
        this.editorContent.set(content);
        setCurrentFilePath(path);
        this.fileModified.set(false);
    }
}
