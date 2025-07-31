package dev.sch.simpletexteditor.service;

import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.UIStateModel;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class EditorFileService implements FileOperationService.FileOperationCallback {
    private final EditorModel editorModel;
    private final UIStateModel uiStateModel;

    public EditorFileService(EditorModel editorModel,
                             UIStateModel uiStateModel){
        this.editorModel = editorModel;
        this.uiStateModel = uiStateModel;
    }

    public void saveFile(Path targetPath, String content, Runnable onSuccess, Consumer<Throwable> onError) {
        if (targetPath == null) {
            if (onError != null) onError.accept(new IllegalArgumentException("Target path is null"));
            return;
        }
        FileOperationService service = FileOperationService.createSaveService(targetPath, content, this);
        bindState(service, Operation.SAVE, targetPath);
        service.start();
    }

    public void bindState(FileOperationService service, Operation operation, Path path){
        String base = switch (operation){
            case LOAD -> "Loading file: ";
            case SAVE -> "Saving file: ";
            case LIST_DIR -> "Loading folder: ";
        };
        String targetName = path != null ?path.getFileName().toString():"null";

        Platform.runLater(()->{
            uiStateModel.setStatusMessage(base+targetName);
        });

        service.setOnSucceeded(e->Platform.runLater(()->{
            uiStateModel.setStatusMessage("Success: "+base+targetName);
        }));
        service.setOnFailed(e->Platform.runLater(()->{
            Throwable ex = service.getException();
            uiStateModel.setStatusMessage("Failed: "+base+targetName+(ex != null ? " (" + ex.getMessage() + ")" : ""));
            if (ex != null) ex.printStackTrace();
        }));
    }

    @Override
    public String onLoadFIle(Path path) throws IOException {
        return editorModel.readFileContent(path);
    }

    @Override
    public void onSaveFile(Path path, String content) throws IOException {
        editorModel.writeFileContent(path, content);
        Platform.runLater(() -> {
            editorModel.setCurrentFilePath(path);
            editorModel.fileModifiedProperty().set(false);
        });
    }

    @Override
    public List<File> onListDirectory(Path directory) throws IOException {
        return editorModel.getFilesInDirectory(directory);
    }

    private enum Operation {
        LOAD, SAVE, LIST_DIR
    }
}
