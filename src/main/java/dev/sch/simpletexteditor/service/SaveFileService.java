package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class SaveFileService extends Service<Void> {
    private final Path path;
    private final String content;

    public SaveFileService(Path path, String content) {
        this.path = path;
        this.content = content;
    }
    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0.1, 1.0);
                updateMessage("Saving file: " + path.getFileName());
                Files.writeString(
                        path,
                        content,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                );
                updateProgress(1.0, 1.0);
                return null;
            }
        };
    }
}
