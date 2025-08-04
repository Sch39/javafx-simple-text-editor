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
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorFileWatcherService {
    private final Map<Path, List<Path>> cache = new HashMap<>();
    private final ExecutorService fileTaskExecutor = Executors.newCachedThreadPool();

    private volatile WatchService currentWatchService;
    private volatile ExecutorService currentWatchExecutor;
    private volatile Path currentWatchedDir;

    public void loadDirectoryAsync(Path dirPath, Function<List<Path>, Void> onSucceeded, Function<Exception, Void> onFailed){
        if (cache.containsKey(dirPath)){
            onSucceeded.apply(cache.get(dirPath));
            return;
        }

        Task<List<Path>> task = new Task<>() {
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

        fileTaskExecutor.execute(task);
    }

    public void setupDirectoryWatcher(Path dirToWatch, Runnable onChange){
        shutdownWatcher();
        this.currentWatchedDir = dirToWatch;
        try {
            WatchService newWatcher = FileSystems.getDefault().newWatchService();
            ExecutorService newExecutor = Executors.newSingleThreadExecutor();
            dirToWatch.register(
                    newWatcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            this.currentWatchService = newWatcher;
            this.currentWatchExecutor = newExecutor;

            newExecutor.execute(()->{
                try {
                    while (true){
                        WatchKey key = newWatcher.take();
                        if (!key.pollEvents().isEmpty()){
                            if (newWatcher.equals(currentWatchService) && dirToWatch.equals(currentWatchedDir)) {
                                cache.clear();
                                Platform.runLater(onChange);
                            }
                        }
                        if (!key.reset()){
                            break;
                        }
                    }
                }catch (InterruptedException |ClosedWatchServiceException ignored){}
            });
        }catch (AccessDeniedException e){
            System.err.println("Access denied for directory: " + dirToWatch);
            shutdownWatcher();
        }catch (IOException e){
            e.printStackTrace();
            shutdownWatcher();
        }
    }

    public void registerDirectory(Path dir) throws IOException {
        try {
            if (currentWatchService != null) {
                dir.register(
                        currentWatchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                );
            }
        } catch (AccessDeniedException e) {
            System.err.println("Access denied for directory: " + dir);
        } catch (ClosedWatchServiceException | IOException e) {
            e.printStackTrace();
        }
    }

    public void hasChildrenAsync(Path  path, Consumer<Boolean> onResult){
        fileTaskExecutor.execute(()->{
            boolean hasChildren = false;
            if (Files.isDirectory(path)){
                try(DirectoryStream<Path> ds = Files.newDirectoryStream(path)){
                    hasChildren = ds.iterator().hasNext();
                }catch (IOException ignored){}
            }
            boolean finalHasChildren = hasChildren;
            Platform.runLater(()->onResult.accept(finalHasChildren));
        });
    }

    private void shutdownWatcher() {
        if (currentWatchService != null) {
            try {
                currentWatchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (currentWatchExecutor != null) {
            currentWatchExecutor.shutdownNow();
            currentWatchExecutor = null;
        }
    }
}
