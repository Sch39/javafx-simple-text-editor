package dev.sch.simpletexteditor.util;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class IconLoader {
    private static final String ICONS_PATH = "icons/";

    public static ImageView getIcon(String filename, double size){
        try {
            InputStream is = SimpleTextEditorApp.class.getResourceAsStream(ICONS_PATH+filename);
            if (is == null){
                System.out.println("Ikon tidak ditemukan, file: "+ICONS_PATH+filename);
                return null;
            }
            Image image = new Image(is);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);

            return imageView;
        }catch (Exception e){
            System.out.println();
            System.out.println("Ikon gagal dimuat, file: "+ICONS_PATH+filename);
            return null;
        }
    }
}
