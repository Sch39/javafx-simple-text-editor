package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.ui.components.SidebarComponent;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SidebarController implements IController<SidebarComponent> {
    private final AppContext ctx;
    private final SidebarComponent sidebarComponent;
    private final ObservableSettings observableSettings;

    public SidebarController(AppContext ctx){
        this.ctx = ctx;
        this.sidebarComponent = new SidebarComponent();
        this.observableSettings = ctx.getObservableSettings();
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

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)){
            for (Path entry:stream){
                TreeItem<File> item = createTreeItem(entry);
                rootItem.getChildren().add(item);
            }
        }catch (IOException e){
            e.printStackTrace();
            ctx.getUiStateModel().setStatusMessage("Failed load folder: "+e.getMessage());
        }
    }

    private TreeItem<File> createTreeItem(Path path){
        TreeItem<File> item = new TreeItem<>(path.getFileName().toFile());

        if (Files.isDirectory(path)){
            item.setExpanded(false);
            item.getChildren().add(new TreeItem<>());
            item.expandedProperty()
                    .addListener((obs, oldVal, newVal)->{
                        if (newVal
                                && item.getChildren().getFirst() instanceof TreeItem<File>
                                && item.getChildren().getFirst().getValue() == null){
                            item.getChildren().removeFirst();
                            try(DirectoryStream<Path> stream = Files.newDirectoryStream(path)){
                                for (Path subEntry : stream){
                                    item.getChildren()
                                            .add(createTreeItem(subEntry));
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                                ctx.getUiStateModel().setStatusMessage("Failed load sub-folder: "+e.getMessage());
                            }
                        }
                    });
        }
        return item;
    }
}
