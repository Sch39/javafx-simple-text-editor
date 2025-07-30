package dev.sch.simpletexteditor.context;

import dev.sch.simpletexteditor.model.TextEditorModel;
import javafx.scene.control.TextArea;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AppContext {
    private final TextEditorModel model = new TextEditorModel();
    private final TextArea editorTextArea = new TextArea();

}
