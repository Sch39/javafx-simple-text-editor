package dev.sch.simpletexteditor;

import dev.sch.simpletexteditor.util.IconLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

public class FileListCell extends ListCell<File> {
    private HBox content;
    private ImageView iconView;
    private Label fileName;
    private Label fileExtension;

    public FileListCell(){
        super();
        iconView = new ImageView();
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);

        fileName = new Label();
        fileName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        fileExtension = new Label();
        fileExtension.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        VBox textContainer = new VBox(fileName, fileExtension);
        textContainer.setSpacing(2);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        content = new HBox(5, iconView, textContainer);
        content.setPadding(new Insets(5));
        content.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(File file, boolean empty){
        if (empty || file == null){
            setGraphic(null);
            setText(null);
        }else {
            if (file.isDirectory()){
                iconView.setImage(IconLoader.getIcon("folder.png", 24).getImage());
            }else if (file.isFile()){
                iconView.setImage(IconLoader.getIcon("file.png", 24).getImage());
            }else {
                iconView.setImage(null);
            }
            fileName.setText(file.getName());
            fileExtension.setText(".");

            setGraphic(content);
            setText(null);
        }
    }

    private String getFileExtension(String fileName){
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex>0
        && dotIndex <fileName.length()-1){
            return fileName.substring(dotIndex+1);
        }
        return "Unknown";
    }
}
