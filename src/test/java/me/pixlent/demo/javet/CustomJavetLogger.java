package me.pixlent.demo.javet;

import com.caoccao.javet.interfaces.IJavetLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJavetLogger implements IJavetLogger {
    private static final Logger logger = LoggerFactory.getLogger("JAVET");

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable cause) {
        logger.error(message, cause);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }
}