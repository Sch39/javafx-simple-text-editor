package dev.sch.simpletexteditor;

import dev.sch.simpletexteditor.context.AppContext;
import dev.sch.simpletexteditor.core.Router;
import dev.sch.simpletexteditor.router.Routes;
import dev.sch.simpletexteditor.ui.components.ToolbarComponent;
import dev.sch.simpletexteditor.model.TextEditorModel;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class SimpleTextEditorApp extends Application {
//    model
    private TextEditorModel model = new TextEditorModel();

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

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
//        editorTextArea = new TextArea();
//        editorTextArea.setWrapText(true);
//
////        bind textarea to model
//        editorTextArea.textProperty().bindBidirectional(model.editorContentProperty());
//
//        fileNameLabel = new Label();
//        fileNameLabel.textProperty().bind(
//                Bindings.when(model.fileModifiedProperty())
//                        .then(model.currentFileNameProperty()+" *")
//                        .otherwise(model.currentFileNameProperty())
//        );
//        fileNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
//
//        progressBar = new ProgressBar();
//        progressBar.setPrefWidth(200);
//        progressBar.setVisible(false);
//
////        toolbar
//        HBox toolbar = new ToolbarComponent();

        BorderPane root = new BorderPane();
        AppContext appContext = new AppContext();
        Router router = new Router(root, appContext);

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Simple Text Editor");
        stage.setScene(scene);
        stage.show();

        router.navigate(Routes.HOME);
    }

    public static void main(String[] args) {
        launch();
    }
}