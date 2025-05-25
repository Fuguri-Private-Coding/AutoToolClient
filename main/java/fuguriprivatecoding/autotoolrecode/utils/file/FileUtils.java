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

    /**
     * Создает {@code file} если он не существует
     * @param file файл который надо создать если его нет
     * @return если файл был создан - true иначе - false
     */
    public static boolean createIfNotExists(File file) {
        if (file.exists()) return false;
        try {
            if (file.isDirectory()) {
                return file.mkdirs();
            } else if (file.isFile()) {
                return file.createNewFile();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return false;
    }

    /**
     * Создает все несуществующие файлы из {@code files}
     * @param files массив с файлами которые надо создать если они не существуют
     * @return если был создан хотя-бы 1 файл - true иначе - false
     */
    public static boolean createIfNotExists(File... files) {
        boolean created = false;
        for (File file : files) {
            if (createIfNotExists(file)) {
                created = true;
            }
        }
        return created;
    }
}