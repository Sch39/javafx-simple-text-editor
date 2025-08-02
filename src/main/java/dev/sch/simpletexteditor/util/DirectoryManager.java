package dev.sch.simpletexteditor.util;

import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.ObservableSettings;

import java.nio.file.Path;

public class DirectoryManager {
    private final EditorModel editorModel;
    private final ObservableSettings observableSettings;

    public DirectoryManager(EditorModel em, ObservableSettings os) {
        this.editorModel = em;
        this.observableSettings = os;

        // initial sync
        editorModel.setCurrentDirectoryPath(observableSettings.getLastDirectory());
    }

    public void changeDirectory(Path newDir) {
        if (newDir != null) {
            editorModel.setCurrentDirectoryPath(newDir);
            observableSettings.setLastDirectory(newDir);
        }
    }
}
