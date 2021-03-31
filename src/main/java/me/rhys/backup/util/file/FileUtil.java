package me.rhys.backup.util.file;

import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {
    public static void write(String file, String content) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
