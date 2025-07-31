package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class FileOperationService extends Service<Object> {
    private final FileOperationType operationType;
    private final Path targetPath;
    private final String contentToSave;
    private final Path directoryToScan;
    private final FileOperationCallback callback;

    public FileOperationService(
            FileOperationType operationType,
            Path targetPath,
            String contentToSave,
            Path directoryToScan,
            FileOperationCallback callback
    ){
        this.targetPath = targetPath;
        this.operationType = operationType;
        this.contentToSave = contentToSave;
        this.directoryToScan = directoryToScan;
        this.callback = Objects.requireNonNull(callback, "Callback can't be null");
    }

    public static FileOperationService createLoadService(Path path, FileOperationCallback callback){
        return new FileOperationService(
                FileOperationType.LOAD_CONTENT,
                path,
                null,
                null,
                callback
        );
    }

    public static FileOperationService createSaveService(Path path, String content, FileOperationCallback callback){
        return new FileOperationService(
                FileOperationType.SAVE_CONTENT,
                path,
                content,
                null,
                callback
        );
    }

    public static FileOperationService createListDirectoryService(Path dir, FileOperationCallback callback){
        return new FileOperationService(
                FileOperationType.LIST_DIRECTORY,
                null,
                null,
                dir,
                callback
        );
    }

    @Override
    protected Task<Object> createTask() {
        return new Task<>() {
            @Override
            protected Object call() throws Exception {
                this.updateProgress(0.1, 1.0);
                Object result = null;
                switch (operationType){
                    case LOAD_CONTENT -> {
                        updateMessage("Reading file: "+targetPath.getFileName());
                        result = callback.onLoadFIle(targetPath);
                    }
                    case SAVE_CONTENT -> {
                        updateMessage("Saving file: "+targetPath.getFileName());
                        callback.onSaveFile(targetPath, contentToSave);
                    }
                    case LIST_DIRECTORY -> {
                        updateMessage("Search file in folder: "+directoryToScan.getFileName());
                        result = callback.onListDirectory(directoryToScan);
                    }
                }
                updateProgress(1.0, 1.0);
                return result;
            }
        };
    }




    public enum FileOperationType{
        LOAD_CONTENT,
        SAVE_CONTENT,
        LIST_DIRECTORY
    }

    public interface FileOperationCallback{
        String onLoadFIle(Path path) throws IOException;
        void onSaveFile(Path path, String content) throws IOException;
        List<File> onListDirectory(Path directory) throws IOException;
    }
}
