package simple.http.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import simple.http.business.exc.SuperSimpleException;
import simple.http.business.logging.LoggerHelper;

public class PropertiesReader {
    
    private static final LoggerHelper LOG = new LoggerHelper(PropertiesReader.class.getName());

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static final String CLASSPATH_PREFIX = "classpath:";
    public static final String DEFAULT_APP_PROPERTIES_PATH = CLASSPATH_PREFIX + "/application.properties";

    public Properties readProperties(String pathToFile) {
        Properties properties = new Properties();
        InputStream inputStream;
        try {
            if (pathToFile.startsWith(CLASSPATH_PREFIX)) {
                inputStream = getClass().getResourceAsStream(pathToFile.substring(CLASSPATH_PREFIX.length()));
                if (inputStream == null) {
                    LOG.error("Could not read properties file from classpath: " + pathToFile);
                    throw new SuperSimpleException("Could not read properties file from classpath");
                }
            } else {
                inputStream = new FileInputStream(new File(pathToFile));
            }
            properties.load(inputStream);
        } catch (IOException | NullPointerException e) {
            LOG.error("Could not read properties file: " + pathToFile, e);
            throw new SuperSimpleException("Could not read properties file", e);
        }
        
        LOG.info("Properties read: " + properties);
        
        return properties;
    }
}
