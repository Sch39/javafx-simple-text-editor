package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.service.EditorFileWatcherService;
import dev.sch.simpletexteditor.ui.components.SidebarComponent;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SidebarController implements IController<SidebarComponent> {
    private final AppContext ctx;
    private final SidebarComponent sidebarComponent;
    private final ObservableSettings observableSettings;
    private final EditorFileWatcherService fileWatcherService;

    public SidebarController(AppContext ctx){
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
                .addListener((obs, oldDir, newDir)->{
                    if (newDir != null){
                        loadDirectory(newDir);
                    }else {
                        sidebarComponent.getFileTreeView().setRoot(null);
                    }
                });

        if (observableSettings.getLastDirectory() != null){
            loadDirectory(observableSettings.getLastDirectory());
        }
    }

    private void loadDirectory(Path dirPath){
        TreeItem<File> rootItem = new TreeItem<>(dirPath.getFileName().toFile());
        sidebarComponent.getFileTreeView().setRoot(rootItem);
        sidebarComponent.getFileTreeView().setShowRoot(true);
        rootItem.setExpanded(true);

        populateTreeItem(rootItem, dirPath);
        fileWatcherService.setupDirectoryWatcher(
                dirPath,
                ()->{
                    loadDirectory(dirPath);
                    System.out.println("Directory reloaded due to change.");
                }
        );
    }

    private TreeItem<File> createTreeItem(Path path){
        TreeItem<File> item = new TreeItem<>(path.getFileName().toFile());

        if (Files.isDirectory(path)){
            item.setExpanded(false);
            item.getChildren().add(new TreeItem<>());
            item.expandedProperty()
                    .addListener((obs, oldVal, newVal)->{
                        if (newVal && item.getChildren().size() == 1 && item.getChildren().getFirst().getValue() == null){
                           populateTreeItem(item, path);
                        }
                    });
        }
        return item;
    }

    private void populateTreeItem(TreeItem<File> parentItem, Path dirPath) {
        TreeItem<File> loadingItem = new TreeItem<>(new File("Loading..."));
        parentItem.getChildren().setAll(loadingItem);

        fileWatcherService.loadDirectoryAsync(
                dirPath,
                (sortedPaths) -> {
                    parentItem.getChildren().clear();
                    for (Path entry : sortedPaths) {
                        parentItem.getChildren().add(createTreeItem(entry));

                        if (Files.isDirectory(entry)){
                            try {
                                fileWatcherService.registerDirectory(entry);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    return null;
                },
                (e) -> {
                    ctx.getUiStateModel().setStatusMessage("Failed load folder: " + e.getMessage());
                    return null;
                }
        );
    }
}
