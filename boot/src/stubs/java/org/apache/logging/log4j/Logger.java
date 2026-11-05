package org.apache.logging.log4j;

public interface Logger {
    void error(String message);

    void error(String message, Throwable t);
}
