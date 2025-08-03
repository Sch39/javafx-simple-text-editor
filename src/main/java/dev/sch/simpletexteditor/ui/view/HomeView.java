package dev.sch.simpletexteditor.ui.view;

import dev.sch.simpletexteditor.ui.components.EditorComponent;
import dev.sch.simpletexteditor.ui.components.SidebarComponent;
import dev.sch.simpletexteditor.ui.components.StatusBarComponent;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import javafx.scene.layout.BorderPane;

public class HomeView extends BorderPane {
    public HomeView(ToolbarComponent toolbarComponent, EditorComponent editorComponent, SidebarComponent sidebarComponent, StatusBarComponent statusBarComponent){
        this.setTop(toolbarComponent);
        this.setCenter(editorComponent);
        this.setLeft(sidebarComponent);
        this.setBottom(statusBarComponent);
    }
}
