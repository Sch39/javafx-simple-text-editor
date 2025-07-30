package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.model.TextEditorModel;
import javafx.scene.control.TextArea;


public class ToolbarController implements IController<ToolbarComponent> {
    private final TextEditorModel model;
    private final TextArea editorTextArea;
    private final ToolbarComponent toolbar;

    public ToolbarController(AppContext ctx){
        model = ctx.getModel();
        editorTextArea = ctx.getEditorTextArea();
        toolbar = new ToolbarComponent();
    }

    @Override
    public ToolbarComponent getView() {
        return toolbar;
    }

    @Override
    public void initialize() {
        bindButtons();
        setupActions();
    }

    private void bindButtons() {
        toolbar.getUndoButton().disableProperty().bind(editorTextArea.undoableProperty().not());
        toolbar.getRedoButton().disableProperty().bind(editorTextArea.redoableProperty().not());
        toolbar.getSaveButton().disableProperty().bind(model.fileModifiedProperty().not());
    }

    private void setupActions() {
        toolbar.getUndoButton().setOnAction(e -> editorTextArea.undo());
        toolbar.getRedoButton().setOnAction(e -> editorTextArea.redo());

        toolbar.getNewFileButton().setOnAction(e -> {
            if (model.fileModifiedProperty().get()) {
                System.out.println("saving file");
                // TODO: Prompt user to save before clearing
            } else {
                model.newFile();
                editorTextArea.clear();
            }
        });

//        toolbar.getSaveButton().setOnAction(e -> {
//            model.saveFile(editorTextArea.getText());
//        });
//
//        toolbar.getSaveAsButton().setOnAction(e -> {
//            model(editorTextArea.getText());
//        });
//
//        toolbar.getOpenFolderButton().setOnAction(e -> {
//            model.openFile(content -> editorTextArea.setText(content));
//        });
    }
}
