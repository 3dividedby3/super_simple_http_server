package simple.http.ws;

import static simple.http.business.PropertiesReader.DEFAULT_APP_PROPERTIES_PATH;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import simple.http.business.PropertiesReader;
import simple.http.business.SimpleServer;
import simple.http.business.base.SimpleHttpHandler;
import simple.http.business.logging.LoggerHelper;


public class SimpleExampleWebServerApp {
    
    private static final LoggerHelper LOG = new LoggerHelper(SimpleExampleWebServerApp.class.getName());

    public static final int ARGS_IDX_PROP_FILE_PATH = 0;

    public static final String PROP_LOGGING_LEVEL = "logging.level";
    public static final String PROP_WEB_SERVER_PORT = "web.server.port";

    public static void main(String[] args) throws Exception {
        
        Properties properties = new PropertiesReader().readProperties(args.length > 0 ? args[ARGS_IDX_PROP_FILE_PATH] : DEFAULT_APP_PROPERTIES_PATH);
        LoggerHelper.Level logLevel = LoggerHelper.Level.valueOf(properties.getProperty(PROP_LOGGING_LEVEL, "INFO"));
        LoggerHelper.setLogLevel(logLevel);
        
        int webServerPort = Integer.valueOf(properties.getProperty(PROP_WEB_SERVER_PORT));
        LOG.info("Test the application with: http://localhost:" + webServerPort + "/echo");

        Map<String, SimpleHttpHandler> handlerMap= new HashMap<>();
        EchoHandler echoHandler = new EchoHandler();
        handlerMap.put("/echo", echoHandler);
        
        SimpleServer server = new SimpleServer(handlerMap, 10 * 1000, Integer.valueOf(properties.getProperty(PROP_WEB_SERVER_PORT)));
        server.startHandlingWithTimeout();
    }
}
