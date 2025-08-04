package dev.sch.simpletexteditor.model;

import dev.sch.simpletexteditor.config.DefaultConfig;
import dev.sch.simpletexteditor.util.SettingsStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ObservableSettings {
    private final SettingsStore settingsStore;
    private final ObjectProperty<Path> lastDirectory = new SimpleObjectProperty<>();

    public ObservableSettings(SettingsStore settingsStore){
        this.settingsStore = settingsStore;

        Path initial = settingsStore.getPath(SettingsStore.Keys.SETTING_LAST_DIR, Paths.get(DefaultConfig.DEFAULT_WORKSPACE_DIRECTORY));
        lastDirectory.set(initial.toAbsolutePath());

        this.bind();
    }

    private void bind(){
        lastDirectory.addListener((obs, oldVal, newVal)->{
            if (newVal == null){
                settingsStore.putPath(SettingsStore.Keys.SETTING_LAST_DIR, null);
            }else {
                settingsStore.putPath(SettingsStore.Keys.SETTING_LAST_DIR, newVal);
            }
        });

        settingsStore.addChangeListener(SettingsStore.Keys.SETTING_LAST_DIR, newVal->{
            if (newVal != null && !newVal.isBlank()) {
                Path p = Paths.get(newVal);
                if (!p.equals(lastDirectory.get())) {
                    lastDirectory.set(p);
                }
            }
        });
    }

    public ObjectProperty<Path> lastDirectoryProperty(){
        return lastDirectory;
    }

    public Path getLastDirectory(){
        return lastDirectory.get();
    }
    public void setLastDirectory(Path p){
        lastDirectory.set(p.toAbsolutePath());
    }
}
