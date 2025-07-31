package dev.sch.simpletexteditor.model;

import javafx.beans.property.*;

public class ProgressModel {
    private final DoubleProperty progress = new SimpleDoubleProperty(-1); // -1 = indeterminate
    private final BooleanProperty visible = new SimpleBooleanProperty(false);
    private final StringProperty message = new SimpleStringProperty("");

    public DoubleProperty progressProperty() { return progress; }
    public BooleanProperty visibleProperty() { return visible; }
    public StringProperty messageProperty() { return message; }
}
