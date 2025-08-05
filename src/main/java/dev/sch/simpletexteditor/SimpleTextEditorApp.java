package dev.sch.simpletexteditor;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.core.Router;
import dev.sch.simpletexteditor.router.Routes;
import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.service.ServiceManager;
import dev.sch.simpletexteditor.util.SettingsStore;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SimpleTextEditorApp extends Application {
//    model
    private EditorModel model = new EditorModel();

//    ui
    private TextArea editorTextArea;
    private Label fileNameLabel;
    private Label statusLabel;
    private ProgressBar progressBar;
    private ListView<File> fileListView;
    private Button saveButton;
    private Button newFileButton;
    private Button openFolderButton;
    private Button undoButton;
    private Button redoButton;

//    file choosers
    private DirectoryChooser directoryChooser;
    private FileChooser fileChooser;
    private AppContext appContext;

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        BorderPane root = new BorderPane();
        appContext = new AppContext();
        Router router = new Router(root, appContext);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle(appContext.getSettingsStore().get(SettingsStore.Keys.APP_NAME, "Simple Text Editor"));
        stage.setScene(scene);
        stage.show();

        router.navigate(Routes.HOME);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop(){
        ServiceManager.shutdownAll();

        Thread.getAllStackTraces().keySet().forEach(t -> {
            System.out.println(t.getName() + " - daemon: " + t.isDaemon());
        });
    }
}