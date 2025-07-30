package dev.sch.simpletexteditor.router;

import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.controller.page.HomeController;
import lombok.Getter;

@Getter
public enum Routes {
    HOME(HomeController.class);

    private final Class<? extends IController<?>> controllerClass;

    Routes(Class<? extends IController<?>> controllerClass){
        this.controllerClass = controllerClass;
    }

}
