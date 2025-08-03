package dev.sch.simpletexteditor.controller.component;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.ui.components.StatusBarComponent;
import dev.sch.simpletexteditor.context.AppContext;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

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
        statusBarComponent.getProgressBar().progressProperty().bind(ctx.getProgressModel().progressProperty());
        statusBarComponent.getProgressBar().visibleProperty().bind(ctx.getProgressModel().visibleProperty());

        ctx.getProgressModel().progressProperty().addListener((obs, oldVal, newVal) -> {
            if ((newVal.doubleValue() == 1.0 || newVal.doubleValue() == -1.0)) {
                PauseTransition delay = new PauseTransition(Duration.millis(500));
                delay.setOnFinished(event -> {
                    ctx.getProgressModel().visibleProperty().set(false);
                });
                delay.play();
            }
        });
    }
}
