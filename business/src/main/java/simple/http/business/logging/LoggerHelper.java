package simple.http.business.logging;

import static simple.http.business.logging.LoggerHelper.Level.INFO;

import java.util.Arrays;
import java.util.Date;

public class LoggerHelper {

    public static enum Level {
        ERROR, WARN, INFO, DEBUG;
    }
    
    private static volatile Level SELECTED_LEVEL = Level.INFO;
    
    public static void setLogLevel(Level level) {
        SELECTED_LEVEL = level;
    }
    
    private final String className;

    public LoggerHelper(String className) {
        this.className = className;
    }

    public void info(String message) {
        log(INFO, message);
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    public void warn(String message) {
        log(Level.WARN, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void error(String message, Throwable thrown) {
        log(Level.ERROR, message + "\n      --- Throwable: " + thrown.getMessage() + " - " + Arrays.asList(thrown.getStackTrace()));
    }
    
    protected void log(Level messageLevel, String message) {
        if (messageLevel.ordinal() <= SELECTED_LEVEL.ordinal()) {
            System.out.println(new Date() + " [" + messageLevel + "] [" + className + "] [" + Thread.currentThread().getName() + "] - " + message);
        }
    }
}
