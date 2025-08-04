package dev.sch.simpletexteditor.service;

import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.ProgressModel;
import dev.sch.simpletexteditor.model.UIStateModel;
import javafx.application.Platform;
import javafx.concurrent.Service;

import java.nio.file.Path;
import java.util.function.Consumer;

public class EditorFileService {
    private final EditorModel editorModel;
    private final UIStateModel uiStateModel;
    private final ProgressModel progressModel;

    public EditorFileService(EditorModel editorModel,
                             UIStateModel uiStateModel,
                             ProgressModel progressModel
    ){
        this.editorModel = editorModel;
        this.uiStateModel = uiStateModel;
        this.progressModel = progressModel;
    }

    public Service<String> createOpenFileService(Path filePath, Runnable onSuccess, Consumer<Throwable> onError){
        if (filePath == null){
            throw new IllegalArgumentException("Target path is null");
        }

        OpenFileService service = new OpenFileService(filePath);
        progressModel.progressProperty().bind(service.progressProperty());
        progressModel.messageProperty().bind(service.messageProperty());
        progressModel.visibleProperty().set(true);

        service.setOnSucceeded(e -> Platform.runLater(() -> {
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
            progressModel.visibleProperty().set(false);

            String content = service.getValue();
            editorModel.setEditorContent(content);
            editorModel.setCurrentFilePath(filePath);
            editorModel.setFileModified(false);

            uiStateModel.setStatusMessage("Successfully open file '" + filePath.getFileName() + "'");
            if (onSuccess != null) {
                onSuccess.run();
            }
        }));

        service.setOnFailed(e -> Platform.runLater(() -> {
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
            progressModel.visibleProperty().set(false);

            Throwable ex = service.getException();
            uiStateModel.setStatusMessage("Failed to open file: " + (ex != null ? ex.getMessage() : "Unknown error"));
            if (ex != null) {
                ex.printStackTrace();
                if (onError != null) {
                    onError.accept(ex);
                }
            }
        }));

        return service;
    }

    public Service<Void> createSaveFileService(Path targetPath, String content, Runnable onSuccess, Consumer<Throwable> onError) {
        if (targetPath == null) {
            throw new IllegalArgumentException("Target path is null");
        }

        SaveFileService service = new SaveFileService(targetPath, content);

        progressModel.progressProperty().bind(service.progressProperty());
        progressModel.messageProperty().bind(service.messageProperty());
        progressModel.visibleProperty().set(true);

        service.setOnSucceeded(e->Platform.runLater(()->{
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
//            progressModel.visibleProperty().set(false);

            editorModel.setContentAndMarkAsSaved(content, targetPath);
            uiStateModel.setStatusMessage("Successfully save file '"+targetPath.getFileName()+"'");
            if (onSuccess != null){
                onSuccess.run();
            }
        }));

        service.setOnFailed(e->Platform.runLater(()->{
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
//            progressModel.visibleProperty().set(false);

            Throwable ex = service.getException();
            uiStateModel.setStatusMessage("Failed save: "+(ex != null ? ex.getMessage() : "Unknown error"));
            if (ex != null){
                ex.printStackTrace();
                if (onError != null){
                    onError.accept(ex);
                }
            }
        }));

        return service;
    }
}
