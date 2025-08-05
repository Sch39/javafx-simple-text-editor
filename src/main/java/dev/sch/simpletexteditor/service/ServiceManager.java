package dev.sch.simpletexteditor.service;

import javafx.concurrent.Service;

import java.io.IOException;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServiceManager {
    private static final List<Service<?>> activeServices = new ArrayList<>();
    private static final List<ExecutorService> activeExecutorServices = new ArrayList<>();
    private static final List<WatchService> activeWatchServices = new ArrayList<>();

    public static void register(Service<?> service) {
        activeServices.add(service);
    }
    public static void register(ExecutorService service){
        activeExecutorServices.add(service);
    }
    public static void register(WatchService service){
        activeWatchServices.add(service);
    }

    public static void shutdownAll() {
        for (Service<?> service : activeServices) {
            if (service.isRunning()) {
                service.cancel();
            }
        }

        for (ExecutorService es : activeExecutorServices){
            try {
                es.shutdownNow();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        for (WatchService ws : activeWatchServices){
            try {
                ws.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        activeServices.clear();
        activeExecutorServices.clear();
        activeWatchServices.clear();
    }
}
