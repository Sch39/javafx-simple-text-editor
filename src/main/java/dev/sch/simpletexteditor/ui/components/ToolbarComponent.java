package dev.sch.simpletexteditor.ui.components;

import dev.sch.simpletexteditor.util.IconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;

@Getter
public class ToolbarComponent extends HBox{
    private final Button undoButton = createToolbarButton("undo.png", "Undo");
    private final Button redoButton = createToolbarButton("redo.png", "Redo");
    private final Button newFileButton = createToolbarButton("new-file.png", "New File");
    private final Button saveButton = createToolbarButton("save.png", "Save");
    private final Button saveAsButton = createToolbarButton("save-as.png", "Save As");
    private final Button openFolderButton = createToolbarButton("folder.png", "Open Folder");


    public ToolbarComponent(int space, int padding, String style, Pos alignment){
        super(space);
        this.setPadding(new Insets(padding));
        this.setStyle(style);
        this.setAlignment(alignment);

        this.getChildren().addAll(
                undoButton,
                redoButton,
                new Separator(),
                newFileButton,
                saveButton,
                saveAsButton,
                new Separator(),
                openFolderButton
        );
    }
    public ToolbarComponent(){
        this(10, 8, "-fx-background-color: #eee;", Pos.CENTER_LEFT);
    }

    private Button createToolbarButton(String iconFilename, String tooltipText){
        ImageView icon = IconLoader.getIcon(iconFilename, 24);
        Button button = new Button(null, icon);
        if (icon == null){
            button.setText(tooltipText);
        }
        Tooltip tooltip = new Tooltip(tooltipText);
        button.setTooltip(tooltip);
        return button;
    }
}
