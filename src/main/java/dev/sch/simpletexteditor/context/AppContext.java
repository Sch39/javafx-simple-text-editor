package dev.sch.simpletexteditor.context;

import dev.sch.simpletexteditor.model.ObservableSettings;
import dev.sch.simpletexteditor.model.ProgressModel;
import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.UIStateModel;
import dev.sch.simpletexteditor.service.EditorFileService;
import dev.sch.simpletexteditor.util.SettingsStore;
import javafx.scene.control.TextArea;
import lombok.Getter;


@Getter
public class AppContext {
    private final EditorModel editorModel;
    private final TextArea editorTextArea;
    private final ProgressModel progressModel;
    private final UIStateModel uiStateModel;
    private final SettingsStore settingsStore;
    private final ObservableSettings observableSettings;
    private final EditorFileService editorFileService;

    public AppContext(){
        this.editorModel = new EditorModel();
        this.progressModel = new ProgressModel();

        this.editorTextArea = new TextArea();
        this.editorTextArea.textProperty()
                .bindBidirectional(this.editorModel.editorContentProperty());

        this.uiStateModel = new UIStateModel();
        this.settingsStore = new SettingsStore();

        this.observableSettings = new ObservableSettings(settingsStore);
        this.editorFileService = new EditorFileService(editorModel, uiStateModel, progressModel);
    }
}
