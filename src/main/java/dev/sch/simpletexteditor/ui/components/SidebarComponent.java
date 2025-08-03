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
import java.util.Objects;

@Getter
public class SidebarComponent extends VBox {
    private final TreeView<File> fileTreeView;

    public SidebarComponent(){
        this.fileTreeView = new TreeView<>();
        this.fileTreeView.setPrefWidth(200);
        this.fileTreeView.setShowRoot(false);

        this.fileTreeView.setCellFactory(tc -> new TreeCell<>() {
            private final StackPane arrowClickArea = new StackPane();
            private final HBox cellLayout = new HBox(2);
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
                    System.out.println("clicked: "+getItem().getName());
                    if (getTreeItem() != null && !getTreeItem().isLeaf()) {
                        getTreeItem().setExpanded(!getTreeItem().isExpanded());
                        event.consume();
                    }
                });

                cellLayout.getChildren().addAll(arrowClickArea, fileIcon, folderIcon);
                cellLayout.setPadding(new Insets(2, 0, 2, 0));
                HBox.setHgrow(cellLayout, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());

                    TreeItem<File> treeItem = getTreeItem();

                    if (treeItem != null && !treeItem.isLeaf()) {
                        arrowClickArea.setVisible(true);
                        folderIcon.setVisible(true);
                        fileIcon.setVisible(false);

                        if (treeItem.isExpanded()) {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("bottom-arrow.png", arrowIconSize)).getImage());
                        } else {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("right-arrow.png", arrowIconSize)).getImage());
                        }
                        folderIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("folder.png", folderIconSize)).getImage());
                    } else {
                        arrowClickArea.setVisible(false);
                        folderIcon.setVisible(false);
                        fileIcon.setVisible(true);
                        fileIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("file.png", fileIconSize)).getImage());
                    }

                    setGraphic(cellLayout);
                }
            }
        });

        this.getStyleClass().add("sidebar-component");
        this.setPadding(new Insets(10));
        this.getChildren().add(this.fileTreeView);

        this.getStylesheets()
                .add(Objects.requireNonNull(SimpleTextEditorApp.class.getResource("styles/component/sidebar-component.css")).toExternalForm());


    }
}
