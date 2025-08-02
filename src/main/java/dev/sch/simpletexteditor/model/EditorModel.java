package dev.sch.simpletexteditor.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EditorModel {
    private final String DEFAULT_NEW_FILE_NAME = "new file";
    private final StringProperty currentFileName = new SimpleStringProperty(DEFAULT_NEW_FILE_NAME);
    private final StringProperty editorContent = new SimpleStringProperty("");
    private final BooleanProperty fileModified = new SimpleBooleanProperty(false);

    @Getter
    private Path currentFilePath;
    @Setter
    @Getter
    private Path currentDirectoryPath;

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

    //    sync
    public void newFile(){
        setCurrentFilePath(null);
        editorContent.set("");
        initialContent = "";
        fileModified.set(false);
    }

    public String readFileContent(Path path) throws IOException {
        String content = Arrays.toString(Files.readAllBytes(path));
        initialContent = content;
        return  content;
    }

    public void writeFileContent(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
        initialContent = content;
    }

    public ObservableList<File> getFilesInDirectory(Path directory) throws IOException {
        return Files.list(directory)
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
