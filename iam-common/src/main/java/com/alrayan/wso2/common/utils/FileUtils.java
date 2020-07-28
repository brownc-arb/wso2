package com.alrayan.wso2.common.utils;

import com.alrayan.wso2.common.AlRayanConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class to handle file operations.
 *
 * @since 1.0.0
 */
public class FileUtils {

    private static final String CARBON_HOME_NAME = "carbon.home";

    private static final Path CONF_FILE_LOCATION = Paths.get("repository", "conf", "finance",
            AlRayanConstants.CONF_FILE_NAME);
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);


    /**
     * Returns the property set specified in the configuration.
     *
     * @return configuration properties
     */
    public static Properties readConfiguration() {
        String carbonHome = System.getProperty(CARBON_HOME_NAME);
        if (StringUtils.isEmpty(carbonHome)) {
            return new Properties();
        }
        Path filePath = Paths.get(carbonHome, CONF_FILE_LOCATION.toString());

        Properties properties = null;

        try {
            properties = getConfigProperties(filePath);
        } catch (IOException e) {
            log.error("Error while reading the alrayan-identity.properties", e);
        }

        return properties;
    }

    /**
     * Returns the properties from the configuration file.
     *
     * @param filePath configuration file path to read properties
     * @return configuration properties
     */
    private static Properties getConfigProperties(Path filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
            Properties properties = new Properties();
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            log.error("Error while reading the alrayan-identity.properties", e);
            throw new IOException("Error while reading the alrayan-identity.properties");
        }
    }
}
