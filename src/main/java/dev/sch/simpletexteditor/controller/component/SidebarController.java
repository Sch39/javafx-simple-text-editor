package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.service.EditorFileWatcherService;
import dev.sch.simpletexteditor.ui.components.SidebarComponent;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SidebarController implements IController<SidebarComponent> {
    private final AppContext ctx;
    private final SidebarComponent sidebarComponent;
    private final ObservableSettings observableSettings;
    private final EditorFileWatcherService fileWatcherService;
    @Setter
    private Consumer<Path> onFileDoubleClick;

    private final Map<Path, TreeItem<Path>> pathToTreeItem = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Path, Runnable> pendingSyncs = new ConcurrentHashMap<>();
    private static final long SYNC_DELAY_MS = 150;

    @Setter
    private BiConsumer<Path, Path> onMoveEditCommit;

    public SidebarController(AppContext ctx) {
        this.ctx = ctx;
        this.sidebarComponent = new SidebarComponent();
        this.observableSettings = ctx.getObservableSettings();
        this.fileWatcherService = ctx.getFileWatcherService();
    }

    @Override
    public SidebarComponent getView() {
        return sidebarComponent;
    }

    @Override
    public void initialize() {
        observableSettings.lastDirectoryProperty()
                .addListener((obs, oldDir, newDir) -> {
                    if (newDir != null) {
                        loadDirectory(newDir);
                    } else {
                        sidebarComponent.getFileTreeView().setRoot(null);
                        pathToTreeItem.clear();
                    }
                });

        if (observableSettings.getLastDirectory() != null) {
            loadDirectory(observableSettings.getLastDirectory());
        }

        sidebarComponent.getFileTreeView()
                .setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        TreeItem<Path> selectedItem = sidebarComponent.getFileTreeView().getSelectionModel().getSelectedItem();

                        if (selectedItem != null && Files.isRegularFile(selectedItem.getValue())) {
                            Path fileToOpen = selectedItem.getValue();

                            if (onFileDoubleClick != null) {
                                onFileDoubleClick.accept(fileToOpen);
                            }
                        } else if (selectedItem != null && !Files.isDirectory(selectedItem.getValue())) {
                            new Alert(Alert.AlertType.ERROR, "File not found, maybe delete/moved ").showAndWait();
                            TreeItem<Path> root = sidebarComponent.getFileTreeView().getRoot();
                            if (root != null) {
                                scheduleSync(root, root.getValue());
                            }
                        }
                    }
                });

        sidebarComponent.getFileTreeView().setOnEditCommit(event -> {
            Path oldPath = event.getTreeItem().getValue();
            Path newPath = event.getNewValue();
            if (onMoveEditCommit != null){
                onMoveEditCommit.accept(oldPath, newPath);
            }
            System.out.println("Rename file from " + oldPath.getFileName() + " to " + newPath.getFileName());
        });

        sidebarComponent.getFileTreeView().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.F12) {
                TreeItem<Path> selectedItem = sidebarComponent.getFileTreeView().getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() != null && selectedItem.getParent() != null) {
                    sidebarComponent.setEditModeRequested(true);
                    sidebarComponent.getFileTreeView().edit(selectedItem);
                }
                event.consume();
            }
        });
    }

    private void loadDirectory(Path dirPath) {
        TreeItem<Path> rootItem = createTreeItem(dirPath);
        sidebarComponent.getFileTreeView().setRoot(rootItem);
        sidebarComponent.getFileTreeView().setShowRoot(true);
        rootItem.setExpanded(true);

        if (Files.isDirectory(dirPath)) {
            syncChildren(rootItem, dirPath);
        }

        fileWatcherService.setupDirectoryWatcher(dirPath, (watchedDir, kind, relativePath) -> {
            Path fullChanged = relativePath != null ? watchedDir.resolve(relativePath) : null;
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                scheduleSync(pathToTreeItem.get(watchedDir), watchedDir);
                return;
            }
            fileWatcherService.invalidateCache(watchedDir);
            TreeItem<Path> parentItem = pathToTreeItem.get(watchedDir);
            if (parentItem != null) {
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (fullChanged != null) {
                        TreeItem<Path> removed = pathToTreeItem.remove(fullChanged);
                        if (removed != null && removed.getParent() != null) {
                            Platform.runLater(() -> parentItem.getChildren().remove(removed));
                        }
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    scheduleSync(parentItem, watchedDir);
                }
            }
        });
    }

    private TreeItem<Path> createTreeItem(Path path) {
        if (pathToTreeItem.containsKey(path)) {
            return pathToTreeItem.get(path);
        }
        TreeItem<Path> item = new TreeItem<>(path);
        pathToTreeItem.put(path, item);

        if (Files.isDirectory(path)) {
            item.setExpanded(false);
            fileWatcherService.hasChildrenAsync(path, hasChildren -> {
                if (hasChildren) {
                    item.getChildren().add(new TreeItem<>());
                    item.expandedProperty()
                            .addListener((obs, oldVal, newVal) -> {
                        if (newVal && item.getChildren().size() == 1 && item.getChildren().getFirst().getValue() == null) {
                            syncChildren(item, path);
                        }
                    });
                }
            });
        }
        return item;
    }

    private void syncChildren(TreeItem<Path> parentItem, Path dirPath) {
        if (parentItem.getChildren().isEmpty()) {
            TreeItem<Path> loading = new TreeItem<>(Path.of("Loading..."));
            parentItem.getChildren().setAll(List.of(loading));
        }
        fileWatcherService.loadDirectoryAsync(
                dirPath,
                (sortedPaths) -> {
                    Map<Path, TreeItem<Path>> existing = new HashMap<>();
                    for (TreeItem<Path> child : parentItem.getChildren()) {
                        Path val = child.getValue();
                        if (val != null) existing.put(val, child);
                    }
                    List<TreeItem<Path>> updatedChildren = new ArrayList<>();
                    for (Path entry : sortedPaths) {
                        if (existing.containsKey(entry)) {
                            updatedChildren.add(existing.get(entry));
                        } else {
                            TreeItem<Path> child = createTreeItem(entry);
                            updatedChildren.add(child);
                            if (Files.isDirectory(entry)) {
                                try {
                                    fileWatcherService.registerDirectory(entry, (watchedDir, kind, relative) -> {
                                        Path full = relative != null ? watchedDir.resolve(relative) : null;
                                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                                            scheduleSync(pathToTreeItem.get(watchedDir), watchedDir);
                                            return;
                                        }
                                        TreeItem<Path> parentOfSub = pathToTreeItem.get(watchedDir);
                                        fileWatcherService.invalidateCache(watchedDir);
                                        if (parentOfSub != null) {
                                            if (kind == StandardWatchEventKinds.ENTRY_DELETE && full != null) {
                                                TreeItem<Path> removed = pathToTreeItem.remove(full);
                                                if (removed != null && removed.getParent() != null) {
                                                    Platform.runLater(() -> parentOfSub.getChildren().remove(removed));
                                                }
                                            } else if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                                scheduleSync(parentOfSub, watchedDir);
                                            }
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    Platform.runLater(() -> parentItem.getChildren().setAll(updatedChildren));
                    return null;
                },
                (e) -> {
                    ctx.getUiStateModel().setStatusMessage("Failed load folder: " + e.getMessage());
                    return null;
                }
        );
    }

    private void scheduleSync(TreeItem<Path> item, Path path) {
        if (item == null) return;
        Runnable task = () -> syncChildren(item, path);
        pendingSyncs.put(path, task);
        scheduler.schedule(() -> {
            Runnable current = pendingSyncs.get(path);
            if (current == task) {
                current.run();
                pendingSyncs.remove(path);
            }
        }, SYNC_DELAY_MS, TimeUnit.MILLISECONDS);
    }
}