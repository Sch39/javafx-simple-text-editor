package dev.sch.simpletexteditor.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.Getter;

@Getter
public class StatusBarComponent extends HBox {
    private final Label statusLabel;
    private final ProgressBar progressBar;

    public StatusBarComponent(String status, boolean isVisible, Insets padding, int spacing, Pos alignment, String style){
        this.statusLabel = new Label(status);
        this.progressBar = new ProgressBar(0);

        setupLayout(isVisible, padding, spacing, alignment, style);
    }

    public StatusBarComponent(String status, boolean isVisible){
        this(status, isVisible, new Insets(8), 10, Pos.CENTER_LEFT, "-fx-background-color: #f5f5f5; -fx-border-color: lightgray; -fx-border-width: 1 0 0 0;");
    }

    private void setupLayout(boolean isVisible, Insets padding, int spacing, Pos alignment, String style){
        this.setPadding(padding);
        this.setSpacing(spacing);
        this.setAlignment(alignment);
        this.setStyle(style);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        progressBar.setPrefWidth(150);
        progressBar.setVisible(isVisible);

        this.getChildren().addAll(statusLabel, progressBar);
    }

    public void setStatus(String status){
        statusLabel.setText(status);
    }

    public void showProgress(boolean show){
        progressBar.setVisible(show);
    }

}
