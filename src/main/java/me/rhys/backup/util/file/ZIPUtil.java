package me.rhys.backup.util.file;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZIPUtil {
    private final int BUFFER_SIZE = 4096;

    public void zip(List<File> listFiles, String dest) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest));
        for (File file : listFiles) {
            if (file.isDirectory()) {
                zipDirectory(file, file.getName(), zos);
            } else {
                zipFile(file, zos);
            }
        }
        zos.flush();
        zos.close();
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zipOutputStream) throws IOException {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zipOutputStream);
                continue;
            }
            zipOutputStream.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = bis.read(bytesIn)) != -1) {
                zipOutputStream.write(bytesIn, 0, read);
            }
            zipOutputStream.closeEntry();
        }
    }

    private void zipFile(File file, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read;
        while ((read = bis.read(bytesIn)) != -1) {
            zipOutputStream.write(bytesIn, 0, read);
        }
        zipOutputStream.closeEntry();
    }
}