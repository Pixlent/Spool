package net.spoolmc;

import net.spoolmc.logger.Logger;

public class Server {
    private final Logger logger = new Logger("Server");
    public void print(String message) {
        logger.info(message);
    }

    public String getVersion() {
        return "v1.2";
    }
}
