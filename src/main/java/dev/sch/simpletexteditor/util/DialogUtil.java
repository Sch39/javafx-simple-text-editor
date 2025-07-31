package dev.sch.simpletexteditor.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class DialogUtil {
    public static void showSaveConfirmationDialog(String fileName,
                                                  Runnable onSaveAndContinueCallback,
                                                  Runnable onDiscardCallback){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Changes not saved!");
        alert.setHeaderText("There are unsaved changes in '"+fileName+"'.");
        alert.setContentText("Do you want to save changes?");

        ButtonType buttonTypeSave = new ButtonType("Simpan");
        ButtonType buttonTypeDiscard = new ButtonType("Buang");
        ButtonType buttonTypeCancel = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeSave, buttonTypeDiscard, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()){
            if (result.get() == buttonTypeSave
                    && onSaveAndContinueCallback != null){
                onSaveAndContinueCallback.run();
            } else if (result.get() == buttonTypeDiscard
                    && onDiscardCallback != null) {
                onDiscardCallback.run();
            }
        }
    }
}
