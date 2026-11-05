package config;

import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ConfigReaderRuntimeTest {

    @DataProvider(name = "sourceValues")
    public Object[][] sourceValues() {
        return new Object[][] {
            {Map.of("unit.runtime.source", "environment-value"), "config-value", "environment-value"},
            {Map.of(), "config-value", "config-value"},
            {Map.of("unit.runtime.source", "   "), "config-value", "config-value"},
            {Map.of("unit.runtime.source", "environment-value"), null, "environment-value"},
            {Map.of(), null, null}
        };
    }

    @Test(dataProvider = "sourceValues")
    public void supportsEnvironmentAndConfigReaderWithoutChangingTheirValues(
            Map<String, String> environment, String configValue, String expected) {
        String key = "unit.runtime.source";
        String previous = System.getProperty(key);
        try {
            if (configValue == null) System.clearProperty(key);
            else System.setProperty(key, configValue);
            Assert.assertEquals(ConfigReader.getRuntime(key, environment), expected);
        } finally {
            if (previous == null) System.clearProperty(key);
            else System.setProperty(key, previous);
        }
    }
}
