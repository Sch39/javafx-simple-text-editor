package dev.sch.simpletexteditor.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class FileComparator implements Comparator<Path> {

    @Override
    public int compare(Path path1, Path path2) {
        boolean isDir1 = Files.isDirectory(path1);
        boolean isDir2 = Files.isDirectory(path2);

        if (isDir1==isDir2){
            return path1.getFileName().toString().compareToIgnoreCase(path2.getFileName().toString());
        }
        if (isDir1){
            return -1;
        }
        return 1;
    }
}