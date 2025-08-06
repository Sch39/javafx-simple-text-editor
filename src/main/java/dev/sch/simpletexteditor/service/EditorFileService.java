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

    private final SaveFileService saveFileService;

    public EditorFileService(EditorModel editorModel,
                             UIStateModel uiStateModel,
                             ProgressModel progressModel
    ){
        this.editorModel = editorModel;
        this.uiStateModel = uiStateModel;
        this.progressModel = progressModel;

        this.saveFileService = new SaveFileService();
        ServiceManager.register(saveFileService);
    }

    public void openFile(Path filePath, Runnable onSuccess, Consumer<Throwable> onError){
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

        ServiceManager.register(service);
        service.start();
    }

    public void saveFile(Path targetPath, String content, Runnable onSuccess, Consumer<Throwable> onError) {
        if (targetPath == null) {
            throw new IllegalArgumentException("Target path is null");
        }

        if (saveFileService.isRunning()){
            System.out.println("Save operation is already running...");
            return;
        }

        saveFileService.setPath(targetPath);
        saveFileService.setContent(content);

        progressModel.progressProperty().bind(saveFileService.progressProperty());
        progressModel.messageProperty().bind(saveFileService.messageProperty());
        progressModel.visibleProperty().set(true);

        saveFileService.setOnSucceeded(e->Platform.runLater(()->{
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
//            progressModel.visibleProperty().set(false);

            editorModel.setContentAndMarkAsSaved(content, targetPath);
            uiStateModel.setStatusMessage("Successfully save file '"+targetPath.getFileName()+"'");

            saveFileService.setPath(null);
            saveFileService.setContent(null);
            if (onSuccess != null){
                onSuccess.run();
            }
        }));

        saveFileService.setOnFailed(e->Platform.runLater(()->{
            progressModel.progressProperty().unbind();
            progressModel.messageProperty().unbind();
//            progressModel.visibleProperty().set(false);

            Throwable ex = saveFileService.getException();
            uiStateModel.setStatusMessage("Failed save: "+(ex != null ? ex.getMessage() : "Unknown error"));
            saveFileService.setPath(null);
            saveFileService.setContent(null);
            if (ex != null){
                ex.printStackTrace();
                if (onError != null){
                    onError.accept(ex);
                }
            }
        }));

        saveFileService.restart();
    }

    public void moveFile(Path sourcePath, Path targetPath, Runnable onSuccess, Consumer<Throwable> onFailed){
        MoveFileService service = new MoveFileService(sourcePath, targetPath);
        progressModel.progressProperty().bind(service.progressProperty());
        progressModel.visibleProperty().set(true);

        service.setOnSucceeded((e)->Platform.runLater(()->{
            progressModel.progressProperty().unbind();
            progressModel.visibleProperty().set(false);

            if (onSuccess != null){
                onSuccess.run();
            }
        }));

        service.setOnFailed(e->Platform.runLater(()->{
            progressModel.progressProperty().bind(service.progressProperty());
            progressModel.visibleProperty().set(true);

            Throwable ex = service.getException();
            if (ex != null){
                ex.printStackTrace();
                if (onFailed != null){
                    onFailed.accept(ex);
                }
            }
        }));

        ServiceManager.register(service);
        service.start();
    }
}
