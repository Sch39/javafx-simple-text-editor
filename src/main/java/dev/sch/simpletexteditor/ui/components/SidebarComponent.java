package dev.sch.simpletexteditor.ui.components;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import dev.sch.simpletexteditor.util.IconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Getter
public class SidebarComponent extends VBox {
    private final TreeView<Path> fileTreeView;

    public SidebarComponent(){
        this.fileTreeView = new TreeView<>();
        this.fileTreeView.setPrefWidth(200);
        this.fileTreeView.setShowRoot(false);

        this.fileTreeView.setCellFactory(tc -> new TreeCell<>() {
            private final StackPane arrowClickArea = new StackPane();
            private final HBox cellLayout = new HBox(4);
            private final ImageView arrowIcon = new ImageView();
            private final ImageView fileIcon = new ImageView();
            private final ImageView folderIcon = new ImageView();
            private final int arrowIconSize = 10;
            private final int folderIconSize = 16;
            private final int fileIconSize = 16;

            {
                arrowIcon.setFitWidth(arrowIconSize);
                arrowIcon.setFitHeight(arrowIconSize);
                fileIcon.setFitWidth(fileIconSize);
                fileIcon.setFitHeight(fileIconSize);
                folderIcon.setFitWidth(folderIconSize);
                folderIcon.setFitHeight(folderIconSize);

                arrowClickArea.getChildren().add(arrowIcon);
                arrowClickArea.setAlignment(Pos.CENTER);

                arrowClickArea.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (getTreeItem() != null && !getTreeItem().isLeaf()) {
                        getTreeItem().setExpanded(!getTreeItem().isExpanded());
                        event.consume();
                    }
                });

                cellLayout.getChildren().addAll(arrowClickArea, folderIcon, fileIcon);
                cellLayout.setPadding(new Insets(2, 0, 2, 0));
                HBox.setHgrow(cellLayout, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(item.getFileName() != null ? item.getFileName().toString() : item.toString());

                TreeItem<Path> treeItem = getTreeItem();
                boolean isDirectory = Files.isDirectory(item);

                // reset visibility first
                arrowClickArea.setVisible(false);
                folderIcon.setVisible(false);
                fileIcon.setVisible(false);
                arrowIcon.setImage(null);

                if (isDirectory) {
                    // always show folder icon for directories
                    folderIcon.setVisible(true);
                    folderIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("folder.png", folderIconSize)).getImage());

                    // show arrow only if expandable (i.e., has children placeholder or real)
                    if (treeItem != null && !treeItem.getChildren().isEmpty()) {
                        arrowClickArea.setVisible(true);
                        if (treeItem.isExpanded()) {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("bottom-arrow.png", arrowIconSize)).getImage());
                        } else {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("right-arrow.png", arrowIconSize)).getImage());
                        }
                    }
                } else {
                    // regular file
                    fileIcon.setVisible(true);
                    fileIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("file.png", fileIconSize)).getImage());
                }

                setGraphic(cellLayout);
            }
        });

        this.getStyleClass().add("sidebar-component");
        this.setPadding(new Insets(10));
        this.getChildren().add(this.fileTreeView);

        this.getStylesheets()
                .add(Objects.requireNonNull(SimpleTextEditorApp.class.getResource("styles/component/sidebar-component.css")).toExternalForm());


    }
}
