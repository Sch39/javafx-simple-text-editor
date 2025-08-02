package dev.sch.simpletexteditor.util;

import dev.sch.simpletexteditor.SimpleTextEditorApp;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SettingsStore {
    private static final String SETTING_PROPERTIES = "settings.properties";
    private static final String APP_PROPERTIES = "config/app.properties";
    private static final String KEY_APP_NAME = "app.name";
    private static final String USER_HOME = System.getProperty("user.home");

    private static final Properties settingProperties = new Properties();
    private static final Properties appProperties = new Properties();
    private final Path settingsFile;

    private final Map<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();

    public SettingsStore(){
        loadDefaults();
        Path baseDir = computeStoragePath();
        this.settingsFile = baseDir.resolve(SETTING_PROPERTIES);
        loadSettings();

    }

    private void loadDefaults(){
        try(InputStream in = SimpleTextEditorApp.class.getResourceAsStream(APP_PROPERTIES)) {
            if (in != null){
                appProperties.load(in);
            }else {
                System.out.println(APP_PROPERTIES+" not found!");
            }
        }catch (IOException e){
            System.out.println("Failed load "+APP_PROPERTIES);
        }
    }

    private Path computeStoragePath(){
        String appName = appProperties.getProperty(KEY_APP_NAME, "simple-text-editor");
        appName = appName.isBlank() ? "simple-text-editor" : appName.trim().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9._-]", "");

        return Paths.get(USER_HOME, "."+appName);
    }

    private void loadSettings(){
        try {
            if (Files.notExists(settingsFile)){
                ensurePathExists();
                return;
            }

            try(InputStream in = Files.newInputStream(settingsFile, StandardOpenOption.READ)){
                settingProperties.load(in);
            }

            settingProperties.forEach((k, v)->{
                notifyListeners(k.toString(), v.toString());
            });
        }catch (IOException e){
            System.out.println("Failed load settings: "+e.getMessage());
        }
    }

    private void notifyListeners(String key, String newValue){
        List<Consumer<String>> lst = listeners.get(key);

        if (lst != null){
            new ArrayList<>(lst).forEach(l->{
                try {
                    l.accept(newValue);
                }catch (Exception ignored){}
            });
        }
    }

    private void ensurePathExists() throws IOException {
        Path parent = settingsFile.getParent();
        if (parent != null
        && Files.notExists(parent)){
            Files.createDirectories(parent);
        }
    }

    private void save(){
        try{
            ensurePathExists();
            Path temp = settingsFile.resolveSibling(SETTING_PROPERTIES+".tmp");
            try(OutputStream out = Files.newOutputStream(
                    temp,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )){
                settingProperties.store(out, "User settings overrides");
            }
            Files.move(temp,
                    settingsFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        }catch (IOException e){
            System.out.println("Failed save settings: "+e.getMessage());
        }

    }

    public synchronized String get(Keys key, String fallback){
        String val = appProperties.getProperty(key.getKey(), null);
        if (val == null){
            val = settingProperties.getProperty(key.getKey(), fallback);
        }
        return val;
    }

    public synchronized void put(Keys key, String value){
        String existing = settingProperties.getProperty(key.getKey());
        if (value == null){
            if (existing==null) return;
            settingProperties.remove(key.getKey());
        }else {
            if (value.equals(existing)) return;
            settingProperties.setProperty(key.getKey(), value);
        }
        save();
        notifyListeners(key.getKey(), value);
    }

    public synchronized boolean getBoolean(Keys key, boolean fallback){
        String val = this.get(key, Boolean.toString(fallback));
        return Boolean.parseBoolean(val);
    }

    public synchronized void putBoolean(Keys key, boolean value){
        this.put(key, Boolean.toString(value));
    }

    public synchronized int getInt(Keys key, int fallback){
        String val = get(key, Integer.toString(fallback));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public synchronized void putInt(Keys key, int value){
        put(key, Integer.toString(value));
    }

    public synchronized Path getPath(Keys key, Path fallback){
        String raw = get(key, null);
        if (raw != null && !raw.isBlank()){
            Path p = Paths.get(raw);
            if (Files.exists(p)){
                return p;
            }
        }
        return fallback;
    }

    public synchronized void putPath(Keys key, Path path){
        if (path == null){
            put(key, null);
        }else {
            put(key, path.toAbsolutePath().toString());
        }
    }

    public synchronized void remove(Keys key) {
        settingProperties.remove(key.getKey());
        save();
    }

    public IListenerHandler addChangeListener(Keys key, Consumer<String> listener){
        listeners.computeIfAbsent(key.getKey(), k-> new ArrayList<>()).add(listener);
        return ()->removeChangeListener(key, listener);
    }

    public void removeChangeListener(Keys key, Consumer<String> listener){
        List<Consumer<String>> lst = listeners.get(key.getKey());
        if (lst != null){
            lst.remove(listener);
            if (lst.isEmpty()){
                listeners.remove(key.getKey());
            }
        }
    }

    @Getter
    public enum Keys{
        APP_NAME("app.name"),
        APP_VERSION("app.version"),
        SETTING_LAST_DIR("lastDirectory");


        private final String key;
        Keys(String key) {
            this.key = key;
        }

    }
}
