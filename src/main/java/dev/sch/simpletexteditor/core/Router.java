package dev.sch.simpletexteditor.core;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.router.Routes;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private final BorderPane rootPane;
    @Getter
    private final AppContext appContext;
    private final Map<Routes, IController<?>> controllerCache = new HashMap<>();

    public Router(BorderPane rootPane, AppContext appContext) {
        this.rootPane = rootPane;
        this.appContext = appContext;
    }

    public void navigate(Routes route) {
        IController<?> controller = getOrCreateController(route);
        controller.initialize();
        rootPane.setCenter(controller.getView());
    }

    public IController<?> getOrCreateController(Routes route) {
        return controllerCache.computeIfAbsent(route, r -> {
            try {
                Constructor<?> constructor = r.getControllerClass().getConstructor(AppContext.class);
                return (IController<?>) constructor.newInstance(appContext);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate controller: " + r.name(), e);
            }
        });
    }
}
