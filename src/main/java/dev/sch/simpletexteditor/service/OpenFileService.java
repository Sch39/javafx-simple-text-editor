package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;

public class OpenFileService extends Service<String> {
    private final Path path;

    public OpenFileService(Path path) {
        this.path = path;
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                updateProgress(0.1, 1.0);
                updateMessage("Opening file: " + path.getFileName());
                String fileContent = Files.readString(path);

                updateProgress(1.0, 1.0);
                updateMessage("File loaded successfully.");

                return fileContent;
            }
        };
    }
}
