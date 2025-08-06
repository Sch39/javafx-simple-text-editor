package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Setter
public class SaveFileService extends Service<Void> {
    private Path path;
    private String content;

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.currentThread().setName("File-Save-Worker");
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
