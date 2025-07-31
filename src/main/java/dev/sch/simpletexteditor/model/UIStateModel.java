package dev.sch.simpletexteditor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UIStateModel {
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public StringProperty statusMessageProperty() { return statusMessage; }

    public void setStatusMessage(String statusMessage){
        this.statusMessage.set(statusMessage);
    }
}
