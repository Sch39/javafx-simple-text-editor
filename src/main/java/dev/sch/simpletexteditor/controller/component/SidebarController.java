package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.service.EditorFileWatcherService;
import dev.sch.simpletexteditor.ui.components.SidebarComponent;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;

public class SidebarController implements IController<SidebarComponent> {
    private final AppContext ctx;
    private final SidebarComponent sidebarComponent;
    private final ObservableSettings observableSettings;
    private final EditorFileWatcherService fileWatcherService;
    @Setter
    private Consumer<Path> onFileDoubleClick;


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

        sidebarComponent.getFileTreeView()
                .setOnMouseClicked(e->{
                    if (e.getClickCount() == 2){
                        TreeItem<Path> selectedItem = sidebarComponent.getFileTreeView().getSelectionModel().getSelectedItem();

                        if (selectedItem != null && Files.isRegularFile(selectedItem.getValue())) {
                            Path fileToOpen = selectedItem.getValue();

                            if (onFileDoubleClick != null) {
                                onFileDoubleClick.accept(fileToOpen);
                            }
                        }
                    }
                });
    }

    private void loadDirectory(Path dirPath){
        TreeItem<Path> rootItem = new TreeItem<>(dirPath);
        sidebarComponent.getFileTreeView().setRoot(rootItem);
        sidebarComponent.getFileTreeView().setShowRoot(true);
        rootItem.setExpanded(true);

        if (Files.isDirectory(dirPath)) {
            populateTreeItem(rootItem, dirPath);
        }

        fileWatcherService.setupDirectoryWatcher(
                dirPath,
                ()->{
                    Platform.runLater(()->{
                        loadDirectory(dirPath);
                        System.out.println("Directory reloaded due to change.");
                    });
                }
        );
    }

    private TreeItem<Path> createTreeItem(Path path){
        TreeItem<Path> item = new TreeItem<>(path);

        if (Files.isDirectory(path)){
            item.setExpanded(false);

            if (hasChildren(path)){
                item.getChildren().add(new TreeItem<>());
                item.expandedProperty()
                        .addListener((obs, oldVal, newVal)->{
                            if (newVal && item.getChildren().size() == 1 && item.getChildren().getFirst().getValue() == null){
                                populateTreeItem(item, path);
                            }
                        });
            }
        }
        return item;
    }

    private void populateTreeItem(TreeItem<Path> parentItem, Path dirPath) {
        TreeItem<Path> loadingItem = new TreeItem<>(Path.of("Loading..."));
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

    private boolean hasChildren(Path path) {
        if (!Files.isDirectory(path)) return false;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            Iterator<Path> it = ds.iterator();
            return it.hasNext();
        } catch (IOException e) {
            return false;
        }
    }
}
