package net.spoolmc.file;

import net.spoolmc.Guard.Guard;
import net.spoolmc.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class FileManager {
    private static final Logger logger = new Logger("FileManager");

    /**
     * Returns the base path.
     *
     * @return The base path as a Path object.
     */
    public static Path getBasePath() {
        return Path.of("");
    }

    /**
     * Searches a directory for files and returns a list of files
     *
     * @param directory The directory to search.
     * @return A list of File objects found in the directory.
     */
    public static List<File> searchDirectory(final Path directory) {
        List<File> files = new ArrayList<>();

        Guard.tryCatch("Failed to search directory", () -> {
            DirectoryStream<Path> stream = Files.newDirectoryStream(directory);

            for (Path file : stream) {
                files.add(file.toFile());
            }
        });

        return files;
    }

    /**
     * Searches a directory and its subdirectories for files and returns a list of files.
     *
     * @param directory The directory to start the deep search from.
     * @return A list of File objects found in the directory and its subdirectories.
     */
    public static List<File> searchDirectoryDeep(final Path directory) {
        List<File> files = new ArrayList<>();

        Guard.tryCatch("Failed to search directory", () -> {
            DirectoryStream<Path> stream = Files.newDirectoryStream(directory);

            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    files.addAll(searchDirectoryDeep(file));
                } else {
                    files.add(file.toFile());
                }
            }
        });

        return files;
    }

    /**
     * Saves a resource to a file if the file does not already exist.
     *
     * @param path     The path to the file where the resource will be saved.
     * @param resource The name of the resource to save, typically a file in the classpath.
     */
    public static void saveResourceIfNotExists(Path path, String resource) {
        if (Files.exists(path)) return;
        // Try to write the file, because it doesn't exist

        Guard.tryCatch("Failed to write file: " + path, () -> Files.write(path, Objects.requireNonNull(FileManager.class.getClassLoader().getResourceAsStream(resource)).readAllBytes(), StandardOpenOption.CREATE));
    }

    /**
     * Reads the contents of a file and returns them as a single string.
     *
     * @param file The file to read.
     * @return The contents of the file as a single string.
     * @throws RuntimeException If the file does not exist or cannot be read.
     */
    public static String readFile(File file) {
        Scanner reader;

        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            logger.error("Couldn't read file: " + file.getPath());
            throw new RuntimeException(e);
        }

        String contents = "";
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            contents = contents.concat(data);
        }

        return contents;
    }
}