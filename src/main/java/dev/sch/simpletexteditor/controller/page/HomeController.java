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
import dev.sch.simpletexteditor.util.DialogUtil;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        toolbarController.setOnNewFileRequested(this::handleNewFile);
        toolbarController.setOnSaveFileRequested(this::handleSaveFile);
        toolbarController.setOnSaveAsFileRequested(this::handleSaveAsFile);
        toolbarController.setOnOpenFolderRequested(this::handleOpenFolder);

        //        when view added to scene
        viewAddedToSceneProperty();
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

    private void viewAddedToSceneProperty(){
        view.sceneProperty()
                .addListener((obs, oldScene, newScene)->{
                    if (newScene != null){
                        System.out.println("viewAddedToSceneProperty");
                        setupShortcuts();
                    }
                });
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

    private void handleNewFile(){
        if (!editorModel.isFileModified()){
            editorModel.newFile();
            return;
        }

        DialogUtil.showSaveConfirmationDialog(
                editorModel.getCurrentFileName(),
                () -> {
                    handleSaveFile(() -> {
                        editorModel.newFile();
                        System.out.println("File saved, opening new file.");
                    });
                },
                () -> {
                    editorModel.newFile();
                    System.out.println("Changes discarded, opening new file.");
                }
        );
    }

    private void handleSaveFile(){
        handleSaveFile(null);
    }

    private void handleSaveFile(Runnable onSuccessCallback){
        Path currentFilePath = editorModel.getCurrentFilePath();
        if (currentFilePath == null){
            handleSaveAsFile(onSuccessCallback);
            return;
        }
        editorFileService.createSaveFileService(
                currentFilePath,
                editorModel.getEditorContent(),
                ()->{
                    System.out.println("Succesfully save file to: "+currentFilePath.getParent().toString()+", name:"+editorModel.getCurrentFileName());
                    resetStatusAfterDelay("Ready", 1);

                    if (onSuccessCallback != null){
                        onSuccessCallback.run();
                    }
                },
                (err)->{
                    new Alert(Alert.AlertType.ERROR, "Gagal menyimpan: " + err.getMessage()).showAndWait();
                    resetStatusAfterDelay("Ready", 1);

                }
        ).start();
    }

    private void handleSaveAsFile(){
        handleSaveAsFile(null);
    }

    private void handleSaveAsFile(Runnable onSuccessCallback){
        Path lastDir = observableSettings.getLastDirectory();
        if (lastDir != null && lastDir.toFile().exists()){
            fileChooser.setInitialDirectory(lastDir.toFile());
        }else {
            fileChooser.setInitialDirectory(new File(lastDirFallback.toString()));
        }

        String uniqueFileName = getUniqueFileName(lastDir, editorModel.getCurrentFileName());
        fileChooser.setInitialFileName(uniqueFileName);
        File file = fileChooser.showSaveDialog(ctx.getEditorTextArea().getScene().getWindow());
        if (file != null){
            Path newFilePath = file.toPath();
            editorFileService.createSaveFileService(
                    newFilePath,
                    editorModel.getEditorContent(),
                    ()->{
                        System.out.println("Successfully saved file as: " + newFilePath.toString()+", name: "+editorModel.getCurrentFileName());
                        observableSettings.setLastDirectory(newFilePath.getParent());
                        resetStatusAfterDelay("Ready", 1);
                        if (onSuccessCallback != null){
                            onSuccessCallback.run();
                        }
                    },
                    (err)->{
                        new Alert(Alert.AlertType.ERROR, "Gagal Save As: " + err.getMessage()).showAndWait();
                        resetStatusAfterDelay("Ready", 1);

                    }
            ).start();
        }else {
            System.out.println("Cancelling save");
        }
    }

    private void handleOpenFolder(){
        Path lastDir = observableSettings.getLastDirectory();
        if (lastDir != null && lastDir.toFile().exists()){
            directoryChooser.setInitialDirectory(lastDir.toFile());
        }else {
            directoryChooser.setInitialDirectory(new File(lastDirFallback.toString()));
        }
        File selectedDir = directoryChooser.showDialog(ctx.getEditorTextArea().getScene().getWindow());
        if (selectedDir != null){
            observableSettings.setLastDirectory(selectedDir.toPath());
            System.out.println("Success set folder");
        }
    }

    private void setupShortcuts(){
        view.getScene()
                        .getAccelerators()
                                .put(
                                        new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                                        this::handleNewFile
                                );

        view.getScene()
                .getAccelerators()
                .put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                        ()->{
                            if (!editorModel.isFileModified()){
                                return;
                            }
                            handleSaveFile();
                        }
                );

        view.getScene()
                .getAccelerators()
                .put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                        ()->{
                            if (!editorModel.isFileModified()){
                                return;
                            }
                            handleSaveAsFile();
                        }
                );

        view.getScene()
                .getAccelerators()
                .put(
                new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                () -> {
                    ctx.getEditorTextArea().undo();
                }
        );

        view.getScene()
                .getAccelerators()
                .put(
                new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
                () -> {
                    ctx.getEditorTextArea().redo();
                }
        );

        view.getScene()
                .getAccelerators()
                .put(
                        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                        this::handleOpenFolder // Method reference ke handleOpenFolder
                );
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

    private String getUniqueFileName(Path directory, String originalFileName) {
        if (directory == null || !Files.exists(directory)) {
            System.out.println("directory == null || !Files.exists(directory)");
            return originalFileName;
        }

        Path fullPath = directory.resolve(originalFileName);
        if (Files.notExists(fullPath)) {
            System.out.println("Files.notExists(fullPath)");
            return originalFileName;
        }

        String nameWithoutExtension = "";
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            nameWithoutExtension = originalFileName.substring(0, dotIndex);
            extension = originalFileName.substring(dotIndex);
        } else {
            nameWithoutExtension = originalFileName;
        }

        Pattern pattern = Pattern.compile("(.+)\\s*\\((\\d+)\\)$");
        Matcher matcher = pattern.matcher(nameWithoutExtension);

        if (matcher.matches()) {
            nameWithoutExtension = matcher.group(1).trim();
        }

        int increment = 1;
        while (true) {
            String newFileName = String.format("%s (%d)%s", nameWithoutExtension, increment, extension);
            fullPath = directory.resolve(newFileName);
            if (Files.notExists(fullPath)) {
                return newFileName;
            }
            increment++;
        }
    }

    private void resetStatusAfterDelay(String status, int seconds){
        PauseTransition pause= new PauseTransition(Duration.seconds(seconds));

        pause.setOnFinished(e->ctx.getUiStateModel().setStatusMessage(status));
        pause.play();
    }

}
