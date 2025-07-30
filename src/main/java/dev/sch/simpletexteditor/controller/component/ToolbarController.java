package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import dev.sch.simpletexteditor.context.AppContext;


public class ToolbarController implements IController<ToolbarComponent> {
    private final AppContext ctx;
    private final ToolbarComponent toolbar;

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
        toolbar.getSaveButton().disableProperty().bind(ctx.getModel().fileModifiedProperty().not());

        toolbar.getUndoButton().disableProperty().bind(ctx.getEditorTextArea().undoableProperty().not());
        toolbar.getRedoButton().disableProperty().bind(ctx.getEditorTextArea().redoableProperty().not());
    }

    private void setupActions() {
        toolbar.getUndoButton().setOnAction(e -> ctx.getEditorTextArea().undo());
        toolbar.getRedoButton().setOnAction(e -> ctx.getEditorTextArea().redo());


        toolbar.getNewFileButton().setOnAction(e -> {
            if (ctx.getModel().fileModifiedProperty().get()) {
                System.out.println("saving file");
                // TODO: Prompt user to save before clearing
            } else {
                ctx.getModel().newFile();
//                ctx.getEditorTextArea().clear();
            }
        });

    }
}
