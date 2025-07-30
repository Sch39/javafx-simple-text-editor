package dev.sch.simpletexteditor.controller.page;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.controller.component.StatusBarController;
import dev.sch.simpletexteditor.controller.component.ToolbarController;
import dev.sch.simpletexteditor.ui.view.HomeView;

public class HomeController implements IController<HomeView> {
    private final HomeView view;
    private final ToolbarController toolbarController;
    private final StatusBarController statusBarController;

    public HomeController(AppContext ctx){
    this.toolbarController = new ToolbarController(ctx);
    this.statusBarController = new StatusBarController(ctx);

    this.view = new HomeView(
            toolbarController.getView(),
            statusBarController.getView()
    );
    }
    @Override
    public HomeView getView() {
        return view;
    }

    @Override
    public void initialize() {
        toolbarController.initialize();
        statusBarController.initialize();
    }
}
