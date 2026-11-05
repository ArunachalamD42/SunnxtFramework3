package config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AdsUsersConfigTest {

    @Test
    public void exampleDefinesApprovedBehaviourWithoutCredentialValues() throws Exception {
        Properties users = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("testdata/ads-users.example.properties"))) {
            users.load(input);
        }
        Map<String, String> expected = Map.of(
                "COMBO_ANNUAL", "PLAY,NO_ADS,FULL", "SAMPLE", "PLAY,NO_ADS,FIRST_7",
                "COMBO_QUARTERLY", "PLAY,ADS,FULL", "SN_LITE", "PLAY,ADS,FIRST_7",
                "SNS_LITE", "BLOCK,ADS,FULL", "SNS_PREMIUM", "BLOCK,NO_ADS,FULL");
        String[] accounts = users.getProperty("ads.accounts").split(",");
        Assert.assertEquals(accounts.length, 6);
        var aliases = new HashSet<String>();
        for (String account : accounts) {
            Assert.assertTrue(aliases.add(account));
            Assert.assertTrue(expected.containsKey(account));
            String prefix = "ads." + account + ".";
            Assert.assertNotNull(users.getProperty(prefix + "plan"));
            String actual = users.getProperty(prefix + "sn.expected") + ","
                    + users.getProperty(prefix + "ads.expected") + "," + users.getProperty(prefix + "shorts.expected");
            Assert.assertEquals(actual, expected.get(account));
            for (String field : new String[] {"MOBILE", "PASSWORD"}) {
                String value = users.getProperty("SUNNXT_ENTITLEMENT_" + account + "_CREDENTIAL_" + field);
                Assert.assertTrue(value != null && value.isBlank(), "Example must contain blank credential slots only");
            }
        }
        Assert.assertEquals(users.getProperty("ads.COMBO_QUARTERLY.username.config.key"), "test.platinumCombo");
        Assert.assertEquals(users.getProperty("ads.COMBO_QUARTERLY.password.config.key"), "test.password1");
    }

    @Test
    public void localFileOverridesTheExampleAndSystemPropertiesStillTakePriority() throws Exception {
        Path local = Files.createTempFile("ads-user-config-test-", ".properties");
        String key = "ads.COMBO_QUARTERLY.ads.expected";
        String previousFile = System.getProperty("ads.users.file");
        String previousValue = System.getProperty(key);
        try {
            Files.writeString(local, key + "=NO_ADS\n");
            System.setProperty("ads.users.file", local.toString());
            System.clearProperty(key);
            ConfigReader.reload();
            Assert.assertEquals(ConfigReader.get(key), "NO_ADS");
            Assert.assertEquals(ConfigReader.get("ads.COMBO_QUARTERLY.plan"), "Combo Quarterly Platinum");
            System.setProperty(key, "ADS");
            Assert.assertEquals(ConfigReader.get(key), "ADS");
        } finally {
            if (previousFile == null) System.clearProperty("ads.users.file");
            else System.setProperty("ads.users.file", previousFile);
            if (previousValue == null) System.clearProperty(key);
            else System.setProperty(key, previousValue);
            ConfigReader.reload();
            Files.deleteIfExists(local);
        }
    }
}
