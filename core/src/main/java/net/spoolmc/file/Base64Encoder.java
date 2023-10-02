package net.spoolmc.file;

import net.spoolmc.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A utility class to encode and decode base64
 */
public class Base64Encoder {
    private final Logger logger = new Logger("Base64Encoder");

    /**
     *
     * @param file The file that you want to encode to base64
     * @return A base64 encoded string of the file you specified
     */
    public String encodeToBase64(File file) {
        byte[] bytes;

        try {
            bytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            logger.error("Unable to read file: " + file.getPath());
            throw new RuntimeException(e);
        }

        return java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
