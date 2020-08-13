package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.config.Config;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTest
{
    @Test
    @BeforeAll
    @Order(1)
    @DisplayName("Load Properties")
    static void loadPropertiesTest() throws IOException
    {
        Config.getInstance().loadProperties();
    }

    @Test
    @Order(2)
    @DisplayName("Get Properties")
    void getPropertiesTest()
    {
        Properties properties = Config.getInstance().getProperties();
        assertFalse(properties.isEmpty(), "The properties file has failed to load");
    }
}
