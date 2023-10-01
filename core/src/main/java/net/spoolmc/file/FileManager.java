package net.spoolmc.file;

import net.spoolmc.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class FileManager {
    /*
     * Get the base path
     */
    public static Path getBasePath() {
        return Path.of("");
    }

    /*
     * Searches a directory for any files (filtered)
     */
    public static List<File> searchDirectory(final Path dir) {
        List<File> files = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                files.add(file.toFile());
            }
        }

        catch (IOException e) {
            Logger.error("Core/FileManager", "Couldn't search directory: "
                    + dir);
            e.printStackTrace();
        }

        return files;
    }

    /*
     * Makes the file if it doesn't already exist
     */
    public static void saveResourceIfNotExists(Path path, String resource) {
        if (Files.exists(path)) return;
        // Try to write the file, because it doesn't exist
        try {
            Files.write(path, Objects.requireNonNull(FileManager.class.getClassLoader().getResourceAsStream(resource)).readAllBytes(), StandardOpenOption.CREATE);
        }
        // Catch if write failed
        catch (IOException e) {
            Logger.error("Core/FileManager", "Couldn't saveResourceIfNotExists: "
                    + path
                    + " & "
                    + resource);
            e.printStackTrace();
        }
    }

    /*
     * Read a file as a string
     */
    public static String readFile(File file) {
        Scanner reader;

        try {
            reader = new Scanner(file);
        }

        catch (FileNotFoundException e) {
            Logger.error("Core/FileManager", "Couldn't read file: " + file.getPath());
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
