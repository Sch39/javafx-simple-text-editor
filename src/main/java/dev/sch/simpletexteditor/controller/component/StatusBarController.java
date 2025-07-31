package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.StatusBarComponent;
import dev.sch.simpletexteditor.context.AppContext;

public class StatusBarController implements IController<StatusBarComponent> {
    private final AppContext ctx;
private final StatusBarComponent statusBarComponent;

    public StatusBarController(AppContext ctx){
        this.statusBarComponent = new StatusBarComponent("Ready", false);
        this.ctx = ctx;
    }


    @Override
    public StatusBarComponent getView() {
        return statusBarComponent;
    }

    @Override
    public void initialize() {
        statusBarComponent.getStatusLabel().textProperty().bind(ctx.getUiStateModel().statusMessageProperty());
    }
}
