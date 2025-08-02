package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import dev.sch.simpletexteditor.context.AppContext;
import lombok.Setter;


public class ToolbarController implements IController<ToolbarComponent> {
    private final AppContext ctx;
    private final ToolbarComponent toolbar;

    @Setter
    private Runnable onNewFileRequested;

    @Setter
    private Runnable onSaveFileRequested;

    public ToolbarController(AppContext ctx){
        this.ctx = ctx;
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
        toolbar.getSaveButton().disableProperty().bind(ctx.getEditorModel().fileModifiedProperty().not());

        toolbar.getUndoButton().disableProperty().bind(ctx.getEditorTextArea().undoableProperty().not());
        toolbar.getRedoButton().disableProperty().bind(ctx.getEditorTextArea().redoableProperty().not());
    }

    private void setupActions() {
        toolbar.getUndoButton().setOnAction(e -> ctx.getEditorTextArea().undo());
        toolbar.getRedoButton().setOnAction(e -> ctx.getEditorTextArea().redo());


        toolbar.getNewFileButton().setOnAction(e -> {
            if (onNewFileRequested != null) {
                onNewFileRequested.run();
            }
        });

        toolbar.getSaveButton().setOnAction(e->{
            if (onSaveFileRequested != null){
                onSaveFileRequested.run();
            }
        });
    }

}
