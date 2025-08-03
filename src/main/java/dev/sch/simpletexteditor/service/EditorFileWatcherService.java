package dev.sch.simpletexteditor.service;

import dev.sch.simpletexteditor.util.FileComparator;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class EditorFileWatcherService {
    private final Map<Path, List<Path>> cache = new HashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private WatchService watchService;

    public void loadDirectoryAsync(Path dirPath, Function<List<Path>, Void> onSucceeded, Function<Exception, Void> onFailed){
        if (cache.containsKey(dirPath)){
            onSucceeded.apply(cache.get(dirPath));
            return;
        }

        Task<List<Path>> task = new Task<List<Path>>() {
            @Override
            protected List<Path> call() throws Exception {
                List<Path> sortedPaths = Files.list(dirPath)
                        .sorted(new FileComparator())
                        .toList();
                cache.put(dirPath, sortedPaths);
                return sortedPaths;
            }
        };

        task.setOnSucceeded(e->onSucceeded.apply(task.getValue()));
        task.setOnFailed(e->onFailed.apply(new Exception(task.getException())));

        executorService.execute(task);
    }

    public void setupDirectoryWatcher(Path dirToWatch, Runnable onChange){
        try {
            if (watchService != null){
                watchService.close();
            }
            watchService = FileSystems.getDefault().newWatchService();
            dirToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            executorService.execute(()->{
                try {
                    while (true){
                        WatchKey key = watchService.take();
                        if (!key.pollEvents().isEmpty()){
                            cache.clear();
                            Platform.runLater(onChange);
                        }
                        if (!key.reset()){
                            break;
                        }
                    }
                }catch (InterruptedException |ClosedWatchServiceException ignored){}
            });
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void registerDirectory(Path dir) throws IOException {
        dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );
    }
}
