package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.EditorComponent;

public class EditorController implements IController<EditorComponent> {
    private final AppContext ctx;
    private final EditorComponent editorComponent;

    public EditorController(AppContext ctx){
        this.ctx = ctx;
        this.editorComponent = new EditorComponent(ctx.getEditorTextArea());
    }

    @Override
    public EditorComponent getView() {
        return editorComponent;
    }

    @Override
    public void initialize() {
        editorComponent.getEditorTextArea().textProperty().bindBidirectional(ctx.getModel().editorContentProperty());
    }
}
