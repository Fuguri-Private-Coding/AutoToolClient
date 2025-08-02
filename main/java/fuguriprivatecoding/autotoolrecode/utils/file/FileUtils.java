package fuguriprivatecoding.autotoolrecode.utils.file;

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

    public static void createDirectoriesIfNotExists(File... files) throws IOException {
        for (File file : files) {
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static void createFilesIfNotExists(File... files) throws IOException {
        for (File file : files) {
            if (!file.exists()) {
                file.createNewFile();
            }
        }
    }

    /**
     * Создает {@code file} если он не существует
     *
     * @param file файл который надо создать если его нет
     */
    public static void createIfNotExists(File file) {
        if (file.exists()) return;
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}