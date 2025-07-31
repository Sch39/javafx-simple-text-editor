package dev.sch.simpletexteditor.context;

import dev.sch.simpletexteditor.model.ProgressModel;
import dev.sch.simpletexteditor.model.EditorModel;
import dev.sch.simpletexteditor.model.UIStateModel;
import javafx.scene.control.TextArea;
import lombok.Getter;

@Getter
public class AppContext {
    private final EditorModel editorModel;
    private final TextArea editorTextArea;
    private final ProgressModel progressModel;
    private final UIStateModel uiStateModel;

    public AppContext(){
        this.editorModel = new EditorModel();
        this.progressModel = new ProgressModel();

        this.editorTextArea = new TextArea();
        this.editorTextArea.textProperty()
                .bindBidirectional(this.editorModel.editorContentProperty());

        this.uiStateModel = new UIStateModel();
    }
}
