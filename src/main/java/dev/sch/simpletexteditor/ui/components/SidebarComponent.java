package dev.sch.simpletexteditor.ui.components;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import dev.sch.simpletexteditor.util.IconLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Getter
public class SidebarComponent extends VBox {
    private final TreeView<Path> fileTreeView;
    @Setter
    private boolean isEditModeRequested = false;

    public SidebarComponent() {
        this.fileTreeView = new TreeView<>();
        this.fileTreeView.setPrefWidth(200);
        this.fileTreeView.setShowRoot(false);
        this.fileTreeView.setEditable(true);

        this.fileTreeView.setCellFactory(tc -> new TreeCell<>() {
            private TextField textField;
            private final StackPane arrowClickArea = new StackPane();
            private final StackPane iconWrapper = new StackPane();
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

                arrowClickArea.setPrefSize(arrowIconSize + 4, arrowIconSize + 4);
                arrowClickArea.getChildren().add(arrowIcon);
                arrowClickArea.setAlignment(Pos.CENTER);

                arrowClickArea.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    if (getTreeItem() != null && !getTreeItem().isLeaf()) {
                        getTreeItem().setExpanded(!getTreeItem().isExpanded());
                    }
                    event.consume();
                });

                iconWrapper.getChildren().addAll(folderIcon, fileIcon);
                iconWrapper.setAlignment(Pos.CENTER_LEFT);
                iconWrapper.setMinWidth(Math.max(folderIconSize, fileIconSize));
                iconWrapper.setPrefWidth(Math.max(folderIconSize, fileIconSize));

                cellLayout.getChildren().addAll(arrowClickArea, iconWrapper);
                cellLayout.setAlignment(Pos.CENTER_LEFT);
                cellLayout.setPadding(new Insets(1, 2, 1, 0));

                setContentDisplay(ContentDisplay.LEFT);
            }

            @Override
            public void startEdit() {
                TreeView<Path> treeView = getTreeView();
                if (treeView != null) {
                    VBox parent = (VBox) treeView.getParent();
                    if (parent instanceof SidebarComponent sidebarComponent) {
                        if (getTreeItem() != null && getTreeItem().getValue() != null && sidebarComponent.isEditModeRequested()) {
                            super.startEdit();
                            if (textField == null) {
                                createTextField();
                            }
                            setText(null);
                            setGraphic(textField);
                            textField.setText(getString());
                            textField.selectAll();
                            Platform.runLater(()->textField.requestFocus());
                            sidebarComponent.setEditModeRequested(false);
                        }
                    }
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
                setGraphic(cellLayout);
            }

            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(getString());
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(getString());
                        setGraphic(cellLayout);
                        setGraphicIcons(item);
                    }
                }
            }

            private void setGraphicIcons(Path item) {
                TreeItem<Path> treeItem = getTreeItem();
                boolean isDirectory = Files.isDirectory(item);
                arrowIcon.setVisible(false);
                folderIcon.setVisible(false);
                fileIcon.setVisible(false);
                arrowIcon.setImage(null);
                if (isDirectory) {
                    folderIcon.setVisible(true);
                    folderIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("folder.png", folderIconSize)).getImage());
                    if (treeItem != null && !treeItem.getChildren().isEmpty()) {
                        arrowIcon.setVisible(true);
                        if (treeItem.isExpanded()) {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("bottom-arrow.png", arrowIconSize)).getImage());
                        } else {
                            arrowIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("right-arrow.png", arrowIconSize)).getImage());
                        }
                    }
                } else {
                    fileIcon.setVisible(true);
                    fileIcon.setImage(Objects.requireNonNull(IconLoader.getIcon("file.png", fileIconSize)).getImage());
                }
            }

            private void createTextField() {
                textField = new TextField(getString());
                textField.setOnKeyReleased(event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        Path parent = getItem().getParent();
                        if (parent != null) {
                            commitEdit(parent.resolve(textField.getText()));
                        } else {
                            cancelEdit();
                        }
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                });
            }

            private String getString() {
                return getItem() == null ? "" : getItem().getFileName() != null ? getItem().getFileName().toString() : "";
            }
        });

        this.getStyleClass().add("sidebar-component");
        this.setPadding(new Insets(10));
        this.getChildren().add(this.fileTreeView);
        this.getStylesheets().add(Objects.requireNonNull(SimpleTextEditorApp.class.getResource("styles/component/sidebar-component.css")).toExternalForm());
    }
}