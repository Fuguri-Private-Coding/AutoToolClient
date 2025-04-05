package me.hackclient.utils.file;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * The type File utils.
 */
public class FileUtils {
    /**
     * Unpack file.
     *
     * @param file the file
     * @param name the name
     * @throws IOException the io exception
     */
    public static void unpackFile(File file, String name) throws IOException {
        file.getParentFile().mkdirs(); // Создание папок для файла, если они не существуют
        try (FileOutputStream fos = new FileOutputStream(file);
             InputStream resourceStream = Objects.requireNonNull(FileUtils.class.getClassLoader().getResourceAsStream(name))) {
            IOUtils.copy(resourceStream, fos);
        }
    }
}