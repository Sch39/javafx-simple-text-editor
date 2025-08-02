package dev.sch.simpletexteditor.controller.page;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.controller.IController;
import dev.sch.simpletexteditor.controller.component.EditorController;
import dev.sch.simpletexteditor.controller.component.StatusBarController;
import dev.sch.simpletexteditor.controller.component.ToolbarController;
import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.service.EditorFileService;
import dev.sch.simpletexteditor.ui.view.HomeView;
import dev.sch.simpletexteditor.util.SettingsStore;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

public class HomeController implements IController<HomeView> {
    private final AppContext ctx;
    private final HomeView view;
    private final ToolbarController toolbarController;
    private final EditorController editorController;
    private final StatusBarController statusBarController;

    private Label fileNameLabel;
    private final EditorFileService editorFileService;

    private FileChooser fileChooser;
    private DirectoryChooser directoryChooser;

    private final Path lastDirFallback;
    private final EditorModel editorModel;
    private final ObservableSettings observableSettings;

    public HomeController(AppContext ctx){
    this.toolbarController = new ToolbarController(ctx);
    this.statusBarController = new StatusBarController(ctx);
    this.editorController = new EditorController(ctx);
    this.ctx = ctx;
    this.editorModel = ctx.getEditorModel();
    this.editorFileService = ctx.getEditorFileService();
    this.lastDirFallback = Path.of(System.getProperty("user.home"));
    this.observableSettings = ctx.getObservableSettings();

//    create nodes
    createNodes();

    this.statusBarController.getView().getChildren().addFirst(new Separator(Orientation.VERTICAL));
    this.statusBarController.getView().getChildren().addFirst(fileNameLabel);
    this.view = new HomeView(
            this.toolbarController.getView(),
            this.editorController.getView(),
            this.statusBarController.getView()
    );

        toolbarController.setOnSaveFileRequested(this::handleSaveFile);
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
        this.editorModel.newFile();

        bindNodes();
        setupFileChoosers();
    }

    private void createNodes(){
        fileNameLabel = new Label();
    }

    private void bindNodes(){
        fileNameLabel.textProperty().bind(
                Bindings.when(editorModel.fileModifiedProperty())
                        .then(editorModel.currentFileNameProperty().concat("*"))
                        .otherwise(editorModel.currentFileNameProperty())
        );
    }

    private void handleSaveFile(){
        Path currentFilePath = editorModel.getCurrentFilePath();
        if (currentFilePath == null){
            handleSaveAsFile();
            return;
        }
        editorFileService.createSaveFileService(
                currentFilePath,
                editorModel.getEditorContent(),
                ()->{
                    System.out.println("Succesfully save file to: "+currentFilePath.getParent().toString()+", name:"+editorModel.getCurrentFileName());
                },
                (err)->{
                    new Alert(Alert.AlertType.ERROR, "Gagal menyimpan: " + err.getMessage()).showAndWait();
                }
        ).start();
    }

    public void handleSaveAsFile(){
        Path lastDir = observableSettings.getLastDirectory();
        if (lastDir != null && lastDir.toFile().exists()){
            fileChooser.setInitialDirectory(lastDir.toFile());
        }else {
            fileChooser.setInitialDirectory(new File(lastDirFallback.toString()));
        }
            fileChooser.setInitialFileName(editorModel.getCurrentFileName());
        File file = fileChooser.showSaveDialog(ctx.getEditorTextArea().getScene().getWindow());
        if (file != null){
            Path newFilePath = file.toPath();
            editorFileService.createSaveFileService(
                    newFilePath,
                    editorModel.getEditorContent(),
                    ()->{
                        System.out.println("Successfully saved file as: " + newFilePath.toString()+", name: "+editorModel.getCurrentFileName());
                        observableSettings.setLastDirectory(newFilePath.getParent());
//                        editorModel.set
                    },
                    (err)->{
                        new Alert(Alert.AlertType.ERROR, "Gagal Save As: " + err.getMessage()).showAndWait();
                    }
            ).start();
        }else {
            System.out.println("Cancelling save");
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
