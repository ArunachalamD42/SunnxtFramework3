package config;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;

import org.apache.logging.log4j.Logger;

/**
 * ConfigReader - Reads configuration from properties files.
 * Supports multiple environments: prod, staging, preprod, uat.
 *
 * Usage:
 *   ConfigReader.get("base.url")  -> returns the URL for the active environment
 *   ConfigReader.get("browser")   -> returns the configured browser
 *
 * Switch environment:
 *   - Edit config.properties: environment=uat
 *   - Or via CLI: mvn test -Denvironment=preprod
 *   - Or override any key: mvn test -Dbase.url=https://custom.url/
 */
public class ConfigReader {

    private static final Logger logger = Log.getLogger(ConfigReader.class);
    private static Properties properties = new Properties();
    private static Properties envProperties = new Properties();
    private static Properties adsProperties = new Properties();
    private static boolean isLoaded = false;

    private ConfigReader() {
    }

    public static void loadConfig() {
        if (isLoaded) return;

        try {
            String configPath = "src/main/resources/config/config.properties";
            FileInputStream fis = new FileInputStream(configPath);
            properties.load(fis);
            fis.close();
            logger.info("Loaded main config: {}", configPath);

            String env = System.getProperty("environment", properties.getProperty("environment", "prod"));
            logger.info("Active environment: {}", env);

            String envPath = "src/main/resources/config/environments/" + env + ".properties";
            FileInputStream envFis = new FileInputStream(envPath);
            envProperties.load(envFis);
            envFis.close();
            logger.info("Loaded environment config: {}", envPath);

            String[] adsFiles = {
                "testdata/ads-users.example.properties",
                System.getProperty("ads.users.file", "testdata/ads-users.local.properties")
            };
            for (String path : adsFiles) {
                if (new File(path).isFile()) {
                    try (FileInputStream input = new FileInputStream(path)) {
                        adsProperties.load(input);
                    }
                    logger.info("Loaded ads user config: {}", path);
                }
            }

            isLoaded = true;

        } catch (IOException e) {
            logger.error("Failed to load configuration files", e);
            throw new RuntimeException("Configuration loading failed: " + e.getMessage());
        }
    }

    public static String get(String key) {
        if (!isLoaded) loadConfig();

        String systemValue = System.getProperty(key);
        if (systemValue != null) return systemValue;

        String adsValue = adsProperties.getProperty(key);
        if (adsValue != null && !adsValue.isBlank()) return adsValue;

        String envValue = envProperties.getProperty(key);
        if (envValue != null) return envValue;

        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /** Uses a nonblank runtime environment value first, then normal ConfigReader lookup. */
    public static String getRuntime(String key) {
        return getRuntime(key, System.getenv());
    }

    /** Map overload keeps source-precedence tests independent of the real environment. */
    static String getRuntime(String key, Map<String, String> environment) {
        String value = environment.get(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return get(key);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for key '{}': {}. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value.trim());
    }

    public static String getBaseUrl() {
        return get("base.url");
    }

    public static String getBrowser() {
        return get("browser", "chrome");
    }

    public static void reload() {
        isLoaded = false;
        properties.clear();
        envProperties.clear();
        adsProperties.clear();
        loadConfig();
    }
}
