package dev.sch.simpletexteditor.service;

import dev.sch.simpletexteditor.util.FileComparator;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorFileWatcherService {
    public interface DirectoryChangeListener{
        void onChange(Path watchedDir, WatchEvent.Kind<?> kind, Path relativePath);
    }

    private final Map<Path, List<Path>> cache = new ConcurrentHashMap<>();
    private final ExecutorService fileTaskExecutor = Executors.newCachedThreadPool();

    private final Map<Path, WatchService> watchServices = new ConcurrentHashMap<>();
    private final Map<Path, ExecutorService> watcherExecutors = new ConcurrentHashMap<>();
    private final Map<Path, List<DirectoryChangeListener>> listeners = new ConcurrentHashMap<>();

//    debounce per-directory updates
    private final ScheduledExecutorService debounceExecutor = Executors.newScheduledThreadPool(1);
    private final Map<Path, Runnable> pendingDebounces = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_MS = 150;

    public void invalidateCache(Path dir){
        cache.remove(dir);
    }

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

    public void setupDirectoryWatcher(Path dirToWatch, DirectoryChangeListener listener){
        listeners.computeIfAbsent(dirToWatch, k->new ArrayList<>()).add(listener);

        if (watchServices.containsKey(dirToWatch)){
            return;
        }

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            dirToWatch.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            watchServices.put(dirToWatch, watcher);
            watcherExecutors.put(dirToWatch, executor);

            executor.execute(()->{
                try {
                    while (true){
                        WatchKey key = watcher.take();
                        boolean sawOverflow = false;
                        Path watched = (Path) key.watchable();

                        for (WatchEvent<?> rawEvent : key.pollEvents()){
                            WatchEvent.Kind<?> kind = rawEvent.kind();
                            if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                                sawOverflow = true;
                                continue;
                            }

                            Path relative = ((WatchEvent<Path>) rawEvent).context();
                            scheduleDebounced(dirToWatch, ()->{
                                invalidateCache(dirToWatch);
                                List<DirectoryChangeListener> ls = listeners.getOrDefault(dirToWatch, List.of());

                                for (DirectoryChangeListener l : ls){
                                    Platform.runLater(()->l.onChange(dirToWatch, kind, relative));
                                }
                            });
                        }
                        if (sawOverflow){
                            scheduleDebounced(dirToWatch, ()->{
                                invalidateCache(dirToWatch);
                                List<DirectoryChangeListener> ls = listeners.getOrDefault(dirToWatch, List.of());
                                for (DirectoryChangeListener l : ls) {
                                    Platform.runLater(() -> l.onChange(dirToWatch, StandardWatchEventKinds.OVERFLOW, null));
                                }
                            });
                        }
                        if (!key.reset()){
                            break;
                        }
                    }
                }catch (InterruptedException |ClosedWatchServiceException ignored){}
                finally {
                    closeWatcher(dirToWatch);
                }
            });
        }catch (AccessDeniedException e){
            System.err.println("Access denied for directory: " + dirToWatch);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void registerDirectory(Path dir, DirectoryChangeListener listener) throws IOException {
        setupDirectoryWatcher(dir, listener);
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

    private void scheduleDebounced(Path dir, Runnable task){
        pendingDebounces.put(dir, task);
        debounceExecutor.schedule(()->{
            Runnable current = pendingDebounces.get(dir);
            if (current == task){
                current.run();
                pendingDebounces.remove(dir);
            }
        }, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
    }

    private void closeWatcher(Path dir) {
        WatchService ws = watchServices.remove(dir);
        if (ws != null){
            try {
                ws.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        ExecutorService es = watcherExecutors.remove(dir);
        if (es != null)
            es.shutdown();

        listeners.remove(dir);
    }
}
