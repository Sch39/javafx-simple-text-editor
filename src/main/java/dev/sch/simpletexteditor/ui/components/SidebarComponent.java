package dev.sch.simpletexteditor.ui.components;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import javafx.geometry.Insets;
import javafx.scene.control.TreeView;
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

        this.getStyleClass().add("sidebar-component");
        this.setPadding(new Insets(10));
        this.getChildren().add(this.fileTreeView);

        this.getStylesheets()
                .add(Objects.requireNonNull(SimpleTextEditorApp.class.getResource("styles/component/sidebar-component.css")).toExternalForm());
    }
}
