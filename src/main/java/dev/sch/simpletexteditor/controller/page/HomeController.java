package dev.sch.simpletexteditor.controller.page;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.controller.component.EditorController;
import dev.sch.simpletexteditor.controller.component.StatusBarController;
import dev.sch.simpletexteditor.controller.component.ToolbarController;
import dev.sch.simpletexteditor.ui.view.HomeView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;

public class HomeController implements IController<HomeView> {
    private final AppContext ctx;
    private final HomeView view;
    private final ToolbarController toolbarController;
    private final EditorController editorController;
    private final StatusBarController statusBarController;

    private Label fileNameLabel;

    public HomeController(AppContext ctx){
    this.toolbarController = new ToolbarController(ctx);
    this.statusBarController = new StatusBarController(ctx);
    this.editorController = new EditorController(ctx);
    this.ctx = ctx;

//    create nodes
        createNodes();


    this.statusBarController.getView().getChildren().addFirst(new Separator(Orientation.VERTICAL));
    this.statusBarController.getView().getChildren().addFirst(fileNameLabel);
    this.view = new HomeView(
            this.toolbarController.getView(),
            this.editorController.getView(),
            this.statusBarController.getView()
    );
    }
    @Override
    public HomeView getView() {
        return view;
    }

    @Override
    public void initialize() {
        toolbarController.initialize();
        editorController.initialize();
        statusBarController.initialize();

        bindNodes();
    }

    private void createNodes(){
        fileNameLabel = new Label();
    }

    private void bindNodes(){
        fileNameLabel.textProperty().bind(
                Bindings.when(ctx.getModel().fileModifiedProperty())
                        .then(ctx.getModel().currentFileNameProperty().get()+"*")
                        .otherwise(ctx.getModel().currentFileNameProperty().get())
        );
    }
}
