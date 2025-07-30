package dev.sch.simpletexteditor.ui.view;

import dev.sch.simpletexteditor.ui.components.StatusBarComponent;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import javafx.scene.layout.BorderPane;

public class HomeView extends BorderPane {
    public HomeView(ToolbarComponent toolbarComponent, StatusBarComponent statusBarComponent){
        this.setTop(toolbarComponent);
        this.setBottom(statusBarComponent);
    }
}
