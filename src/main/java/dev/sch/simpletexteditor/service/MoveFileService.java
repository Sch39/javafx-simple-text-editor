package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@AllArgsConstructor
public class MoveFileService extends Service<Void> {
    private final Path sourcePath;
    private final Path destinationPath;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (sourcePath == null || destinationPath == null) {
                    throw new IllegalArgumentException("Source and destination paths cannot be null.");
                }

                if (!Files.exists(sourcePath)) {
                    throw new IOException("File or directory not found: " + sourcePath);
                }
                updateProgress(0.1, 1.0);
                Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                updateProgress(1.0, 1.0);
                return null;
            }
        };
    }
}
