package dev.sch.simpletexteditor.controller.page;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.controller.component.EditorController;
import dev.sch.simpletexteditor.controller.component.StatusBarController;
import dev.sch.simpletexteditor.controller.component.ToolbarController;
import dev.sch.simpletexteditor.service.EditorFileService;
import dev.sch.simpletexteditor.ui.view.HomeView;
import dev.sch.simpletexteditor.util.DialogUtil;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class HomeController implements IController<HomeView> {
    private final AppContext ctx;
    private final HomeView view;
    private final ToolbarController toolbarController;
    private final EditorController editorController;
    private final StatusBarController statusBarController;

    private Label fileNameLabel;
    private final EditorFileService fileService;

    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;

    public HomeController(AppContext ctx){
    this.toolbarController = new ToolbarController(ctx);
    this.statusBarController = new StatusBarController(ctx);
    this.editorController = new EditorController(ctx);
    this.ctx = ctx;
    this.fileService = new EditorFileService(ctx.getEditorModel(), ctx.getUiStateModel());

//    create nodes
    createNodes();

    this.statusBarController.getView().getChildren().addFirst(new Separator(Orientation.VERTICAL));
    this.statusBarController.getView().getChildren().addFirst(fileNameLabel);
    this.view = new HomeView(
            this.toolbarController.getView(),
            this.editorController.getView(),
            this.statusBarController.getView()
    );

    toolbarController.setOnNewFileRequested(this::handleNewFile);
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

        //    make newfile when app started
        this.ctx.getEditorModel().newFile();

        bindNodes();
        setupFileChoosers();
    }

    private void createNodes(){
        fileNameLabel = new Label();
    }

    private void bindNodes(){
        fileNameLabel.textProperty().bind(
                Bindings.when(ctx.getEditorModel().fileModifiedProperty())
                        .then(ctx.getEditorModel().getCurrentFileName()+"*")
                        .otherwise(ctx.getEditorModel().getCurrentFileName())
        );
    }

    private void handleNewFile(){
        if (ctx.getEditorModel().fileModifiedProperty().get()){
            DialogUtil.showSaveConfirmationDialog(
                    ctx.getEditorModel().getCurrentFileName(),
                    ()->{
                        System.out.println("on save and continue");
                        saveCurrentFile();
                        ctx.getEditorModel().newFile();
                        PauseTransition delay = new PauseTransition(Duration.seconds(2));
                        delay.setOnFinished((e)->{
                            ctx.getUiStateModel().setStatusMessage("Ready");
                        });
                        delay.play();
                    },
                    ()->{
                        System.out.println("on discard");
                        ctx.getEditorModel().newFile();
                        ctx.getUiStateModel().setStatusMessage("Ready");
                    }
            );
        }
    }

    public void saveCurrentFile() {
        Path current = ctx.getEditorModel().getCurrentFilePath();
        if (current == null
        || ctx.getEditorModel().getCurrentDirectory() == null) {
            saveAs();
            return;
        }
        System.out.println("currentFilePath != null");
        fileService.saveFile(current, ctx.getEditorModel().getEditorContent(),
                () -> {
                    System.out.println("success savefile");
                },
                err -> Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Gagal menyimpan: " + err.getMessage()).showAndWait();
                })
        );
    }

    public void saveAs() {
        if (ctx.getEditorModel().getCurrentDirectory() != null){
            System.out.println("getCurrentDirectory");
            fileChooser.setInitialDirectory(ctx.getEditorModel().getCurrentDirectory().toFile());
        }else if (ctx.getEditorModel().getCurrentFilePath() != null){
            System.out.println("getCurrentFilePath");
            fileChooser.setInitialDirectory(ctx.getEditorModel().getCurrentFilePath().getParent().toFile());
            fileChooser.setInitialFileName(ctx.getEditorModel().getCurrentFilePath().getFileName().toString());
        }else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName(ctx.getEditorModel().getCurrentFileName());
        }

        File file = fileChooser.showSaveDialog(ctx.getEditorTextArea().getScene().getWindow());

        if (file != null){
            System.out.println("file != null");
            fileService.saveFile(
                    file.toPath(),
                    ctx.getEditorModel().editorContentProperty().get(),
                    () -> Platform.runLater(() -> ctx.getEditorModel().setCurrentFilePath(file.toPath())),
                    (err) -> Platform.runLater(() -> {
                        new Alert(Alert.AlertType.ERROR, "Gagal Save As: " + err.getMessage()).showAndWait();
                    })
            );
        }
    }

    private void setupFileChoosers() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Pilih Folder");

        fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
    }
}
